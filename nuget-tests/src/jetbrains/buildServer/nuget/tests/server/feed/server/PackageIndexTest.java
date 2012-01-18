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
import jetbrains.buildServer.nuget.server.feed.server.index.PackagesIndex;
import jetbrains.buildServer.nuget.server.feed.server.index.impl.PackagesIndexImpl;
import jetbrains.buildServer.serverSide.BuildsManager;
import jetbrains.buildServer.serverSide.ProjectManager;
import jetbrains.buildServer.serverSide.auth.AuthorityHolder;
import jetbrains.buildServer.serverSide.auth.Permission;
import jetbrains.buildServer.serverSide.auth.SecurityContext;
import jetbrains.buildServer.serverSide.metadata.BuildMetadataEntry;
import jetbrains.buildServer.serverSide.metadata.MetadataStorage;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Map;
import java.util.TreeMap;

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
  private BuildMetadataEntry myEntry;
  private Map<String, String> myEntryData;

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
    myEntry = m.mock(BuildMetadataEntry.class);
    myEntryData = new TreeMap<String, String>();

    myIndex = new PackagesIndexImpl(
            myStorage,
            myBuildsManager,
            myProjectManager,
            myContext);

    m.checking(new Expectations(){{
      allowing(myContext).getAuthorityHolder(); will(returnValue(myAuthorityHolder));
      allowing(myStorage).getAllEntries("nuget"); will(returnIterator(myEntry));
      allowing(myEntry).getBuildId(); will(returnValue(7L));
      allowing(myEntry).getMetadata(); will(returnValue(myEntryData));
      allowing(myEntry).getKey(); will(returnValue("Foo.1.2.34"));
    }});
    myEntryData.put("teamcity.buildTypeId", "btX");
    myEntryData.put("teamcity.artifactPath", "btX/ZZZ");
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
