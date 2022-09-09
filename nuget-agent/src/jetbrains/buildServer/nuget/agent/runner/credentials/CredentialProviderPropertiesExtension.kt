package jetbrains.buildServer.nuget.agent.runner.credentials

import jetbrains.buildServer.ExtensionHolder
import jetbrains.buildServer.agent.config.AgentConfigurationAdapter
import jetbrains.buildServer.agent.config.AgentConfigurationSnapshot
import jetbrains.buildServer.nuget.common.exec.NuGetTeamCityProvider
import org.apache.log4j.Logger
import java.io.File

class CredentialProviderPropertiesExtension(
        extensionHolder: ExtensionHolder,
        private val _nuGetTeamCityProvider: NuGetTeamCityProvider)
    : AgentConfigurationAdapter() {

    init {
        extensionHolder.registerExtension(AgentConfigurationSnapshot::class.java, javaClass.name, this)
    }

    override fun addParameters(parameters: MutableMap<String, String>) {
        LOG.debug("Locating credential providers")
        addParam(parameters,"DotNetCredentialProvider4.0.0_Path", _nuGetTeamCityProvider.pluginFxPath)
        for (version in (1 .. 10).filter { it != 4 }) {
            addParam(parameters,"DotNetCredentialProvider$version.0.0_Path", _nuGetTeamCityProvider.getPluginCorePath(version))
        }
    }

    private fun addParam(parameters: MutableMap<String, String>, paramName: String, credentialProviderPath: String) {
        val credentialProvider = File(credentialProviderPath)
        if (credentialProvider.exists() && credentialProvider.isFile) {
            LOG.info("Found .NET credential provider $paramName at $credentialProviderPath")
            parameters.put(paramName, credentialProviderPath)
        }
    }

    companion object {
        private val LOG = Logger.getLogger(CredentialProviderPropertiesExtension::class.java)
    }
}
