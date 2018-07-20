package jetbrains.buildServer.nuget.agent.runner.install

import jetbrains.buildServer.agent.BuildRunnerContext
import jetbrains.buildServer.agent.runner.CommandLineBuildService
import jetbrains.buildServer.nuget.agent.runner.NuGetBuildSessionBase
import kotlin.coroutines.experimental.buildIterator

/**
 * Build session for NuGet Installer run type.
 */
class NuGetInstallerBuildSession(private val context: BuildRunnerContext) : NuGetBuildSessionBase() {
    override fun getSteps() = buildIterator<CommandLineBuildService> {

    }
}
