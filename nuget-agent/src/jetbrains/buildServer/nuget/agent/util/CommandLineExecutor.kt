package jetbrains.buildServer.nuget.agent.util

import com.intellij.execution.configurations.GeneralCommandLine
import jetbrains.buildServer.ExecResult

interface CommandLineExecutor {
    fun execute(commandLine: GeneralCommandLine): ExecResult
}
