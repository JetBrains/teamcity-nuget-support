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

package jetbrains.buildServer.nuget.tests.server;

import jetbrains.buildServer.BaseTestCase;
import jetbrains.buildServer.ExtensionHolder;
import jetbrains.buildServer.RootUrlHolder;
import jetbrains.buildServer.agent.AgentRuntimeProperties;
import jetbrains.buildServer.buildTriggers.BuildTriggerDescriptor;
import jetbrains.buildServer.buildTriggers.BuildTriggerException;
import jetbrains.buildServer.buildTriggers.PolledTriggerContext;
import jetbrains.buildServer.nuget.common.FeedConstants;
import jetbrains.buildServer.nuget.server.TriggerUrlPostProcessor;
import jetbrains.buildServer.nuget.server.exec.SourcePackageInfo;
import jetbrains.buildServer.nuget.server.exec.SourcePackageReference;
import jetbrains.buildServer.nuget.server.trigger.*;
import jetbrains.buildServer.nuget.server.trigger.impl.CheckResult;
import jetbrains.buildServer.nuget.server.trigger.impl.PackageCheckRequest;
import jetbrains.buildServer.nuget.server.trigger.impl.PackageCheckRequestFactory;
import jetbrains.buildServer.nuget.server.trigger.impl.mode.CheckRequestMode;
import jetbrains.buildServer.nuget.server.trigger.impl.mode.CheckRequestModeFactory;
import jetbrains.buildServer.nuget.server.trigger.impl.mode.CheckRequestModeNuGet;
import jetbrains.buildServer.nuget.server.trigger.impl.mode.CheckRequestModeTeamCity;
import jetbrains.buildServer.nuget.server.trigger.impl.settings.PackageCheckerSettingsImpl;
import jetbrains.buildServer.nuget.server.util.SystemInfo;
import jetbrains.buildServer.serverSide.BuildTypeEx;
import jetbrains.buildServer.serverSide.CustomDataStorage;
import jetbrains.buildServer.serverSide.SProject;
import jetbrains.buildServer.serverSide.impl.ProjectEx;
import jetbrains.buildServer.tools.ServerToolManager;
import jetbrains.buildServer.tools.ToolType;
import jetbrains.buildServer.util.TestFor;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jmock.Mockery;
import org.jmock.api.Invocation;
import org.jmock.lib.action.CustomAction;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 14.07.11 18:57
 */
public class NamedPackagesUpdateCheckerTest extends BaseTestCase {
  private Mockery m;
  private NamedPackagesUpdateChecker checker;
  private PolledTriggerContext context;
  private BuildTriggerDescriptor desr;
  private CustomDataStorage store;
  private PackageChangesManager chk;
  private Map<String, String> params;
  private File nugetFakePath;
  private RootUrlHolder myRootUrlHolder;
  private BuildTypeEx myBuildType;
  private ProjectEx myProject;

  private boolean myIsWindows;

