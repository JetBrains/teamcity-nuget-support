package jetbrains.buildServer.nuget.feed.server.json

import com.google.gson.annotations.SerializedName
import jetbrains.buildServer.nuget.feed.server.index.NuGetIndexEntry

class JsonRegistrationPackageResponse(
        @SerializedName("@id")
        val atId: String,
        @SerializedName("@type")
        val atType: List<String>,
        val packageContent: String,
        entry: NuGetIndexEntry
) : NuGetPackage(entry)
