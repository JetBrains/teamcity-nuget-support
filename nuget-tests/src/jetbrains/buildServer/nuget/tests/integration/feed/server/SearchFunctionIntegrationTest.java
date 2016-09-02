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

import com.google.common.collect.Lists;
import jetbrains.buildServer.nuget.feed.server.index.NuGetIndexEntry;
import jetbrains.buildServer.nuget.server.version.FrameworkConstraints;
import jetbrains.buildServer.util.CollectionsUtil;
import org.testng.annotations.Test;

import static jetbrains.buildServer.nuget.feedReader.NuGetPackageAttributes.*;
import static jetbrains.buildServer.nuget.feed.server.index.PackagesIndex.TEAMCITY_FRAMEWORK_CONSTRAINTS;

/**
 * @author Evgeniy.Koshkin
 */
public class SearchFunctionIntegrationTest extends NuGetJavaFeedIntegrationTestBase {

  @Test(dataProvider = "nugetFeedLibrariesData")
  public void testBadRequest(final NugetFeedLibrary library) throws Exception {
    setODataSerializer(library);
    addMockPackage("foo", "1.0.0.0");
    assert400("Search()").run();
    assert400("Search()?&searchTerm='foo'&includePrerelease=true").run();
    assert400("Search()?&targetFramework='net45'&includePrerelease=true").run();
    assert400("Search()?&searchTerm='foo'&targetFramework='net45'").run();
  }

  @Test(dataProvider = "nugetFeedLibrariesData")
  public void testNoPackagesFound(final NugetFeedLibrary library) throws Exception {
    setODataSerializer(library);
    addMockPackage(new NuGetIndexEntry("foo", CollectionsUtil.asMap(ID, "foo", TEAMCITY_FRAMEWORK_CONSTRAINTS, FrameworkConstraints.convertToString(Lists.newArrayList("net45")), VERSION, "1")));
    assert200("Search()?&searchTerm='noSuchPackage'&targetFramework='net45'&includePrerelease=true").run();
  }

  @Test(dataProvider = "nugetFeedLibrariesData")
  public void testEmptySearchTerm(final NugetFeedLibrary library) throws Exception {
    setODataSerializer(library);
    addMockPackage(new NuGetIndexEntry("foo", CollectionsUtil.asMap(ID, "foo", TEAMCITY_FRAMEWORK_CONSTRAINTS, FrameworkConstraints.convertToString(Lists.newArrayList("net45")), VERSION, "1")));
    final String response = openRequest("Search()?&searchTerm=''&targetFramework='net45'&includePrerelease=true");
    assertContainsPackageVersion(response, "1.0");
  }

  @Test(dataProvider = "nugetFeedLibrariesData")
  public void testVSRequests(final NugetFeedLibrary library) throws Exception {
    setODataSerializer(library);
    addMockPackage("foo", "1.0.0.0");
    final String[] reqs = {
            "Search()/$count?$filter=IsAbsoluteLatestVersion&searchTerm='foo'&targetFramework='net45%7Cnet45%7Cnet45%7Cnet45%7Cnet45%7Cnet45%7Cnet45'&includePrerelease=true",
            "Search()?$filter=IsAbsoluteLatestVersion&$skip=0&$top=30&searchTerm='foo'&targetFramework='net45%7Cnet45%7Cnet45%7Cnet45%7Cnet45%7Cnet45%7Cnet45'&includePrerelease=true",
            "Search()/$count?$filter=IsAbsoluteLatestVersion&searchTerm=''&targetFramework='net45%7Cnet45%7Cnet45%7Cnet45%7Cnet45%7Cnet45%7Cnet45'&includePrerelease=true",
            "Search()?$filter=IsAbsoluteLatestVersion&$orderby=DownloadCount%20desc,Id&$skip=0&$top=30&searchTerm=''&targetFramework='net45%7Cnet45%7Cnet45%7Cnet45%7Cnet45%7Cnet45%7Cnet45'&includePrerelease=true"
    };
    for (String req : reqs) {
      assert200(req).run();
    }
  }

