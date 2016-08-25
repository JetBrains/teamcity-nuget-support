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

import org.testng.SkipException;
import org.testng.annotations.Test;

/**
 * OData batch request tests.
 */
public class NuGetJavaFeedBatchTest extends NuGetJavaFeedIntegrationTestBase {

    @Test(dataProvider = "nugetFeedLibrariesData")
    public void testBatchRequest(final NugetFeedLibrary library) throws Exception {
        if (library == NugetFeedLibrary.OData4j) {
            throw new SkipException("OData4j does not support batch requests.");
        }

        setODataSerializer(library);
        addMockPackage("MyPackage", "1.0.0.0");

        final TestFeedRequestWrapper request = new TestFeedRequestWrapper(SERVLET_PATH, "$batch");
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

        assertContainsPackageVersion(executeRequest(request), "1.0.0.0");
        assert200("FindPackagesById()?id='MyPackage2'").run();
    }
}
