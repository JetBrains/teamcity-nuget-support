package jetbrains.buildServer.nuget.tests.util

import jetbrains.buildServer.BuildProblemData
import jetbrains.buildServer.agent.*
import jetbrains.buildServer.agent.runner.ProgramCommandLine
import jetbrains.buildServer.nuget.agent.util.impl.CommandLineBuildSession
import org.jmock.Expectations
import org.jmock.Mockery
import org.testng.Assert
import org.testng.annotations.Test

class CommandLineBuildSessionTest {

    @Test
    fun testLogMessageFromStdout() {
        val m = Mockery()
        val commandLine = m.mock(ProgramCommandLine::class.java)
        val context = m.mock(BuildRunnerContext::class.java)
        val build = m.mock(AgentRunningBuild::class.java)
        val buildLogger = m.mock(BuildProgressLogger::class.java)
        val flowLogger = m.mock(FlowLogger::class.java)
        val buildSession = CommandLineBuildSession(commandLine, context)

        m.checking(object : Expectations() {
            init {
                allowing(context).build
                will(returnValue(build))
                oneOf(build).buildLogger
                will(returnValue(buildLogger))
                oneOf(buildLogger).getFlowLogger(with(any(String::class.java)))
                will(returnValue(flowLogger))
                oneOf(flowLogger).startFlow()
                oneOf(flowLogger).disposeFlow()

                oneOf(flowLogger).message("text")
            }
        })

        buildSession.sessionStarted()
        buildSession.onStandardOutput("text")
        val status = buildSession.sessionFinished()

        Assert.assertEquals(status, BuildFinishedStatus.FINISHED_SUCCESS)
        m.assertIsSatisfied()
    }

    @Test
    fun testLogWarningFromStderr() {
        val m = Mockery()
        val commandLine = m.mock(ProgramCommandLine::class.java)
        val context = m.mock(BuildRunnerContext::class.java)
        val build = m.mock(AgentRunningBuild::class.java)
        val buildLogger = m.mock(BuildProgressLogger::class.java)
        val flowLogger = m.mock(FlowLogger::class.java)
        val buildSession = CommandLineBuildSession(commandLine, context)

        m.checking(object : Expectations() {
            init {
                allowing(context).build
                will(returnValue(build))
                oneOf(build).buildLogger
                will(returnValue(buildLogger))
                oneOf(buildLogger).getFlowLogger(with(any(String::class.java)))
                will(returnValue(flowLogger))
                oneOf(flowLogger).startFlow()
                oneOf(flowLogger).disposeFlow()

                oneOf(flowLogger).warning("text")
            }
        })

        buildSession.sessionStarted()
        buildSession.onErrorOutput("text")
        val status = buildSession.sessionFinished()

        Assert.assertEquals(status, BuildFinishedStatus.FINISHED_SUCCESS)
        m.assertIsSatisfied()
    }

    @Test
    fun testLogWarningFromStdout() {
        val m = Mockery()
        val commandLine = m.mock(ProgramCommandLine::class.java)
        val context = m.mock(BuildRunnerContext::class.java)
        val build = m.mock(AgentRunningBuild::class.java)
        val buildLogger = m.mock(BuildProgressLogger::class.java)
        val flowLogger = m.mock(FlowLogger::class.java)
        val buildSession = CommandLineBuildSession(commandLine, context)
        val warnings = listOf(
                "project.csproj : warning NU1503: Message",
                "nugetrestore.targets(144,5): warning : Message",
                "WARNING something went wrong"
        )

        m.checking(object : Expectations() {
            init {
                allowing(context).build
                will(returnValue(build))
                oneOf(build).buildLogger
                will(returnValue(buildLogger))
                oneOf(buildLogger).getFlowLogger(with(any(String::class.java)))
                will(returnValue(flowLogger))
                oneOf(flowLogger).startFlow()
                oneOf(flowLogger).disposeFlow()

                warnings.forEach {
                    oneOf(flowLogger).warning(it)
                }
            }
        })

        buildSession.sessionStarted()
        warnings.forEach {
            buildSession.onStandardOutput(it)
        }

        val status = buildSession.sessionFinished()

        Assert.assertEquals(status, BuildFinishedStatus.FINISHED_SUCCESS)
        m.assertIsSatisfied()
    }

    @Test
    fun testLogBuildProblemsFromStdout() {
        val m = Mockery()
        val commandLine = m.mock(ProgramCommandLine::class.java)
        val context = m.mock(BuildRunnerContext::class.java)
        val build = m.mock(AgentRunningBuild::class.java)
        val buildLogger = m.mock(BuildProgressLogger::class.java)
        val flowLogger = m.mock(FlowLogger::class.java)
        val buildSession = CommandLineBuildSession(commandLine, context)
        val errors = listOf(
                "project.csproj : error MSB4041: Message",
                "nugetrestore.targets(144,5): error : Message",
                "ERROR something went wrong"
        )

        m.checking(object : Expectations() {
            init {
                allowing(context).build
                will(returnValue(build))
                oneOf(build).buildLogger
                will(returnValue(buildLogger))
                oneOf(buildLogger).getFlowLogger(with(any(String::class.java)))
                will(returnValue(flowLogger))
                oneOf(flowLogger).startFlow()
                oneOf(flowLogger).disposeFlow()

                exactly(errors.size).of(commandLine).workingDirectory
                will(returnValue("dir"))
                exactly(errors.size).of(flowLogger).logBuildProblem(with(any(BuildProblemData::class.java)))

                errors.forEach {
                    oneOf(flowLogger).warning(it)
                }
            }
        })

        buildSession.sessionStarted()
        errors.forEach {
            buildSession.onStandardOutput(it)
        }

        val status = buildSession.sessionFinished()

        Assert.assertEquals(status, BuildFinishedStatus.FINISHED_WITH_PROBLEMS)
        m.assertIsSatisfied()
    }

    @Test
    fun testLogExitCodeProblem() {
        val m = Mockery()
        val commandLine = m.mock(ProgramCommandLine::class.java)
        val context = m.mock(BuildRunnerContext::class.java)
        val build = m.mock(AgentRunningBuild::class.java)
        val buildLogger = m.mock(BuildProgressLogger::class.java)
        val flowLogger = m.mock(FlowLogger::class.java)
        val buildSession = CommandLineBuildSession(commandLine, context)

        m.checking(object : Expectations() {
            init {
                allowing(context).build
                will(returnValue(build))
                oneOf(build).buildLogger
                will(returnValue(buildLogger))
                oneOf(buildLogger).getFlowLogger(with(any(String::class.java)))
                will(returnValue(flowLogger))
                oneOf(flowLogger).startFlow()
                oneOf(flowLogger).disposeFlow()
                oneOf(context).id
                will(returnValue("id"))
                oneOf(context).name
                will(returnValue("name"))
                oneOf(context).runType
                will(returnValue("runType"))

                oneOf(flowLogger).logBuildProblem(with(any(BuildProblemData::class.java)))
            }
        })

        buildSession.sessionStarted()
        buildSession.processFinished(1)
        val status = buildSession.sessionFinished()

        Assert.assertEquals(status, BuildFinishedStatus.FINISHED_WITH_PROBLEMS)
        m.assertIsSatisfied()
    }
}
