package jetbrains.buildServer.nuget.tests.integration.feed.json

import jetbrains.buildServer.nuget.feed.server.NuGetAPIVersion
import jetbrains.buildServer.nuget.tests.integration.feed.server.NuGetJavaFeedIntegrationTestBase

open class JsonFeedIntegrationTestBase : NuGetJavaFeedIntegrationTestBase() {
    override fun getAPIVersion(): NuGetAPIVersion {
        return NuGetAPIVersion.V3
    }

    override fun assertContainsPackageVersion(responseBody: String?, version: String?) {
        assertContains(responseBody, "\"version\":\"$version\"")
    }

    override fun assertNotContainsPackageVersion(responseBody: String?, version: String?) {
        assertNotContains(responseBody, "\"version\":\"$version\"", false)
    }

    fun assertContainsCorrectUrls(responseBody: String?) {
        assertContainsDataIdUrl(responseBody)
        assertContainsDataIdVersionsUrl(responseBody)
        assertContainsRegistrationUrl(responseBody)
    }

    fun assertContainsDataIdUrl(responseBody: String?) {
        assertContains(responseBody, "\"data\":[{\"@id\":\"${serverUrl}${myAuthenticationType}${SERVLET_V3_PATH}");
    }

    fun assertContainsDataIdVersionsUrl(responseBody: String?) {
        assertContains(responseBody, "\"versions\":[{\"@id\":\"${serverUrl}${myAuthenticationType}${SERVLET_V3_PATH}");
    }

    fun assertContainsRegistrationUrl(responseBody: String?) {
        assertContains(responseBody, "\"registration\":\"${serverUrl}${myAuthenticationType}${SERVLET_V3_PATH}");
    }

    fun assertContainsIdUrl(responseBody: String?) {
        assertContains(responseBody, "{\"@id\":\"${serverUrl}${myAuthenticationType}${SERVLET_V3_PATH}");
    }

    fun assertContainsDownloadUrl(responseBody: String?) {
        assertContains(responseBody, "\"packageContent\":\"${serverUrl}${myAuthenticationType}${DOWNLOAD_URL}");
    }
}
