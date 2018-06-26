package jetbrains.buildServer.nuget.feed.server.json

import com.google.gson.annotations.SerializedName
import java.util.*

data class JsonRegistrationPackageResponse(
        @SerializedName("@id")
        val atId: String,
        @SerializedName("@type")
        val atType: List<String>,
        val packageContent: String,
        val id: String,
        val version: String,
        val isPrerelease: Boolean,
        val authors: String?,
        val created: Date,
        val description: String?,
        val dependencyGroups: List<JsonPackageDependencyGroup>,
        val language: String?,
        val lastEdited: Date,
        val packageHash: String?,
        val packageHashAlgorithm: String?,
        val packageSize: Long?,
        val published: Date,
        val requireLicenseAcceptance: Boolean,
        val summary: String?
)
