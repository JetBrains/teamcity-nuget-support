package jetbrains.buildServer.nuget.agent.util.impl

import jetbrains.buildServer.agent.BuildFinishedStatus
import jetbrains.buildServer.agent.BuildProgressLogger
import jetbrains.buildServer.agent.BuildRunnerContext
import jetbrains.buildServer.agent.runner.*

/**
 * Executes command line and logs output.
 */
class CommandLineBuildSession(private val myCommandLine: ProgramCommandLine,
                              private val myHostContext: BuildRunnerContext)
    : ProcessListenerAdapter(), MultiCommandBuildSession, CommandExecution {
    private var myExecuted: Boolean = false
    private var myExitCode: Int = 0

    override fun sessionStarted() {}

    override fun getNextCommand(): CommandExecution? {
        if (!myExecuted) {
            myExecuted = true
            return this
        }
        return null
    }

    override fun makeProgramCommandLine() = myCommandLine

    override fun beforeProcessStarted() {}

    override fun onStandardOutput(text: String) {
        if (text.startsWith("WARNING")) {
            buildLogger.warning(text)
        } else {
            buildLogger.message(text)
        }
    }

    override fun onErrorOutput(text: String) {
        buildLogger.error(text)
    }

    override fun processFinished(exitCode: Int) {
        myExitCode = exitCode
    }

    override fun sessionFinished(): BuildFinishedStatus? {
        return if (myExitCode == 0) BuildFinishedStatus.FINISHED_SUCCESS else BuildFinishedStatus.FINISHED_FAILED
    }

    override fun interruptRequested(): TerminationAction {
        return TerminationAction.KILL_PROCESS_TREE
    }

    override fun isCommandLineLoggingEnabled(): Boolean {
        return true
    }

    private val buildLogger: BuildProgressLogger by lazy {
        myHostContext.build.buildLogger
    }
}
