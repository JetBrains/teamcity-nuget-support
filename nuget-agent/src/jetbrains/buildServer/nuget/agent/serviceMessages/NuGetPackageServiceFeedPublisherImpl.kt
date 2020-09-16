package jetbrains.buildServer.nuget.agent.serviceMessages

import jetbrains.buildServer.BuildAuthUtil
import jetbrains.buildServer.agent.AgentLifeCycleAdapter
import jetbrains.buildServer.agent.AgentLifeCycleListener
import jetbrains.buildServer.agent.AgentRunningBuild
import jetbrains.buildServer.agent.BuildProgressLogger
import jetbrains.buildServer.agent.impl.artifacts.ArtifactProcessorUtils
import jetbrains.buildServer.agent.impl.artifacts.ArtifactProcessorUtils.readResponse
import jetbrains.buildServer.http.HttpUserAgent
import jetbrains.buildServer.log.Loggers
import jetbrains.buildServer.messages.DefaultMessagesInfo
import jetbrains.buildServer.nuget.common.PackagePublishException
import jetbrains.buildServer.nuget.common.index.NuGetPackageData
import jetbrains.buildServer.serverSide.crypt.EncryptUtil
import jetbrains.buildServer.util.EventDispatcher
import jetbrains.buildServer.util.StringUtil
import org.apache.commons.httpclient.Header
import org.apache.commons.httpclient.HttpClient
import org.apache.commons.httpclient.methods.PutMethod
import org.apache.commons.httpclient.methods.RequestEntity
import org.apache.commons.httpclient.methods.multipart.FilePart
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity
import org.apache.commons.httpclient.params.HttpMethodParams
import org.jetbrains.annotations.NotNull
import java.io.File
import java.util.*

class NuGetPackageServiceFeedPublisherImpl (
        @NotNull private val myDispatcher: EventDispatcher<AgentLifeCycleListener>,
        @NotNull private val myUrlProvider: NuGetPackageServiceFeedUrlProvider
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
            val params = HttpMethodParams()
            val apiKey = createApiKey()
            val client = createHttpClient()
            try {
                for (nuGetPackage in packages) {
                    logger.message("Publishing ${nuGetPackage.path} package")

                    val file = File(nuGetPackage.path)
                    val post = createPutMethod(apiKey, params, file)
                    try {
                        executeRequest(client, post)
                    } finally {
                        post.releaseConnection()
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
    private fun createRequestEntity(file: File, params: HttpMethodParams): RequestEntity {
        val parts = Collections.singleton(FilePart(PACKAGE_PART_NAME, file.name, file))
        return MultipartRequestEntity(parts.toTypedArray(), params)
    }

    private fun executeRequest(client: HttpClient, post: PutMethod) {
        val statusCode = client.executeMethod(post)
        val response = readResponse(post)

        handleResponse(statusCode, response)
    }

    private fun createPutMethod(apiKey: String?, params: HttpMethodParams, file: File): PutMethod {
        val put = PutMethod(myUrlProvider.getUrl())
        put.doAuthentication = true
        put.addRequestHeader(Header(NUGET_APIKEY_HEADER, apiKey))
        put.requestEntity = createRequestEntity(file, params)

        HttpUserAgent.addHeader(put)

        return put
    }

    private fun handleResponse(statusCode: Int, response: String) {
        if (statusCode == 200 && StringUtil.isEmptyOrSpaces(response)) return
        throw PackagePublishException("Failed to publush NuGet package. Server returned StatusCode: $statusCode, Response: $response")
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

    private fun createHttpClient(): HttpClient {
        return ArtifactProcessorUtils.prepareHttpClient(myUrlProvider.getUrl(), myBuild)
    }

    private companion object {
        const val PACKAGE_PART_NAME = "package"
        const val NUGET_APIKEY_HEADER = "x-nuget-apikey"
        const val BLOCK_TYPE = "publish-nuget-packages"
        const val BLOCK_NAME = "Publishing NuGet packages"
    }
}
