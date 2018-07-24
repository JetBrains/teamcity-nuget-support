package jetbrains.buildServer.nuget.agent.util.impl

import com.intellij.execution.configurations.GeneralCommandLine
import com.intellij.openapi.util.SystemInfo
import jetbrains.buildServer.agent.BuildRunnerContext
import jetbrains.buildServer.agent.runner.ProgramCommandLine
import jetbrains.buildServer.agent.runner.SimpleProgramCommandLine
import jetbrains.buildServer.dotNet.DotNetConstants
import jetbrains.buildServer.nuget.agent.util.CommandLineExecutor
import jetbrains.buildServer.nuget.common.exec.NuGetTeamCityProvider
import jetbrains.buildServer.nuget.common.version.SemanticVersion
import java.io.File

import java.util.concurrent.ConcurrentHashMap

/**
 * Constructs NuGet command line.
 */
class NuGetCommandLineProvider(private val myNugetProvider: NuGetTeamCityProvider,
                               private val myCommandLineExecutor: CommandLineExecutor) {

    private val myVersions = ConcurrentHashMap<String, SemanticVersion>()

    fun getProgramCommandLine(context: BuildRunnerContext,
                              executable: String,
                              args: Collection<String>,
                              workingDir: File,
                              env: Map<String, String>): ProgramCommandLine {
        var (executablePath, arguments) = getExecutableAndArguments(executable, args, context)
        val buildLogger = context.build.buildLogger
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
                // NuGet 3.5+ does not properly work under SYSTEM account:
                // https://github.com/NuGet/Home/issues/4277
                if (version >= NUGET_VERSION_3_5 && "SYSTEM".equals(userName, true) &&
                    !context.buildParameters.environmentVariables.containsKey(NUGET_PACKAGES_ENV)) {
                    val packagesPath = File(context.build.buildTempDirectory, ".nuget/packages")
                    buildLogger.message("Setting '$NUGET_PACKAGES_ENV' environment variable to '$packagesPath'")
                    context.addEnvironmentVariable(NUGET_PACKAGES_ENV, packagesPath.path)
                    buildLogger.message("##teamcity[setParameter name='env.$NUGET_PACKAGES_ENV' value='$packagesPath']")
                }
            }
        }

        // Disable interactive mode for credentials requests
        context.addEnvironmentVariable("NUGET_EXE_NO_PROMPT", "true")
        for ((key, value) in env) {
            context.addEnvironmentVariable(key, value)
        }

        return SimpleProgramCommandLine(
                context.buildParameters.environmentVariables,
                workingDir.path,
                executablePath,
                arguments
        )
    }

    private fun getExecutableAndArguments(executable: String,
                                          args: Collection<String>,
                                          context: BuildRunnerContext): Pair<String, MutableList<String>> {
      val arguments = args.toMutableList()
      if (!SystemInfo.isWindows) {
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
      val (executablePath, arguments) = getExecutableAndArguments(executable, emptyList(), context)
      val commandLine = GeneralCommandLine().apply {
          this.exePath = executablePath
          this.addParameters(arguments)
      }

      val result = myCommandLineExecutor.execute(commandLine)
      if (result.exitCode == 0) {
          NUGET_VERSION_REGEX.find(result.stdout)?.let {
              val (version) = it.destructured
              return SemanticVersion.valueOf(version)!!
          }
      }

      return NUGET_UNKNOWN_VERSION
    }

    private val userName: String by lazy {
        if (SystemInfo.isWindows) {
          try {
              Class.forName("com.sun.security.auth.module.NTSystem")
              return@lazy com.sun.security.auth.module.NTSystem().name
          } catch (ignored: ClassNotFoundException) {
          }
        }
        System.getProperty("user.name")
    }

    companion object {
        private const val NUGET_PACKAGES_ENV = "NUGET_PACKAGES"
        private val NUGET_VERSION_2_0 = SemanticVersion.valueOf("2.0.0")!!
        private val NUGET_VERSION_3_3 = SemanticVersion.valueOf("3.3.0")!!
        private val NUGET_VERSION_3_5 = SemanticVersion.valueOf("3.5.0")!!
        private val NUGET_UNKNOWN_VERSION = SemanticVersion.valueOf("0.0.0")!!
        private val NUGET_VERSION_REGEX = Regex("NuGet Version:\\s([\\d\\.]+)", RegexOption.IGNORE_CASE)
    }
}