  @Test(dataProvider = "nugetFeedLibrariesData")
  public void testTargetFramework(final NugetFeedLibrary library) throws Exception {
    setODataSerializer(library);
    addMockPackage(new NuGetIndexEntry("foo", CollectionsUtil.asMap(ID, "foo", TEAMCITY_FRAMEWORK_CONSTRAINTS, "", VERSION, "1")));
    addMockPackage(new NuGetIndexEntry("foo", CollectionsUtil.asMap(ID, "foo", TEAMCITY_FRAMEWORK_CONSTRAINTS, FrameworkConstraints.convertToString(Lists.newArrayList("net45")), VERSION, "2")));
    addMockPackage(new NuGetIndexEntry("foo", CollectionsUtil.asMap(ID, "foo", TEAMCITY_FRAMEWORK_CONSTRAINTS, FrameworkConstraints.convertToString(Lists.newArrayList("MonoTouch")), VERSION, "3")));
    addMockPackage(new NuGetIndexEntry("foo", CollectionsUtil.asMap(ID, "foo", TEAMCITY_FRAMEWORK_CONSTRAINTS, FrameworkConstraints.convertToString(Lists.newArrayList("MonoTouch", "net45")), VERSION, "4")));

    final String response = openRequest("Search()?&searchTerm=''&targetFramework='net45%7Cnet45%7Cnet45%7Cnet45%7Cnet45%7Cnet45%7Cnet45'&includePrerelease=true");

    assertContainsPackageVersion(response, "1.0");
    assertContainsPackageVersion(response, "2.0");
    assertNotContainsPackageVersion(response, "3.0");
    assertContainsPackageVersion(response, "4.0");

    final String emptyTargetFrameworkresponse = openRequest("Search()?&searchTerm=''&targetFramework=''&includePrerelease=true");

    assertContainsPackageVersion(emptyTargetFrameworkresponse, "1.0");
    assertContainsPackageVersion(emptyTargetFrameworkresponse, "2.0");
    assertContainsPackageVersion(emptyTargetFrameworkresponse, "3.0");
    assertContainsPackageVersion(emptyTargetFrameworkresponse, "4.0");
  }

  @Test(dataProvider = "nugetFeedLibrariesData")
  public void testIncludePreRelease(final NugetFeedLibrary library) throws Exception {
    setODataSerializer(library);
    addMockPackage(new NuGetIndexEntry("pre-release", CollectionsUtil.asMap(ID, "foo", IS_PRERELEASE, Boolean.TRUE.toString(), VERSION, "1")));
    addMockPackage(new NuGetIndexEntry("release", CollectionsUtil.asMap(ID, "foo", IS_PRERELEASE, Boolean.FALSE.toString(), VERSION, "2")));
    addMockPackage(new NuGetIndexEntry("release", CollectionsUtil.asMap(ID, "foo", VERSION, "3")));

    final String preReleaseIncludedFeedResponse = openRequest("Search()?&searchTerm=''&targetFramework='net45'&includePrerelease=true");
    assertContainsPackageVersion(preReleaseIncludedFeedResponse, "1.0");
    assertContainsPackageVersion(preReleaseIncludedFeedResponse, "2.0");
    assertContainsPackageVersion(preReleaseIncludedFeedResponse, "3.0");

    final String preReleaseNotIncludedFeedResponse = openRequest("Search()?&searchTerm=''&targetFramework='net45'&includePrerelease=false");
    assertNotContainsPackageVersion(preReleaseNotIncludedFeedResponse, "1.0");
    assertContainsPackageVersion(preReleaseNotIncludedFeedResponse, "2.0");
    assertContainsPackageVersion(preReleaseNotIncludedFeedResponse, "3.0");
  }

