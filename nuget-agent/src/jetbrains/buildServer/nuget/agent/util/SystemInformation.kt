package jetbrains.buildServer.nuget.agent.util

interface SystemInformation {
    val isWindows: Boolean
    val userName: String
}
