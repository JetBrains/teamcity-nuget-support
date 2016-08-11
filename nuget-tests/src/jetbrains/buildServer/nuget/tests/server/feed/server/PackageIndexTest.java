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

package jetbrains.buildServer.nuget.tests.server.feed.server;

import com.google.common.collect.Maps;
import jetbrains.buildServer.BaseTestCase;
import jetbrains.buildServer.nuget.feed.server.NuGetServerSettings;
import jetbrains.buildServer.nuget.feed.server.index.NuGetIndexEntry;
import jetbrains.buildServer.nuget.feed.server.index.PackagesIndex;
import jetbrains.buildServer.nuget.feed.server.index.impl.PackagesIndexImpl;
import jetbrains.buildServer.nuget.feed.server.index.impl.transform.AccessCheckTransformation;
import jetbrains.buildServer.nuget.feed.server.index.impl.transform.DownloadUrlComputationTransformation;
import jetbrains.buildServer.nuget.feed.server.index.impl.transform.IsPrereleaseTransformation;
import jetbrains.buildServer.nuget.feed.server.index.impl.transform.SamePackagesFilterTransformation;
import jetbrains.buildServer.nuget.feed.server.odata4j.entity.PackageEntityAdapter;
import jetbrains.buildServer.nuget.tests.integration.feed.server.MockExternalIdTransformation;
import jetbrains.buildServer.serverSide.ProjectManager;
import jetbrains.buildServer.serverSide.auth.AuthorityHolder;
import jetbrains.buildServer.serverSide.auth.Permission;
import jetbrains.buildServer.serverSide.auth.SecurityContext;
import jetbrains.buildServer.serverSide.metadata.BuildMetadataEntry;
import jetbrains.buildServer.serverSide.metadata.MetadataStorage;
import jetbrains.buildServer.util.CollectionsUtil;
import jetbrains.buildServer.util.TestFor;
import org.jetbrains.annotations.NotNull;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.UnsupportedEncodingException;
import java.security.SecureRandom;
import java.util.*;

import static jetbrains.buildServer.nuget.feedReader.NuGetPackageAttributes.*;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 18.01.12 19:22
 */
public class PackageIndexTest extends BaseTestCase {
  private Mockery m;
  private ProjectManager myProjectManager;
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
    myContext = m.mock(SecurityContext.class);
    myAuthorityHolder = m.mock(AuthorityHolder.class);
    myStorage = m.mock(MetadataStorage.class);
    final NuGetServerSettings serverSettings = m.mock(NuGetServerSettings.class);
    m.checking(new Expectations(){{
      allowing(serverSettings).getNuGetFeedControllerPath(); will(returnValue("foo"));
    }});
    myIndex = new PackagesIndexImpl(
            myStorage,
            Arrays.asList(
                    new SamePackagesFilterTransformation(),
                    new AccessCheckTransformation(myProjectManager, myContext),
                    new IsPrereleaseTransformation(),
                    new MockExternalIdTransformation(),
                    new DownloadUrlComputationTransformation(serverSettings)
            ));

    myEntries = new ArrayList<BuildMetadataEntry>();

