package jetbrains.buildServer.nuget.agent.serviceMessages

import jetbrains.buildServer.BuildProblemData
import jetbrains.buildServer.agent.*
import jetbrains.buildServer.log.Loggers
import jetbrains.buildServer.messages.serviceMessages.ServiceMessage
import jetbrains.buildServer.messages.serviceMessages.ServiceMessageHandler
import jetbrains.buildServer.messages.serviceMessages.ServiceMessagesRegister
import jetbrains.buildServer.nuget.common.PackageLoadException
import jetbrains.buildServer.nuget.common.index.NuGetPackageData
import jetbrains.buildServer.nuget.common.index.PackageAnalyzer
import jetbrains.buildServer.nuget.common.version.VersionUtility
import jetbrains.buildServer.nuget.feedReader.NuGetPackageAttributes.ID
import jetbrains.buildServer.nuget.feedReader.NuGetPackageAttributes.NORMALIZED_VERSION
import jetbrains.buildServer.problems.BuildProblemTypesEx
import jetbrains.buildServer.util.EventDispatcher
import jetbrains.buildServer.util.FileUtil
import jetbrains.buildServer.util.StringUtil
import org.jetbrains.annotations.NotNull
import java.io.File
import java.io.FileInputStream
import java.util.concurrent.ConcurrentHashMap

class PublishPackageServiceMessageHandler(
        @NotNull private val myServiceMessagesRegister: ServiceMessagesRegister,
        @NotNull private val myDispatcher: EventDispatcher<AgentLifeCycleListener>,
        @NotNull private val myPackageAnalyzer: PackageAnalyzer,
        @NotNull private val myPackagePublisher: NuGetPackageServiceFeedPublisher
        ) : ServiceMessageHandler {

    private val myPackagesMap: ConcurrentHashMap<PackageKey, NuGetPackageData> = ConcurrentHashMap<PackageKey, NuGetPackageData>()
    private var myBuild: AgentRunningBuild? = null

    init {
        myDispatcher.addListener(object: AgentLifeCycleAdapter() {
            override fun buildStarted(runningBuild: AgentRunningBuild) {
                myBuild = runningBuild
            }

            override fun beforeRunnerStart(runner: BuildRunnerContext) {
                clearPackages()
            }

            override fun runnerFinished(runner: BuildRunnerContext, status: BuildFinishedStatus) {
                publishPackages()
                clearPackages()
            }

            override fun buildFinished(build: AgentRunningBuild, buildStatus: BuildFinishedStatus) {
                myBuild = null
                clearPackages()
            }
        })
        myServiceMessagesRegister.registerHandler(MESSAGE_NAME, this)
    }

    override fun handle(@NotNull serviceMessage: ServiceMessage) {
        val build = myBuild
        if (build == null) {
            Loggers.AGENT.warn("Trying to handle ${serviceMessage.asString()} service message for finished build")
            return
        }

        if (Loggers.AGENT.isDebugEnabled) {
            Loggers.AGENT.debug("Handling service message: ${serviceMessage.asString()}")
        }

        val logger = build.buildLogger
        val path = serviceMessage.argument ?: serviceMessage.attributes.get(MESSAGE_PATH_ATTRIBUTE)
        if (path.isNullOrEmpty()) {
            var message = "\"$MESSAGE_PATH_ATTRIBUTE\" attribute must be a non-empty value for \"$MESSAGE_NAME\" service message"
            logger.error(message)
            Loggers.AGENT.warn(message)
            return
        }

        try {
            var file = File(path)
            if (!file.isAbsolute) {
                file = File(build.checkoutDirectory, path)
            }

            val keyToDataPair = readPackageInfo(file)
            myPackagesMap.put(keyToDataPair.first, keyToDataPair.second)

            Loggers.AGENT.debug("Service message $MESSAGE_NAME sucessfully handled")
        } catch (e: Throwable) {
            var message = "Could not read NuGet package. File path: $path. Message: ${e.message}"
            Loggers.AGENT.warnAndDebugDetails(message, e)

            var buildProblemText = "Could not read NuGet package. File path: $path."
            logger.logBuildProblem(BuildProblemData.createBuildProblem(buildProblemText.hashCode().toString(), BuildProblemTypesEx.TC_BUILD_FAILURE_TYPE, buildProblemText))
        }
    }

    public fun dispose() {
        myServiceMessagesRegister.removeHandler(MESSAGE_NAME)
    }

    private fun readPackageInfo(file: File) : Pair<PackageKey, NuGetPackageData> {
        var stream : FileInputStream? = null
        try {
            stream = file.inputStream()
            var metadata = myPackageAnalyzer.analyzePackage(stream)

            var id = metadata.getOrDefault(ID, "")
            var version  = metadata.getOrDefault(NORMALIZED_VERSION, "")

            // Package must have id and version specified
            if (StringUtil.isEmptyOrSpaces(id) || StringUtil.isEmptyOrSpaces(version)) {
                throw PackageLoadException("Lack of Id or Version in NuGet package specification")
            }

            val key = PackageKey(id.toLowerCase(), VersionUtility.normalizeVersion(version)?.toLowerCase())
            val data = NuGetPackageData(file.absolutePath, metadata)
            return key to data
        }
        finally {
            FileUtil.close(stream)
        }
    }

    private fun clearPackages() {
        myPackagesMap.clear()
    }

    private fun publishPackages() {
        if (myPackagesMap.isEmpty()) return;

        try {
            myPackagePublisher.publishPackages(myPackagesMap.values)
        } finally {
            clearPackages()
        }
    }

    private data class PackageKey (val name: String, val version: String?)

    private companion object {
        const val MESSAGE_NAME = "publishNuGetPackage"
        const val MESSAGE_PATH_ATTRIBUTE = "path"
    }
}
