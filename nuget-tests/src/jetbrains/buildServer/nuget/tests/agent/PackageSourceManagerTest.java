/*
 * Copyright 2000-2015 JetBrains s.r.o.
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

package jetbrains.buildServer.nuget.tests.agent;

import jetbrains.buildServer.AgentServerFunctionalTestCase;
import jetbrains.buildServer.agent.AgentRunningBuildEx;
import jetbrains.buildServer.nuget.agent.parameters.PackageSource;
import jetbrains.buildServer.nuget.agent.parameters.PackageSourceManager;
import jetbrains.buildServer.nuget.agent.parameters.impl.PackageSourceManagerImpl;
import jetbrains.buildServer.nuget.common.NuGetServerConstants;
import jetbrains.buildServer.nuget.common.PackagesConstants;
import jetbrains.buildServer.nuget.server.runner.auth.AuthBean;
import jetbrains.buildServer.parameters.ReferencesResolverUtil;
import jetbrains.buildServer.serverSide.BuildTypeEx;
import jetbrains.buildServer.serverSide.SimpleParameter;
import jetbrains.buildServer.util.CollectionsUtil;
import org.jetbrains.annotations.NotNull;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Evgeniy.Koshkin
 */
public class PackageSourceManagerTest extends AgentServerFunctionalTestCase {

  private static final String URL = "tc_internal_feed_url";
  private static final AuthBean AUTH_BEAN = new AuthBean();

  private PackageSourceManager mySources;
  private BuildTypeEx myNugetEnabledBuildType;

  @BeforeMethod
  @Override
  protected void setUp1() throws Throwable {
    super.setUp1();
    mySources = new PackageSourceManagerImpl();
    myNugetEnabledBuildType = createBuildType();
    myNugetEnabledBuildType.addConfigParameter(new SimpleParameter(NuGetServerConstants.FEED_AUTH_REFERENCE, URL));
  }

  @AfterMethod
  @Override
  protected void tearDown1() throws Exception {
    finishBuild();
    super.tearDown1();
  }

  @Test
  public void shouldSetupCredentialsForInternalFeed_IfNoAuthFeaturesConfigured() throws Exception {
    final AgentRunningBuildEx runningBuild = startBuild();
    final List<PackageSource> packageSources = getPackageSourcesOfRunningBuild(runningBuild);

    assertFalse(packageSources.isEmpty());
    final PackageSource packageSource = packageSources.get(0);
    assertEquals(URL, packageSource.getSource());
    assertEquals(runningBuild.getAccessUser(), packageSource.getUsername());
    assertEquals(runningBuild.getAccessCode(), packageSource.getPassword());
  }

  @Test
  public void shouldSetupCredentialsForInternalFeed_AuthFeatureConfiguredForInternalFeed() throws Exception {
    addAuthBuildFeature(ReferencesResolverUtil.makeReference(NuGetServerConstants.FEED_AUTH_REFERENCE), "user1", "password1");

    final AgentRunningBuildEx runningBuild = startBuild();
    final List<PackageSource> packageSources = getPackageSourcesOfRunningBuild(runningBuild);

    assertFalse(packageSources.isEmpty());
    final PackageSource packageSource = packageSources.get(0);
    assertEquals(URL, packageSource.getSource());
    assertEquals(runningBuild.getAccessUser(), packageSource.getUsername());
    assertEquals(runningBuild.getAccessCode(), packageSource.getPassword());
  }

  @Test
  public void shouldHandleMultipleFeaturesConfiguredForGivenFeedUrl() throws Exception {
    addAuthBuildFeature("some_url", "user1", "password1");
    addAuthBuildFeature("some_url", "user2", "password2");

    final AgentRunningBuildEx runningBuild = startBuild();
    final List<PackageSource> packageSources = getPackageSourcesOfRunningBuild(runningBuild);

    assertEquals(2, packageSources.size());
  }

  private void addAuthBuildFeature(String url, String user, String password) {
    final Map<String, String> featureParams = CollectionsUtil.asMap(AUTH_BEAN.getFeedKey(), url, AUTH_BEAN.getUsernameKey(), user, AUTH_BEAN.getPasswordKey(), password);
    myNugetEnabledBuildType.addBuildFeature(PackagesConstants.ATHU_FEATURE_TYPE, featureParams);
  }

  private AgentRunningBuildEx startBuild() throws IOException {
    startBuild(myNugetEnabledBuildType, true);
    return getAgentRunningBuild();
  }

  @NotNull
  private List<PackageSource> getPackageSourcesOfRunningBuild(AgentRunningBuildEx runningBuild) {
    return new ArrayList<PackageSource>(mySources.getGlobalPackageSources(runningBuild));
  }
}
