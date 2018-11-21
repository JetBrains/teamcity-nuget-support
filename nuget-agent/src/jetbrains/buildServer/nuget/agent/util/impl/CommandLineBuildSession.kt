package jetbrains.buildServer.nuget.agent.util.impl

import jetbrains.buildServer.BuildProblemTypes
import jetbrains.buildServer.agent.BuildFinishedStatus
import jetbrains.buildServer.agent.BuildRunnerContext
import jetbrains.buildServer.agent.FlowGenerator
import jetbrains.buildServer.agent.FlowLogger
import jetbrains.buildServer.agent.problems.ExitCodeProblemBuilder
import jetbrains.buildServer.agent.runner.*
import jetbrains.buildServer.problems.BuildProblemUtil

/**
 * Executes command line and logs output.
 */
class CommandLineBuildSession(private val myCommandLine: ProgramCommandLine,
                              private val myRunnerContext: BuildRunnerContext)
    : ProcessListenerAdapter(), MultiCommandBuildSession, CommandExecution {
    private var myExecuted: Boolean = false
    private var myExitCode: Int = 0
    private lateinit var myFlowId: String
    private var myHasBuildProblems = false

    override fun sessionStarted() {
        myFlowId = FlowGenerator.generateNewFlow()
        buildLogger.startFlow()
    }

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
        when {
            WARNING_REGEX.matches(text) -> buildLogger.warning(text)
            ERROR_REGEX.matches(text) -> {
                buildLogger.logBuildProblem(BuildProblemUtil.createBuildProblem(
                        BuildProblemTypes.TC_ERROR_MESSAGE_TYPE,
                        text,
                        myCommandLine.workingDirectory
                ))
                myHasBuildProblems = true
            }
            else -> buildLogger.message(text)
        }
    }

    override fun onErrorOutput(text: String) {
        if (text.isNotBlank()) {
            buildLogger.warning(text)
        }
    }

    override fun processFinished(exitCode: Int) {
        myExitCode = exitCode
    }

    override fun sessionFinished(): BuildFinishedStatus? {
        return try {
            if (myExitCode == 0) {
                if (myHasBuildProblems) {
                    BuildFinishedStatus.FINISHED_WITH_PROBLEMS
                } else {
                    BuildFinishedStatus.FINISHED_SUCCESS
                }
            } else {
                val exitCodeProblemBuilder = ExitCodeProblemBuilder()
                        .setExitCode(myExitCode)
                        .setBuildRunnerContext(myRunnerContext)
                        .setProcessFlowId(myFlowId)
                buildLogger.logBuildProblem(exitCodeProblemBuilder.build())
                BuildFinishedStatus.FINISHED_WITH_PROBLEMS
            }
        } finally {
            buildLogger.disposeFlow()
        }
    }

    override fun interruptRequested(): TerminationAction {
        return TerminationAction.KILL_PROCESS_TREE
    }

    override fun isCommandLineLoggingEnabled(): Boolean {
        return true
    }

    private val buildLogger: FlowLogger by lazy {
        myRunnerContext.build.buildLogger.getFlowLogger(myFlowId)
    }

    companion object {
        private val WARNING_REGEX = Regex("^(WARNING|.+\\:\\swarning\\s(?:[a-z]+[a-z0-9]*)?\\:).+$", RegexOption.IGNORE_CASE)
        private val ERROR_REGEX = Regex("^(ERROR.+|.+\\:\\serror\\s(?:[a-z]+[a-z0-9]*)?\\:).+", RegexOption.IGNORE_CASE)
    }
}
