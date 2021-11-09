package org.kryptonmc.data.json

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class JenkinsArtifacts(val artifacts: List<JenkinsArtifact>, val timestamp: Long, val url: String)

@Serializable
data class JenkinsArtifact(val fileName: String, val relativePath: String = "")

@Serializable
data class JenkinsBuilds(val builds: List<JenkinsBuild>)

@Serializable
data class JenkinsBuild(
    val artifacts: List<JenkinsArtifact>,
    val result: String,
    val timestamp: Long,
    @SerialName("changeSet") val changes: JenkinsChanges
)

@Serializable
data class JenkinsChanges(@SerialName("_class") val clazz: String, val items: List<JenkinsChangeItem>)

@Serializable
data class JenkinsChangeItem(@SerialName("msg") val message: String, val commitId: String)
