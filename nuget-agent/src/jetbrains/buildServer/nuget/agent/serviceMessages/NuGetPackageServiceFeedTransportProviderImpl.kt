package jetbrains.buildServer.nuget.agent.serviceMessages

import jetbrains.buildServer.agent.AgentRunningBuild
import jetbrains.buildServer.agent.impl.artifacts.ArtifactProcessorUtils
import jetbrains.buildServer.http.HttpUserAgent
import jetbrains.buildServer.util.StringUtil
import org.apache.commons.httpclient.Header
import org.apache.commons.httpclient.HttpClient
import org.apache.commons.httpclient.HttpMethod
import org.apache.commons.httpclient.methods.PutMethod
import org.apache.commons.httpclient.methods.RequestEntity
import org.apache.commons.httpclient.methods.multipart.FilePart
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity
import org.apache.commons.httpclient.params.HttpMethodParams
import org.jetbrains.annotations.NotNull
import java.io.File
import java.util.*

class NuGetPackageServiceFeedTransportProviderImpl(
        @NotNull private val myUrlProvider: NuGetPackageServiceFeedUrlProvider
) : NuGetPackageServiceFeedTransportProvider {

    override fun createTransport(build: AgentRunningBuild): NuGetPackageServiceFeedTransport {
        val serverUrl = myUrlProvider.getUrl(build)
        val httpClient = ArtifactProcessorUtils.prepareHttpClient(serverUrl, build)
        return NuGetPackageServiceFeedTransportImpl(httpClient, serverUrl)
    }

    private class NuGetPackageServiceFeedTransportImpl(
            private val myHttpClient: HttpClient,
            private val myServerUrl: String
    ) : NuGetPackageServiceFeedTransport {
        override fun sendPackage(apiKey: String, file: File): NuGetPackageServiceFeedResponse {
            val httpMethod = createHttpMethod(apiKey, file)
            try {
                val statusCode = myHttpClient.executeMethod(httpMethod)
                val response = ArtifactProcessorUtils.readResponse(httpMethod)

                return object: NuGetPackageServiceFeedResponse {
                    override val statusCode: Int
                        get() = statusCode
                    override val message: String
                        get() = response
                    override val isSuccessful: Boolean
                        get() = statusCode == 200 && StringUtil.isEmptyOrSpaces(message)
                }
            } finally {
                httpMethod.releaseConnection()
            }
        }

        private fun createHttpMethod(apiKey: String, file: File): HttpMethod {
            val params = HttpMethodParams()
            val put = PutMethod(myServerUrl)
            put.doAuthentication = true
            put.addRequestHeader(Header(NUGET_APIKEY_HEADER, apiKey))
            put.requestEntity = createRequestEntity(file, params)

            HttpUserAgent.addHeader(put)

            return put
        }

        private fun createRequestEntity(file: File, params: HttpMethodParams): RequestEntity {
            val parts = Collections.singleton(FilePart(PACKAGE_PART_NAME, file.name, file))
            return MultipartRequestEntity(parts.toTypedArray(), params)
        }
    }

    private companion object {
        const val PACKAGE_PART_NAME = "package"
        const val NUGET_APIKEY_HEADER = "x-nuget-apikey"
    }
}
