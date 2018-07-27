package jetbrains.buildServer.nuget.tests.integration.feed.json

import org.testng.annotations.Test

class JsonAutocompleteTest : JsonFeedIntegrationTestBase() {

    @Test
    fun get_package_names_semver10() {
        addMockPackage("MyPackage", "1.0.0.0")
        addMockPackage("OtherPackage", "2.0.0.0+metadata")

        val responseBody = openRequest("autocomplete/?q=MyPackage")
        assertContains(responseBody, "MyPackage")
        assertNotContains(responseBody, "OtherPackage", true)
    }

    @Test
    fun get_package_names_semver20() {
        addMockPackage("MyPackage", "1.0.0.0")
        addMockPackage("OtherPackage", "2.0.0.0+metadata")

        val responseBody = openRequest("autocomplete/?q=MyPackage&semVerLevel=2.0.0")
        assertContains(responseBody, "MyPackage")
        assertContains(responseBody, "OtherPackage", true)
    }

    @Test
    fun get_package_versions_semver10() {
        addMockPackage("MyPackage", "1.0.0.0")
        addMockPackage("MyPackage", "2.0.0.0+metadata")

        val responseBody = openRequest("autocomplete/?id=MyPackage")
        assertContains(responseBody, "1.0.0.0")
        assertNotContains(responseBody, "2.0.0.0+metadata", true)
    }

    @Test
    fun get_package_versions_semver20() {
        addMockPackage("MyPackage", "1.0.0.0")
        addMockPackage("MyPackage", "2.0.0.0+metadata")

        val responseBody = openRequest("autocomplete/?id=MyPackage&semVerLevel=2.0.0")
        assertContains(responseBody, "1.0.0.0")
        assertContains(responseBody, "2.0.0.0+metadata", true)
    }
}
