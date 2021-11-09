package org.kryptonmc.data.json

import kotlinx.serialization.Serializable

@Serializable
data class CrowdinProject(val data: CrowdinProjectData)

@Serializable
data class CrowdinProjectData(val targetLanguages: List<TargetLanguage>)

@Serializable
data class TargetLanguage(val id: String, val name: String, val locale: String)
