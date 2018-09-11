package jetbrains.buildServer.nuget.agent.runner.credentials

import jetbrains.buildServer.agent.BuildRunnerContext

/**
 * Provides a path to credentials plugin if runner supports them.
 */
interface CredentialsPathProvider {

    /**
     * Gets a supported run types.
     */
    val runTypes: List<String>

    /**
     * Returns a path to credentials plugin.
     */
    fun getPluginPath(runner: BuildRunnerContext): String? = null

    /**
     * Returns a path to credentials provider.
     */
    fun getProviderPath(runner: BuildRunnerContext): String? = null
}
