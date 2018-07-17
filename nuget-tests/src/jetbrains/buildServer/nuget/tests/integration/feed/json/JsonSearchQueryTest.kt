package jetbrains.buildServer.nuget.tests.integration.feed.json

import org.testng.annotations.Test

class JsonSearchQueryTest : JsonFeedIntegrationTestBase() {

    @Test
    fun get_existing_package() {
        addMockPackage("MyPackage", "1.0.0.0")

        val responseBody = openRequest("query/?q=MyPackage")
        assertContainsPackageVersion(responseBody, "1.0.0")
    }

    @Test
    fun get_not_existing_package() {
        val responseBody = openRequest("query/?q=MyPackage")
        assertNotContainsPackageVersion(responseBody, "1.0.0")
    }

    @Test
    fun take_first_package() {
        addMockPackage("MyPackage", "1.0.0.0")
        addMockPackage("OtherPackage", "2.0.0.0")

        val responseBody = openRequest("query/?q=package&take=1")
        assertContainsPackageVersion(responseBody, "1.0.0")
    }

    @Test
    fun skip_first_package() {
        addMockPackage("MyPackage", "1.0.0.0")
        addMockPackage("OtherPackage", "2.0.0.0")

        val responseBody = openRequest("query/?q=package&skip=1")
        assertContainsPackageVersion(responseBody, "2.0.0")
    }
}
