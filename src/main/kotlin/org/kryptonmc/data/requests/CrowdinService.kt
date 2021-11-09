package org.kryptonmc.data.requests

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import org.kryptonmc.data.config.MainConfig
import org.kryptonmc.data.json.CrowdinExport
import org.kryptonmc.data.json.CrowdinExportRequest
import org.kryptonmc.data.json.CrowdinLanguages
import org.kryptonmc.data.json.CrowdinProject
import org.kryptonmc.data.json.CrowdinReport
import org.kryptonmc.data.json.CrowdinReportDownload
import org.kryptonmc.data.json.CrowdinReportRequest
import org.kryptonmc.data.json.CrowdinReportRequestSchema
import org.kryptonmc.data.json.CrowdinReportResponse
import org.kryptonmc.data.json.CrowdinReportStatus
import org.kryptonmc.data.meta.Language
import org.kryptonmc.data.meta.LanguageContributor
import org.kryptonmc.data.meta.CrowdinMetadata
import org.kryptonmc.data.util.executeSuccess
import org.springframework.stereotype.Service
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.create
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Body
import retrofit2.http.Streaming
import retrofit2.http.Url
import java.nio.file.Files
import kotlin.io.path.createDirectory
import kotlin.io.path.exists

interface CrowdinService {

    @GET("projects/{id}")
    fun project(@Path("id") id: Int): Call<CrowdinProject>

    @GET("projects/{id}/languages/progress")
    fun progress(@Path("id") id: Int, @Query("limit") limit: Int = 100): Call<CrowdinLanguages>

    @POST("projects/{id}/reports")
    fun generateReport(@Path("id") id: Int, @Body request: CrowdinReportRequest): Call<CrowdinReportResponse>

    @GET("projects/{id}/reports/{reportId}")
    fun reportStatus(@Path("id") id: Int, @Path("reportId") reportId: String): Call<CrowdinReportStatus>

    @GET("projects/{id}/reports/{reportId}/download")
    fun reportDownload(@Path("id") id: Int, @Path("reportId") reportId: String): Call<CrowdinReportDownload>

    @GET
    @Streaming
    fun downloadReport(@Url url: String): Call<CrowdinReport>

    @POST("projects/{id}/translations/exports")
    fun requestExport(@Path("id") id: Int, @Body request: CrowdinExportRequest): Call<CrowdinExport>

    @GET
    @Streaming
    fun downloadExportData(@Url url: String): Call<ResponseBody>
}

@Service
class CrowdinRequester(private val config: MainConfig) {

    private val service = Retrofit.Builder()
        .baseUrl(CROWDIN_API_URL)
        .client(OkHttpClient.Builder().addInterceptor {
            val request = it.request()
            if (CROWDIN_API_URL !in request.url.toString()) return@addInterceptor it.proceed(request)
            it.proceed(request.newBuilder().addHeader("Authorization", "Bearer ${config.token}").build())
        }.build())
        .addConverterFactory(Json {
            ignoreUnknownKeys = true
            encodeDefaults = true
        }.asConverterFactory("application/json".toMediaType()))
        .build()
        .create<CrowdinService>()

    fun request(): CrowdinMetadata {
        val targetLanguages = service.project(KRYPTON_PROJECT_ID).executeSuccess().data.targetLanguages.associate {
            it.id to MutableLanguage(it.id, it.name, it.locale.replace("-", "_"))
        }

        service.progress(KRYPTON_PROJECT_ID).executeSuccess().data.forEach { targetLanguages[it.data.languageId]!!.progress = it.data.translationProgress }
        val reportId = service.generateReport(KRYPTON_PROJECT_ID, CrowdinReportRequest("top-members", CrowdinReportRequestSchema("strings", "json"))).executeSuccess().data.identifier

        var waiting = true
        while (waiting) {
            waiting = service.reportStatus(KRYPTON_PROJECT_ID, reportId).executeSuccess().data.status != "finished"
            if (waiting) Thread.sleep(2000)
        }
        val downloadUrl = service.reportDownload(KRYPTON_PROJECT_ID, reportId).executeSuccess().data.url

        service.downloadReport(downloadUrl).executeSuccess().data.forEach { user ->
            user.languages.forEach { targetLanguages[it.id]?.contributors?.add(LanguageContributor(user.user.username, user.translated)) }
        }

        return CrowdinMetadata(
            CACHE_MAX_AGE,
            targetLanguages.map { (_, it) -> Language(it.id, it.name, it.tag, it.progress, it.contributors) }
        )
    }

    fun export(data: CrowdinMetadata) {
        val directory = java.nio.file.Path.of("translations").apply { if (!exists()) createDirectory() }
        data.languages.forEach {
            if (it.progress == 0) return@forEach
            val url = service.requestExport(KRYPTON_PROJECT_ID, CrowdinExportRequest(it.id)).executeSuccess().data.url
            val file = directory.resolve("${it.tag}.properties")
            if (file.exists()) return@forEach
            Files.copy(service.downloadExportData(url).executeSuccess().byteStream(), file)
        }
    }

    private data class MutableLanguage(
        val id: String,
        val name: String,
        val tag: String,
        var progress: Int = 0,
        val contributors: MutableList<LanguageContributor> = mutableListOf()
    )

    companion object {

        private const val CROWDIN_API_URL = "https://crowdin.com/api/v2/"
        private const val KRYPTON_PROJECT_ID = 457314
        private const val CACHE_MAX_AGE = 604800000L // 7 days (milliseconds)
    }
}