  @Test(dataProvider = "nugetFeedLibrariesData")
  public void testFilterIsAbsoluteLatestVersion(final NugetFeedLibrary library) throws Exception {
    setODataSerializer(library);
    addMockPackage(new NuGetIndexEntry("pre-release-old", CollectionsUtil.asMap(ID, "foo", IS_PRERELEASE, Boolean.TRUE.toString(), VERSION, "1")));
    addMockPackage(new NuGetIndexEntry("release-old", CollectionsUtil.asMap(ID, "foo", IS_PRERELEASE, Boolean.FALSE.toString(), VERSION, "2")));
    addMockPackage(new NuGetIndexEntry("release-latest", CollectionsUtil.asMap(ID, "foo", VERSION, "3")));
    addMockPackage(new NuGetIndexEntry("pre-release-latest", CollectionsUtil.asMap(ID, "foo", IS_PRERELEASE, Boolean.TRUE.toString(), VERSION, "4")), true);
    addMockPackage(new NuGetIndexEntry("pre-release-old", CollectionsUtil.asMap(ID, "boo", IS_PRERELEASE, Boolean.TRUE.toString(), VERSION, "5")));
    addMockPackage(new NuGetIndexEntry("pre-release-latest", CollectionsUtil.asMap(ID, "boo", IS_PRERELEASE, Boolean.TRUE.toString(), VERSION, "6")));
    addMockPackage(new NuGetIndexEntry("release-old", CollectionsUtil.asMap(ID, "boo", IS_PRERELEASE, Boolean.FALSE.toString(), VERSION, "7")));
    addMockPackage(new NuGetIndexEntry("release-latest", CollectionsUtil.asMap(ID, "boo", VERSION, "8")), true);

    final String includedPrereleaseResponse = openRequest("Search()?$filter=IsAbsoluteLatestVersion&searchTerm=''&targetFramework='net45'&includePrerelease=true");

    assertContainsPackageVersion(includedPrereleaseResponse, "4.0");
    assertContainsPackageVersion(includedPrereleaseResponse, "8.0");
    assertNotContainsPackageVersion(includedPrereleaseResponse, "1.0");
    assertNotContainsPackageVersion(includedPrereleaseResponse, "2.0");
    assertNotContainsPackageVersion(includedPrereleaseResponse, "3.0");
    assertNotContainsPackageVersion(includedPrereleaseResponse, "5.0");
    assertNotContainsPackageVersion(includedPrereleaseResponse, "6.0");
    assertNotContainsPackageVersion(includedPrereleaseResponse, "7.0");

    final String stableOnlyResponse = openRequest("Search()?$filter=IsAbsoluteLatestVersion&searchTerm=''&targetFramework='net45'&includePrerelease=false");

    assertContainsPackageVersion(stableOnlyResponse, "8.0");
    assertNotContainsPackageVersion(stableOnlyResponse, "1.0");
    assertNotContainsPackageVersion(stableOnlyResponse, "2.0");
    assertNotContainsPackageVersion(stableOnlyResponse, "3.0");
    assertNotContainsPackageVersion(stableOnlyResponse, "4.0");
    assertNotContainsPackageVersion(stableOnlyResponse, "5.0");
    assertNotContainsPackageVersion(stableOnlyResponse, "6.0");
    assertNotContainsPackageVersion(stableOnlyResponse, "7.0");
  }

  @Test(dataProvider = "nugetFeedLibrariesData")
  public void testSkipTop(final NugetFeedLibrary library) throws Exception {
    setODataSerializer(library);
    addMockPackage(new NuGetIndexEntry("pre-release", CollectionsUtil.asMap(ID, "foo", IS_PRERELEASE, Boolean.TRUE.toString(), VERSION, "1")));
    addMockPackage(new NuGetIndexEntry("release", CollectionsUtil.asMap(ID, "foo", IS_PRERELEASE, Boolean.FALSE.toString(), VERSION, "2")));
    addMockPackage(new NuGetIndexEntry("release", CollectionsUtil.asMap(ID, "foo", VERSION, "3")));

    final String skipResponse = openRequest("Search()?$skip=1&searchTerm=''&targetFramework='net45'&includePrerelease=true");
    assertNotContainsPackageVersion(skipResponse, "1.0");
    assertContainsPackageVersion(skipResponse, "2.0");
    assertContainsPackageVersion(skipResponse, "3.0");

    final String skipZeroResponse = openRequest("Search()?$skip=0&searchTerm=''&targetFramework='net45'&includePrerelease=true");
    assertContainsPackageVersion(skipZeroResponse, "1.0");
    assertContainsPackageVersion(skipZeroResponse, "2.0");
    assertContainsPackageVersion(skipZeroResponse, "3.0");

    final String topResponse = openRequest("Search()?$top=2&searchTerm=''&targetFramework='net45'&includePrerelease=true");
    assertContainsPackageVersion(topResponse, "1.0");
    assertContainsPackageVersion(topResponse, "2.0");
    assertNotContainsPackageVersion(topResponse, "3.0");

    final String topZeroResponse = openRequest("Search()?$top=0&searchTerm=''&targetFramework='net45'&includePrerelease=true");
    assertNotContainsPackageVersion(topZeroResponse, "1.0");
    assertNotContainsPackageVersion(topZeroResponse, "2.0");
    assertNotContainsPackageVersion(topZeroResponse, "3.0");

    final String skipTopResponse = openRequest("Search()?$skip=1&$top=1&searchTerm=''&targetFramework='net45'&includePrerelease=true");
    assertNotContainsPackageVersion(skipTopResponse, "1.0");
    assertContainsPackageVersion(skipTopResponse, "2.0");
    assertNotContainsPackageVersion(skipTopResponse, "3.0");
  }

