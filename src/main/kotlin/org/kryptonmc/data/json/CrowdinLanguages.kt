package org.kryptonmc.data.json

import kotlinx.serialization.Serializable

@Serializable
data class CrowdinLanguages(val data: List<CrowdinLanguage>)

@Serializable
data class CrowdinLanguage(val data: CrowdinLanguageData)

@Serializable
data class CrowdinLanguageData(val languageId: String, val translationProgress: Int)
