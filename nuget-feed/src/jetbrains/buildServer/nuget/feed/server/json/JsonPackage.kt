package jetbrains.buildServer.nuget.feed.server.json

import com.google.gson.annotations.SerializedName

data class JsonPackage(
        @SerializedName("@id")
        val atId: String,
        @SerializedName("@type")
        val atType: String,
        val id: String,
        val version: String,
        val versions: List<JsonPackageVersion>,
        val description: String?,
        val authors: String?,
        val iconUrl: String?,
        val licenseUrl: String?,
        val owners: String?,
        val projectUrl: String?,
        val registration: String?,
        val summary: String?,
        val tags: String?,
        val title: String?,
        val totalDownloads: Int?,
        val verified: Boolean?
)
