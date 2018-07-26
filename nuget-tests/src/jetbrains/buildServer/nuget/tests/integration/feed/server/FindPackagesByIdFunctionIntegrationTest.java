/*
 * Copyright 2000-2017 JetBrains s.r.o.
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

import jetbrains.buildServer.util.CollectionsUtil;
import org.apache.http.HttpStatus;
import org.testng.annotations.Test;

import static jetbrains.buildServer.nuget.feedReader.NuGetPackageAttributes.*;

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

  @Test(dataProvider = "nugetFeedLibrariesData")
  public void testFindPackageWithoutSemVer(final NugetFeedLibrary library) throws Exception {
    setODataSerializer(library);
    enableDebug();
    addMockPackage("MyPackage", "1.0.0.0");
    addMockPackage("MyPackage", "1.0.0.1+metadata");
    addMockPackage(CollectionsUtil.asMap(
      ID, "MyPackage",
      VERSION, "1.0.0.2",
      PACKAGE_SIZE, "0",
      DEPENDENCIES, "package:[2.0.0+metadata, ):fw"
    ));

    String responseBody = openRequest("FindPackagesById()?id='MyPackage'");
    assertContainsPackageVersion(responseBody, "1.0.0.0");
    assertNotContainsPackageVersion(responseBody, "1.0.0.1+metadata");
    assertNotContainsPackageVersion(responseBody, "1.0.0.2");
  }

  @Test(dataProvider = "nugetFeedLibrariesData")
  public void testFindPackageWithSemVer10(final NugetFeedLibrary library) throws Exception {
    setODataSerializer(library);
    enableDebug();
    addMockPackage("MyPackage", "1.0.0.0");
    addMockPackage("MyPackage", "1.0.0.1+metadata");

    String responseBody = openRequest("FindPackagesById()?id='MyPackage'&semVerLevel='1.0.0'");
    assertContainsPackageVersion(responseBody, "1.0.0.0");
    assertNotContainsPackageVersion(responseBody, "1.0.0.1+metadata");
  }

  @Test(dataProvider = "nugetFeedLibrariesData")
  public void testFindPackageWithSemVer20(final NugetFeedLibrary library) throws Exception {
    setODataSerializer(library);
    enableDebug();
    addMockPackage("MyPackage", "1.0.0.0");
    addMockPackage("MyPackage", "1.0.0.1+metadata");

    String responseBody = openRequest("FindPackagesById()?id='MyPackage'&semVerLevel='2.0.0'");
    assertContainsPackageVersion(responseBody, "1.0.0.0");
    assertContainsPackageVersion(responseBody, "1.0.0.1+metadata");
  }

  @Test(dataProvider = "nugetFeedLibrariesData")
  public void testFindPackageWithSemVer20SkipToken(final NugetFeedLibrary library) throws Exception {
    setODataSerializer(library);
    enableDebug();
    addMockPackage("MyPackage", "1.0.0.0");
    addMockPackage("MyPackage", "1.0.0.1+metadata");
    addMockPackage("MyPackage", "1.0.0.2+metadata");

    final String skipToken;
    if (library == NugetFeedLibrary.OData4j) {
      skipToken = "(Id='MyPackage',Version='1.0.0.1+metadata')";
    } else {
      skipToken = "'MyPackage','1.0.0.1+metadata'";
    }
    String responseBody = openRequest("FindPackagesById()?id='MyPackage'&$skiptoken=" + skipToken + "&semVerLevel='2.0.0'");
    assertNotContainsPackageVersion(responseBody, "1.0.0.0");
    assertNotContainsPackageVersion(responseBody, "1.0.0.1+metadata");
    assertContainsPackageVersion(responseBody, "1.0.0.2+metadata");
  }
}
