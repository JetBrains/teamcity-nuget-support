package jetbrains.buildServer.nuget.agent.serviceMessages

import jetbrains.buildServer.BuildAuthUtil
import jetbrains.buildServer.agent.*
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
    private var myBuild: AgentRunningBuild? = null

    init {
        myDispatcher.addListener(object : AgentLifeCycleAdapter() {
            override fun buildStarted(runningBuild: AgentRunningBuild) {
                myBuild = runningBuild
            }

            override fun buildFinished(build: AgentRunningBuild, buildStatus: BuildFinishedStatus) {
                myBuild = null
            }
        })
    }

    override fun publishPackages(packages: Collection<NuGetPackageData>) {
        val build = myBuild
        if (build == null) {
            Loggers.AGENT.warn("Trying to publish packages for finished build")
            return
        }
        logInProgressBlock(build.buildLogger.getFlowLogger(FLOW_ID)) { logger ->
            try {
                val apiKey = createApiKey(build)
                val transport = myFeedTransportProvider.createTransport(build)

                val failedToPublish = mutableListOf<NuGetPackageData>()
                for (nuGetPackage in packages) {
                    logger.message("Publishing ${nuGetPackage.path} package")

                    val file = File(nuGetPackage.path)
                    val response = transport.sendPackage(apiKey, file)
                    if (!response.isSuccessful) {
                        failedToPublish.add(nuGetPackage)
                        Loggers.AGENT.debug("Failed publishing ${nuGetPackage.path} package StatusCode: ${response.statusCode}, response: ${response.message}")
                    }
                }

                if (failedToPublish.any()) {
                    throw PackagePublishException("Failed to publish NuGet package(s) ${failedToPublish.joinToString(", ") { "\"${it.path}\"" }}.")
                }
            }
            catch (e: Throwable) {
                logger.error(e.message)
                Loggers.AGENT.warnAndDebugDetails("Failed to publish NuGet packages", e)
            }
        }
    }

    private fun createApiKey(build: AgentRunningBuild): String {
        val buildToken = String.format("%s:%s", BuildAuthUtil.makeUserId(build.buildId), build.accessCode)
        return EncryptUtil.scramble(buildToken)
    }

    private fun logInProgressBlock(progressLogger: FlowLogger, action: (progressLogger: BuildProgressLogger) -> Unit) {
        progressLogger.startFlow()
        progressLogger.logMessage(DefaultMessagesInfo.createBlockStart(BLOCK_NAME, BLOCK_TYPE))
        try {
            action(progressLogger)
        }
        finally {
            progressLogger.logMessage(DefaultMessagesInfo.createBlockEnd(BLOCK_NAME, BLOCK_TYPE))
            progressLogger.disposeFlow()
        }
    }

    private companion object {
        const val BLOCK_TYPE = "publish-nuget-packages"
        const val BLOCK_NAME = "Publishing NuGet packages"
        const val FLOW_ID = "publish-nuget-packages-flow-id"
    }
}
