package jetbrains.buildServer.nuget.tests.agent

import jetbrains.buildServer.nuget.agent.commands.impl.CommandFactoryImpl
import jetbrains.buildServer.nuget.agent.parameters.NuGetFetchParameters
import jetbrains.buildServer.nuget.agent.parameters.PackagesUpdateParameters
import jetbrains.buildServer.util.FileUtil
import org.jmock.Expectations
import org.jmock.Mockery
import org.testng.Assert
import org.testng.annotations.Test
import java.io.File

class CommandFactoryTest {
    @Test
    fun testUpdateCommand() {
        val m = Mockery()
        val parameters = m.mock(PackagesUpdateParameters::class.java)
        val fetchParameters = m.mock(NuGetFetchParameters::class.java)
        val factory = CommandFactoryImpl()
        val nugetExe = File("nuget.exe")

        m.checking(object : Expectations() {
            init {
                oneOf(parameters).useSafeUpdate; will(returnValue(true))
                oneOf(parameters).includePrereleasePackages; will(returnValue(true))
                oneOf(parameters).customCommandline; will(returnValue(listOf("-Id", "package1", "-Id", "package2")))
                oneOf(parameters).packagesToUpdate; will(returnValue(listOf("package3")))
                oneOf(parameters).nuGetParameters; will(returnValue(fetchParameters))

                oneOf(fetchParameters).nuGetPackageSources; will(returnValue(emptyList<String>()))
                oneOf(fetchParameters).nuGetExeFile; will(returnValue(nugetExe))
            }
        })

        val packagesConfig = FileUtil.createTempFile("nuget", "packages.config")
        val targetDir = FileUtil.createTempDirectory("nuget", "target")

        try {
            factory.createUpdate(parameters, packagesConfig, targetDir) {
                program, workingDir, arguments, environment ->
                Assert.assertEquals(program, nugetExe)
                Assert.assertEquals(workingDir, packagesConfig.parentFile)
                Assert.assertEquals(arguments, listOf(
                        "update",
                        packagesConfig.absolutePath,
                        "-Safe",
                        "-Prerelease",
                        "-Id",
                        "package1",
                        "-Id",
                        "package2",
                        "-RepositoryPath",
                        targetDir.absolutePath,
                        "-Id",
                        "package3"
                ))
            }
        } finally {
            FileUtil.delete(packagesConfig)
            FileUtil.delete(targetDir)
        }
    }
}
