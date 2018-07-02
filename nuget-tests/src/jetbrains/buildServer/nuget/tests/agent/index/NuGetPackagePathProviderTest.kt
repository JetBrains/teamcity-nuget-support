package jetbrains.buildServer.nuget.tests.agent.index

import jetbrains.buildServer.ExtensionHolder
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

class NuGetPackagePathProviderTest {

    @Test(dataProvider = "testPackagePaths")
    fun testPackagePath(path: String, fileName: String, expectedPath: String) {
        val m = Mockery()
        val extensionHolder = m.mock(ExtensionHolder::class.java)
        val propertiesHolder = m.mock(InternalPropertiesHolder::class.java)
        val preprocessor = ZipPreprocessor(AgentEventDispatcher(), propertiesHolder)

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
    fun testPackagePaths(): Array<Array<Any>> {
        return arrayOf(
                arrayOf<Any>("archive.zip", "package.nupkg", "archive.zip!/package.nupkg"),
                arrayOf<Any>("archive.zip/", "package.nupkg", "archive.zip!/package.nupkg"),
                arrayOf<Any>("path/to/", "package.nupkg", "path/to/package.nupkg"),
                arrayOf<Any>("", "package.nupkg", "package.nupkg"),
                arrayOf<Any>("/", "package.nupkg", "package.nupkg"),
                arrayOf<Any>("\\", "package.nupkg", "package.nupkg")
        )
    }
}
