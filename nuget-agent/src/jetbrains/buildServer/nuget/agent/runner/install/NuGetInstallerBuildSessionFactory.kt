package jetbrains.buildServer.nuget.agent.runner.install

import jetbrains.buildServer.agent.AgentBuildRunnerInfo
import jetbrains.buildServer.agent.BuildAgentConfiguration
import jetbrains.buildServer.agent.BuildRunnerContext
import jetbrains.buildServer.agent.runner.MultiCommandBuildSession
import jetbrains.buildServer.agent.runner.MultiCommandBuildSessionFactory
import jetbrains.buildServer.nuget.common.PackagesConstants

/**
 * Creates a session for NuGet Installer run type.
 */
class NuGetInstallerBuildSessionFactory : MultiCommandBuildSessionFactory {
    override fun getBuildRunnerInfo(): AgentBuildRunnerInfo {
        return object : AgentBuildRunnerInfo {
            override fun getType(): String {
                return PackagesConstants.INSTALL_RUN_TYPE
            }

            override fun canRun(config: BuildAgentConfiguration): Boolean {
                return true
            }
        }
    }

    override fun createSession(runnerContext: BuildRunnerContext): MultiCommandBuildSession {
        return NuGetInstallerBuildSession(runnerContext)
    }
}
