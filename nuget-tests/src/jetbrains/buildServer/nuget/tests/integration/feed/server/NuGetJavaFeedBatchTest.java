

package jetbrains.buildServer.nuget.tests.integration.feed.server;

import org.apache.http.HttpStatus;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * OData batch request tests.
 */
public class NuGetJavaFeedBatchTest extends NuGetJavaFeedIntegrationTestBase {

    @Test(dataProvider = "nugetFeedLibrariesData")
    public void testBatchRequest(final NugetFeedLibrary library) throws Exception {
        setODataSerializer(library);
        addMockPackage("MyPackage", "1.0.0.0");

        final RequestWrapper request = createRequest("/$batch");
        request.setMethod("POST");
        request.setContentType("multipart/mixed; boundary=batch_e3b6819b-13c3-43bb-85b2-24b14122fed1");
        request.setBody((
                "--batch_e3b6819b-13c3-43bb-85b2-24b14122fed1\r\n" +
                        "Content-Type: application/http\r\n" +
                        "Content-Transfer-Encoding: binary\r\n" +
                        "\r\n" +
                        String.format("GET %sFindPackagesById()?id='MyPackage' HTTP/1.1\r\n", getNuGetServerUrl()) +
                        "\r\n" +
                        "\r\n" +
                        "--batch_e3b6819b-13c3-43bb-85b2-24b14122fed1--\r\n").getBytes());

        ResponseWrapper response = processRequest(request);
        Assert.assertEquals(response.getStatus(), HttpStatus.SC_ACCEPTED);

        String body = response.toString();
        assertContainsPackageVersion(body, "1.0.0.0");
        Assert.assertTrue(body.contains("HTTP/1.1 200"));
    }

    @Test(dataProvider = "nugetFeedLibrariesData")
    public void testBatchMultipleRequests(final NugetFeedLibrary library) throws Exception {
        setODataSerializer(library);
        addMockPackage("MyPackage", "1.0.0.0");
        addMockPackage("OtherPackage", "2.0.0.0");

        final RequestWrapper request = createRequest("/$batch");
        request.setMethod("POST");
        String boundary = "batch_e3b6819b-13c3-43bb-85b2-24b14122fed1";
        String contentTypePrefix = "multipart/mixed; boundary=";
        request.setContentType(contentTypePrefix + boundary);
        request.setBody((
                "--" + boundary + "\r\n" +
                        "Content-Type: application/http\r\n" +
                        "Content-Transfer-Encoding: binary\r\n" +
                        "\r\n" +
                        String.format("GET %sFindPackagesById()?id='MyPackage' HTTP/1.1\r\n", getNuGetServerUrl()) +
                        "\r\n" +
                        "\r\n" +
                        "--" + boundary + "\r\n" +
                        "Content-Type: application/http\r\n" +
                        "Content-Transfer-Encoding: binary\r\n" +
                        "\r\n" +
                        String.format("GET %sFindPackagesById()?id='OtherPackage' HTTP/1.1\r\n", getNuGetServerUrl()) +
                        "\r\n" +
                        "\r\n" +
                        "--" + boundary + "--\r\n").getBytes());

        ResponseWrapper response = processRequest(request);
        Assert.assertEquals(response.getStatus(), HttpStatus.SC_ACCEPTED);

        String body = response.toString().trim();
        assertContainsPackageVersion(body, "1.0.0.0");
        assertContainsPackageVersion(body, "2.0.0.0");
        Assert.assertTrue(body.contains("HTTP/1.1 200"));

        String contentType = response.getHeader("Content-Type");
        Assert.assertTrue(contentType.startsWith(contentTypePrefix));
        boundary = contentType.substring(contentTypePrefix.length());

        // Check that body starts with --boundary
        int firstBoundary = body.indexOf("--" + boundary);
        Assert.assertTrue(firstBoundary == 0);

        // Check that body contains --boundary for second request
        Assert.assertTrue(body.indexOf("--" + boundary, firstBoundary + 1) > 0);

        // Check that body ends with --boundary--
        Assert.assertTrue(body.endsWith("--" + boundary + "--"));
    }

    private RequestWrapper createRequest(final String path) {
      return new RequestWrapper(
        myContextPath,
        getServletPath(),
        myContextPath + getServletPath() + path
      );
    }
}
