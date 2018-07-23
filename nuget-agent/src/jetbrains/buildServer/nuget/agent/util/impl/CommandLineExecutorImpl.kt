package jetbrains.buildServer.nuget.agent.util.impl

import com.intellij.execution.configurations.GeneralCommandLine
import jetbrains.buildServer.ExecResult
import jetbrains.buildServer.SimpleCommandLineProcessRunner
import jetbrains.buildServer.nuget.agent.util.CommandLineExecutor

class CommandLineExecutorImpl : CommandLineExecutor {
    override fun execute(commandLine: GeneralCommandLine): ExecResult {
        return SimpleCommandLineProcessRunner.runCommand(commandLine, byteArrayOf())
    }
}
