/*
 * Copyright 2000-2014 JetBrains s.r.o.
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

import jetbrains.buildServer.nuget.server.feed.server.PackageAttributes;
import jetbrains.buildServer.nuget.server.feed.server.index.NuGetIndexEntry;
import jetbrains.buildServer.nuget.server.feed.server.javaFeed.NuGetAPIVersion;
import jetbrains.buildServer.util.CollectionsUtil;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * @author Evgeniy.Koshkin
 */
public class SearchFeedFunctionIntegrationTest extends NuGetJavaFeedIntegrationTestBase {

  @Override
  @BeforeMethod
  public void setUp() throws Exception {
    super.setUp();
    System.setProperty(NuGetAPIVersion.TEAMCITY_NUGET_API_VERSION_PROP_NAME, NuGetAPIVersion.V2);
  }

  @Override
  @AfterMethod
  public void tearDown() throws Exception {
    System.getProperties().remove(NuGetAPIVersion.TEAMCITY_NUGET_API_VERSION_PROP_NAME);
    super.tearDown();
  }

  @Test
  public void testBadRequest() throws Exception {
    addMockPackage("foo", "1.0.0.0");
    assert400("Search()").run();
    assert400("Search()?&searchTerm='foo'&includePrerelease=true").run();
    assert400("Search()?&targetFramework='net45'&includePrerelease=true").run();
    assert400("Search()?&searchTerm='foo'&targetFramework='net45'").run();
  }

  @Test
  public void testNoPackagesFound() throws Exception {
    addMockPackage("foo", "1.0.0.0");
    assert404("Search()?&searchTerm='noSuchPackage'&targetFramework='net45'&includePrerelease=true").run();
  }

  @Test
  public void testEmptySearchTerm() throws Exception {
    addMockPackage("foo", "1.0.0.0");
    assert200("Search()?&searchTerm=''&targetFramework='net45'&includePrerelease=true").run();
  }

  @Test
  public void testVSRequests() throws Exception {
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

  @Test
  public void testTargetFramework() throws Exception {
    fail();
  }

  @Test
  public void testIncludePreRelease() throws Exception {
    fail();
  }

  @Test
  public void testFilterIsAbsoluteLatestVersion() throws Exception {
    fail();
  }

  @Test
  public void testOrderBy() throws Exception {
    fail();
  }

  @Test
  public void testSkipTop() throws Exception {
    fail();
  }

  @Test
  public void testCountRequest() throws Exception {
    fail();
  }

  @Test
  public void testAllAttributesAreProcessed() throws Exception {
    addMockPackage(new NuGetIndexEntry("id-matches", CollectionsUtil.asMap(PackageAttributes.ID, "foo", PackageAttributes.VERSION, "1")));
    addMockPackage(new NuGetIndexEntry("id-not-matches", CollectionsUtil.asMap(PackageAttributes.ID, "boo", PackageAttributes.VERSION, "2")));
    addMockPackage(new NuGetIndexEntry("description-matches", CollectionsUtil.asMap(PackageAttributes.DESCRIPTION, "foo", PackageAttributes.VERSION, "3")));
    addMockPackage(new NuGetIndexEntry("description-not-matches", CollectionsUtil.asMap(PackageAttributes.DESCRIPTION, "boo", PackageAttributes.VERSION, "4")));
    addMockPackage(new NuGetIndexEntry("tags-matches", CollectionsUtil.asMap(PackageAttributes.TAGS, "foo", PackageAttributes.VERSION, "5")));
    addMockPackage(new NuGetIndexEntry("tags-not-matches", CollectionsUtil.asMap(PackageAttributes.TAGS, "boo", PackageAttributes.VERSION, "6")));
    addMockPackage(new NuGetIndexEntry("authors-matches", CollectionsUtil.asMap(PackageAttributes.AUTHORS, "foo", PackageAttributes.VERSION, "7")));
    addMockPackage(new NuGetIndexEntry("authors-not-matches", CollectionsUtil.asMap(PackageAttributes.AUTHORS, "boo", PackageAttributes.VERSION, "8")));

    final String responseBody = openRequest("Search()?&searchTerm='foo'&targetFramework='net45'&includePrerelease=true");
    assertContains(responseBody, "1.0");
    assertNotContains(responseBody, "2.0", false);
    assertContains(responseBody, "3.0");
    assertNotContains(responseBody, "4.0", false);
    assertContains(responseBody, "5.0");
    assertNotContains(responseBody, "6.0", false);
    assertContains(responseBody, "7.0");
    assertNotContains(responseBody, "8.0", false);
  }
}