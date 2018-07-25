package jetbrains.buildServer.nuget.agent.util.impl

import com.intellij.openapi.util.SystemInfo
import jetbrains.buildServer.nuget.agent.util.SystemInformation

class SystemInformationImpl : SystemInformation {
    override val isWindows: Boolean
        get() = SystemInfo.isWindows

    override val userName: String by lazy {
        if (SystemInfo.isWindows) {
            try {
                Class.forName("com.sun.security.auth.module.NTSystem")
                return@lazy com.sun.security.auth.module.NTSystem().name
            } catch (ignored: ClassNotFoundException) {
            }
        }
        System.getProperty("user.name")
    }
}
