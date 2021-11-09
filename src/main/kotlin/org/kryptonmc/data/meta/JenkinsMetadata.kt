package org.kryptonmc.data.meta

import io.swagger.v3.oas.annotations.media.Schema
import kotlinx.serialization.Serializable

@Serializable
@Schema
data class JenkinsMetadata(
    @field:Schema(name = "version", example = "0.20.1") val version: String,
    @field:Schema(name = "versionTimestamp", description = "The UNIX timestamp in milliseconds", example = "1621784244596") val versionTimestamp: Long,
    @field:Schema(name = "changelog") val changelog: List<Changelog>,
    @field:Schema(name = "downloads", example = "{\"server\":\"https://ci.kryptonmc.org/job/Krypton/101/artifact/server/build/libs/Krypton-0.20.1.jar\"}") val downloads: Map<String, String>
) : Metadata

@Serializable
@Schema
data class Changelog(
    @field:Schema(name = "version", example = "0.20.1") val version: String,
    @field:Schema(name = "timestamp", description = "The UNIX timestamp in milliseconds", example = "1621615701698") val timestamp: Long,
    @field:Schema(name = "title", example = "Removed lateinit UUID and fixed issue with player data saving") val title: String,
    @field:Schema(name = "commit", description = "The commit hash", example = "3a92ffb26d6f5a62d1cc52827a71895f8db60db9") val commit: String
)
