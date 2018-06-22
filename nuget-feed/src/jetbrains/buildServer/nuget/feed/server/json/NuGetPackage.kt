package jetbrains.buildServer.nuget.feed.server.json

import jetbrains.buildServer.nuget.common.index.ODataDataFormat
import jetbrains.buildServer.nuget.feed.server.index.NuGetIndexEntry
import jetbrains.buildServer.nuget.feedReader.NuGetPackageAttributes
import java.util.*

open class NuGetPackage(private val entry: NuGetIndexEntry) {
    val id: String
        get() = entry.attributes[NuGetPackageAttributes.ID]!!

    val version: String
        get() = entry.attributes[NuGetPackageAttributes.NORMALIZED_VERSION]!!

    val isPrerelease: Boolean
        get() = entry.attributes[NuGetPackageAttributes.IS_PRERELEASE]?.toBoolean() ?: false

    val authors: String?
        get() = entry.attributes[NuGetPackageAttributes.AUTHORS]

    val created: Date
        get() = entry.getDate(NuGetPackageAttributes.CREATED)

    val description: String?
        get() = entry.attributes[NuGetPackageAttributes.DESCRIPTION]

    val language: String?
        get() = entry.attributes[NuGetPackageAttributes.LANGUAGE]

    val lastEdited: Date
        get() = entry.getDate(NuGetPackageAttributes.LAST_EDITED)

    val packageHash: String?
        get() = entry.attributes[NuGetPackageAttributes.PACKAGE_HASH]

    val packageHashAlgorithm: String?
        get() = entry.attributes[NuGetPackageAttributes.PACKAGE_HASH_ALGORITHM]

    val packageSize: Long?
        get() = entry.attributes[NuGetPackageAttributes.PACKAGE_SIZE]?.toLong()

    val published: Date
        get() = entry.getDate(NuGetPackageAttributes.PUBLISHED)

    val requireLicenseAcceptance: Boolean
        get() = entry.attributes[NuGetPackageAttributes.REQUIRE_LICENSE_ACCEPTANCE]?.toBoolean() ?: false

    val summary: String?
        get() = entry.attributes[NuGetPackageAttributes.SUMMARY]

    private fun NuGetIndexEntry.getDate(key: String): Date {
        attributes[key]?.let {
            ODataDataFormat.parseDate(it)?.let {
                return it.toDate()
            }
        }
        return Date()
    }
}

fun NuGetIndexEntry.getVersion(): String {
    return this.attributes[NuGetPackageAttributes.NORMALIZED_VERSION]!!.toLowerCase()
}
