package jetbrains.buildServer.nuget.agent.serviceMessages

import jetbrains.buildServer.agent.AgentRunningBuild
import jetbrains.buildServer.agent.impl.artifacts.ArtifactProcessorUtils
import jetbrains.buildServer.http.HttpUserAgent
import jetbrains.buildServer.util.HTTPRequestBuilder
import jetbrains.buildServer.util.HTTPRequestBuilder.DelegatingRequestHandler
import jetbrains.buildServer.util.StringUtil
import jetbrains.buildServer.util.executors.ExecutorsFactory
import jetbrains.buildServer.util.http.EntityProducer
import jetbrains.buildServer.util.http.HttpMethod.PUT
import org.apache.commons.httpclient.methods.RequestEntity
import org.apache.commons.httpclient.methods.multipart.FilePart
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity
import org.apache.commons.httpclient.params.HttpMethodParams
import org.apache.http.HttpEntity
import org.apache.http.entity.mime.HttpMultipartMode
import org.apache.http.entity.mime.MultipartEntityBuilder
import org.jetbrains.annotations.NotNull
import java.io.File
import java.util.*

class NuGetPackageServiceFeedTransportProviderImpl(
    @NotNull private val myUrlProvider: NuGetPackageServiceFeedUrlProvider
) : NuGetPackageServiceFeedTransportProvider {

    override fun createTransport(build: AgentRunningBuild): NuGetPackageServiceFeedTransport {
        val serverUrl = myUrlProvider.getUrl(build)
        val httpClient = ArtifactProcessorUtils.prepareHttpRequest(serverUrl, build)
        return NuGetPackageServiceFeedTransportImpl(httpClient, serverUrl)
    }

    private class NuGetPackageServiceFeedTransportImpl(
        private val myHttpRequest: HTTPRequestBuilder,
        private val myServerUrl: String
    ) : NuGetPackageServiceFeedTransport {
        val myExecutorService by lazy {
            ExecutorsFactory.newDaemonExecutor("NuGetPackgeServiceFeedTransportProvider")
        }

        override fun sendPackage(apiKey: String, file: File): NuGetPackageServiceFeedResponse {
            populateRequest(apiKey, file)
            val request = DelegatingRequestHandler().doSyncRequest(myHttpRequest.build())

            return request.use { response ->
                val responseBody = response.bodyAsString
                object : NuGetPackageServiceFeedResponse {
                    override val statusCode: Int
                        get() = response.statusCode
                    override val message: String
                        get() = StringUtil.emptyIfNull(responseBody)
                    override val isSuccessful: Boolean
                        get() = statusCode == 200 && StringUtil.isEmptyOrSpaces(message)
                }
            }
        }

        private fun populateRequest(apiKey: String, file: File) {
            myHttpRequest
                .withMethod(PUT)
                .withHeader(NUGET_APIKEY_HEADER, apiKey)
                .withHeader("User-Agent", HttpUserAgent.getUserAgent())
                .withData(object : EntityProducer {
                    override fun entity4(): HttpEntity = createEntity(file)

                    override fun entity3(): RequestEntity = createLegacyEntity(file)

                })
                .withExecutorService(myExecutorService)

        }

        private fun createEntity(file: File): HttpEntity = MultipartEntityBuilder.create()
            .setMode(HttpMultipartMode.BROWSER_COMPATIBLE)
            .addBinaryBody(PACKAGE_PART_NAME, file)
            .build()

        private fun createLegacyEntity(file: File): RequestEntity {
            val parts = Collections.singleton(FilePart(PACKAGE_PART_NAME, file.name, file))
            return MultipartRequestEntity(parts.toTypedArray(), HttpMethodParams())
        }
    }

    private companion object {
        const val PACKAGE_PART_NAME = "package"
        const val NUGET_APIKEY_HEADER = "x-nuget-apikey"
    }
}