  @BeforeMethod
  @Override
  protected void setUp() throws Exception {
    super.setUp();

    myIsWindows = true;
    m = new Mockery();
    context = m.mock(PolledTriggerContext.class);
    desr = m.mock(BuildTriggerDescriptor.class);
    store = m.mock(CustomDataStorage.class);
    params = new TreeMap<String, String>();
    final ServerToolManager toolManager = m.mock(ServerToolManager.class);
    chk = m.mock(PackageChangesManager.class);
    myRootUrlHolder = m.mock(RootUrlHolder.class);
    myBuildType = m.mock(BuildTypeEx.class);
    myProject = m.mock(ProjectEx.class);

    final SystemInfo si = m.mock(SystemInfo.class);

    ExtensionHolder extensionHolder = m.mock(ExtensionHolder.class);
    checker = new NamedPackagesUpdateChecker(chk, new TriggerRequestFactory(new CheckRequestModeFactory(si), toolManager, new PackageCheckRequestFactory(new PackageCheckerSettingsImpl()), extensionHolder), new PackagesHashCalculator());

    final File nugetHome = createTempDir();
    nugetFakePath = new File(nugetHome, FeedConstants.PATH_TO_NUGET_EXE);
    assertTrue(nugetFakePath.getParentFile().mkdirs());
    assertTrue(nugetFakePath.createNewFile());
    final String path = nugetFakePath.getPath();

    m.checking(new Expectations(){{
      allowing(context).getTriggerDescriptor(); will(returnValue(desr));
      allowing(context).getCustomDataStorage(); will(returnValue(store));
      allowing(context).getBuildType(); will(returnValue(myBuildType));
      allowing(desr).getProperties(); will(returnValue(params));
      allowing(toolManager).getUnpackedToolVersionPath(with(any(ToolType.class)), with(any(String.class)), with(any(SProject.class))); will(returnValue(nugetHome));
      allowing(extensionHolder).getExtensions(TriggerUrlPostProcessor.class); will(returnValue(Collections.<TriggerUrlPostProcessor>singletonList(new TriggerUrlRootPostProcessor(myRootUrlHolder))));
      allowing(myBuildType).getProject(); will(returnValue(myProject));

      allowing(si).canStartNuGetProcesses(); will(new CustomAction("Return myIsWindows") {
        public Object invoke(Invocation invocation) throws Throwable {
          return myIsWindows;
        }
      });
    }});

    params.put(TriggerConstants.NUGET_PATH_PARAM_NAME, path);
    params.put(TriggerConstants.PACKAGE, "NUnit");
  }

  @Test
  public void test_resolves_own_feed_url() {
    params.put(TriggerConstants.SOURCE, "%"+ AgentRuntimeProperties.TEAMCITY_SERVER_URL+"%/a/b/c");

    m.checking(new Expectations(){{
      allowing(myRootUrlHolder).getRootUrl(); will(returnValue("http://some-teamcity-with-nuget.org/jonnyzzz"));

      oneOf(chk).checkPackage(with(req(nugetFakePath, "http://some-teamcity-with-nuget.org/jonnyzzz/a/b/c", "NUnit", null)));
      will(returnValue(CheckResult.fromResult(Arrays.asList(new SourcePackageInfo("src", "pkg", "5.6.87")))));

      oneOf(store).getValue("hash"); will(returnValue(null));
      oneOf(store).putValue(with(equal("hash")), with(any(String.class)));
      oneOf(store).flush();
    }});
    Assert.assertNull(checker.checkChanges(context));

    m.assertIsSatisfied();
  }

  @Test
  public void test_check_first_time_should_not_trigger() {
    m.checking(new Expectations(){{
      oneOf(chk).checkPackage(with(req(nugetFakePath, null, "NUnit", null)));
      will(returnValue(CheckResult.fromResult(Arrays.asList(new SourcePackageInfo("src", "pkg", "5.6.87")))));

      oneOf(store).getValue("hash"); will(returnValue(null));
      oneOf(store).putValue(with(equal("hash")), with(any(String.class)));
      oneOf(store).flush();
    }});
    Assert.assertNull(checker.checkChanges(context));

    m.assertIsSatisfied();
  }

  @Test
  public void test_check_first_time_should_not_trigger_linux() {
    myIsWindows = false;
    m.checking(new Expectations(){{
      oneOf(chk).checkPackage(with(reqTC(null, "NUnit")));
      will(returnValue(CheckResult.fromResult(Arrays.asList(new SourcePackageInfo("src", "pkg", "5.6.87")))));

      oneOf(store).getValue("hash"); will(returnValue(null));
      oneOf(store).putValue(with(equal("hash")), with(any(String.class)));
      oneOf(store).flush();
    }});
    Assert.assertNull(checker.checkChanges(context));

    m.assertIsSatisfied();
  }

