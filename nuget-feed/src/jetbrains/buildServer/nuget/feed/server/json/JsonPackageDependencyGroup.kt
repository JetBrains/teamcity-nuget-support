package jetbrains.buildServer.nuget.feed.server.json

import com.google.gson.annotations.SerializedName

data class JsonPackageDependencyGroup(
        @SerializedName("@id")
        val atId: String,
        @SerializedName("@type")
        val atType: String,
        val targetFramework: String,
        val dependencies: List<JsonPackageDependency>
)
