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
}