  @Test
  public void test_should_trigger_on_new_package() {
    final String source = "\\\\ServerNameRemoved\\NugetTest\\Repository";

    params.put(TriggerConstants.PACKAGE, "Common");
    params.put(TriggerConstants.SOURCE, source);
    m.checking(new Expectations(){{
      oneOf(chk).checkPackage(with(req(nugetFakePath, source, "Common", null)));
      will(returnValue(CheckResult.fromResult(Arrays.asList(new SourcePackageInfo(source, "Common", "1.0.0.21")))));

      oneOf(chk).checkPackage(with(req(nugetFakePath, source, "Common", null)));
      will(returnValue(CheckResult.fromResult(Arrays.asList(
              new SourcePackageInfo(source, "Common", "1.0.0.21"),
              new SourcePackageInfo(source, "Common", "2.0.0.22")
      ))));

      final String hash1 = "v2|s:\\\\ServerNameRemoved\\NugetTest\\Repository|p:Common|v:1.0.0.21";
      final String hash2 = "v2|s:\\\\ServerNameRemoved\\NugetTest\\Repository|p:Common|v:1.0.0.21|s:\\\\ServerNameRemoved\\NugetTest\\Repository|p:Common|v:2.0.0.22";

      oneOf(store).getValue("hash"); will(returnValue(null));
      oneOf(store).putValue("hash", hash1);
      oneOf(store).flush();

      oneOf(store).getValue("hash"); will(returnValue(hash1));
      oneOf(store).putValue("hash", hash2);
      oneOf(store).flush();

    }});
    Assert.assertNull(checker.checkChanges(context));


    Assert.assertNotNull(checker.checkChanges(context));

    m.assertIsSatisfied();
  }

  @Test
  public void test_should_trigger_on_new_package2() {
    final String source = "\\\\ServerNameRemoved\\NugetTest\\Repository";

    params.put(TriggerConstants.PACKAGE, "Common");
    params.put(TriggerConstants.SOURCE, source);
    m.checking(new Expectations(){{
      oneOf(chk).checkPackage(with(req(nugetFakePath, source, "Common", null)));
      will(returnValue(CheckResult.fromResult(Arrays.asList(new SourcePackageInfo(source, "Common", "1.0.0.21")))));

      oneOf(chk).checkPackage(with(req(nugetFakePath, source, "Common", null)));
      will(returnValue(CheckResult.fromResult(Arrays.asList(
              new SourcePackageInfo(source, "Common", "1.0.0.21"),
              new SourcePackageInfo(source, "Common", "2.0.0.22")
      ))));

      final String hash1 = "v2|s:\\\\ServerNameRemoved\\NugetTest\\Repository|p:Common|v:1.0.0.21";
      final String hash2 = "v2|s:\\\\ServerNameRemoved\\NugetTest\\Repository|p:Common|v:1.0.0.21|s:\\\\ServerNameRemoved\\NugetTest\\Repository|p:Common|v:2.0.0.22";

      oneOf(store).getValue("hash"); will(returnValue(hash1));
      oneOf(store).getValue("hash"); will(returnValue(hash1));
      oneOf(store).putValue("hash", hash2);
      oneOf(store).flush();

    }});
    Assert.assertNull(checker.checkChanges(context));


    Assert.assertNotNull(checker.checkChanges(context));

    m.assertIsSatisfied();
  }

  @Test
  public void test_should_sort_packages() {
    final String source = "\\\\ServerNameRemoved\\NugetTest\\Repository";

    params.put(TriggerConstants.PACKAGE, "Common");
    params.put(TriggerConstants.SOURCE, source);
    m.checking(new Expectations(){{
      oneOf(chk).checkPackage(with(req(nugetFakePath, source, "Common", null)));
      will(returnValue(CheckResult.fromResult(Arrays.asList(
              new SourcePackageInfo(source, "Common", "1.0.0.21"),
              new SourcePackageInfo(null, "Common", "2.0.0.22"),
              new SourcePackageInfo("s2", "C3ommon", "2.0.0.22"),
              new SourcePackageInfo(null, "C3ommon", "2.0.0.22"),
              new SourcePackageInfo(null, "C3o4mmon", "2.0.0.22"),
              new SourcePackageInfo("s4", "C3o3mmon", "2.0.0.22"),
              new SourcePackageInfo(null, "C3omm5on", "2.0.0.22")
      ))));

      final String hash1 = "v2|s:\\\\ServerNameRemoved\\NugetTest\\Repository|p:Common|v:1.0.0.21|s:s2|p:C3ommon|v:2.0.0.22|s:s4|p:C3o3mmon|v:2.0.0.22|p:C3o4mmon|v:2.0.0.22|p:C3omm5on|v:2.0.0.22|p:C3ommon|v:2.0.0.22|p:Common|v:2.0.0.22";
      oneOf(store).getValue("hash"); will(returnValue("v2foo"));
      oneOf(store).putValue("hash", hash1);
      oneOf(store).flush();

    }});
    Assert.assertNotNull(checker.checkChanges(context));

    m.assertIsSatisfied();
  }

