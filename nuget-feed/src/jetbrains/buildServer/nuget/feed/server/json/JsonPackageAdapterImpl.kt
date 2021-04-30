package jetbrains.buildServer.nuget.feed.server.json

import jetbrains.buildServer.nuget.common.version.VersionUtility
import jetbrains.buildServer.nuget.feed.server.index.NuGetIndexEntry

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
}

