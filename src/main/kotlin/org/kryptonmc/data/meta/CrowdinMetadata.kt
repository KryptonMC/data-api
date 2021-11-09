package org.kryptonmc.data.meta

import io.swagger.v3.oas.annotations.media.Schema
import kotlinx.serialization.Serializable

@Serializable
@Schema
data class CrowdinMetadata(
    @field:Schema(name = "cacheMaxAge", example = "604800000") val cacheMaxAge: Long,
    @field:Schema(name = "languages") val languages: List<Language>
) : Metadata

@Serializable
@Schema
data class Language(
    @field:Schema(name = "id", example = "en-PT") val id: String,
    @field:Schema(name = "name", example = "Pirate English") val name: String,
    @field:Schema(name = "tag", example = "en_PT") val tag: String,
    @field:Schema(name = "progress", example = "10") val progress: Int,
    @field:Schema(name = "contributors") val contributors: List<LanguageContributor>
)

@Serializable
@Schema
data class LanguageContributor(
    @field:Schema(name = "name", example = "BomBardyGamer") val name: String,
    @field:Schema(name = "translated", example = "5") val translated: Int
)
