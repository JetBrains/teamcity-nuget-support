package jetbrains.buildServer.nuget.tests.server.feed.server

import jetbrains.buildServer.BaseTestCase
import jetbrains.buildServer.nuget.common.PackageLoadException
import jetbrains.buildServer.nuget.common.index.PackageAnalyzer
import jetbrains.buildServer.nuget.feed.server.index.impl.NuGetBuildMetadataProviderImpl
import jetbrains.buildServer.serverSide.BuildPromotionEx
import jetbrains.buildServer.serverSide.CurrentNodeInfo
import jetbrains.buildServer.serverSide.SBuild
import jetbrains.buildServer.serverSide.ServerResponsibilityImpl
import jetbrains.buildServer.serverSide.artifacts.BuildArtifact
import jetbrains.buildServer.serverSide.artifacts.BuildArtifactHolder
import jetbrains.buildServer.serverSide.artifacts.BuildArtifacts
import jetbrains.buildServer.serverSide.artifacts.BuildArtifactsViewMode
import jetbrains.buildServer.util.FileUtil
import org.hamcrest.Description
import org.jmock.Expectations
import org.jmock.Mockery
import org.jmock.api.Action
import org.jmock.api.Invocation
import org.testng.Assert
import org.testng.annotations.Test
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Paths

@Test
class NuGetBuildMetadataProviderTest : BaseTestCase() {

    fun readAgentProvidedPackagesList() {
        val m = Mockery()
        val packageAnalyzer = m.mock(PackageAnalyzer::class.java)
        val buildArtifacts = m.mock(BuildArtifacts::class.java)
        val buildArtifact = m.mock(BuildArtifact::class.java, "json")
        val buildAgentArtifact = m.mock(BuildArtifact::class.java, "jsonAgent")

        val build = m.mock(SBuild::class.java)
        CurrentNodeInfo.init()

        val buildArtifactHolder = m.mock(BuildArtifactHolder::class.java)

        val buildPromotion = m.mock(BuildPromotionEx::class.java)
        CurrentNodeInfo.init()
        val responsibility = object : ServerResponsibilityImpl() {
            override fun isResponsibleForBuild(build: SBuild): Boolean {
                return true
            }
        }

        val metadataProvider = NuGetBuildMetadataProviderImpl(packageAnalyzer, responsibility)

        val tempArtifactsDir = createTempDir()
        FileUtil.copyDir(Paths.get("testData/feed/indexer").toFile(), tempArtifactsDir)

        m.checking(object : Expectations() {
            init {
                allowing(build).buildPromotion
                will(returnValue(buildPromotion))

                allowing(buildPromotion)

                allowing(build).getArtifacts(BuildArtifactsViewMode.VIEW_ALL)
                will(returnValue(buildArtifacts))

                allowing(build).artifactsDirectory
                will(returnValue(tempArtifactsDir))

                oneOf(buildArtifacts).getArtifact(".teamcity/nuget/packages.json")
                will(returnValue(buildArtifact))

                oneOf(buildArtifacts).getArtifact(".teamcity/nuget/temp.packages.json")
                will(returnValue(buildAgentArtifact))

                oneOf(buildArtifact).inputStream
                will (object : Action {
                    var myStream : InputStream? = null
                    override fun describeTo(description: Description?) {
                        description?.appendText("returns ");
                        description?.appendValue(myStream ?: "No opened stream");
                    }
                    override fun invoke(invocation: Invocation?): Any {
                        myStream = Files.newInputStream(Paths.get(tempArtifactsDir.path, ".teamcity/nuget/packages.json"))
                        return myStream!!
                    }
                })

                oneOf(buildArtifacts).findArtifact("aa")
                will(returnValue(buildArtifactHolder))

                exactly(2).of(buildArtifactHolder).isAccessible
                will(returnValue(true))

                exactly(2).of(buildArtifactHolder).isAvailable
                will(returnValue(true))

                oneOf(buildArtifactHolder).relativePath
                will(returnValue(""))
            }
        })

        val metadata = metadataProvider.getPackagesMetadata(build)

        Assert.assertTrue(metadata.packages.isNotEmpty())
        val first = metadata.packages.first()
        Assert.assertEquals(first.metadata["Id"], "NuGetFeedTest")
        Assert.assertEquals(first.metadata["NormalizedVersion"], "0.0.138")

        m.assertIsSatisfied()
    }

