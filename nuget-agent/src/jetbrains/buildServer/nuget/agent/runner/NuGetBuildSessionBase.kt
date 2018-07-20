package jetbrains.buildServer.nuget.agent.runner

import jetbrains.buildServer.agent.BuildFinishedStatus
import jetbrains.buildServer.agent.runner.CommandLineBuildService
import jetbrains.buildServer.agent.runner.MultiCommandBuildSession

abstract class NuGetBuildSessionBase : MultiCommandBuildSession {

    private lateinit var buildSteps: Iterator<CommandLineBuildService>
    private lateinit var lastCommand: CommandExecutionAdapter

    override fun sessionStarted() {
        buildSteps = getSteps()
    }

    override fun getNextCommand() = if (buildSteps.hasNext()) {
        lastCommand = CommandExecutionAdapter(buildSteps.next())
        lastCommand
    } else {
        null
    }

    override fun sessionFinished(): BuildFinishedStatus? {
        return lastCommand.result
    }

    abstract fun getSteps(): Iterator<CommandLineBuildService>
}