  @Test
  public void test_check_should_not_trigger_twice() {
    m.checking(new Expectations(){{
      oneOf(chk).checkPackage(with(req(nugetFakePath, null, "NUnit", null)));
      will(returnValue(CheckResult.fromResult(Arrays.asList(new SourcePackageInfo("src", "pkg", "5.6.87")))));

      oneOf(chk).checkPackage(with(req(nugetFakePath, null, "NUnit", null)));
      will(returnValue(CheckResult.fromResult(Arrays.asList(new SourcePackageInfo("src", "pkg", "5.6.87")))));

      oneOf(store).getValue("hash"); will(returnValue("v2aaa"));
      oneOf(store).putValue("hash", "v2|s:src|p:pkg|v:5.6.87");
      oneOf(store).getValue("hash"); will(returnValue("v2|s:src|p:pkg|v:5.6.87"));
      oneOf(store).flush();
    }});

    Assert.assertNotNull(checker.checkChanges(context));
    Assert.assertNull(checker.checkChanges(context));

    m.assertIsSatisfied();
  }

  @Test
  @TestFor(issues = "TW-24575")
  public void test_should_not_trigger_build_if_feed_was_empty() {
    //feed is empty
    m.checking(new Expectations(){{
      oneOf(chk).checkPackage(with(req(nugetFakePath, null, "NUnit", null)));
      will(returnValue(CheckResult.fromResult(Collections.<SourcePackageInfo>emptyList())));

      oneOf(store).getValue("hash"); will(returnValue("v2|s:src|p:pkg|v:5.6.87"));
    }});


    //error should be reported
    try {
      checker.checkChanges(context);
      Assert.fail("Exception is expected");
    } catch (BuildTriggerException e) {
      Assert.assertTrue(e.getMessage().contains("Package NUnit was not found in the feed"));
    }
    m.assertIsSatisfied();


    //feed again is not empty
    m.checking(new Expectations(){{
      oneOf(chk).checkPackage(with(req(nugetFakePath, null, "NUnit", null)));
      will(returnValue(CheckResult.fromResult(Arrays.asList(new SourcePackageInfo("src", "pkg", "5.6.87")))));

      oneOf(store).getValue("hash"); will(returnValue("v2|s:src|p:pkg|v:5.6.87"));
    }});

    //no build is expected
    Assert.assertNull(checker.checkChanges(context));
    m.assertIsSatisfied();
  }

  @Test
  @TestFor(issues = "TW-24575")
  public void test_should_throw_error_if_no_packages_found_but_not_update_hash() {
    m.checking(new Expectations(){{
      oneOf(chk).checkPackage(with(req(nugetFakePath, null, "NUnit", null)));
      will(returnValue(CheckResult.fromResult(Collections.<SourcePackageInfo>emptyList())));


      oneOf(store).getValue("hash"); will(returnValue("v2|s:src|p:pkg|v:5.6.87"));
      never(store).putValue("hash", "v2");
      never(store).flush();
    }});

    try {
      checker.checkChanges(context);
      Assert.fail("Exception is expected");
    } catch (BuildTriggerException e) {
      Assert.assertTrue(e.getMessage().contains("Package NUnit was not found in the feed"));
    }

    m.assertIsSatisfied();
  }

