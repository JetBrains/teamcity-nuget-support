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
public class GetUpdatesFunctionIntegrationTest extends NuGetJavaFeedIntegrationTestBase {

  @Test(dataProvider = "nugetFeedLibrariesData")
  public void testVSRequest(final NugetFeedLibrary library) throws Exception {
    setODataSerializer(library);
    assert200("GetUpdates()?packageIds='Microsoft.Web.Infrastructure%7CRouteMagic%7Celmah%7Celmah.corelibrary%7Cxunit%7Cxunit.extensions%7CWebActivatorEx%7CNinject%7CMoq'&versions='1.0.0.0%7C1.2%7C1.2.2%7C1.2.2%7C1.9.2%7C1.9.2%7C2.0.2%7C2.2.1.4%7C4.1.1309.0919'&includePrerelease=true&includeAllVersions=false&targetFrameworks=''&versionConstraints=''").run();
  }

  @Test(dataProvider = "nugetFeedLibrariesData")
  public void shouldHandleIncludePreReleaseParameter(final NugetFeedLibrary library) throws Exception {
    setODataSerializer(library);
    addMockPackage(new NuGetIndexEntry("old", CollectionsUtil.asMap(ID, "foo", IS_PRERELEASE, Boolean.FALSE.toString(), VERSION, "1", PACKAGE_SIZE, "0")));
    addMockPackage(new NuGetIndexEntry("current", CollectionsUtil.asMap(ID, "foo", IS_PRERELEASE, Boolean.FALSE.toString(), VERSION, "2")));
    addMockPackage(new NuGetIndexEntry("new-stable", CollectionsUtil.asMap(ID, "foo", IS_PRERELEASE, Boolean.FALSE.toString(), VERSION, "3")));
    addMockPackage(new NuGetIndexEntry("newest-pre-release", CollectionsUtil.asMap(ID, "foo", IS_PRERELEASE, Boolean.TRUE.toString(), VERSION, "4")));

    final String preReleaseIncludedResponse = openRequest("GetUpdates()?packageIds='foo'&versions='2.0.0.0'&includePrerelease=true&includeAllVersions=false&targetFrameworks=''&versionConstraints=''");
    assertNotContainsPackageVersion(preReleaseIncludedResponse, "1.0");
    assertNotContainsPackageVersion(preReleaseIncludedResponse, "2.0");
    assertNotContainsPackageVersion(preReleaseIncludedResponse, "3.0");
    assertContainsPackageVersion(preReleaseIncludedResponse, "4.0");

    final String stableResponse = openRequest("GetUpdates()?packageIds='foo'&versions='2.0.0.0'&includePrerelease=false&includeAllVersions=false&targetFrameworks=''&versionConstraints=''");
    assertNotContainsPackageVersion(stableResponse, "1.0");
    assertNotContainsPackageVersion(stableResponse, "2.0");
    assertContainsPackageVersion(stableResponse, "3.0");
    assertNotContainsPackageVersion(stableResponse, "4.0");
  }

  @Test(dataProvider = "nugetFeedLibrariesData")
  public void shouldHandleVersionConstraintsParameter(final NugetFeedLibrary library) throws Exception {
    setODataSerializer(library);
    addMockPackage(new NuGetIndexEntry("foo", CollectionsUtil.asMap(ID, "foo", VERSION, "3.2")));
    addMockPackage(new NuGetIndexEntry("foo", CollectionsUtil.asMap(ID, "foo", VERSION, "3.3")));
    addMockPackage(new NuGetIndexEntry("foo", CollectionsUtil.asMap(ID, "foo", VERSION, "3.4")));
    addMockPackage(new NuGetIndexEntry("foo", CollectionsUtil.asMap(ID, "foo", VERSION, "3.4.1")));

    String response = openRequest("GetUpdates()?packageIds='foo'&versions='3.3'&includePrerelease=true&includeAllVersions=true&targetFrameworks=''&versionConstraints='(3.4,)'");
    assertNotContainsPackageVersion(response, "3.2.0");
    assertNotContainsPackageVersion(response, "3.3.0");
    assertNotContainsPackageVersion(response, "3.4.0");
    assertContainsPackageVersion(response, "3.4.1.0");
  }

