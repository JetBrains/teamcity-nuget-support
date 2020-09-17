package jetbrains.buildServer.nuget.agent.serviceMessages

import jetbrains.buildServer.BuildAuthUtil
import jetbrains.buildServer.agent.AgentLifeCycleAdapter
import jetbrains.buildServer.agent.AgentLifeCycleListener
import jetbrains.buildServer.agent.AgentRunningBuild
import jetbrains.buildServer.agent.BuildProgressLogger
import jetbrains.buildServer.log.Loggers
import jetbrains.buildServer.messages.DefaultMessagesInfo
import jetbrains.buildServer.nuget.common.PackagePublishException
import jetbrains.buildServer.nuget.common.index.NuGetPackageData
import jetbrains.buildServer.serverSide.crypt.EncryptUtil
import jetbrains.buildServer.util.EventDispatcher
import jetbrains.buildServer.util.StringUtil
import org.jetbrains.annotations.NotNull
import java.io.File

class NuGetPackageServiceFeedPublisherImpl (
        @NotNull private val myDispatcher: EventDispatcher<AgentLifeCycleListener>,
        @NotNull private val myFeedTransportProvider: NuGetPackageServiceFeedTransportProvider
) : NuGetPackageServiceFeedPublisher {
    private lateinit var myBuild: AgentRunningBuild

    init {
        myDispatcher.addListener(object : AgentLifeCycleAdapter() {
            override fun buildStarted(runningBuild: AgentRunningBuild) {
                myBuild = runningBuild
            }
        })
    }

    override fun publishPackages(packages: Collection<NuGetPackageData>) {
        logInProgressBlock { logger ->
            try {
                val apiKey = createApiKey()
                val transport = myFeedTransportProvider.createTransport(myBuild)

                for (nuGetPackage in packages) {
                    logger.message("Publishing ${nuGetPackage.path} package")

                    val file = File(nuGetPackage.path)
                    val response = transport.sendPackage(apiKey, file)
                    if (!response.isSuccessful) {
                        throw PackagePublishException("Failed to publush NuGet package. Server returned StatusCode: ${response.statusCode}, Response: ${response.message}")
                    }
                }
            } catch (e: Throwable) {
                val message = "Failed to publish NuGet packages"
                logger.exception(e)
                Loggers.AGENT.warn(message, e)
                logger.buildFailureDescription(message)
            }
        }
    }

    private fun createApiKey(): String {
        val buildToken = String.format("%s:%s", BuildAuthUtil.makeUserId(myBuild.buildId), myBuild.accessCode)
        return EncryptUtil.scramble(buildToken)
    }

    private fun logInProgressBlock(action: (progressLogger: BuildProgressLogger) -> Unit) {
        this.myBuild.buildLogger.logMessage(DefaultMessagesInfo.createBlockStart(BLOCK_NAME, BLOCK_TYPE))
        try {
            action(this.myBuild.buildLogger)
        }
        finally {
            this.myBuild.buildLogger.logMessage(DefaultMessagesInfo.createBlockEnd(BLOCK_NAME, BLOCK_TYPE))
        }
    }

    private companion object {
        const val BLOCK_TYPE = "publish-nuget-packages"
        const val BLOCK_NAME = "Publishing NuGet packages"
    }
}
