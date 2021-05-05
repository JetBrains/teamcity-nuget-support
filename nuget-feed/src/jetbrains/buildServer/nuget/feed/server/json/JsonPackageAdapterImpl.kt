package jetbrains.buildServer.nuget.feed.server.json

import jetbrains.buildServer.nuget.common.version.VersionUtility
import jetbrains.buildServer.nuget.feed.server.index.NuGetIndexEntry
import jetbrains.buildServer.nuget.feedReader.NuGetPackageAttributes

class JsonPackageAdapterImpl(private val context: JsonNuGetFeedContext) : JsonPackageAdapter {
    override fun createPackageResponse(entry: NuGetIndexEntry): JsonRegistrationPackageResponse {
        return entry.toRegistrationEntry(
                context.getFullPath(),
                listOf("Package", "catalog:Permalink"),
                context.getDownloadUrl(entry))
    }

    override fun createPackagesResponse(id: String, entries: List<NuGetIndexEntry>): JsonRegistrationResponse {
        val items = entries.map {
            val version = VersionUtility.normalizeVersion(it.version)
            val registrationUrl = context.getRegistrationUrl(id, version)
            JsonRegistrationPackage(
                    registrationUrl,
                    "Package",
                    it.toRegistrationEntry(
                            registrationUrl,
                            listOf("PackageDetails"),
                            registrationUrl
                    ),
                    context.getDownloadUrl(it),
                    registrationUrl
            )
        }
        val registrationPage = JsonRegistrationPage(
                context.getFullPath(),
                entries.size,
                lower = VersionUtility.normalizeVersion(entries.first().version),
                upper = VersionUtility.normalizeVersion(entries.last().version),
                items = items
        )
        val registration = JsonRegistrationResponse(
                context.getFullPath(),
                listOf("catalog:CatalogRoot", "PackageRegistration", "catalog:Permalink"),
                1,
                listOf(registrationPage)
        )
        return registration
    }

    override fun createSearchPackagesResponse(entries: List<NuGetIndexEntry>, take: Int, skip: Int?): JsonSearchResponse {
        val results = entries.groupBy { it.packageInfo.id }
        val totalHits = results.size
        val data = arrayListOf<JsonPackage>()
        var keys = results.keys.asSequence()
        skip?.let {
            keys = keys.drop(it)
        }

        keys.take(take).forEach { packageId ->
            results[packageId]?.let { packages ->
                val entry = packages.last()
                val versions = packages.map {
                    val version = VersionUtility.normalizeVersion(it.version)
                    JsonPackageVersion(
                            context.getRegistrationUrl(packageId.toLowerCase(), version),
                            version,
                            0
                    )
                }
                val version = VersionUtility.normalizeVersion(entry.version)
                data.add(JsonPackage(
                        context.getRegistrationUrl(packageId.toLowerCase(), version),
                        "Package",
                        packageId,
                        version,
                        versions,
                        entry.attributes[NuGetPackageAttributes.DESCRIPTION],
                        entry.attributes[NuGetPackageAttributes.AUTHORS],
                        entry.attributes[NuGetPackageAttributes.ICON_URL],
                        entry.attributes[NuGetPackageAttributes.LICENSE_URL],
                        null,
                        entry.attributes[NuGetPackageAttributes.PROJECT_URL],
                        context.getRegistrations(packageId.toLowerCase()),
                        entry.attributes[NuGetPackageAttributes.SUMMARY],
                        entry.attributes[NuGetPackageAttributes.TAGS],
                        entry.attributes[NuGetPackageAttributes.TITLE],
                        null,
                        null
                ))
            }
        }

        return JsonSearchResponse(totalHits, data)
    }

    override fun createPackageVersionsResponse(entries: List<NuGetIndexEntry>): JsonPackageVersions {
        val versions = entries.map { VersionUtility.normalizeVersion(it.version) }
        return JsonPackageVersions(versions)
    }

    override fun createDownloadContentUrl(entry: NuGetIndexEntry, extension: String): String? {
        return when(extension.toLowerCase()) {
            "nupkg" -> context.getDownloadUrl(entry)
            "nuspec" -> context.getDownloadNuspecUrl(entry)
            else -> return null
        }
    }
}