  @Test(dataProvider = "nugetFeedLibrariesData")
  public void shouldHandleTargetFrameworksParameter(final NugetFeedLibrary library) throws Exception {
    setODataSerializer(library);
    addMockPackage(new NuGetIndexEntry("old-foo", CollectionsUtil.asMap(ID, "foo", TEAMCITY_FRAMEWORK_CONSTRAINTS, "", VERSION, "1")));
    addMockPackage(new NuGetIndexEntry("current-foo", CollectionsUtil.asMap(ID, "foo", TEAMCITY_FRAMEWORK_CONSTRAINTS, FrameworkConstraints.convertToString(Lists.newArrayList("net40")), VERSION, "2.0.0.0")));
    addMockPackage(new NuGetIndexEntry("new-foo", CollectionsUtil.asMap(ID, "foo", TEAMCITY_FRAMEWORK_CONSTRAINTS, FrameworkConstraints.convertToString(Lists.newArrayList("net45")), VERSION, "3")));
    addMockPackage(new NuGetIndexEntry("latest-switched-framework-foo", CollectionsUtil.asMap(ID, "foo", TEAMCITY_FRAMEWORK_CONSTRAINTS, FrameworkConstraints.convertToString(Lists.newArrayList("net50")), VERSION, "4")));

    final String response = openRequest("GetUpdates()?packageIds='foo'&versions='2.0.0.0'&includePrerelease=false&includeAllVersions=true&targetFrameworks='net40%7Cnet45'&versionConstraints=''");

    assertNotContainsPackageVersion(response, "1.0");
    assertNotContainsPackageVersion(response, "2.0.0.0");
    assertContainsPackageVersion(response, "3.0");
    assertNotContainsPackageVersion(response, "4.0");
  }

  @Test(dataProvider = "nugetFeedLibrariesData")
  public void shouldHandleIncludeAllVersionsParameter(final NugetFeedLibrary library) throws Exception {
    setODataSerializer(library);
    addMockPackage(new NuGetIndexEntry("old", CollectionsUtil.asMap(ID, "foo", VERSION, "1")));
    addMockPackage(new NuGetIndexEntry("current", CollectionsUtil.asMap(ID, "foo", VERSION, "2")));
    addMockPackage(new NuGetIndexEntry("new", CollectionsUtil.asMap(ID, "foo", VERSION, "3")));
    addMockPackage(new NuGetIndexEntry("newest", CollectionsUtil.asMap(ID, "foo", VERSION, "4")));

    final String includeAllVersionsResponse = openRequest("GetUpdates()?packageIds='foo'&versions='2.0.0.0'&includePrerelease=true&includeAllVersions=true&targetFrameworks=''&versionConstraints=''");
    assertNotContainsPackageVersion(includeAllVersionsResponse, "1.0");
    assertNotContainsPackageVersion(includeAllVersionsResponse, "2.0");
    assertContainsPackageVersion(includeAllVersionsResponse, "3.0");
    assertContainsPackageVersion(includeAllVersionsResponse, "4.0");

    final String includeSingleVersionResponse = openRequest("GetUpdates()?packageIds='foo'&versions='2.0.0.0'&includePrerelease=true&includeAllVersions=false&targetFrameworks=''&versionConstraints=''");
    assertNotContainsPackageVersion(includeSingleVersionResponse, "1.0");
    assertNotContainsPackageVersion(includeSingleVersionResponse, "2.0");
    assertNotContainsPackageVersion(includeSingleVersionResponse, "3.0");
    assertContainsPackageVersion(includeSingleVersionResponse, "4.0");
  }

  @Test(dataProvider = "nugetFeedLibrariesData")
  public void shouldResponseWithNoContentWhenNumberOfPackageIdsAndVersionsDoNotMatch(final NugetFeedLibrary library) throws Exception {
    setODataSerializer(library);
    addMockPackage(new NuGetIndexEntry("foo", CollectionsUtil.asMap(ID, "foo", TEAMCITY_FRAMEWORK_CONSTRAINTS, "", VERSION, "2.0.0.1")));
    addMockPackage(new NuGetIndexEntry("foo", CollectionsUtil.asMap(ID, "boo", TEAMCITY_FRAMEWORK_CONSTRAINTS, "", VERSION, "2.0.0.2")));
    assert200("GetUpdates()?packageIds='foo%7Cboo'&versions='2.0.0.0'&includePrerelease=true&includeAllVersions=false&targetFrameworks=''&versionConstraints=''").run();
  }

  @Test(dataProvider = "nugetFeedLibrariesData")
  public void shouldHandleParameterWithQuota(final NugetFeedLibrary library) throws Exception {
    setODataSerializer(library);
    assert200("GetUpdates()?packageIds='foo'&versions='3.3''&includePrerelease=true&includeAllVersions=true&targetFrameworks=''&versionConstraints='(3.4,)'").run();
  }
}