  @Test(dataProvider = "nugetFeedLibrariesData")
  public void testOrderBy(final NugetFeedLibrary library) throws Exception {
    setODataSerializer(library);
    addMockPackage(new NuGetIndexEntry("1", CollectionsUtil.asMap(ID, "foo", IS_PRERELEASE, Boolean.TRUE.toString(), DESCRIPTION, "3", VERSION, "1")));
    addMockPackage(new NuGetIndexEntry("2", CollectionsUtil.asMap(ID, "foo", IS_PRERELEASE, Boolean.TRUE.toString(), DESCRIPTION, "2", VERSION, "2")));
    addMockPackage(new NuGetIndexEntry("3", CollectionsUtil.asMap(ID, "foo", IS_PRERELEASE, Boolean.TRUE.toString(), DESCRIPTION, "1", VERSION, "3")));

    final String defaultOrderingResponse = openRequest("Search()?&searchTerm=''&targetFramework='net45'&includePrerelease=true");
    final String orderByDescriptionDescResponse = openRequest("Search()?$orderby=Description%20desc,Id&searchTerm=''&targetFramework='net45'&includePrerelease=true");
    final String orderByDescriptionAscResponse = openRequest("Search()?$orderby=Description%20asc,Id&searchTerm=''&targetFramework='net45'&includePrerelease=true");

    assertPackageVersionsOrder(defaultOrderingResponse, "1.0", "2.0", "3.0");
    assertPackageVersionsOrder(orderByDescriptionDescResponse, "1.0", "2.0", "3.0");
    assertPackageVersionsOrder(orderByDescriptionAscResponse, "3.0", "2.0", "1.0");
  }

  @Test(dataProvider = "nugetFeedLibrariesData")
  public void testCountRequest(final NugetFeedLibrary library) throws Exception {
    setODataSerializer(library);
    addMockPackage(new NuGetIndexEntry("aaa", CollectionsUtil.asMap(ID, "foo", VERSION, "1")));
    addMockPackage(new NuGetIndexEntry("bbb", CollectionsUtil.asMap(ID, "foo", VERSION, "2")), true);

    assertEquals("2", openRequest("Search()/$count?&searchTerm=''&targetFramework='net45'&includePrerelease=true"));
    assertEquals("1", openRequest("Search()/$count?$filter=IsAbsoluteLatestVersion&searchTerm=''&targetFramework='net45'&includePrerelease=true"));
  }

  @Test(dataProvider = "nugetFeedLibrariesData")
  public void testSearchVersions(final NugetFeedLibrary library) throws Exception {
    setODataSerializer(library);
    addMockPackage(CollectionsUtil.asMap(
            ID, "aaa",
            VERSION, "1.0.0",
            IS_PRERELEASE, "false",
            IS_LATEST_VERSION, "false",
            IS_ABSOLUTE_LATEST_VERSION, "false"));
    addMockPackage(CollectionsUtil.asMap(
            ID, "aaa",
            VERSION, "1.1.0",
            IS_PRERELEASE, "false",
            IS_LATEST_VERSION, "true",
            IS_ABSOLUTE_LATEST_VERSION, "false"));
    addMockPackage(CollectionsUtil.asMap(
            ID, "aaa",
            VERSION, "1.2.0-beta",
            IS_PRERELEASE, "true",
            IS_LATEST_VERSION, "false",
            IS_ABSOLUTE_LATEST_VERSION, "true"));

    String response = openRequest("Search()?$filter=IsLatestVersion&searchTerm='aaa'&targetFramework='net45'&includePrerelease=false");
    assertContainsPackageVersion(response, "1.1.0");

    response = openRequest("Search()?$filter=IsAbsoluteLatestVersion&searchTerm='NuGet.CommandLine'&targetFramework='net45'&includePrerelease=true");
    assertContainsPackageVersion(response, "1.2.0-beta");
  }

}