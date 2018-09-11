package jetbrains.buildServer.nuget.agent.runner.credentials

import jetbrains.buildServer.agent.BuildRunnerContext
import jetbrains.buildServer.nuget.common.exec.NuGetTeamCityProvider
import jetbrains.buildServer.nuget.common.version.SemanticVersion

class DotNetCredentialsPathProvider(private val provider: NuGetTeamCityProvider) : CredentialsPathProvider {

    override val runTypes: List<String>
        get() = listOf("dotnet.cli")

    override fun getPluginPath(runner: BuildRunnerContext): String? {
        if (runner.isVirtualContext) {
            provider.pluginCorePath
        }

        val version = runner.configParameters["DotNetCLI"]?.let { SemanticVersion.valueOf(it) } ?: return null
        if (version < VERSION_2_1_400) {
            return null
        }

        return provider.pluginCorePath
    }

    companion object {
        val VERSION_2_1_400 = SemanticVersion.valueOf("2.1.400")!!
    }
}
