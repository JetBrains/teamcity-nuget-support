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
import jetbrains.buildServer.nuget.server.feed.server.javaFeed.entity.PackageEntityAdapter;
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
        return (b1 > b2 ? -1 : b1 == b2 ? 0 : 1);
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

    addEntry("Foo", "1.2.34", "btX", 7);
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

    addEntry("Foo", "1.2.34", "btX", 7);

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

    addEntry("Foo", "1.2.34", "btX", 7);
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
  public void test_same_packages() {
    m.checking(new Expectations(){{
      allowing(myProjectManager).findProjectId("btX"); will(returnValue("proj1"));
      allowing(myProjectManager).findProjectId("btY"); will(returnValue("proj1"));
      allowing(myAuthorityHolder).isPermissionGrantedForProject("proj1", Permission.VIEW_PROJECT); will(returnValue(true));
    }});

    addEntry("Foo", "1.2.34", "btX", 7);
    addEntry("Foo", "1.2.34", "btY", 9);

    final Iterator<NuGetIndexEntry> it = myIndex.getNuGetEntries();
    final NuGetIndexEntry next = it.next();

    Assert.assertFalse(it.hasNext());

    Assert.assertEquals(next.getAttributes().get("teamcity.buildTypeId"), "btY");
  }

  @Test
  @TestFor(issues = "TW-19686")
  public void test_two_package_isLatest_prerelease() {
    m.checking(new Expectations(){{
      allowing(myProjectManager).findProjectId("btX"); will(returnValue("proj1"));
      allowing(myAuthorityHolder).isPermissionGrantedForProject("proj1", Permission.VIEW_PROJECT); will(returnValue(true));
    }});

    addEntry("Foo", "1.2.34", "btX", 7);
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
    return FlagMode.IsLatest.readField(entry);
  }

  private boolean isAbsoluteLatestVersion(@NotNull NuGetIndexEntry entry) {
    return FlagMode.IsAbsoluteLatest.readField(entry);
  }

  @Test
  public void testCheckesProjectAccess() {
    m.checking(new Expectations(){{
      allowing(myProjectManager).findProjectId("btX"); will(returnValue("proj1"));
      allowing(myAuthorityHolder).isPermissionGrantedForProject("proj1", Permission.VIEW_PROJECT); will(returnValue(false));
    }});
    addEntry("Foo", "1.2.34", "btX", 7);

    Assert.assertFalse(myIndex.getNuGetEntries().hasNext());
  }

  @Test
  public void testCheckesProjectAccess_exception() {
    m.checking(new Expectations(){{
      allowing(myProjectManager).findProjectId("btX"); will(throwException(new RuntimeException("proj1")));
    }});
    addEntry("Foo", "1.2.34", "btX", 7);

    Assert.assertFalse(myIndex.getNuGetEntries().hasNext());
  }

  @Test
  @TestFor(issues = "TW-20661")
  public void test_Flags_prerelease_only() {
    m.checking(new Expectations(){{
      allowing(myProjectManager).findProjectId("btX"); will(returnValue("proj1"));
      allowing(myAuthorityHolder).isPermissionGrantedForProject("proj1", Permission.VIEW_PROJECT); will(returnValue(true));
    }});
    addEntry("Foo", "1.2.34-alpha", "btX", 7);
    addEntry("Foo", "1.2.34-beta", "btX", 8);

    dumpFeed();
    assertPackagesCollection(FlagMode.Exists, "Foo.1.2.34-alpha", "Foo.1.2.34-beta");
    assertPackagesCollection(FlagMode.IsPrerelease, "Foo.1.2.34-alpha", "Foo.1.2.34-beta");
    assertPackagesCollection(FlagMode.IsAbsoluteLatest, "Foo.1.2.34-beta"); //sorted by build number
    assertPackagesCollection(FlagMode.IsLatest); //first entry in list
  }

  @Test
  @TestFor(issues = "TW-20661")
  public void test_Flags_mixrelease_only() {
    m.checking(new Expectations(){{
      allowing(myProjectManager).findProjectId("btX"); will(returnValue("proj1"));
      allowing(myAuthorityHolder).isPermissionGrantedForProject("proj1", Permission.VIEW_PROJECT); will(returnValue(true));
    }});
    addEntry("Foo", "1.2.34-alpha", "btX", 7);
    addEntry("Foo", "1.2.34-beta", "btX", 9);
    addEntry("Foo", "1.2.32", "btX", 8);
    addEntry("Foo", "1.2.36", "btX", 10);

    assertPackagesCollection(FlagMode.Exists, "Foo.1.2.34-alpha", "Foo.1.2.34-beta", "Foo.1.2.32", "Foo.1.2.36");
    assertPackagesCollection(FlagMode.IsPrerelease, "Foo.1.2.34-alpha", "Foo.1.2.34-beta");
    assertPackagesCollection(FlagMode.IsAbsoluteLatest, "Foo.1.2.36"); //sorted by build number
    assertPackagesCollection(FlagMode.IsLatest, "Foo.1.2.36"); //first entry in list
  }

  @Test
  @TestFor(issues = "TW-20661")
  public void test_Flags_mixrelease2_only() {
    m.checking(new Expectations(){{
      allowing(myProjectManager).findProjectId("btX"); will(returnValue("proj1"));
      allowing(myAuthorityHolder).isPermissionGrantedForProject("proj1", Permission.VIEW_PROJECT); will(returnValue(true));
    }});
    addEntry("Foo", "1.2.34-alpha", "btX", 7);
    addEntry("Foo", "1.2.34-beta", "btX", 9);
    addEntry("Foo", "1.2.32", "btX", 8);
    addEntry("Foo", "1.2.36", "btX", 10);
    addEntry("Foo", "1.2.37-b", "btX", 12);

    dumpFeed();
    assertPackagesCollection(FlagMode.Exists, "Foo.1.2.34-alpha", "Foo.1.2.34-beta", "Foo.1.2.32", "Foo.1.2.36", "Foo.1.2.37-b");
    assertPackagesCollection(FlagMode.IsPrerelease, "Foo.1.2.34-alpha", "Foo.1.2.34-beta", "Foo.1.2.37-b");
    assertPackagesCollection(FlagMode.IsAbsoluteLatest, "Foo.1.2.37-b"); //sorted by build number
    assertPackagesCollection(FlagMode.IsLatest, "Foo.1.2.36"); //first entry in list
  }

  @Test
  @TestFor(issues = "TW-20661")
  public void test_Flags_release_only() {
    m.checking(new Expectations(){{
      allowing(myProjectManager).findProjectId("btX"); will(returnValue("proj1"));
      allowing(myAuthorityHolder).isPermissionGrantedForProject("proj1", Permission.VIEW_PROJECT); will(returnValue(true));
    }});
    addEntry("Foo", "1.2.34", "btX", 7);
    addEntry("Foo", "1.2.38", "btX", 8);

    dumpFeed();
    assertPackagesCollection(FlagMode.Exists, "Foo.1.2.34", "Foo.1.2.38");
    assertPackagesCollection(FlagMode.IsPrerelease);
    assertPackagesCollection(FlagMode.IsAbsoluteLatest, "Foo.1.2.38"); //sorted by build number
    assertPackagesCollection(FlagMode.IsLatest, "Foo.1.2.38"); //first entry in list
  }

  private void dumpFeed() {
    System.out.println("Dump the feed: ");
    final Iterator<NuGetIndexEntry> it = myIndex.getNuGetEntries();
    while(it.hasNext()) {
      NuGetIndexEntry p = it.next();
      System.out.println("p = " + p);
    }
  }

  private void assertPackagesCollection(FlagMode mode, String... ids) {
    final Iterator<NuGetIndexEntry> it = myIndex.getNuGetEntries();
    final Set<String> t = new HashSet<String>(Arrays.asList(ids));
    while(it.hasNext()) {
      NuGetIndexEntry p = it.next();
      Assert.assertTrue(mode.readField(p) == t.remove(p.getKey()), "package " + p + " must have " + mode);
    }
    Assert.assertTrue(t.isEmpty(), "Unexpected package for " + mode + ": " + t.toString());
  }

  private static enum FlagMode {
    IsPrerelease,
    IsLatest,
    IsAbsoluteLatest,
    Exists
    ;

    public boolean readField(@NotNull final NuGetIndexEntry e) {
      final PackageEntityAdapter ad = new PackageEntityAdapter() {
        public String getAtomEntityType() {
          return null;
        }

        public String getAtomEntitySource(String baseUrl) {
          return null;
        }

        @Override
        protected String getValue(@NotNull String key) {
          return e.getAttributes().get(key);
        }
      };

      switch (this) {
        case Exists: return true;
        case IsAbsoluteLatest: return ad.getIsAbsoluteLatestVersion();
        case IsLatest: return ad.getIsLatestVersion();
        case IsPrerelease: return ad.getIsPrerelease();
        default: throw new IllegalArgumentException("unknown");
      }
    }
  }


}
