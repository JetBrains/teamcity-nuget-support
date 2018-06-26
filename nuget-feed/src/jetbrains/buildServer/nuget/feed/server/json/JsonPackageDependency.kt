package jetbrains.buildServer.nuget.feed.server.json

import com.google.gson.annotations.SerializedName

data class JsonPackageDependency(
        @SerializedName("@id")
        val atId: String,
        @SerializedName("@type")
        val atType: String,
        val id: String,
        val registration: String,
        val range: String
)
