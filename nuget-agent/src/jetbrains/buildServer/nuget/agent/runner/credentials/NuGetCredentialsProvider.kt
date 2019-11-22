package jetbrains.buildServer.nuget.agent.runner.credentials

import com.intellij.openapi.diagnostic.Logger
import jetbrains.buildServer.RunBuildException
import jetbrains.buildServer.agent.AgentLifeCycleAdapter
import jetbrains.buildServer.agent.AgentLifeCycleListener
import jetbrains.buildServer.agent.BuildFinishedStatus
import jetbrains.buildServer.agent.BuildRunnerContext
import jetbrains.buildServer.nuget.agent.parameters.PackageSourceManager
import jetbrains.buildServer.nuget.common.auth.NuGetAuthConstants.*
import jetbrains.buildServer.nuget.common.auth.PackageSourceUtil
import jetbrains.buildServer.serverSide.TeamCityProperties
import jetbrains.buildServer.util.EventDispatcher
import jetbrains.buildServer.util.FileUtil
import jetbrains.buildServer.util.StringUtil
import java.io.File
import java.io.IOException

/**
 * Provides NuGet feeds credentials for runners.
 */
class NuGetCredentialsProvider(events: EventDispatcher<AgentLifeCycleListener>,
                               private val packageSourceManager: PackageSourceManager,
                               credentialsPathProviders: List<CredentialsPathProvider>) : AgentLifeCycleAdapter() {

    private val myCredentialsPathProviders: Map<String, CredentialsPathProvider>
    private var mySourcesFile: File? = null

    init {
        myCredentialsPathProviders = hashMapOf()
        credentialsPathProviders.forEach { pathProvider ->
            pathProvider.runTypes.forEach { runType ->
                myCredentialsPathProviders[runType] = pathProvider
            }
        }
        events.addListener(this)
    }

    override fun beforeRunnerStart(runner: BuildRunnerContext) {
        if (TeamCityProperties.getBoolean("teamcity.nuget.credentialprovider.disabled")) return
        if (StringUtil.isTrue(runner.configParameters.get("teamcity.nuget.credentialprovider.disabled")?.trim())) return

        val pathProvider = myCredentialsPathProviders[runner.runType] ?: return

        val packageSources = packageSourceManager.getGlobalPackageSources(runner.build)
        if (packageSources.isEmpty()) return

        if (LOG.isDebugEnabled) {
            LOG.debug("Will provide credentials for NuGet package sources: " + packageSources.joinToString { it.source })
        }

        try {
            FileUtil.createTempFile(runner.build.agentTempDirectory, "nuget-sources", ".xml", true)?.let {
                mySourcesFile = it

                PackageSourceUtil.writeSources(it, packageSources)
                runner.addEnvironmentVariable(TEAMCITY_NUGET_FEEDS_ENV_VAR, it.path)

                val suppressCredentials = runner.configParameters["TEAMCITY_NUGET_CREDENTIAL"]?.equals("false", true) ?: false
                if (!suppressCredentials) {
                    pathProvider.getProviderPath(runner)?.let { credentialsProviderPath ->
                        LOG.debug("Set credentials provider location to $credentialsProviderPath")
                        runner.addEnvironmentVariable(NUGET_CREDENTIALPROVIDERS_PATH_ENV_VAR, credentialsProviderPath)
                    }

                    pathProvider.getPluginPath(runner)?.let { pluginPaths ->
                        LOG.debug("Set credentials plugin paths to $pluginPaths")
                        runner.addEnvironmentVariable(NUGET_PLUGIN_PATH_ENV_VAR, pluginPaths)
                    }
                }

                val environmentVariables = runner.buildParameters.environmentVariables;
                if (!environmentVariables.containsKey(NUGET_PLUGIN_HANDSHAKE_TIMEOUT_IN_SECONDS_ENV_VAR)) {
                    LOG.debug("Set NUGET handshake timeout to $NUGET_PLUGIN_HANDSHAKE_TIMEOUT_IN_SECONDS")
                    runner.addEnvironmentVariable(NUGET_PLUGIN_HANDSHAKE_TIMEOUT_IN_SECONDS_ENV_VAR, NUGET_PLUGIN_HANDSHAKE_TIMEOUT_IN_SECONDS.toString())
                }

                if (!environmentVariables.containsKey(NUGET_PLUGIN_REQUEST_TIMEOUT_IN_SECONDS_ENV_VAR)) {
                    LOG.debug("Set NUGET request timeout to $NUGET_PLUGIN_REQUEST_TIMEOUT_IN_SECONDS")
                    runner.addEnvironmentVariable(NUGET_PLUGIN_REQUEST_TIMEOUT_IN_SECONDS_ENV_VAR, NUGET_PLUGIN_REQUEST_TIMEOUT_IN_SECONDS.toString())
                }
            }
        } catch (e: IOException) {
            throw RunBuildException("Failed to create temp file for NuGet sources. " + e.message, e)
        }
    }

    override fun runnerFinished(runner: BuildRunnerContext, status: BuildFinishedStatus) {
        mySourcesFile?.let {
            FileUtil.delete(it)
            mySourcesFile = null
        }
    }

    companion object {
        private val LOG = Logger.getInstance(NuGetCredentialsProvider::class.java.name)
    }
}
