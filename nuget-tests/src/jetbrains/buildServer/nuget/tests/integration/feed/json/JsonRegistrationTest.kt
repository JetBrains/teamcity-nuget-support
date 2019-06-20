package jetbrains.buildServer.nuget.tests.integration.feed.json

import org.apache.http.HttpStatus
import org.testng.annotations.Test

class JsonRegistrationTest : JsonFeedIntegrationTestBase() {

    @Test
    fun get_package_registrations() {
        addMockPackage("MyPackage", "1.0.0.0")
        addMockPackage("MyPackage", "1.1.0.0")

        val responseBody = openRequest("registration1/mypackage/index.json")
        assertContainsPackageVersion(responseBody, "1.0.0")
        assertContainsPackageVersion(responseBody, "1.1.0")
        assertContainsIdUrl(responseBody)
        assertContainsDownloadUrl(responseBody)
    }

    @Test
    fun get_package_registration() {
        addMockPackage("MyPackage", "1.0.0.0")

        val responseBody = openRequest("registration1/mypackage/1.0.0.json")
        assertContainsPackageVersion(responseBody, "1.0.0")
        assertContainsIdUrl(responseBody)
        assertContainsDownloadUrl(responseBody)
    }

    @Test
    fun get_not_existing_package_registrations() {
        assertStatusCode(HttpStatus.SC_NOT_FOUND, "registration1/mypackage/index.json")
    }

    @Test
    fun get_not_existing_package_registration() {
        assertStatusCode(HttpStatus.SC_NOT_FOUND, "registration1/mypackage/1.0.0.json")
    }

    @Test
    fun request_invalid_resource() {
        assertStatusCode(HttpStatus.SC_NOT_FOUND, "registration1/index.html")
    }
}
