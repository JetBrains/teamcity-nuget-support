package jetbrains.buildServer.nuget.tests.feed

import jetbrains.buildServer.nuget.common.index.PackageConstants
import jetbrains.buildServer.nuget.feed.server.index.NuGetFeedData
import jetbrains.buildServer.nuget.feed.server.index.impl.NuGetPackageBuilder
import jetbrains.buildServer.nuget.feed.server.index.impl.transform.DownloadUrlComputationTransformation
import jetbrains.buildServer.nuget.feedReader.NuGetPackageAttributes
import jetbrains.buildServer.serverSide.metadata.impl.metadata.BuildMetadataEntryImpl
import jetbrains.buildServer.serverSide.metadata.impl.metadata.EntryImpl
import jetbrains.buildServer.serverSide.metadata.impl.metadata.EntryKey
import org.testng.Assert
import org.testng.annotations.Test

class DownloadUrlComputationTransformationTest {
    @Test
    fun testDownloadUrlContainsExternalProjectId() {
        val buildId = 123L
        val buildTypeExtId = "buildTypeExtId"
        val projectExtId = "projectExtId"
        val artifactPath = "/artifact.nupkg"
        val parameters = mapOf(
                PackageConstants.TEAMCITY_ARTIFACT_RELPATH to artifactPath,
                NuGetPackageAttributes.VERSION to "1.0.0"
        )
        val entryKey = EntryKey(buildId, "key")
        val entryValue = BuildMetadataEntryImpl(entryKey, EntryImpl(true, parameters))
        val packageBuilder = NuGetPackageBuilder(NuGetFeedData("projectId", "feedId"), entryValue)
        packageBuilder.setBuildTypeExternalId(buildTypeExtId)
        packageBuilder.setProjectExternalId(projectExtId)

        DownloadUrlComputationTransformation().applyTransformation(packageBuilder)

        Assert.assertEquals(
                packageBuilder.downloadUrl,
                "/app/nuget/feed/projectExtId/feedId/download/buildTypeExtId/123:id/artifact.nupkg"
        )
    }

    @Test
    fun testEscapePlusCharInArtifactPath() {
        val buildId = 123L
        val buildTypeExtId = "buildTypeExtId"
        val projectExtId = "projectExtId"
        val artifactPath = "/artifact+metadata.nupkg"
        val parameters = mapOf(
                PackageConstants.TEAMCITY_ARTIFACT_RELPATH to artifactPath,
                NuGetPackageAttributes.VERSION to "1.0.0"
        )
        val entryKey = EntryKey(buildId, "key")
        val entryValue = BuildMetadataEntryImpl(entryKey, EntryImpl(true, parameters))
        val packageBuilder = NuGetPackageBuilder(NuGetFeedData("projectId", "feedId"), entryValue)
        packageBuilder.setBuildTypeExternalId(buildTypeExtId)
        packageBuilder.setProjectExternalId(projectExtId)

        DownloadUrlComputationTransformation().applyTransformation(packageBuilder)

        Assert.assertEquals(
                packageBuilder.downloadUrl,
                "/app/nuget/feed/projectExtId/feedId/download/buildTypeExtId/123:id/artifact%2Bmetadata.nupkg"
        )
    }
}
