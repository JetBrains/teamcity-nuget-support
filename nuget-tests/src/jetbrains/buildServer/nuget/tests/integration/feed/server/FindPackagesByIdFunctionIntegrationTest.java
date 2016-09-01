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

import org.apache.http.HttpStatus;
import org.testng.annotations.Test;

/**
 * @author Evgeniy.Koshkin
 */
public class FindPackagesByIdFunctionIntegrationTest extends NuGetJavaFeedIntegrationTestBase {

  @Test(dataProvider = "nugetFeedLibrariesData")
  public void testFindPackageById(final NugetFeedLibrary library) throws Exception {
    setODataSerializer(library);
    enableDebug();
    addMockPackage("MyPackage", "1.0.0.0");

    assertContainsPackageVersion(openRequest("FindPackagesById()?id='MyPackage'"), "1.0.0.0");
  }

  @Test(dataProvider = "nugetFeedLibrariesData")
  public void testFindNotExistingPackage(final NugetFeedLibrary library) throws Exception {
    setODataSerializer(library);

    assert200("FindPackagesById()?id='MyPackage2'").run();
  }

  @Test(dataProvider = "nugetFeedLibrariesData")
  public void testFindPackageWithQuota(final NugetFeedLibrary library) throws Exception {
    setODataSerializer(library);

    int statusCode = library == NugetFeedLibrary.OData4j ? HttpStatus.SC_BAD_REQUEST : HttpStatus.SC_OK;
    assertStatusCode(statusCode, "FindPackagesById()?id=''MyTestLibrary'").run();
  }
}
