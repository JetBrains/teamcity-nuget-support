package jetbrains.buildServer.nuget.tests.server.feed.server

import jetbrains.buildServer.nuget.feed.server.NuGetServerSettings
import jetbrains.buildServer.nuget.feed.server.cache.ResponseCacheReset
import jetbrains.buildServer.nuget.feed.server.index.NuGetFeedData
import jetbrains.buildServer.nuget.feed.server.index.impl.NuGetArtifactsMetadataProvider
import jetbrains.buildServer.nuget.feed.server.index.impl.NuGetBuildMetadataProvider
import jetbrains.buildServer.nuget.feed.server.index.impl.NuGetBuildFeedsProvider
import jetbrains.buildServer.nuget.feedReader.NuGetPackageAttributes
import jetbrains.buildServer.serverSide.SBuild
import jetbrains.buildServer.serverSide.metadata.MetadataStorage
import jetbrains.buildServer.serverSide.metadata.MetadataStorageWriter
import org.jmock.Expectations
import org.jmock.Mockery
import org.testng.annotations.Test

@Test
class NuGetArtifactsMetadataProviderTest {

    fun doNotWriteMetadataInDisabledServer() {
        val m = Mockery()
        val cacheReset = m.mock(ResponseCacheReset::class.java)
        val serverSettings = m.mock(NuGetServerSettings::class.java)
        val buildMetadataProvider = m.mock(NuGetBuildMetadataProvider::class.java)
        val targetFeedProvider = m.mock(NuGetBuildFeedsProvider::class.java)
        val metadataStorage = m.mock(MetadataStorage::class.java)
        val build = m.mock(SBuild::class.java)
        val storageWriter = m.mock(MetadataStorageWriter::class.java)

        val metadataProvider = NuGetArtifactsMetadataProvider(cacheReset, serverSettings, buildMetadataProvider, targetFeedProvider, metadataStorage)

        m.checking(object : Expectations() {
            init {
                oneOf(serverSettings).isNuGetServerEnabled
                will(returnValue(false))

                oneOf(build).buildId
                will(returnValue(1L))

                oneOf(build).buildNumber
                will(returnValue("123"))

                oneOf(build).buildTypeExternalId
                will(returnValue("bt"))
            }
        })

        metadataProvider.generateMedatadata(build, storageWriter)

        m.assertIsSatisfied()
    }

    fun doNotWriteMetadataWithoutTargetFeeds() {
        val m = Mockery()
        val cacheReset = m.mock(ResponseCacheReset::class.java)
        val serverSettings = m.mock(NuGetServerSettings::class.java)
        val buildMetadataProvider = m.mock(NuGetBuildMetadataProvider::class.java)
        val targetFeedProvider = m.mock(NuGetBuildFeedsProvider::class.java)
        val metadataStorage = m.mock(MetadataStorage::class.java)
        val build = m.mock(SBuild::class.java)
        val storageWriter = m.mock(MetadataStorageWriter::class.java)

        val metadataProvider = NuGetArtifactsMetadataProvider(cacheReset, serverSettings, buildMetadataProvider, targetFeedProvider, metadataStorage)

        m.checking(object : Expectations() {
            init {
                oneOf(serverSettings).isNuGetServerEnabled
                will(returnValue(true))

                oneOf(targetFeedProvider).getFeeds(build)
                will(returnValue(emptySet<NuGetFeedData>()))

                oneOf(build).buildId
                will(returnValue(1L))

                oneOf(build).buildNumber
                will(returnValue("123"))

                oneOf(build).buildTypeExternalId
                will(returnValue("bt"))
            }
        })

        metadataProvider.generateMedatadata(build, storageWriter)

        m.assertIsSatisfied()
    }

    fun doNotWriteMetadataForBuildWithoutPackages() {
        val m = Mockery()
        val cacheReset = m.mock(ResponseCacheReset::class.java)
        val serverSettings = m.mock(NuGetServerSettings::class.java)
        val buildMetadataProvider = m.mock(NuGetBuildMetadataProvider::class.java)
        val targetFeedProvider = m.mock(NuGetBuildFeedsProvider::class.java)
        val metadataStorage = m.mock(MetadataStorage::class.java)
        val build = m.mock(SBuild::class.java)
        val storageWriter = m.mock(MetadataStorageWriter::class.java)

        val metadataProvider = NuGetArtifactsMetadataProvider(cacheReset, serverSettings, buildMetadataProvider, targetFeedProvider, metadataStorage)

        m.checking(object : Expectations() {
            init {
                oneOf(serverSettings).isNuGetServerEnabled
                will(returnValue(true))

                oneOf(targetFeedProvider).getFeeds(build)
                will(returnValue(setOf(NuGetFeedData.DEFAULT)))

                oneOf(buildMetadataProvider).getPackagesMetadata(build)
                will(returnValue(emptyList<Map<String, String>>()))

                oneOf(build).buildId
                will(returnValue(1L))

                oneOf(build).buildNumber
                will(returnValue("123"))

                oneOf(build).buildTypeExternalId
                will(returnValue("bt"))
            }
        })

        metadataProvider.generateMedatadata(build, storageWriter)

        m.assertIsSatisfied()
    }

