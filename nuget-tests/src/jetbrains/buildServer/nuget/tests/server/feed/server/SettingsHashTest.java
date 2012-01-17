/*
 * Copyright 2000-2011 JetBrains s.r.o.
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
import jetbrains.buildServer.nuget.server.feed.server.NuGetServerRunnerSettings;
import jetbrains.buildServer.nuget.server.feed.server.NuGetServerRunnerTokens;
import jetbrains.buildServer.nuget.server.feed.server.dotNetFeed.process.SettingsHashProvider;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 10.11.11 10:35
 */
public class SettingsHashTest extends BaseTestCase {
  private Mockery m;
  private NuGetServerRunnerSettings mySettings;
  private NuGetServerRunnerTokens myTokens;
  private SettingsHashProvider myHash;

  @BeforeMethod
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    m = new Mockery();
    mySettings = m.mock(NuGetServerRunnerSettings.class);
    myTokens = m.mock(NuGetServerRunnerTokens.class);

    myHash = new SettingsHashProvider(mySettings, myTokens);
  }

  @Test
  public void test_settings_hash() {
    mockOneCall("tttt", "access-token", "server-token", new File("zzz"), true);
    Assert.assertNotNull(myHash.getSettingsHash());
    m.assertIsSatisfied();
  }

  @Test
  public void test_all_parameters_included() throws InvocationTargetException, IllegalAccessException {
    assertAllGettersCalled(NuGetServerRunnerTokens.class, myTokens, "getAccessTokenHeaderName", "getServerTokenHeaderName");
    assertAllGettersCalled(NuGetServerRunnerSettings.class, mySettings, "getNuGetFeedControllerPath", "getNuGetHttpAuthFeedControllerPath", "getNuGetGuestAuthFeedControllerPath");

    myHash.getSettingsHash();
    m.assertIsSatisfied();
  }

  private <T> void assertAllGettersCalled(Class<? super T> clazz, final T tokens, String... excludeNames) throws InvocationTargetException, IllegalAccessException {
    final Set<String> excludes = new HashSet<String>(Arrays.asList(excludeNames));
    for (final Method method : clazz.getMethods()) {
      if (method.getParameterTypes().length > 0) continue;
      if (excludes.contains(method.getName())) continue;

      if (method.getName().startsWith("get") || method.getName().startsWith("is")) {
        m.checking(new Expectations(){{
          method.invoke(oneOf(tokens)); will(returnValue(someValue(method.getReturnType())));
        }});
      }
    }
  }

  private Object someValue(Class<?> clazz) {
    if (clazz.equals(String.class)) { return "foo"; }
    if (clazz.equals(boolean.class) || clazz.equals(Boolean.class)) return false;
    if (clazz.equals(File.class)) return new File("fffoo");
    throw new RuntimeException("Unexpected type: " + clazz);
  }


  private void mockOneCall(final String url, final String agentToken, final String serverToken, final File logs, final boolean enabled) {
    m.checking(new Expectations(){{
      oneOf(myTokens).getAccessToken(); will(returnValue(agentToken));
      oneOf(myTokens).getServerToken(); will(returnValue(serverToken));
      oneOf(mySettings).getLogFilePath(); will(returnValue(logs));
      oneOf(mySettings).isNuGetFeedEnabled(); will(returnValue(enabled));
      oneOf(mySettings).getPackagesControllerUrl(); will(returnValue(url));
    }});
  }


}
