/*
 * Copyright 2000-2012 JetBrains s.r.o.
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

package jetbrains.buildServer.nuget.tests.server.feed.server;

import jetbrains.buildServer.BaseTestCase;
import jetbrains.buildServer.nuget.server.feed.server.index.NuGetIndexEntry;
import jetbrains.buildServer.nuget.server.feed.server.index.PackagesIndex;
import jetbrains.buildServer.nuget.server.feed.server.index.impl.PackagesIndexImpl;
import jetbrains.buildServer.serverSide.BuildsManager;
import jetbrains.buildServer.serverSide.ProjectManager;
import jetbrains.buildServer.serverSide.auth.AuthorityHolder;
import jetbrains.buildServer.serverSide.auth.Permission;
import jetbrains.buildServer.serverSide.auth.SecurityContext;
import jetbrains.buildServer.serverSide.metadata.BuildMetadataEntry;
import jetbrains.buildServer.serverSide.metadata.MetadataStorage;
import jetbrains.buildServer.util.TestFor;
import org.jetbrains.annotations.NotNull;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.*;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 18.01.12 19:22
 */
public class PackageIndexTest extends BaseTestCase {
  private Mockery m;
  private ProjectManager myProjectManager;
  private BuildsManager myBuildsManager;
  private SecurityContext myContext;
  private AuthorityHolder myAuthorityHolder;
  private PackagesIndex myIndex;
  private MetadataStorage myStorage;
  private List<BuildMetadataEntry> myEntries;

  @BeforeMethod
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    m = new Mockery();
    myProjectManager = m.mock(ProjectManager.class);
    myBuildsManager = m.mock(BuildsManager.class);
    myContext = m.mock(SecurityContext.class);
    myAuthorityHolder = m.mock(AuthorityHolder.class);
    myStorage = m.mock(MetadataStorage.class);
    myIndex = new PackagesIndexImpl(
            myStorage,
            myBuildsManager,
            myProjectManager,
            myContext);

    myEntries = new ArrayList<BuildMetadataEntry>();

    m.checking(new Expectations(){{
      allowing(myContext).getAuthorityHolder(); will(returnValue(myAuthorityHolder));
      allowing(myStorage).getAllEntries("nuget"); will(returnIterator(myEntries));
    }});

