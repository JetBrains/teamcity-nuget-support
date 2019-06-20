package jetbrains.buildServer.nuget.tests.integration.feed.json

import org.apache.http.HttpStatus
import org.testng.Assert
import org.testng.annotations.Test

class JsonPackageContentTest : JsonFeedIntegrationTestBase() {

    @Test
    fun get_package_versions() {
        addMockPackage("MyPackage", "1.0.0.0")

        val responseBody = openRequest("flatcontainer/mypackage/index.json")
        Assert.assertEquals(responseBody, "{\"versions\":[\"1.0.0\"]}")
    }

    @Test
    fun get_package_nupkg() {
        addMockPackage("MyPackage", "1.0.0.0")

        val response = processRequest(createRequest("flatcontainer/mypackage/1.0.0/mypackage.1.0.0.nupkg"))
        val redirectUrl = response.getHeader("Location")
        Assert.assertEquals(redirectUrl, "${serverUrl}$DOWNLOAD_URL", "Actual redirect URL: $redirectUrl")
    }

    @Test
    fun get_package_nuspec() {
        addMockPackage("MyPackage", "1.0.0.0")

        val response = processRequest(createRequest("flatcontainer/mypackage/1.0.0/mypackage.1.0.0.nuspec"))
        val redirectUrl = response.getHeader("Location")
        Assert.assertEquals(redirectUrl,"${serverUrl}$DOWNLOAD_URL!/MyPackage.nuspec", "Actual redirect URL: $redirectUrl")
    }

    @Test
    fun get_invalid_package_file() {
        addMockPackage("MyPackage", "1.0.0.0")

        assertStatusCode(HttpStatus.SC_BAD_REQUEST, "flatcontainer/mypackage/1.0.0/mypackage.1.0.0.html")
    }

    @Test
    fun get_not_existing_package_versions() {
        assertStatusCode(HttpStatus.SC_NOT_FOUND, "flatcontainer/mypackage/index.json")
    }
}
