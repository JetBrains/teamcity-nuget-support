package jetbrains.buildServer.nuget.feed.server.json

import com.google.gson.annotations.SerializedName

data class JsonRegistrationResponse(
        @SerializedName("@id")
        val atId: String,
        @SerializedName("@type")
        val atType: List<String>,
        val count: Int,
        val items: List<JsonRegistrationPage>
)
