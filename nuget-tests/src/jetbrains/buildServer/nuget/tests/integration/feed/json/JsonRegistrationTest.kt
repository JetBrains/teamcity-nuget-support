package jetbrains.buildServer.nuget.tests.integration.feed.json

import jetbrains.buildServer.nuget.feed.server.json.JsonExtensions
import jetbrains.buildServer.nuget.feed.server.json.JsonRegistrationResponse
import org.apache.http.HttpStatus
import org.testng.Assert
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

    @Test
    fun should_return_long_description() {
        val description1 = "Description 1"
        val description2 = "Description 2"
        addMockPackage(mapOf(
                "Id" to "LongDescription",
                "Version" to "1.2.3",
                "NormalizedVersion" to "1.2.3",
                "Description" to description1,
                "Description1" to description2
        ))
        val response = openRequest("registration1/longdescription/1.2.3.json")
        val responseObj = JsonExtensions.gson.fromJson(response, JsonRegistrationResponse::class.java)
        val description = responseObj
                .items.first()
                .items.first()
                .catalogEntry.description
        Assert.assertEquals(description1 + description2, description)
    }

    @Test
    fun should_return_long_summary() {
        val summary1 = "Description 1"
        val summary2 = "Description 2"
        addMockPackage(mapOf(
                "Id" to "LongSummary",
                "Version" to "1.2.3",
                "NormalizedVersion" to "1.2.3",
                "Summary" to summary1,
                "Summary1" to summary2
        ))
        val response = openRequest("registration1/longsummary/1.2.3.json")
        val responseObj = JsonExtensions.gson.fromJson(response, JsonRegistrationResponse::class.java)
        val summary = responseObj
                .items.first()
                .items.first()
                .catalogEntry.summary
        Assert.assertEquals(summary1 + summary2, summary)
    }

    @Test
    fun should_return_long_dependencies() {
        val id = "test"
        val range = "1.0.1"
        addMockPackage(mapOf(
                "Id" to "LongDependenciesList",
                "Version" to "1.2.3",
                "NormalizedVersion" to "1.2.3",
                "Dependencies" to id,
                "Dependencies1" to ":" + range
        ))
        val response = openRequest("registration1/longdependencieslist/1.2.3.json")
        val responseObj = JsonExtensions.gson.fromJson(response, JsonRegistrationResponse::class.java)
        val dependency = responseObj
                .items.first()
                .items.first()
                .catalogEntry.dependencyGroups.first()
                .dependencies.first()
        Assert.assertEquals(dependency.id, id)
        Assert.assertEquals(dependency.range, range)
    }
}
