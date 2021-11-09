package org.kryptonmc.data.json

import kotlinx.serialization.Serializable

@Serializable
data class CrowdinExportRequest(
    val targetLanguageId: String,
    val fileIds: List<Int> = listOf(4),
    val skipUntranslatedStrings: Boolean = true
)

@Serializable
data class CrowdinExport(val data: CrowdinExportData)

@Serializable
data class CrowdinExportData(val url: String)
