package jetbrains.buildServer.nuget.tests.feed

import jetbrains.buildServer.ExtensionsProvider
import jetbrains.buildServer.nuget.feed.server.controllers.NuGetCsrfCheck
import jetbrains.buildServer.nuget.tests.integration.feed.server.RequestWrapper
import jetbrains.buildServer.web.CsrfCheck
import org.jmock.Expectations
import org.jmock.Mockery
import org.testng.Assert
import org.testng.annotations.Test

@Test
class NuGetCsrfCheckTest {

    fun setupMethod() {
        val m = Mockery()
        val extensionsProvider = m.mock(ExtensionsProvider::class.java)
        m.checking(object: Expectations() {
            init {
                allowing(extensionsProvider).getExtensions(with(CsrfCheck::class.java))
                will(returnValue(emptyList<CsrfCheck>()))
            }
        })
    }

    fun testGetRequest() {
        val request = RequestWrapper("/app/nuget/feed", "v2")
        request.method = "GET"
        request.setHeader("x-nuget-apikey", "key")

        val result = NuGetCsrfCheck().isSafe(request)

        Assert.assertEquals(result, CsrfCheck.UNKNOWN)
    }

    fun testPostRequest() {
        val request = RequestWrapper("/app/nuget/feed", "v2")
        request.method = "POST"
        request.setHeader("x-nuget-apikey", "key")

        val result = NuGetCsrfCheck().isSafe(request)

        Assert.assertEquals(result, CsrfCheck.UNKNOWN)
    }

    fun testPutRequest() {
        val request = RequestWrapper("/app/nuget/feed", "v2")
        request.method = "PUT"
        request.setHeader("x-nuget-apikey", "key")

        val result = NuGetCsrfCheck().isSafe(request)

        Assert.assertTrue(result.isSafe)
        Assert.assertFalse(result.isUnsafe)
    }

    fun testDeleteRequest() {
        val request = RequestWrapper("/app/nuget/feed", "v2")
        request.method = "DELETE"
        request.setHeader("x-nuget-apikey", "key")

        val result = NuGetCsrfCheck().isSafe(request)

        Assert.assertEquals(result, CsrfCheck.UNKNOWN)
    }
}
