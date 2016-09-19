/*
 * Copyright 2000-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jetbrains.buildServer.nuget.tests.integration.feed.server;

import org.apache.commons.io.IOUtils;
import org.apache.http.Header;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * OData batch request tests.
 */
public class NuGetJavaFeedBatchTest extends NuGetJavaFeedIntegrationTestBase {

  @Test
  public void testBatchRequest() throws Exception {
    addMockPackage("MyPackage", "1.0.0.0");

    String requestBody =
            "--batch_e3b6819b-13c3-43bb-85b2-24b14122fed1\r\n" +
                    "Content-Type: application/http\r\n" +
                    "Content-Transfer-Encoding: binary\r\n" +
                    "\r\n" +
                    String.format("GET %sFindPackagesById()?id='MyPackage' HTTP/1.1\r\n", getNuGetServerUrl()) +
                    "\r\n" +
                    "\r\n" +
                    "--batch_e3b6819b-13c3-43bb-85b2-24b14122fed1--\r\n";

    HttpPost post = new HttpPost(getNuGetServerUrl() + "$batch");
    post.setHeader("Content-Type", "multipart/mixed; boundary=batch_e3b6819b-13c3-43bb-85b2-24b14122fed1");
    post.setEntity(new StringEntity(requestBody));

    final Integer[] statusCode = new Integer[1];
    final String[] responseBody = new String[1];
    final Map<String, String> headers = new HashMap<>();
    executeRequest(post, statusCode, responseBody, headers);

    Assert.assertEquals(Math.toIntExact(statusCode[0]), HttpStatus.SC_ACCEPTED);
    assertContainsPackageVersion(responseBody[0], "1.0.0.0");
    Assert.assertTrue(responseBody[0].contains("HTTP/1.1 200"));
  }

  @Test
  public void testBatchMultipleRequests() throws Exception {
    addMockPackage("MyPackage", "1.0.0.0");
    addMockPackage("OtherPackage", "2.0.0.0");

    HttpPost post = new HttpPost(getNuGetServerUrl() + "$batch");
    String boundary = "batch_e3b6819b-13c3-43bb-85b2-24b14122fed1";
    String contentTypePrefix = "multipart/mixed; boundary=";
    String requestBody =
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
                    "--" + boundary + "--\r\n";

    post.setHeader("Content-Type", "multipart/mixed; boundary=batch_e3b6819b-13c3-43bb-85b2-24b14122fed1");
    post.setEntity(new StringEntity(requestBody));

    final Integer[] statusCode = new Integer[1];
    final String[] responseBody = new String[1];
    final Map<String, String> headers = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    executeRequest(post, statusCode, responseBody, headers);

    Assert.assertEquals(Math.toIntExact(statusCode[0]), HttpStatus.SC_ACCEPTED);

    String body = responseBody[0].trim();
    assertContainsPackageVersion(body, "1.0.0.0");
    assertContainsPackageVersion(body, "2.0.0.0");
    Assert.assertTrue(body.contains("HTTP/1.1 200"));

    String contentType = headers.get("Content-Type");
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

  private void executeRequest(HttpPost post, Integer[] statusCode, String[] responseBody, Map<String, String> headers) {
    execute(post, response1 -> {
      statusCode[0] = response1.getStatusLine().getStatusCode();
      StringWriter writer = new StringWriter();
      IOUtils.copy(response1.getEntity().getContent(), writer, "UTF-8");
      responseBody[0] = writer.toString();
      Header[] headers1 = response1.getAllHeaders();
      for (int i = 0; i < headers1.length; i++) {
        Header header = headers1[i];
        headers.put(header.getName(), header.getValue());
      }
      return null;
    });
  }
}
