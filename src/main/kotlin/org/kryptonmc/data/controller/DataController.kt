package org.kryptonmc.data.controller

import com.github.benmanes.caffeine.cache.Caffeine
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.apache.logging.log4j.LogManager
import org.kryptonmc.data.meta.CrowdinMetadata
import org.kryptonmc.data.meta.JenkinsMetadata
import org.kryptonmc.data.requests.CrowdinRequester
import org.kryptonmc.data.requests.JenkinsRequester
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController
import java.nio.file.Path
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import javax.annotation.PostConstruct
import javax.servlet.http.HttpServletResponse
import kotlin.io.path.exists
import kotlin.io.path.readBytes

@RestController
class DataController(private val crowdin: CrowdinRequester, private val jenkins: JenkinsRequester) {

    private val executor = Executors.newScheduledThreadPool(2)

    private var crowdinData = crowdin.request()
    private var jenkinsData = jenkins.request()

    private val downloadCache = Caffeine.newBuilder()
        .expireAfterWrite(2, TimeUnit.MINUTES)
        .build<String, ByteArray> {
            val file = Path.of("translations/$it.properties")
            if (file.exists()) file.readBytes() else ByteArray(0)
        }

    @PostConstruct
    fun scheduleUpdateTasks() {
        crowdin.export(crowdinData)
        executor.scheduleAtFixedRate({
            LOGGER.info("Updating translations...")
            crowdinData = crowdin.request()
            crowdin.export(crowdinData)
        }, 15, 15, TimeUnit.MINUTES)
        executor.scheduleAtFixedRate({ jenkinsData = jenkins.request() }, 30, 30, TimeUnit.SECONDS)
    }

    @ApiResponse(
        responseCode = "200",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = CrowdinMetadata::class))]
    )
    @Operation(summary = "Get translation metadata")
    @GetMapping("data/translations", produces = ["application/json"])
    fun translations() = ResponseEntity.ok().body(Json.encodeToString(crowdinData))

    @ApiResponse(
        responseCode = "200",
        content = [Content(
            mediaType = "text/plain; charset=utf-8",
            schema = Schema(example = "krypton.version-info=Krypton version {0} for Minecraft {1}\n...")
        )]
    )
    @Operation(summary = "Download an exported translated file in properties format")
    @GetMapping("translation/{tag}")
    fun download(
        @Parameter(description = "The locale tag", example = "en-PT") @PathVariable tag: String,
        response: HttpServletResponse
    ): HttpEntity<*> {
        val fileName = "${tag.replace("-", "_")}.properties"
        val data = downloadCache[tag.replace("-", "_")]!!
        if (data.isEmpty()) return ResponseEntity.status(404).body("No such language")

        val headers = HttpHeaders().apply { contentType = MediaType("text", "x-java-properties") }
        response.setHeader("Content-Disposition", "attachment; filename=$fileName")
        response.setHeader("Cache-Control", "max-age=3600000")
        return HttpEntity<ByteArray>(data, headers)
    }

    @ApiResponse(
        responseCode = "200",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = JenkinsMetadata::class))]
    )
    @Operation(summary = "Get build metadata")
    @GetMapping("data/builds", produces = ["application/json"])
    fun builds() = ResponseEntity.ok().body(Json.encodeToString(jenkinsData))

    companion object {

        private val LOGGER = LogManager.getLogger(DataController::class.java)
    }
}