    fun readServerProvidedPackagesList() {
        val m = Mockery()
        val packageAnalyzer = m.mock(PackageAnalyzer::class.java)
        val buildArtifacts = m.mock(BuildArtifacts::class.java)
        val buildArtifact = m.mock(BuildArtifact::class.java, "json")
        val build = m.mock(SBuild::class.java)
        val buildArtifactHolder = m.mock(BuildArtifactHolder::class.java)
        val metadataProvider = NuGetBuildMetadataProviderImpl(packageAnalyzer, ServerResponsibilityImpl())

        m.checking(object : Expectations() {
            init {
                allowing(build).getArtifacts(BuildArtifactsViewMode.VIEW_ALL)
                will(returnValue(buildArtifacts))

                oneOf(buildArtifacts).getArtifact(".teamcity/nuget/packages.json")
                will(returnValue(buildArtifact))

                oneOf(buildArtifacts).getArtifact(".teamcity/nuget/temp.packages.json")
                will(returnValue(null))

                oneOf(buildArtifact).inputStream
                will(returnValue(Files.newInputStream(Paths.get("testData/feed/indexer/.teamcity/nuget/packages.json"))))

                oneOf(buildArtifacts).findArtifact("aa")
                will(returnValue(buildArtifactHolder))

                exactly(2).of(buildArtifactHolder).isAccessible
                will(returnValue(true))

                exactly(2).of(buildArtifactHolder).isAvailable
                will(returnValue(true))

                oneOf(buildArtifactHolder).relativePath
                will(returnValue(""))
            }
        })

        val metadata = metadataProvider.getPackagesMetadata(build)

        Assert.assertTrue(metadata.packages.isNotEmpty())
        val first = metadata.packages.first()
        Assert.assertEquals(first.metadata["Id"], "NuGetFeedTest")
        Assert.assertEquals(first.metadata["NormalizedVersion"], "0.0.137")

        m.assertIsSatisfied()
    }

    fun indexArtifactsIfNoPackagesList() {
        val m = Mockery()
        val packageAnalyzer = m.mock(PackageAnalyzer::class.java)
        val buildArtifacts = m.mock(BuildArtifacts::class.java)
        val buildArtifact = m.mock(BuildArtifact::class.java)
        val build = m.mock(SBuild::class.java)
        val buildPromotion = m.mock(BuildPromotionEx::class.java)
        CurrentNodeInfo.init()
        val responsibility = object : ServerResponsibilityImpl() {
          override fun isResponsibleForBuild(build: SBuild): Boolean {
            return true
          }
        }
        val metadataProvider = NuGetBuildMetadataProviderImpl(packageAnalyzer, responsibility)
        val artifactsDir = createTempDir()

        m.checking(object : Expectations() {
            init {
                allowing(build).buildPromotion
                will(returnValue(buildPromotion))

                allowing(buildPromotion)

                exactly(2).of(build).getArtifacts(BuildArtifactsViewMode.VIEW_ALL)
                will(returnValue(buildArtifacts))

                oneOf(buildArtifacts).getArtifact(".teamcity/nuget/packages.json")
                will(returnValue(null))

                oneOf(buildArtifacts).getArtifact(".teamcity/nuget/temp.packages.json")
                will(returnValue(null))

                oneOf(buildArtifacts).rootArtifact
                will(returnValue(buildArtifact))

                oneOf(buildArtifact).isDirectory
                will(returnValue(false))

                oneOf(buildArtifact).name
                will(returnValue("package.nupkg"))

                allowing(buildArtifact).relativePath
                will(returnValue("package.nupkg"))

                allowing(buildArtifact).size
                will(returnValue(1L))

                val inputStream = Files.newInputStream(Paths.get("testData/feed/indexer/$PACKAGES_PATH"))
                allowing(buildArtifact).inputStream
                will(returnValue(inputStream))

                oneOf(packageAnalyzer).analyzePackage(inputStream)
                will(returnValue(mapOf("Id" to "id", "NormalizedVersion" to "1.0.0")))

                oneOf(packageAnalyzer).getSha512Hash(inputStream)
                will(returnValue("hash"))

                allowing(build).buildId
                will(returnValue(1L))

                allowing(build).buildNumber
                will(returnValue("123"))

                allowing(build).buildTypeExternalId
                will(returnValue("bt"))

                allowing(build).buildTypeId
                will(returnValue("bt"))

                oneOf(build).finishDate
                will(returnValue(null))

                oneOf(build).artifactsDirectory
                will(returnValue(artifactsDir))
            }
        })

        val metadata = metadataProvider.getPackagesMetadata(build)

        Assert.assertTrue(metadata.packages.isNotEmpty())
        val first = metadata.packages.first()
        Assert.assertEquals(first.metadata["Id"], "id")
        Assert.assertEquals(first.metadata["NormalizedVersion"], "1.0.0")
        val packagesFile = artifactsDir.toPath().resolve(PACKAGES_PATH)
        Assert.assertTrue(Files.exists(packagesFile))
        Assert.assertTrue(Files.size(packagesFile) > 0)

        m.assertIsSatisfied()
    }

