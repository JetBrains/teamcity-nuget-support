package jetbrains.buildServer.nuget.tests.integration.feed.json

import org.testng.annotations.Test

class JsonSearchQueryTest : JsonFeedIntegrationTestBase() {

    @Test
    fun get_existing_package() {
        addMockPackage("MyPackage", "1.0.0.0")

        val responseBody = openRequest("query/?q=MyPackage")
        assertContainsPackageVersion(responseBody, "1.0.0")
        assertContainsCorrectUrls(responseBody)
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

    @Test
    fun find_all_packages() {
        addMockPackage("MyPackage", "1.0.0.0")
        addMockPackage("OtherPackage", "2.0.0.0")

        val responseBody = openRequest("query")
        assertContainsPackageVersion(responseBody, "1.0.0")
        assertContainsPackageVersion(responseBody, "2.0.0")
    }

    @Test
    fun find_semver10_packages() {
        addMockPackage("MyPackage", "1.0.0.0")
        addMockPackage("OtherPackage", "2.0.0.0+metadata")

        val responseBody = openRequest("query")
        assertContainsPackageVersion(responseBody, "1.0.0")
        assertNotContainsPackageVersion(responseBody, "2.0.0")
    }

    @Test
    fun find_semver20_packages() {
        addMockPackage("MyPackage", "1.0.0.0")
        addMockPackage("OtherPackage", "2.0.0.0+metadata")

        val responseBody = openRequest("query/?semVerLevel=2.0.0")
        assertContainsPackageVersion(responseBody, "1.0.0")
        assertContainsPackageVersion(responseBody, "2.0.0")
    }

    @Test
    fun find_semver20_packages_case_insensitive() {
        addMockPackage("MyPackage", "1.0.0.0")
        addMockPackage("OtherPackage", "2.0.0.0+metadata")

        val responseBody = openRequest("query/?semverlevel=2.0.0")
        assertContainsPackageVersion(responseBody, "1.0.0")
        assertContainsPackageVersion(responseBody, "2.0.0")
    }
}
