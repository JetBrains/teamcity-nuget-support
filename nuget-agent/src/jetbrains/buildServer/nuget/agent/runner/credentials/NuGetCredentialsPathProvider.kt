package jetbrains.buildServer.nuget.agent.runner.credentials

import jetbrains.buildServer.agent.BuildRunnerContext
import jetbrains.buildServer.nuget.common.PackagesConstants
import jetbrains.buildServer.nuget.common.exec.NuGetTeamCityProvider

class NuGetCredentialsPathProvider(private val provider: NuGetTeamCityProvider) : CredentialsPathProvider {

    override val runTypes: List<String>
        get() = listOf(PackagesConstants.INSTALL_RUN_TYPE, PackagesConstants.PUBLISH_RUN_TYPE)

    override fun getPluginPath(runner: BuildRunnerContext): String? {
        return provider.pluginFxPath
    }

    override fun getProviderPath(runner: BuildRunnerContext): String? {
        return provider.credentialProviderHomeDirectory.path
    }
}
