package jetbrains.buildServer.nuget.agent.runner.credentials

import jetbrains.buildServer.agent.BuildRunnerContext
import jetbrains.buildServer.nuget.common.exec.NuGetTeamCityProvider

class MsBuildCredentialsPathProvider(private val provider: NuGetTeamCityProvider) : CredentialsPathProvider {

    override val runTypes: List<String>
        get() = listOf("VS.Solution", "MSBuild")

    override fun getPluginPath(runner: BuildRunnerContext): String? {
        return provider.pluginFxPath
    }
}