    fun doNotCreatePackagesJSONIfServerIsReadOnly() {
        val m = Mockery()
        val packageAnalyzer = m.mock(PackageAnalyzer::class.java)
        val buildArtifacts = m.mock(BuildArtifacts::class.java)
        val buildArtifact = m.mock(BuildArtifact::class.java)
        val build = m.mock(SBuild::class.java)
        val responsibility = object : ServerResponsibilityImpl() {
            override fun isResponsibleForBuild(build: SBuild): Boolean {
                return false
            }
        }

        val metadataProvider = NuGetBuildMetadataProviderImpl(packageAnalyzer, responsibility)
        val artifactsDir = createTempDir()

        m.checking(object : Expectations() {
            init {
                exactly(2).of(build).getArtifacts(BuildArtifactsViewMode.VIEW_ALL)
                will(returnValue(buildArtifacts))

                oneOf(buildArtifacts).getArtifact(".teamcity/nuget/packages.json")
                will(returnValue(null))

                oneOf(buildArtifacts).getArtifact(".teamcity/nuget/temp.packages.json")
                will(returnValue(null))

                oneOf(buildArtifacts).rootArtifact
                will(returnValue(buildArtifact))

                oneOf(buildArtifact).isDirectory
                will(returnValue(false))

                oneOf(buildArtifact).name
                will(returnValue("package.nupkg"))

                allowing(buildArtifact).relativePath
                will(returnValue("package.nupkg"))

                allowing(buildArtifact).size
                will(returnValue(1L))

                val inputStream = Files.newInputStream(Paths.get("testData/feed/indexer/$PACKAGES_PATH"))
                allowing(buildArtifact).inputStream
                will(returnValue(inputStream))

                oneOf(packageAnalyzer).analyzePackage(inputStream)
                will(returnValue(mapOf("Id" to "id", "NormalizedVersion" to "1.0.0")))

                oneOf(packageAnalyzer).getSha512Hash(inputStream)
                will(returnValue("hash"))

                allowing(build).buildId
                will(returnValue(1L))

                allowing(build).buildNumber
                will(returnValue("123"))

                allowing(build).buildTypeExternalId
                will(returnValue("bt"))

                allowing(build).buildTypeId
                will(returnValue("bt"))

                oneOf(build).finishDate
                will(returnValue(null))

                allowing(build).artifactsDirectory
                will(returnValue(artifactsDir))
            }
        })

        val metadata = metadataProvider.getPackagesMetadata(build)

        Assert.assertTrue(metadata.packages.isNotEmpty())
        val first = metadata.packages.first().metadata
        Assert.assertEquals(first["Id"], "id")
        Assert.assertEquals(first["NormalizedVersion"], "1.0.0")
        val packagesFile = artifactsDir.toPath().resolve(PACKAGES_PATH)
        Assert.assertFalse(Files.exists(packagesFile))

        m.assertIsSatisfied()
    }

