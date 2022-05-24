package jetbrains.buildServer.nuget.tests.agent.index

import jetbrains.buildServer.ExtensionHolder
import jetbrains.buildServer.agent.BuildProgressLogger
import jetbrains.buildServer.agent.InternalPropertiesHolder
import jetbrains.buildServer.agent.impl.AgentEventDispatcher
import jetbrains.buildServer.agent.impl.artifacts.ArchivePreprocessor
import jetbrains.buildServer.agent.impl.artifacts.ZipPreprocessor
import jetbrains.buildServer.nuget.agent.index.NuGetPackagePathProvider
import org.jmock.Expectations
import org.jmock.Mockery
import org.testng.Assert
import org.testng.annotations.DataProvider
import org.testng.annotations.Test
import java.util.function.BiFunction

class NuGetPackagePathProviderTest {

    @Test(dataProvider = "testPackagePaths")
    fun testPackagePath(path: String, fileName: String, expectedPath: String?) {
        val m = Mockery()
        val extensionHolder = m.mock(ExtensionHolder::class.java)
        val buildProgressLogger = m.mock(BuildProgressLogger::class.java)
        val preprocessor = ZipPreprocessor(buildProgressLogger, createTempDir(), BiFunction<String, String, String> {k, d -> d})

        m.checking(object: Expectations() {
            init {
                allowing(extensionHolder).getExtensions(with(ArchivePreprocessor::class.java))
                will(returnValue(listOf(preprocessor)))
            }
        })

        val artifactPath = NuGetPackagePathProvider(extensionHolder).getArtifactPath(path, fileName)
        Assert.assertEquals(artifactPath, expectedPath)
    }

    @DataProvider
    fun testPackagePaths(): Array<Array<Any?>> {
        return arrayOf(
                arrayOf<Any?>("archive.zip", "package.nupkg", null),
                arrayOf<Any?>("archive.zip/", "package.nupkg", null),
                arrayOf<Any?>("archive.zip!/packages", "package.nupkg", null),
                arrayOf<Any?>("path/to/", "package.nupkg", "path/to/package.nupkg"),
                arrayOf<Any?>("", "package.nupkg", "package.nupkg"),
                arrayOf<Any?>("/", "package.nupkg", "package.nupkg"),
                arrayOf<Any?>("\\", "package.nupkg", "package.nupkg")
        )
    }
}
