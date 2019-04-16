package jetbrains.buildServer.nuget.agent.runner.credentials

import jetbrains.buildServer.agent.BuildRunnerContext
import jetbrains.buildServer.nuget.common.exec.NuGetTeamCityProvider
import jetbrains.buildServer.nuget.common.version.SemanticVersion

class DotNetCredentialsPathProvider(private val provider: NuGetTeamCityProvider) : CredentialsPathProvider {

    override val runTypes: List<String>
        get() = listOf("dotnet.cli")

    override fun getPluginPath(runner: BuildRunnerContext): String? {
        // We need somehow detect dotnet version in container
        // since in images with .NET CLI < 2.1.400 it fails with various problems
        if (runner.isVirtualContext) {
            return null
        }

        val version = runner.configParameters["DotNetCLI"]?.let { SemanticVersion.valueOf(it) } ?: return null
        if (version < VERSION_2_1_400) {
            return null
        }

        val minSdkVersion = runner.configParameters.keys
                .filter { it.startsWith("DotNetCoreSDK") }
                .map { SemanticVersion.valueOf(it.replace("DotNetCoreSDK", "").replace("_Path", "")) }
                .filter { it != null }
                .map { it?.version?.major ?: 1}
                .min() ?: 1

        return provider.getPluginCorePath(minSdkVersion)
    }

    companion object {
        val VERSION_2_1_400 = SemanticVersion.valueOf("2.1.400")!!
    }
}