    fun indexArtifactsIfPackagesListCorrupted() {
        val m = Mockery()
        val packageAnalyzer = m.mock(PackageAnalyzer::class.java)
        val buildArtifacts = m.mock(BuildArtifacts::class.java)
        val packagesArtifact = m.mock(BuildArtifact::class.java , "packagesArtifact")
        val buildArtifact = m.mock(BuildArtifact::class.java)
        val build = m.mock(SBuild::class.java)
        val buildPromotion = m.mock(BuildPromotionEx::class.java)
        CurrentNodeInfo.init()
        val responsibility = object : ServerResponsibilityImpl() {
          override fun isResponsibleForBuild(build: SBuild): Boolean {
            return true
          }
        }
        val metadataProvider = NuGetBuildMetadataProviderImpl(packageAnalyzer, responsibility)
        val artifactsDir = createTempDir()
        val packagesFile = artifactsDir.toPath().resolve(PACKAGES_PATH)

        Files.createDirectories(packagesFile.parent)
        Files.write(packagesFile, listOf("abc"))

        m.checking(object : Expectations() {
            init {
                allowing(build).buildPromotion
                will(returnValue(buildPromotion))

                allowing(buildPromotion)

                exactly(2).of(build).getArtifacts(BuildArtifactsViewMode.VIEW_ALL)
                will(returnValue(buildArtifacts))

                oneOf(buildArtifacts).getArtifact(".teamcity/nuget/packages.json")
                will(returnValue(packagesArtifact))

                oneOf(buildArtifacts).getArtifact(".teamcity/nuget/temp.packages.json")
                will(returnValue(null))

                oneOf(packagesArtifact).inputStream
                will(returnValue(Files.newInputStream(packagesFile)))

                oneOf(buildArtifacts).rootArtifact
                will(returnValue(buildArtifact))

                oneOf(buildArtifact).isDirectory
                will(returnValue(false))

                oneOf(buildArtifact).name
                will(returnValue("package.nupkg"))

                allowing(buildArtifact).relativePath
                will(returnValue("package.nupkg"))

                allowing(buildArtifact).size
                will(returnValue(1L))

                val inputStream = Files.newInputStream(Paths.get("testData/feed/indexer/$PACKAGES_PATH"))
                allowing(buildArtifact).inputStream
                will(returnValue(inputStream))

                oneOf(packageAnalyzer).analyzePackage(inputStream)
                will(returnValue(mapOf("Id" to "id", "NormalizedVersion" to "1.0.0")))

                oneOf(packageAnalyzer).getSha512Hash(inputStream)
                will(returnValue("hash"))

                allowing(build).buildId
                will(returnValue(1L))

                allowing(build).buildNumber
                will(returnValue("123"))

                allowing(build).buildTypeExternalId
                will(returnValue("bt"))

                allowing(build).buildTypeId
                will(returnValue("bt"))

                oneOf(build).finishDate
                will(returnValue(null))

                exactly(2).of(build).artifactsDirectory
                will(returnValue(artifactsDir))
            }
        })

        val metadata = metadataProvider.getPackagesMetadata(build)

        Assert.assertTrue(metadata.packages.isNotEmpty())
        val first = metadata.packages.first()
        Assert.assertEquals(first.metadata["Id"], "id")
        Assert.assertEquals(first.metadata["NormalizedVersion"], "1.0.0")
        Assert.assertTrue(Files.exists(packagesFile))
        Assert.assertTrue(Files.size(packagesFile) > 0)

        m.assertIsSatisfied()
    }

    fun handleExceptionsFromPackageAnalyzer() {
        val m = Mockery()
        val packageAnalyzer = m.mock(PackageAnalyzer::class.java)
        val buildArtifacts = m.mock(BuildArtifacts::class.java)
        val buildArtifact = m.mock(BuildArtifact::class.java)
        val build = m.mock(SBuild::class.java)
        val buildPromotion = m.mock(BuildPromotionEx::class.java)
        val metadataProvider = NuGetBuildMetadataProviderImpl(packageAnalyzer, ServerResponsibilityImpl())
        val artifactsDir = createTempDir()
        val packagesFile = artifactsDir.toPath().resolve(PACKAGES_PATH)

        m.checking(object : Expectations() {
            init {
                allowing(build).buildPromotion
                will(returnValue(buildPromotion))

                allowing(buildPromotion)

                exactly(2).of(build).getArtifacts(BuildArtifactsViewMode.VIEW_ALL)
                will(returnValue(buildArtifacts))

                oneOf(buildArtifacts).getArtifact(".teamcity/nuget/packages.json")
                will(returnValue(null))

                oneOf(buildArtifacts).getArtifact(".teamcity/nuget/temp.packages.json")
                will(returnValue(null))

                oneOf(buildArtifacts).rootArtifact
                will(returnValue(buildArtifact))

                oneOf(buildArtifact).isDirectory
                will(returnValue(false))

                oneOf(buildArtifact).name
                will(returnValue("package.nupkg"))

                allowing(buildArtifact).relativePath
                will(returnValue("package.nupkg"))

                val inputStream = Files.newInputStream(Paths.get("testData/feed/indexer/$PACKAGES_PATH"))
                allowing(buildArtifact).inputStream
                will(returnValue(inputStream))

                oneOf(packageAnalyzer).analyzePackage(inputStream)
                will(throwException(PackageLoadException("Failure")))

                allowing(build).buildId
                will(returnValue(1L))

                allowing(build).buildNumber
                will(returnValue("123"))

                allowing(build).buildTypeExternalId
                will(returnValue("bt"))
            }
        })

        val metadata = metadataProvider.getPackagesMetadata(build)

        Assert.assertTrue(metadata.packages.isEmpty())
        Assert.assertFalse(Files.exists(packagesFile))

        m.assertIsSatisfied()
    }

    companion object {
        private const val PACKAGES_PATH = ".teamcity/nuget/packages.json"
    }
}
