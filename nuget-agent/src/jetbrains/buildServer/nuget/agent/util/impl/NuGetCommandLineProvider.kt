package jetbrains.buildServer.nuget.agent.util.impl

import com.intellij.execution.configurations.GeneralCommandLine
import jetbrains.buildServer.agent.BuildRunnerContext
import jetbrains.buildServer.agent.runner.ProgramCommandLine
import jetbrains.buildServer.agent.runner.SimpleProgramCommandLine
import jetbrains.buildServer.dotNet.DotNetConstants
import jetbrains.buildServer.nuget.agent.util.CommandLineExecutor
import jetbrains.buildServer.nuget.agent.util.SystemInformation
import jetbrains.buildServer.nuget.common.auth.NuGetAuthConstants.*
import jetbrains.buildServer.nuget.common.exec.NuGetTeamCityProvider
import jetbrains.buildServer.nuget.common.version.SemanticVersion
import java.io.File

import java.util.concurrent.ConcurrentHashMap

/**
 * Constructs NuGet command line.
 */
class NuGetCommandLineProvider(private val myNugetProvider: NuGetTeamCityProvider,
                               private val myCommandLineExecutor: CommandLineExecutor,
                               private val mySystemInformation: SystemInformation) {

    private val myVersions = ConcurrentHashMap<String, SemanticVersion>()

    fun getProgramCommandLine(context: BuildRunnerContext,
                              executable: String,
                              args: Collection<String>,
                              workingDir: File,
                              env: Map<String, String>): ProgramCommandLine {
        var (executablePath, arguments) = getExecutableAndArguments(executable, args, context)
        val buildLogger = context.build.buildLogger
        val environment = context.buildParameters.environmentVariables.toMutableMap()
        val version = myVersions.getOrPut(executable) { getNugetVersion(executable, context) }

        // Since NuGet 2.0 for authentication could be used NuGet runner.
        // Since NuGet 3.3 it's possible to use credential providers:
        // https://docs.microsoft.com/en-us/nuget/reference/extensibility/nuget-exe-credential-providers
        // Since NuGet 4.8 it's possible to use NuGet authentication plugin:
        // https://github.com/NuGet/Home/wiki/NuGet-cross-plat-authentication-plugin
        when {
            version < NUGET_VERSION_2_0 -> {
                buildLogger.warning("You use NuGet $version. Feed authentication is only supported from NuGet $NUGET_VERSION_2_0")
            }
            version < NUGET_VERSION_3_3 -> {
                arguments.add(0, executablePath)
                executablePath = myNugetProvider.nuGetRunnerPath.path
            }
            else -> {
                // NuGet 3.5+ does not properly work under SYSTEM account on Windows:
                // https://github.com/NuGet/Home/issues/4277
                if (version >= NUGET_VERSION_3_5 && mySystemInformation.isWindows &&
                        "SYSTEM".equals(mySystemInformation.userName, true) &&
                        !context.buildParameters.environmentVariables.containsKey(NUGET_PACKAGES_ENV)) {
                    val packagesPath = File(context.build.buildTempDirectory, ".nuget/packages")
                    buildLogger.message("Setting '$NUGET_PACKAGES_ENV' environment variable to '$packagesPath'")
                    environment[NUGET_PACKAGES_ENV] = packagesPath.path
                    buildLogger.message("##teamcity[setParameter name='env.$NUGET_PACKAGES_ENV' value='$packagesPath']")
                }

                // NuGet < 4.8 and NuGet < 4.9 on Mono does not support credentials plugin,
                // so we need to remove environment variable to prevent runtime errors
                if (version < NUGET_VERSION_4_8 ||
                    version < NUGET_VERSION_4_9 && !mySystemInformation.isWindows) {
                    environment.remove(NUGET_PLUGIN_PATH_ENV_VAR)
                }
            }
        }

        // Disable interactive mode for credentials requests
        environment["NUGET_EXE_NO_PROMPT"] = "true"
        environment.putAll(env)

        return SimpleProgramCommandLine(
                environment,
                workingDir.path,
                executablePath,
                arguments
        )
    }

    private fun getExecutableAndArguments(executable: String,
                                          args: Collection<String>,
                                          context: BuildRunnerContext): Pair<String, MutableList<String>> {
        val arguments = args.toMutableList()
        if (!mySystemInformation.isWindows) {
            context.configParameters[DotNetConstants.MONO_JIT]?.let { monoPath ->
                if (monoPath.isNotEmpty()) {
                    arguments.add(0, executable)
                    return monoPath to arguments
                }
            }
        }

        return executable to arguments
    }

    private fun getNugetVersion(executable: String, context: BuildRunnerContext): SemanticVersion {
        // Try to get version from executable path
        NUGET_PATH_REGEX.find(executable.replace("\\", "/"))?.let {
            val (version) = it.destructured
            SemanticVersion.valueOf(version)?.let {
                return it
            }
        }

        // Try to get version from NuGet output
        val (executablePath, arguments) = getExecutableAndArguments(executable, emptyList(), context)
        val commandLine = GeneralCommandLine().apply {
            this.exePath = executablePath
            this.addParameters(arguments)
        }

        val result = myCommandLineExecutor.execute(commandLine)
        if (result.exitCode == 0) {
            NUGET_VERSION_REGEX.find(result.stdout)?.let {
                val (version) = it.destructured
                SemanticVersion.valueOf(version)?.let {
                    return it
                }
            }
        }

        return NUGET_UNKNOWN_VERSION
    }

    companion object {
        private const val NUGET_PACKAGES_ENV = "NUGET_PACKAGES"
        private val NUGET_VERSION_2_0 = SemanticVersion.valueOf("2.0.0")!!
        private val NUGET_VERSION_3_3 = SemanticVersion.valueOf("3.3.0")!!
        private val NUGET_VERSION_3_5 = SemanticVersion.valueOf("3.5.0")!!
        private val NUGET_VERSION_4_8 = SemanticVersion.valueOf("4.8.0")!!
        private val NUGET_VERSION_4_9 = SemanticVersion.valueOf("4.9.0")!!
        private val NUGET_UNKNOWN_VERSION = SemanticVersion.valueOf("0.0.0")!!
        private val NUGET_VERSION_REGEX = Regex("NuGet Version:\\s([\\d\\.]+)", RegexOption.IGNORE_CASE)
        private val NUGET_PATH_REGEX = Regex("\\/NuGet\\.CommandLine\\.([^\\/]+)\\/tools\\/NuGet\\.exe\$", RegexOption.IGNORE_CASE)
    }
}
