package jetbrains.buildServer.nuget.agent.runner.credentials

import jetbrains.buildServer.agent.AgentLifeCycleAdapter
import jetbrains.buildServer.agent.AgentLifeCycleListener
import jetbrains.buildServer.agent.BuildAgent
import jetbrains.buildServer.agent.ToolProvider
import jetbrains.buildServer.nuget.common.PackagesConstants
import jetbrains.buildServer.nuget.common.exec.NuGetTeamCityProvider
import jetbrains.buildServer.util.EventDispatcher
import org.apache.log4j.Logger
import java.io.File

class CredentialProviderPropertiesExtension(
        events: EventDispatcher<AgentLifeCycleListener>,
        private val _nuGetTeamCityProvider: NuGetTeamCityProvider)
    : AgentLifeCycleAdapter() {

    init {
        events.addListener(this)
    }

    override fun beforeAgentConfigurationLoaded(agent: BuildAgent) {
        LOG.debug("Locating credential providers")
        addParam(agent,"DotNetCredentialProvider4.0.0_Path", _nuGetTeamCityProvider.pluginFxPath)
        for (version in (1 .. 10).filter { it != 4 }) {
            addParam(agent,"DotNetCredentialProvider$version.0.0_Path", _nuGetTeamCityProvider.getPluginCorePath(version))
        }
    }

    private fun addParam(agent: BuildAgent, paramName: String, credentialProviderPath: String) {
        val credentialProvider = File(credentialProviderPath)
        if (credentialProvider.exists() && credentialProvider.isFile) {
            LOG.info("Found .NET credential provider $paramName at $credentialProviderPath")
            agent.configuration.addConfigurationParameter(paramName, credentialProviderPath)
        }
    }

    companion object {
        private val LOG = Logger.getLogger(CredentialProviderPropertiesExtension::class.java)
    }
}
