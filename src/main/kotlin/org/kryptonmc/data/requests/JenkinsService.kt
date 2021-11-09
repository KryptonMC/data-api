package org.kryptonmc.data.requests

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import org.kryptonmc.data.meta.Changelog
import org.kryptonmc.data.json.JenkinsArtifacts
import org.kryptonmc.data.json.JenkinsBuilds
import org.kryptonmc.data.meta.JenkinsMetadata
import org.kryptonmc.data.util.executeSuccess
import org.springframework.stereotype.Component
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.create
import retrofit2.http.GET
import retrofit2.http.Path

interface JenkinsService {

    @GET("job/{job}/lastSuccessfulBuild/api/json?tree=url,timestamp,artifacts[fileName,relativePath]")
    fun downloads(@Path("job") job: String): Call<JenkinsArtifacts>

    @GET("job/{job}/api/json?tree=builds[timestamp,result,artifacts[fileName],changeSet[items[msg,commitId]]]")
    fun changelog(@Path("job") job: String): Call<JenkinsBuilds>
}

@Component
class JenkinsRequester {

    private val service = Retrofit.Builder()
        .baseUrl(JENKINS_URL)
        .addConverterFactory(Json { ignoreUnknownKeys = true }.asConverterFactory("application/json".toMediaType()))
        .build()
        .create<JenkinsService>()

    fun request(): JenkinsMetadata {
        val downloadData = service.downloads(KRYPTON_JOB).executeSuccess()
        val version = FILE_NAME_REGEX.find(downloadData.artifacts[0].fileName)!!.groupValues[1]
        val versionTimestamp = downloadData.timestamp
        val downloads = downloadData.artifacts.associate {
            val download = it.relativePath.split("/")[0]
            download to "${downloadData.url}artifact/${it.relativePath}"
        }

        val changelogData = service.changelog(KRYPTON_JOB).executeSuccess()
        val log = changelogData.builds.asSequence()
            .filter { it.result == "SUCCESS" }
            .filter { it.changes.items.isNotEmpty() }
            .filter { it.changes.clazz == "hudson.plugins.git.GitChangeSetList" }
            .map {
                Changelog(
                    FILE_NAME_REGEX.find(it.artifacts[0].fileName)!!.groupValues[1],
                    it.timestamp,
                    it.changes.items[0].message,
                    it.changes.items[0].commitId
                )
            }.toList()

        return JenkinsMetadata(version, versionTimestamp, log, downloads)
    }

    companion object {

        private const val JENKINS_URL = "https://ci.kryptonmc.org/"
        private const val KRYPTON_JOB = "Krypton"
        private val FILE_NAME_REGEX = "Krypton-(.*)\\.jar".toRegex()
    }
}