    m.checking(new Expectations(){{
      allowing(myContext).getAuthorityHolder(); will(returnValue(myAuthorityHolder));
      allowing(myStorage).getAllEntries("nuget"); will(returnIterator(myEntries));
      allowing(myStorage).findEntriesWithValue(with(equal("nuget")), with(any(String.class)), with(PackagesIndexImpl.PACKAGE_ATTRIBUTES_TO_SEARCH)); will(returnIterator(myEntries));
    }});
  }

  @Test
  @TestFor(issues = "TW-20047")
  public void testIsLatestFromBuildTypes() {
    allowView();

    addEntry("Foo", "1.2.34", "btX", 7);
    addEntry("Foo", "1.2.44", "btY", 9);

    assertPackages("Foo.1.2.44:L:A", "Foo.1.2.34");
  }

  @Test
  public void test_one_package_isLatest() {
    allowView();

    addEntry("Foo", "1.2.34", "btX", 7);

    assertPackages("Foo.1.2.34:L:A");
  }

  @Test
  public void test_two_package_isLatest() {
    allowView();

    addEntry("Foo", "1.2.34", "btX", 7);
    addEntry("Foo", "1.2.44", "btX", 9);

    assertPackages("Foo.1.2.44:L:A", "Foo.1.2.34");
  }

  @Test
  public void test_same_packages() {
    allowView();

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
    allowView();

    addEntry("Foo", "1.2.34", "btX", 7);
    addEntry("Foo", "1.2.44-alpha", "btX", 9);

    assertPackages("Foo.1.2.44-alpha:A", "Foo.1.2.34:L");
  }

  @Test
  public void testCheckesProjectAccess() {
    m.checking(new Expectations() {{
      allowing(myProjectManager).findProjectId("btX");
      will(returnValue("proj1"));
      allowing(myAuthorityHolder).isPermissionGrantedForProject("proj1", Permission.VIEW_PROJECT);
      will(returnValue(false));
    }});
    addEntry("Foo", "1.2.34", "btX", 7);

    Assert.assertFalse(myIndex.getNuGetEntries().hasNext());
  }

  @Test
  public void testCheckesProjectAccess_exception() {
    m.checking(new Expectations() {{
      allowing(myProjectManager).findProjectId("btX");
      will(throwException(new RuntimeException("proj1")));
    }});
    addEntry("Foo", "1.2.34", "btX", 7);

    Assert.assertFalse(myIndex.getNuGetEntries().hasNext());
  }

  @Test
  @TestFor(issues = "TW-20661")
  public void test_Flags_prerelease_only() {
    allowView();
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
    allowView();
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
    allowView();
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

  private void allowView() {
    m.checking(new Expectations() {{
      allowing(myProjectManager).findProjectId(with(any(String.class)));
      will(returnValue("proj1"));
      allowing(myAuthorityHolder).isPermissionGrantedForProject("proj1", Permission.VIEW_PROJECT);
      will(returnValue(true));
    }});
  }

  @Test
  public void test_for_build() {
    allowView();
    m.checking(new Expectations(){{
      allowing(myStorage).getBuildEntry(42, "nuget"); will(returnIterator(myEntries));
    }});
    addEntry("Foo", "1.2.34-alpha", "btX", 7);
    addEntry("Foo", "1.2.34-beta", "btX", 9);
    addEntry("Foo", "1.2.32", "btX", 8);
    addEntry("Foo", "1.2.36", "btX", 10);
    addEntry("Foo", "1.2.37-b", "btX", 12);

    dumpFeed();
    assertPackagesCollection(myIndex.getNuGetEntries(42), FlagMode.Exists, "Foo.1.2.34-alpha", "Foo.1.2.34-beta", "Foo.1.2.32", "Foo.1.2.36", "Foo.1.2.37-b");
    assertPackagesCollection(myIndex.getNuGetEntries(42), FlagMode.IsPrerelease, "Foo.1.2.34-alpha", "Foo.1.2.34-beta", "Foo.1.2.37-b");
    assertPackagesCollection(myIndex.getNuGetEntries(42), FlagMode.IsAbsoluteLatest, "Foo.1.2.37-b"); //sorted by build number
    assertPackagesCollection(myIndex.getNuGetEntries(42), FlagMode.IsLatest, "Foo.1.2.36"); //first entry in list
  }

  @Test
  @TestFor(issues = "TW-20661")
  public void test_Flags_release_only() {
    allowView();
    addEntry("Foo", "1.2.34", "btX", 7);
    addEntry("Foo", "1.2.38", "btX", 8);

    dumpFeed();
    assertPackagesCollection(FlagMode.Exists, "Foo.1.2.34", "Foo.1.2.38");
    assertPackagesCollection(FlagMode.IsPrerelease);
    assertPackagesCollection(FlagMode.IsAbsoluteLatest, "Foo.1.2.38"); //sorted by build number
    assertPackagesCollection(FlagMode.IsLatest, "Foo.1.2.38"); //first entry in list
  }

  @Test
  public void test_wrong_order_release() {
    allowView();

    addEntry("Foo", "2.2.34", "btY", 7);
    addEntry("Foo", "1.2.38", "btX", 8);

    dumpFeed();
    assertPackagesCollection(FlagMode.Exists, "Foo.1.2.38", "Foo.2.2.34");
    assertPackagesCollection(FlagMode.IsPrerelease);
    assertPackagesCollection(FlagMode.IsAbsoluteLatest, "Foo.2.2.34"); //sorted by build number
    assertPackagesCollection(FlagMode.IsLatest, "Foo.2.2.34"); //first entry in list
  }

  @Test
  public void test_wrong_order_pre_release() {
    allowView();

    addEntry("Foo", "2.3.37", "btZ", 6);
    addEntry("Foo", "2.2.34-beta", "btY", 7);
    addEntry("Foo", "1.2.38", "btX", 8);
    addEntry("Foo", "1.2.38-alpha", "btX", 9);

    dumpFeed();
    assertPackagesCollection(FlagMode.Exists, "Foo.1.2.38", "Foo.1.2.38-alpha", "Foo.2.2.34-beta", "Foo.2.3.37");
    assertPackagesCollection(FlagMode.IsPrerelease, "Foo.1.2.38-alpha", "Foo.2.2.34-beta");
    assertPackagesCollection(FlagMode.IsAbsoluteLatest, "Foo.2.3.37");
    assertPackagesCollection(FlagMode.IsLatest, "Foo.2.3.37");
  }

  @Test(invocationCount = 10)
  public void assertPackagesSorted() throws UnsupportedEncodingException {
    allowView();

    final String[] versions = {
            "1.0.0-333",
            "1.0.0-333.44",
            "1.0.0-333.44.55",
            "1.0.0-333.44.z",
            "1.0.0-alpha",
            "1.0.0-alpha.1",
            "1.0.0-beta.2",
            "1.0.0-beta.11",
            "1.0.0-rc.1",
            "1.0.0",
            "1.3.7"
    };

    final Set<Integer> buildId = new HashSet<Integer>();
    final Collection<String> expected = new ArrayList<String>();
    final Random rnd = new SecureRandom(("iddqd-" + System.nanoTime()).getBytes("utf-8"));
    for (String version : versions) {
      int id;
      do {
        id = rnd.nextInt();
      } while(!buildId.add(id));

      addEntry("Foo", version, "btQ", id);
      expected.add("Foo." + version);
    }

    assertPackagesSorted(expected.toArray(new String[expected.size()]));
  }

  @Test
  public void test_search_allAttributesAreProcessed() throws Exception {
    allowView();

    addEntry("foo", "1", "btX", 1);
    addEntry("boo", "2", "btX", 2);
    addEntry("description-matches", "3", "btX", 3, CollectionsUtil.asMap(DESCRIPTION, "foo"));
    addEntry("description-not-match", "4", "btX", 4, CollectionsUtil.asMap(DESCRIPTION, "boo"));
    addEntry("tags-match", "5", "btX", 5, CollectionsUtil.asMap(TAGS, "foo"));
    addEntry("tags-not-match", "6", "btX", 6, CollectionsUtil.asMap(TAGS, "boo"));
    addEntry("authors-match", "7", "btX", 7, CollectionsUtil.asMap(AUTHORS, "foo"));
    addEntry("authors-not-match", "8", "btX", 8, CollectionsUtil.asMap(AUTHORS, "boo"));
    addEntry("title-matches", "9", "btX", 9, CollectionsUtil.asMap(TITLE, "foo"));
    addEntry("title-not-matches", "10", "btX", 10, CollectionsUtil.asMap(TITLE, "boo"));

    assertTrue(myIndex.search("foo").hasNext());

    m.assertIsSatisfied();
  }

  private void addEntry(final String packageId, final String packageVersion, final String buildTypeId, final long buildId){
    addEntry(packageId, packageVersion, buildTypeId, buildId, Maps.<String, String>newHashMap());
  }

  private void addEntry(final String packageId, final String packageVersion, final String buildTypeId, final long buildId, @NotNull final Map<String, String> entryData) {
    final BuildMetadataEntry entry = m.mock(BuildMetadataEntry.class, packageId + "." + packageVersion + "-" + System.nanoTime());

    m.checking(new Expectations() {{
      allowing(entry).getBuildId();
      will(returnValue(buildId));
      allowing(entry).getMetadata();
      will(returnValue(entryData));
      allowing(entry).getKey();
      will(returnValue(packageId));
    }});

    entryData.put("teamcity.buildTypeId", buildTypeId);
    entryData.put("teamcity.artifactPath", "btX/ZZZ");
    entryData.put(VERSION, packageVersion);
    entryData.put(ID, packageId);
    myEntries.add(entry);

    //recall natural sort order of metadata entries
    Collections.sort(myEntries, new Comparator<BuildMetadataEntry>() {
      public int compare(@NotNull BuildMetadataEntry o1, @NotNull BuildMetadataEntry o2) {
        final long b1 = o1.getBuildId();
        final long b2 = o2.getBuildId();
        return (b1 > b2 ? -1 : b1 == b2 ? 0 : 1);
      }
    });
  }

  private void dumpFeed() {
    System.out.println("Dump the feed: ");
    final Iterator<NuGetIndexEntry> it = myIndex.getNuGetEntries();
    while(it.hasNext()) {
      NuGetIndexEntry p = it.next();
      System.out.println("p = " + p);
    }
  }

  private void assertPackages(@NotNull String... idsEx) {
    final Iterator<NuGetIndexEntry> it = myIndex.getNuGetEntries();

    final Set<String> packages = new TreeSet<String>();
    Collections.addAll(packages, idsEx);

    while(it.hasNext()) {
      final NuGetIndexEntry p = it.next();
      final String actualName = p.getKey() + (createAdaptor(p).getIsLatestVersion() ? ":L" : "") + (createAdaptor(p).getIsAbsoluteLatestVersion() ? ":A" : "");
      Assert.assertTrue(packages.remove(actualName), "Must not contain: " + actualName);
    }
    Assert.assertTrue(packages.isEmpty(), "There should also be: " + packages);
  }

  private void assertPackagesSorted(@NotNull String... idsEx) {
    final Iterator<NuGetIndexEntry> it = myIndex.getNuGetEntries();

    int idx = 0;
    while(it.hasNext()) {
      final NuGetIndexEntry p = it.next();
      final String actualName = p.getKey();
      final String expectedName = idsEx[idx++];
      Assert.assertEquals(actualName, expectedName);
    }
    Assert.assertTrue(idx == idsEx.length);
  }

  private void assertPackagesCollection(@NotNull FlagMode mode, @NotNull String... ids) {
    final Iterator<NuGetIndexEntry> it = myIndex.getNuGetEntries();
    assertPackagesCollection(it, mode, ids);
  }

  private void assertPackagesCollection(@NotNull Iterator<NuGetIndexEntry> it,
                                        @NotNull FlagMode mode,
                                        @NotNull String... ids) {
    final Set<String> t = new HashSet<String>(Arrays.asList(ids));
    while(it.hasNext()) {
      NuGetIndexEntry p = it.next();
      Assert.assertTrue(mode.readField(p) == t.remove(p.getKey()), "package " + p + " must have " + mode);
    }
    Assert.assertTrue(t.isEmpty(), "Unexpected packages for " + mode + ": " + t.toString());
  }

  private static enum FlagMode {
    IsPrerelease,
    IsLatest,
    IsAbsoluteLatest,
    Exists
    ;

    public boolean readField(@NotNull final NuGetIndexEntry e) {
      final PackageEntityAdapter ad = createAdaptor(e);

      switch (this) {
        case Exists: return true;
        case IsAbsoluteLatest: return ad.getIsAbsoluteLatestVersion();
        case IsLatest: return ad.getIsLatestVersion();
        case IsPrerelease: return ad.getIsPrerelease();
        default: throw new IllegalArgumentException("unknown");
      }
    }
  }

  @NotNull
  private static PackageEntityAdapter createAdaptor(@NotNull final NuGetIndexEntry e) {
    return new PackageEntityAdapter() {
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
  }
}
