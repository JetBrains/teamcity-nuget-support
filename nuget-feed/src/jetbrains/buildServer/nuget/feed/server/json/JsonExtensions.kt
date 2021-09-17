package jetbrains.buildServer.nuget.feed.server.json

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.stream.JsonWriter
import jetbrains.buildServer.nuget.common.index.ODataDataFormat
import jetbrains.buildServer.nuget.common.version.SemanticVersion
import jetbrains.buildServer.nuget.feed.server.MetadataConstants
import jetbrains.buildServer.nuget.feed.server.NuGetUtils
import jetbrains.buildServer.nuget.feed.server.impl.HttpServletRequestUtil
import jetbrains.buildServer.nuget.feed.server.index.NuGetIndexEntry
import jetbrains.buildServer.nuget.feedReader.NuGetPackageAttributes
import jetbrains.buildServer.web.util.WebUtil
import java.io.OutputStreamWriter
import java.util.*
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import javax.ws.rs.core.UriBuilder

internal fun NuGetIndexEntry.toRegistrationEntry(atId: String, atType: List<String>, downloadUrl: String): JsonRegistrationPackageResponse {
    return JsonRegistrationPackageResponse(
            atId,
            atType,
            downloadUrl,
            this.getValue(NuGetPackageAttributes.ID)!!,
            this.getValue(NuGetPackageAttributes.NORMALIZED_VERSION)!!,
            this.attributes[NuGetPackageAttributes.IS_PRERELEASE]?.toBoolean() ?: false,
            this.getValue(NuGetPackageAttributes.AUTHORS),
            this.getDate(NuGetPackageAttributes.CREATED),
            this.getValue(NuGetPackageAttributes.DESCRIPTION),
            this.getDependencyGroups(atId),
            this.getValue(NuGetPackageAttributes.LANGUAGE),
            this.getDate(NuGetPackageAttributes.LAST_EDITED),
            this.getValue(NuGetPackageAttributes.PACKAGE_HASH),
            this.getValue(NuGetPackageAttributes.PACKAGE_HASH_ALGORITHM),
            this.getValue(NuGetPackageAttributes.PACKAGE_SIZE)?.toLong(),
            this.getDate(NuGetPackageAttributes.PUBLISHED),
            this.attributes[NuGetPackageAttributes.REQUIRE_LICENSE_ACCEPTANCE]?.toBoolean() ?: false,
            this.getValue(NuGetPackageAttributes.SUMMARY)
    )
}

internal fun NuGetIndexEntry.getValue(key: String): String? {
    return NuGetUtils.getValue(this.attributes, key)
}

private fun NuGetIndexEntry.getDate(key: String): Date {
    attributes[key]?.let {
        ODataDataFormat.parseDate(it)?.let {
            return it.toDate()
        }
    }
    return Date()
}

private fun NuGetIndexEntry.getDependencyGroups(registrationUrl: String): List<JsonPackageDependencyGroup> {
    val groups = LinkedHashMap<String, MutableList<JsonPackageDependency>>()
    (this.getValue(NuGetPackageAttributes.DEPENDENCIES)?:"").split('|').forEach {
        val parts = it.split(':')
        when(parts.size) {
            2 -> {
                groups.getOrPut("", { mutableListOf() }).add(JsonPackageDependency(
                        "$registrationUrl#dependencygroup/${parts[0].toLowerCase()}",
                        "PackageDependency",
                        parts[0],
                        null,
                        parts[1]
                ))
            }
            3 -> {
                groups.getOrPut(parts[2], { mutableListOf() }).add(JsonPackageDependency(
                        "$registrationUrl#dependencygroup/${parts[2].toLowerCase()}/${parts[0].toLowerCase()}",
                        "PackageDependency",
                        parts[0],
                        null,
                        parts[1]
                ))
            }
        }
    }
    return groups.map {
        JsonPackageDependencyGroup(
                "$registrationUrl#dependencygroup/${it.key.toLowerCase()}",
                "PackageDependencyGroup",
                if (it.key.isEmpty()) null else it.key,
                it.value
        )
    }
}

inline fun <reified T> HttpServletResponse.writeJson(obj: T) {
    this.status = HttpServletResponse.SC_OK
    this.contentType = "application/json"
    JsonWriter(OutputStreamWriter(this.outputStream, "UTF-8")).use {
        JsonExtensions.gson.toJson(obj, T::class.java, it)
    }
}

internal fun HttpServletRequest.includeSemVer2(): Boolean {
    return this.getParameter(MetadataConstants.SEMANTIC_VERSION)?.let {
        SemanticVersion.valueOf(it)?.let {
            it >= VERSION_20
        }
    } ?: false
}

internal fun HttpServletRequest.getRootUrl(): String {
    return HttpServletRequestUtil.getRootUrl(this);
}

internal fun HttpServletRequest.getRootUrlWithAuthenticationType(): String {
    return HttpServletRequestUtil.getRootUrlWithAuthenticationType(this);
}

private val VERSION_20 = SemanticVersion.valueOf("2.0.0")!!

object JsonExtensions {
    val gson: Gson = GsonBuilder()
            .setPrettyPrinting()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
            .create()
}


