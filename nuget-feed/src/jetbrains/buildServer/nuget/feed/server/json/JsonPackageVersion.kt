package jetbrains.buildServer.nuget.feed.server.json

import com.google.gson.annotations.SerializedName

data class JsonPackageVersion(
        @SerializedName("@id")
        val versionUrl: String,
        val version: String,
        val downloads: Int
)
