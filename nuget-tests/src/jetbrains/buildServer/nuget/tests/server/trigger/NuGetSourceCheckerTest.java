/*
 * Copyright 2000-2017 JetBrains s.r.o.
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

package jetbrains.buildServer.nuget.tests.server.trigger;

import jetbrains.buildServer.nuget.server.exec.SourcePackageReference;
import jetbrains.buildServer.nuget.server.trigger.impl.CheckablePackage;
import jetbrains.buildServer.nuget.server.trigger.impl.settings.PackageCheckerSettings;
import jetbrains.buildServer.nuget.server.trigger.impl.source.NuGetSourceCheckerImpl;
import jetbrains.buildServer.nuget.server.trigger.impl.source.PackageSourceChecker;
import jetbrains.buildServer.nuget.tests.util.TCJMockUtils;
import jetbrains.buildServer.util.TimeService;
import org.jetbrains.annotations.NotNull;
import org.jmock.Mockery;
import org.jmock.api.Invocation;
import org.jmock.lib.action.CustomAction;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collections;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 18.07.12 17:25
 */
public class NuGetSourceCheckerTest extends TriggerTestBase {
  private TimeService myTimeService;
  private PackageCheckerSettings mySettings;
  private PackageSourceChecker myCheckerImpl;
  private NuGetSourceCheckerImpl myChecker;

  private long myTime = 1;
  private long mySettingsExpire = 100;

  @BeforeMethod
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    m = TCJMockUtils.createInstance();
    mySettings = m.mock(PackageCheckerSettings.class);
    myTimeService = m.mock(TimeService.class);
    myCheckerImpl = m.mock(PackageSourceChecker.class);
    myChecker = new NuGetSourceCheckerImpl(myTimeService, mySettings, myCheckerImpl);

    m.checking(new Expectations(){{
      allowing(mySettings).getPackageSourceAvailabilityCheckInterval(); will(new CustomAction("ret") {
        public Object invoke(Invocation invocation) throws Throwable {
          return mySettingsExpire;
        }
      });

      allowing(myTimeService).now(); will(new CustomAction("ret") {
        public Object invoke(Invocation invocation) throws Throwable {
          return myTime;
        }
      });
    }});
  }

  @AfterMethod
  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
    m.assertIsSatisfied();
  }

  @Test
  public void test_empty() {
    myChecker.getAccessiblePackages(Collections.<CheckablePackage>emptyList());
  }

  @Test
  public void test_same_sources() {
    m.checking(new Expectations(){{
      oneOf(myCheckerImpl).checkSource("foo"); will(returnValue(null));
    }});
    myChecker.getAccessiblePackages(Arrays.asList(cp("foo", "a"), cp("foo", "b")));
  }

  @Test
  public void test_same_sources_cache_flushed() {
    m.checking(new Expectations(){{
      oneOf(myCheckerImpl).checkSource("foo"); will(returnValue(null));
      oneOf(myCheckerImpl).checkSource("foo"); will(returnValue(null));
    }});
    myChecker.getAccessiblePackages(Arrays.asList(cp("foo", "a")));
    myTime += 1000;
    myChecker.getAccessiblePackages(Arrays.asList(cp("foo", "b")));
  }

  @Test
  public void test_same_sources_cache_used() {
    m.checking(new Expectations(){{
      oneOf(myCheckerImpl).checkSource("foo"); will(returnValue(null));
    }});
    myChecker.getAccessiblePackages(Arrays.asList(cp("foo", "a")));
    myTime += 10;
    myChecker.getAccessiblePackages(Arrays.asList(cp("foo", "b")));
  }

  @Test
  public void test_same_sources_cache_filtered() {
    final CheckablePackage cp = cp("foo", "a");
    m.checking(new Expectations(){{
      oneOf(myCheckerImpl).checkSource("foo"); will(returnValue("some error"));
      oneOf(cp).setResult(with(failed("some error")));
    }});

    myChecker.getAccessiblePackages(Arrays.asList(cp));
  }

  @Test
  public void test_same_sources_cache_filtered2() {
    final CheckablePackage cp = cp("foo", "a");
    m.checking(new Expectations(){{
      oneOf(myCheckerImpl).checkSource("foo"); will(returnValue("some error"));
      exactly(3).of(cp).setResult(with(failed("some error")));
    }});

    myChecker.getAccessiblePackages(Arrays.asList(cp));
    myChecker.getAccessiblePackages(Arrays.asList(cp));
    myChecker.getAccessiblePackages(Arrays.asList(cp));
  }

  @Test
  public void test_same_sources_cache_exception() {
    final CheckablePackage cp = cp("foo", "a");
    m.checking(new Expectations(){{
      oneOf(myCheckerImpl).checkSource("foo"); will(throwException(new Exception("failure")));
      exactly(3).of(cp).setResult(with(failed("java.lang.Exception")));
    }});

    myChecker.getAccessiblePackages(Arrays.asList(cp));
    myChecker.getAccessiblePackages(Arrays.asList(cp));
    myChecker.getAccessiblePackages(Arrays.asList(cp));
  }

  private CheckablePackage cp(@NotNull final String source, @NotNull final String id) {
    final CheckablePackage cpp = m.mock(CheckablePackage.class, "cp-" + id + "@" + source);
    m.checking(new Expectations(){{
      allowing(cpp).getPackage(); will(returnValue(new SourcePackageReference(source, id, null)));
    }});
    return cpp;
  }
}
