package jetbrains.buildServer.nuget.agent.runner

import com.intellij.openapi.diagnostic.Logger
import jetbrains.buildServer.RunBuildException
import jetbrains.buildServer.agent.AgentLifeCycleAdapter
import jetbrains.buildServer.agent.AgentLifeCycleListener
import jetbrains.buildServer.agent.BuildFinishedStatus
import jetbrains.buildServer.agent.BuildRunnerContext
import jetbrains.buildServer.nuget.agent.parameters.PackageSourceManager
import jetbrains.buildServer.nuget.common.PackagesConstants
import jetbrains.buildServer.nuget.common.auth.NuGetAuthConstants.*
import jetbrains.buildServer.nuget.common.auth.PackageSourceUtil
import jetbrains.buildServer.nuget.common.exec.NuGetTeamCityProvider
import jetbrains.buildServer.util.EventDispatcher
import jetbrains.buildServer.util.FileUtil
import java.io.File
import java.io.IOException

class NuGetCredentialsProvider(events: EventDispatcher<AgentLifeCycleListener>,
                               private val packageSourceManager: PackageSourceManager,
                               provider: NuGetTeamCityProvider) : AgentLifeCycleAdapter() {

    private val myRunnersWithPlugin: Map<String, String>
    private val myRunnersWithCredentialsProvider: Map<String, String>
    private var mySourcesFile: File? = null

    init {
        myRunnersWithPlugin = hashMapOf(
                "dotnet.cli" to provider.pluginCorePath,
                PackagesConstants.INSTALL_RUN_TYPE to provider.pluginFxPath,
                PackagesConstants.PUBLISH_RUN_TYPE to provider.pluginFxPath,
                "VS.Solution" to provider.pluginFxPath,
                "MSBuild" to provider.pluginFxPath
        )
        myRunnersWithCredentialsProvider = hashMapOf(
                PackagesConstants.INSTALL_RUN_TYPE to provider.credentialProviderHomeDirectory.path,
                PackagesConstants.PUBLISH_RUN_TYPE to provider.credentialProviderHomeDirectory.path
        )

        events.addListener(this)
    }

    override fun beforeRunnerStart(runner: BuildRunnerContext) {
        val runType = runner.runType
        if (!myRunnersWithPlugin.containsKey(runType) &&
            !myRunnersWithCredentialsProvider.containsKey(runType)) {
            return
        }

        val packageSources = packageSourceManager.getGlobalPackageSources(runner.build)
        if (LOG.isDebugEnabled) {
            LOG.debug("Provided credentials for NuGet packages sources: " + packageSources.joinToString { it.source })
        }

        try {
            FileUtil.createTempFile(runner.build.agentTempDirectory, "nuget-sources", ".xml", true)?.let {
                PackageSourceUtil.writeSources(it, packageSources)
                runner.addEnvironmentVariable(TEAMCITY_NUGET_FEEDS_ENV_VAR, it.path)

                myRunnersWithCredentialsProvider[runType]?.let { credentialsProviderPath ->
                    LOG.debug("Set credentials provider location to $credentialsProviderPath")
                    runner.addEnvironmentVariable(NUGET_CREDENTIALPROVIDERS_PATH_ENV_VAR, credentialsProviderPath)
                }

                myRunnersWithPlugin[runType]?.let { pluginPaths ->
                    LOG.debug("Set credentials plugin paths to $pluginPaths")
                    runner.addEnvironmentVariable(NUGET_PLUGIN_PATH_ENV_VAR, pluginPaths)
                }

                mySourcesFile = it
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