  @Test
  @TestFor(issues = "TW-27263")
  public void test_should_not_update_cache_if_no_packages_found_and_cache_already_valid() {
    m.checking(new Expectations(){{
      oneOf(chk).checkPackage(with(req(nugetFakePath, null, "NUnit", null)));
      will(returnValue(CheckResult.fromResult(Collections.<SourcePackageInfo>emptyList())));


      oneOf(store).getValue("hash"); will(returnValue("v2|s:src|p:pkg|v:5.6.87"));
      never(store).putValue("hash", "v2");
      never(store).flush();
    }});

    try {
      checker.checkChanges(context);
      Assert.fail("Exception is expected");
    } catch (BuildTriggerException e) {
      Assert.assertTrue(e.getMessage().contains("Package NUnit was not found in the feed"));
    }

    m.assertIsSatisfied();
  }

  @Test
  @TestFor(issues = "TW-27263")
  public void test_should_update_cache_if_no_packages_found_and_cache_empty() {
    m.checking(new Expectations(){{
      oneOf(chk).checkPackage(with(req(nugetFakePath, null, "NUnit", null)));
      will(returnValue(CheckResult.fromResult(Collections.<SourcePackageInfo>emptyList())));


      oneOf(store).getValue("hash"); will(returnValue(null));
      oneOf(store).putValue("hash", "v2");
      oneOf(store).flush();
    }});

    try {
      checker.checkChanges(context);
      Assert.fail("Exception is expected");
    } catch (BuildTriggerException e) {
      Assert.assertTrue(e.getMessage().contains("Package NUnit was not found in the feed"));
    }

    m.assertIsSatisfied();
  }

  @Test
  @TestFor(issues = "TW-24575")
  public void test_should_trigger_zero_to_one() {
    m.checking(new Expectations(){{
      oneOf(chk).checkPackage(with(req(nugetFakePath, null, "NUnit", null)));
      will(returnValue(CheckResult.fromResult(Arrays.asList(new SourcePackageInfo("src", "pkg", "5.6.87")))));

      oneOf(store).getValue("hash"); will(returnValue("v2"));
      oneOf(store).putValue("hash", "v2|s:src|p:pkg|v:5.6.87");
      oneOf(store).flush();
    }});

    Assert.assertNotNull(checker.checkChanges(context));
    m.assertIsSatisfied();
  }

  @Test
  @TestFor(issues = "TW-24575")
  public void test_should_trigger_none_to_one() {
    m.checking(new Expectations(){{
      oneOf(chk).checkPackage(with(req(nugetFakePath, null, "NUnit", null)));
      will(returnValue(CheckResult.fromResult(Collections.<SourcePackageInfo>emptyList())));

      oneOf(store).getValue("hash"); will(returnValue(null));
      oneOf(store).putValue("hash", "v2");
      oneOf(store).flush();
    }});

    try {
      checker.checkChanges(context);
      Assert.fail("Exception is expected");
    } catch (BuildTriggerException e) {
      Assert.assertTrue(e.getMessage().contains("Package NUnit was not found in the feed"));
    }

    m.assertIsSatisfied();
    m.checking(new Expectations() {{
      oneOf(chk).checkPackage(with(req(nugetFakePath, null, "NUnit", null)));
      will(returnValue(CheckResult.fromResult(Arrays.asList(new SourcePackageInfo("src", "pkg", "5.6.87")))));


      oneOf(store).getValue("hash"); will(returnValue("v2"));
      oneOf(store).putValue("hash", "v2|s:src|p:pkg|v:5.6.87");
      oneOf(store).flush();
    }});

    Assert.assertNotNull(checker.checkChanges(context));
    m.assertIsSatisfied();
  }

  @Test
  @TestFor(issues = "TW-24575")
  public void test_should_throw_error_if_check_result() {
    m.checking(new Expectations(){{
      oneOf(chk).checkPackage(with(req(nugetFakePath, null, "NUnit", null)));
      will(returnValue(CheckResult.failed("something5555")));
    }});

    try {
      checker.checkChanges(context);
      Assert.fail("Exception is expected");
    } catch (BuildTriggerException e) {
      Assert.assertTrue(e.getMessage().contains("something5555"));
    }

    m.assertIsSatisfied();
  }

