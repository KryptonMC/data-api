package org.kryptonmc.data.json

import kotlinx.serialization.Serializable

@Serializable
data class CrowdinReportRequest(val name: String, val schema: CrowdinReportRequestSchema)

@Serializable
data class CrowdinReportRequestSchema(val unit: String, val format: String)

@Serializable
data class CrowdinReportResponse(val data: CrowdinReportResponseData)

@Serializable
data class CrowdinReportResponseData(val identifier: String)

@Serializable
data class CrowdinReportStatus(val data: CrowdinReportStatusData)

@Serializable
data class CrowdinReportStatusData(val status: String)

@Serializable
data class CrowdinReportDownload(val data: CrowdinReportDownloadData)

@Serializable
data class CrowdinReportDownloadData(val url: String)

@Serializable
data class CrowdinReport(val data: List<CrowdinReportEntry>)

@Serializable
data class CrowdinReportEntry(val user: CrowdinReportUser, val languages: List<CrowdinReportLanguage>, val translated: Int)

@Serializable
data class CrowdinReportUser(val username: String)

@Serializable
data class CrowdinReportLanguage(val id: String, val name: String)