    fun writeMetadataIntoRootProjectFeed() {
        val m = Mockery()
        val cacheReset = m.mock(ResponseCacheReset::class.java)
        val serverSettings = m.mock(NuGetServerSettings::class.java)
        val buildMetadataProvider = m.mock(NuGetBuildMetadataProvider::class.java)
        val targetFeedProvider = m.mock(NuGetBuildFeedsProvider::class.java)
        val metadataStorage = m.mock(MetadataStorage::class.java)
        val build = m.mock(SBuild::class.java)
        val storageWriter = m.mock(MetadataStorageWriter::class.java)
        val packageMetadata = mapOf(
            NuGetPackageAttributes.ID to "id",
            NuGetPackageAttributes.NORMALIZED_VERSION to "1.0.0"
        )

        val metadataProvider = NuGetArtifactsMetadataProvider(cacheReset, serverSettings, buildMetadataProvider, targetFeedProvider, metadataStorage)

        m.checking(object : Expectations() {
            init {
                oneOf(serverSettings).isNuGetServerEnabled
                will(returnValue(true))

                oneOf(targetFeedProvider).getFeeds(build)
                will(returnValue(setOf(NuGetFeedData.DEFAULT)))

                allowing(build).buildId
                will(returnValue(1L))

                allowing(build).buildNumber
                will(returnValue("123"))

                allowing(build).buildTypeExternalId
                will(returnValue("bt"))

                oneOf(buildMetadataProvider).getPackagesMetadata(build)

                will(returnValue(listOf(packageMetadata)))

                oneOf(storageWriter).addParameters("id.1.0.0", packageMetadata)

                oneOf(cacheReset).resetCache()
            }
        })

        metadataProvider.generateMedatadata(build, storageWriter)

        m.assertIsSatisfied()
    }

    fun writeMetadataIntoNotRootProjectFeed() {
        val m = Mockery()
        val cacheReset = m.mock(ResponseCacheReset::class.java)
        val serverSettings = m.mock(NuGetServerSettings::class.java)
        val buildMetadataProvider = m.mock(NuGetBuildMetadataProvider::class.java)
        val targetFeedProvider = m.mock(NuGetBuildFeedsProvider::class.java)
        val metadataStorage = m.mock(MetadataStorage::class.java)
        val build = m.mock(SBuild::class.java)
        val storageWriter = m.mock(MetadataStorageWriter::class.java)
        val packageMetadata = mapOf(
            NuGetPackageAttributes.ID to "id",
            NuGetPackageAttributes.NORMALIZED_VERSION to "1.0.0"
        )

        val metadataProvider = NuGetArtifactsMetadataProvider(cacheReset, serverSettings, buildMetadataProvider, targetFeedProvider, metadataStorage)

        m.checking(object : Expectations() {
            init {
                oneOf(serverSettings).isNuGetServerEnabled
                will(returnValue(true))

                oneOf(targetFeedProvider).getFeeds(build)
                will(returnValue(setOf(NuGetFeedData("projectId", "feed"))))

                allowing(build).buildId
                will(returnValue(1L))

                allowing(build).buildNumber
                will(returnValue("123"))

                allowing(build).buildTypeExternalId
                will(returnValue("bt"))

                oneOf(build).isPersonal
                will(returnValue(false))

                oneOf(buildMetadataProvider).getPackagesMetadata(build)

                will(returnValue(listOf(packageMetadata)))

                oneOf(metadataStorage).addBuildEntry(1L, "nuget.projectId.feed", "id.1.0.0", packageMetadata, true)

                oneOf(cacheReset).resetCache()
            }
        })

        metadataProvider.generateMedatadata(build, storageWriter)

        m.assertIsSatisfied()
    }
}