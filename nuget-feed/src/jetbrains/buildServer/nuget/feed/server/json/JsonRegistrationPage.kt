package jetbrains.buildServer.nuget.feed.server.json

import com.google.gson.annotations.SerializedName

data class JsonRegistrationPage (
        @SerializedName("@id")
        val atId: String,
        val count: Int,
        val lower: String,
        val upper: String,
        val items: List<JsonRegistrationPackage>
)