    addEntry("Foo", "1.2.34", "btX", 7);
  }

  private void addEntry(final String name, final String version, final String bt, final long buildId) {
    final Map<String, String> myEntryData = new TreeMap<String, String>();
    final String key = name + "." + version;
    final BuildMetadataEntry entry = m.mock(BuildMetadataEntry.class, key + "-" + System.nanoTime());

    m.checking(new Expectations(){{
      allowing(entry).getBuildId(); will(returnValue(buildId));
      allowing(entry).getMetadata(); will(returnValue(myEntryData));
      allowing(entry).getKey(); will(returnValue(key));
    }});

    myEntryData.put("teamcity.buildTypeId", bt);
    myEntryData.put("teamcity.artifactPath", "btX/ZZZ");
    myEntryData.put("Version", version);
    myEntryData.put("Id", name);
    myEntries.add(entry);

    //recall natural sort order of metadata entries
    Collections.sort(myEntries, new Comparator<BuildMetadataEntry>() {
      public int compare(BuildMetadataEntry o1, BuildMetadataEntry o2) {
        final long b1 = o1.getBuildId();
        final long b2 = o2.getBuildId();
        return (b1 > b2 ? 1 : b1 == b2 ? 0 : 1);
      }
    });
  }

  @Test
  @TestFor(issues = "TW-20047")
  public void testIsLatestFromBuildTypes() {
    m.checking(new Expectations(){{
      allowing(myProjectManager).findProjectId("btX"); will(returnValue("proj1"));
      allowing(myProjectManager).findProjectId("btY"); will(returnValue("proj1"));
      allowing(myAuthorityHolder).isPermissionGrantedForProject("proj1", Permission.VIEW_PROJECT); will(returnValue(true));
    }});

    addEntry("Foo", "1.2.44", "btY", 9);
    final Iterator<NuGetIndexEntry> it = myIndex.getNuGetEntries();

    NuGetIndexEntry next = it.next();
    Assert.assertEquals(next.getKey(), "Foo.1.2.44");
    Assert.assertTrue(isLatestVersion(next));
    Assert.assertTrue(isAbsoluteLatestVersion(next));

    next = it.next();
    Assert.assertEquals(next.getKey(), "Foo.1.2.34");
    Assert.assertFalse(isLatestVersion(next));
    Assert.assertFalse(isAbsoluteLatestVersion(next));
  }

  @Test
  public void test_one_package_isLatest() {
    m.checking(new Expectations(){{
      allowing(myProjectManager).findProjectId("btX"); will(returnValue("proj1"));
      allowing(myAuthorityHolder).isPermissionGrantedForProject("proj1", Permission.VIEW_PROJECT); will(returnValue(true));
    }});

    final NuGetIndexEntry next = myIndex.getNuGetEntries().next();
    Assert.assertTrue(isLatestVersion(next));
    Assert.assertTrue(isAbsoluteLatestVersion(next));
  }

  @Test
  public void test_two_package_isLatest() {
    m.checking(new Expectations(){{
      allowing(myProjectManager).findProjectId("btX"); will(returnValue("proj1"));
      allowing(myAuthorityHolder).isPermissionGrantedForProject("proj1", Permission.VIEW_PROJECT); will(returnValue(true));
    }});

    addEntry("Foo", "1.2.44", "btX", 9);
    final Iterator<NuGetIndexEntry> it = myIndex.getNuGetEntries();

    NuGetIndexEntry next = it.next();
    Assert.assertEquals(next.getKey(), "Foo.1.2.44");
    Assert.assertTrue(isLatestVersion(next));
    Assert.assertTrue(isAbsoluteLatestVersion(next));

    next = it.next();
    Assert.assertEquals(next.getKey(), "Foo.1.2.34");
    Assert.assertFalse(isLatestVersion(next));
    Assert.assertFalse(isAbsoluteLatestVersion(next));
  }

  @Test
  public void test_two_package_isLatest_prerelease() {
    m.checking(new Expectations(){{
      allowing(myProjectManager).findProjectId("btX"); will(returnValue("proj1"));
      allowing(myAuthorityHolder).isPermissionGrantedForProject("proj1", Permission.VIEW_PROJECT); will(returnValue(true));
    }});

    addEntry("Foo", "1.2.44-alpha", "btX", 9);
    final Iterator<NuGetIndexEntry> it = myIndex.getNuGetEntries();

    NuGetIndexEntry next = it.next();
    Assert.assertEquals(next.getKey(), "Foo.1.2.44-alpha");
    Assert.assertFalse(isLatestVersion(next));
    Assert.assertTrue(isAbsoluteLatestVersion(next));

    next = it.next();
    Assert.assertEquals(next.getKey(), "Foo.1.2.34");
    Assert.assertTrue(isLatestVersion(next));
    Assert.assertFalse(isAbsoluteLatestVersion(next));
  }

  private boolean isLatestVersion(@NotNull NuGetIndexEntry entry) {
    return Boolean.parseBoolean(entry.getAttributes().get("IsLatestVersion"));
  }

  private boolean isAbsoluteLatestVersion(@NotNull NuGetIndexEntry entry) {
    return Boolean.parseBoolean(entry.getAttributes().get("IsAbsoluteLatestVersion"));
  }

  @Test
  public void testCheckesProjectAccess() {
    m.checking(new Expectations(){{
      allowing(myProjectManager).findProjectId("btX"); will(returnValue("proj1"));
      allowing(myAuthorityHolder).isPermissionGrantedForProject("proj1", Permission.VIEW_PROJECT); will(returnValue(false));
    }});

    Assert.assertFalse(myIndex.getNuGetEntries().hasNext());
  }

  @Test
  public void testCheckesProjectAccess_exception() {
    m.checking(new Expectations(){{
      allowing(myProjectManager).findProjectId("btX"); will(throwException(new RuntimeException("proj1")));
    }});

    Assert.assertFalse(myIndex.getNuGetEntries().hasNext());
  }

}
