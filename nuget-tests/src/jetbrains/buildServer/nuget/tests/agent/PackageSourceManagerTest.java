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
import jetbrains.buildServer.RootUrlHolder;
import jetbrains.buildServer.agent.AgentRunningBuildEx;
import jetbrains.buildServer.nuget.agent.parameters.PackageSource;
import jetbrains.buildServer.nuget.agent.parameters.PackageSourceManager;
import jetbrains.buildServer.nuget.agent.parameters.impl.PackageSourceManagerImpl;
import jetbrains.buildServer.nuget.common.NuGetServerConstants;
import jetbrains.buildServer.nuget.common.PackagesConstants;
import jetbrains.buildServer.nuget.server.feed.server.NuGetServerPropertiesProvider;
import jetbrains.buildServer.nuget.server.feed.server.NuGetServerSettings;
import jetbrains.buildServer.nuget.server.runner.auth.AuthBean;
import jetbrains.buildServer.parameters.ReferencesResolverUtil;
import jetbrains.buildServer.serverSide.BuildTypeEx;
import jetbrains.buildServer.serverSide.SimpleParameter;
import jetbrains.buildServer.serverSide.parameters.BuildParametersProvider;
import jetbrains.buildServer.util.CollectionsUtil;
import jetbrains.buildServer.util.filters.Filter;
import org.jetbrains.annotations.NotNull;
import org.jmock.Expectations;
import org.jmock.Mockery;
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

  private static final String AGENT_PROVIDED_URL = "server_url_from_agent_props/tc_internal_feed_url";
  private static final String SERVER_PROVIDED_URL = "server_root_url/httpAuth/nuget_feed_controller_path";
  private static final AuthBean AUTH_BEAN = new AuthBean();

  private PackageSourceManager mySources;
  private BuildTypeEx myNugetEnabledBuildType;
  private Mockery m;

  @BeforeMethod
  @Override
  protected void setUp1() throws Throwable {
    super.setUp1();
    m = new Mockery();
    final NuGetServerSettings nuGetServerSettings = m.mock(NuGetServerSettings.class);
    final RootUrlHolder rootUrlHolder = m.mock(RootUrlHolder.class);
    m.checking(new Expectations(){{
      allowing(rootUrlHolder).getRootUrl(); will(returnValue("server_root_url"));
      allowing(nuGetServerSettings).isNuGetServerEnabled(); will(returnValue(true));
      allowing(nuGetServerSettings).getNuGetFeedControllerPath(); will(returnValue("nuget_feed_controller_path"));
    }});

    myServer.registerExtension(BuildParametersProvider.class, "source", new NuGetServerPropertiesProvider(nuGetServerSettings, rootUrlHolder));
    mySources = new PackageSourceManagerImpl();
    myNugetEnabledBuildType = createBuildType();
    myNugetEnabledBuildType.addConfigParameter(new SimpleParameter(NuGetServerConstants.FEED_AUTH_REFERENCE_AGENT_PROVIDED, AGENT_PROVIDED_URL));
  }

  @AfterMethod
  @Override
  protected void tearDown1() throws Exception {
    finishBuild();
    m.assertIsSatisfied();
    super.tearDown1();
  }

  @Test
  public void shouldSetupCredentialsForInternalFeed_IfNoAuthFeaturesConfigured() throws Exception {
    final AgentRunningBuildEx runningBuild = startBuild();

    final List<PackageSource> packageSources = getPackageSourcesOfRunningBuild(runningBuild);

    assertEquals(2, packageSources.size());
    assertContainsPackageSource(AGENT_PROVIDED_URL, runningBuild.getAccessUser(), runningBuild.getAccessCode(), packageSources);
    assertContainsPackageSource(SERVER_PROVIDED_URL, runningBuild.getAccessUser(), runningBuild.getAccessCode(), packageSources);
  }

  @Test
  public void shouldSetupCredentialsForInternalFeed_AuthFeatureConfiguredForInternalFeed() throws Exception {
    addAuthBuildFeature(ReferencesResolverUtil.makeReference(NuGetServerConstants.FEED_AUTH_REFERENCE_AGENT_PROVIDED), "user1", "password1");

    final AgentRunningBuildEx runningBuild = startBuild();
    final List<PackageSource> packageSources = getPackageSourcesOfRunningBuild(runningBuild);

    assertEquals(2, packageSources.size());
    assertContainsPackageSource(AGENT_PROVIDED_URL, runningBuild.getAccessUser(), runningBuild.getAccessCode(), packageSources);
    assertContainsPackageSource(SERVER_PROVIDED_URL, runningBuild.getAccessUser(), runningBuild.getAccessCode(), packageSources);
  }

  @Test
  public void shouldHandleMultipleFeaturesConfiguredForGivenFeedUrl() throws Exception {
    addAuthBuildFeature("some_url", "user1", "password1");
    addAuthBuildFeature("some_url", "user2", "password2");

    final AgentRunningBuildEx runningBuild = startBuild();
    final List<PackageSource> packageSources = getPackageSourcesOfRunningBuild(runningBuild);

    assertEquals(3, packageSources.size());

    assertContainsPackageSource(AGENT_PROVIDED_URL, runningBuild.getAccessUser(), runningBuild.getAccessCode(), packageSources);
    assertContainsPackageSource(SERVER_PROVIDED_URL, runningBuild.getAccessUser(), runningBuild.getAccessCode(), packageSources);
    assertContainsPackageSource("some_url", "user2", "password2", packageSources);
  }

  private void assertContainsPackageSource(final String expectedUrl, final String expectedUser, final String expectedPassword, List<PackageSource> actualPackageSources) {
    assertTrue(CollectionsUtil.contains(actualPackageSources, new Filter<PackageSource>() {
      public boolean accept(@NotNull PackageSource data) {
        return data.getSource().equals(expectedUrl) && expectedUser.equals(data.getUsername()) && expectedPassword.equals(data.getPassword());
      }
    }));
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