  @Test
  public void test_check_should_not_trigger_from_older_hash() {
    m.checking(new Expectations(){{
      oneOf(chk).checkPackage(with(req(nugetFakePath, null, "NUnit", null)));
      will(returnValue(CheckResult.fromResult(Arrays.asList(new SourcePackageInfo("src", "pkg", "5.6.87")))));

      oneOf(chk).checkPackage(with(req(nugetFakePath, null, "NUnit", null)));
      will(returnValue(CheckResult.fromResult(Arrays.asList(new SourcePackageInfo("src", "pkg", "5.6.87")))));

      oneOf(store).getValue("hash"); will(returnValue("aaa"));
      oneOf(store).putValue("hash", "v2|s:src|p:pkg|v:5.6.87");
      oneOf(store).getValue("hash"); will(returnValue("v2|s:src|p:pkg|v:5.6.87"));
      oneOf(store).flush();
    }});

    Assert.assertNull(checker.checkChanges(context));
    Assert.assertNull(checker.checkChanges(context));

    m.assertIsSatisfied();
  }

  @Test
  public void test_check_should_not_trigger_twice_linux() {
    myIsWindows = false;
    m.checking(new Expectations(){{
      oneOf(chk).checkPackage(with(reqTC(null, "NUnit")));
      will(returnValue(CheckResult.fromResult(Arrays.asList(new SourcePackageInfo("src", "pkg", "5.6.87")))));

      oneOf(chk).checkPackage(with(reqTC(null, "NUnit")));
      will(returnValue(CheckResult.fromResult(Arrays.asList(new SourcePackageInfo("src", "pkg", "5.6.87")))));

      oneOf(store).getValue("hash"); will(returnValue("v2aaa"));
      oneOf(store).putValue("hash", "v2|s:src|p:pkg|v:5.6.87");
      oneOf(store).getValue("hash"); will(returnValue("v2|s:src|p:pkg|v:5.6.87"));
      oneOf(store).flush();
    }});

    Assert.assertNotNull(checker.checkChanges(context));
    Assert.assertNull(checker.checkChanges(context));

    m.assertIsSatisfied();
  }

  @Test
  public void test_check_should_not_trigger_after_error() {
    m.checking(new Expectations(){{
      oneOf(chk).checkPackage(with(req(nugetFakePath, null, "NUnit", null)));
      will(returnValue(CheckResult.fromResult(Arrays.asList(new SourcePackageInfo("src", "pkg", "5.6.87")))));

      oneOf(chk).checkPackage(with(req(nugetFakePath)));
      will(throwException(new RuntimeException("Failed to execute command")));

      oneOf(chk).checkPackage(with(req(nugetFakePath, null, "NUnit", null)));
      will(returnValue(CheckResult.fromResult(Arrays.asList(new SourcePackageInfo("src", "pkg", "5.6.87")))));

      oneOf(store).getValue("hash"); will(returnValue("v2aaa"));
      oneOf(store).putValue("hash", "v2|s:src|p:pkg|v:5.6.87");
      oneOf(store).getValue("hash"); will(returnValue("v2|s:src|p:pkg|v:5.6.87"));
      oneOf(store).flush();
    }});

    Assert.assertNotNull(checker.checkChanges(context));
    try {
      checker.checkChanges(context);
      Assert.fail("should throw an exception");
    } catch (BuildTriggerException e) {
      //NOP
    }
    Assert.assertNull(checker.checkChanges(context));

    m.assertIsSatisfied();
  }

  @Test
  public void test_check_should_fail_on_error() {
    m.checking(new Expectations() {{
      oneOf(chk).checkPackage(with(req(nugetFakePath)));
      will(throwException(new RuntimeException("Failed to execute command")));
    }});
    try {
      checker.checkChanges(context);
      Assert.fail("should throw an exception");
    } catch (BuildTriggerException e) {
      //NOP
    }

    m.assertIsSatisfied();
  }

  @Test
  public void test_check_should_fail_on_error2() {
    m.checking(new Expectations() {{
      oneOf(chk).checkPackage(with(req(nugetFakePath)));
      will(returnValue(CheckResult.failed("Failed to execute command")));
    }});
    try {
      checker.checkChanges(context);
      Assert.fail("should throw an exception");
    } catch (BuildTriggerException e) {
      //NOP
    }

    m.assertIsSatisfied();
  }

  @Test
  public void test_check_should_not_trigger_after_upgrade() {
    m.checking(new Expectations(){{
      oneOf(chk).checkPackage(with(req(nugetFakePath, null, "NUnit", null)));
      will(returnValue(CheckResult.fromResult(Arrays.asList(new SourcePackageInfo("src", "pkg", "5.6.87")))));

      oneOf(store).getValue("hash"); will(returnValue("aaa"));
      oneOf(store).putValue("hash", "v2|s:src|p:pkg|v:5.6.87");
      oneOf(store).flush();
    }});

    Assert.assertNull(checker.checkChanges(context));
    m.assertIsSatisfied();
  }

  private static class Expectations extends org.jmock.Expectations {
    public Matcher<PackageCheckRequest> req(@NotNull final File nugetPath, @Nullable final String source, @NotNull final String id, @Nullable final String version) {
      return new BaseMatcher<PackageCheckRequest>() {
        private boolean equals(Object a, Object b) {
          if (a == null && b == null) return true;
          return a != null && a.equals(b);
        }

        public boolean matches(Object o) {
          if (!(o instanceof PackageCheckRequest)) return false;
          PackageCheckRequest r = (PackageCheckRequest) o;

          final SourcePackageReference pkg = r.getPackage();
          if (!pkg.getPackageId().equals(id)) return false;
          if (!equals(pkg.getSource(), source)) return false;
          if (!equals(pkg.getVersionSpec(), version)) return false;

          final CheckRequestMode mode = r.getMode();
          if (!(mode instanceof CheckRequestModeNuGet)) return false;
          if (!((CheckRequestModeNuGet) mode).getNuGetPath().equals(nugetPath)) return false;
          return true;
        }

        public void describeTo(Description description) {
          description.appendText("Package: ").appendValue(id).appendText("Source: ").appendValue(source);
        }
      };
    }

    public Matcher<PackageCheckRequest> reqTC(@Nullable final String source, @NotNull final String id) {
      return new BaseMatcher<PackageCheckRequest>() {
        private boolean equals(Object a, Object b) {
          if (a == null && b == null) return true;
          return a != null && a.equals(b);
        }

        public boolean matches(Object o) {
          if (!(o instanceof PackageCheckRequest)) return false;
          PackageCheckRequest r = (PackageCheckRequest) o;

          final SourcePackageReference pkg = r.getPackage();
          if (!pkg.getPackageId().equals(id)) return false;
          if (!equals(pkg.getSource(), source)) return false;

          final CheckRequestMode mode = r.getMode();
          return mode instanceof CheckRequestModeTeamCity;
        }

        public void describeTo(Description description) {
          description.appendText("Package: ").appendValue(id).appendText("Source: ").appendValue(source);
        }
      };
    }

    public Matcher<PackageCheckRequest> req(@NotNull final File nugetPath) {
      return new BaseMatcher<PackageCheckRequest>() {
        public boolean matches(Object o) {
          if (!(o instanceof PackageCheckRequest)) return false;
          PackageCheckRequest r = (PackageCheckRequest) o;

          final CheckRequestMode mode = r.getMode();
          if (!(mode instanceof CheckRequestModeNuGet)) return false;
          if (!((CheckRequestModeNuGet) mode).getNuGetPath().equals(nugetPath)) return false;
          return true;
        }

        public void describeTo(Description description) {
          description.appendText("NuGet check path ").appendValue(nugetPath);
        }
      };
    }
  }
}
