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

package jetbrains.buildServer.nuget.tests.server;

import jetbrains.buildServer.BaseTestCase;
import jetbrains.buildServer.buildTriggers.BuildTriggerDescriptor;
import jetbrains.buildServer.buildTriggers.BuildTriggerException;
import jetbrains.buildServer.nuget.server.exec.SourcePackageInfo;
import jetbrains.buildServer.nuget.server.exec.SourcePackageReference;
import jetbrains.buildServer.nuget.server.toolRegistry.NuGetToolManager;
import jetbrains.buildServer.nuget.server.trigger.NamedPackagesUpdateChecker;
import jetbrains.buildServer.nuget.server.trigger.PackagesHashCalculator;
import jetbrains.buildServer.nuget.server.trigger.TriggerConstants;
import jetbrains.buildServer.nuget.server.trigger.impl.*;
import jetbrains.buildServer.nuget.server.util.SystemInfo;
import jetbrains.buildServer.nuget.tests.integration.Paths;
import jetbrains.buildServer.serverSide.CustomDataStorage;
import junit.framework.Assert;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jmock.Mockery;
import org.jmock.api.Invocation;
import org.jmock.lib.action.CustomAction;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 14.07.11 18:57
 */
public class NamedPackagesUpdateCheckerTest extends BaseTestCase {
  private Mockery m;
  private NamedPackagesUpdateChecker checker;
  private BuildTriggerDescriptor desr;
  private CustomDataStorage store;
  private NuGetToolManager manager;
  private PackageChangesManager chk;
  private Map<String, String> params;
  private File nugetFakePath;

  private boolean myIsWindows;

  @BeforeMethod
  @Override
  protected void setUp() throws Exception {
    super.setUp();

    myIsWindows = true;
    m = new Mockery();
    desr = m.mock(BuildTriggerDescriptor.class);
    store = m.mock(CustomDataStorage.class);
    params = new TreeMap<String, String>();
    manager = m.mock(NuGetToolManager.class);
    chk = m.mock(PackageChangesManager.class);

    final SystemInfo si = m.mock(SystemInfo.class);

    checker = new NamedPackagesUpdateChecker(chk, new TriggerRequestFactory(new CheckRequestModeFactory(si), manager, new PackageCheckRequestFactory(new PackageCheckerSettingsImpl())), new PackagesHashCalculator());
    nugetFakePath = Paths.getNuGetRunnerPath();
    final String path = nugetFakePath.getPath();

    m.checking(new Expectations(){{
      allowing(desr).getProperties(); will(returnValue(params));
      allowing(manager).getNuGetPath(path); will(returnValue(path));

      allowing(si).canStartNuGetProcesses(); will(new CustomAction("Return myIsWindows") {
        public Object invoke(Invocation invocation) throws Throwable {
          return myIsWindows;
        }
      });
    }});

    params.put(TriggerConstants.NUGET_EXE, path);
    params.put(TriggerConstants.PACKAGE, "NUnit");
  }

  @Test
  public void test_check_first_time_should_not_trigger() {
    m.checking(new Expectations(){{
      oneOf(chk).checkPackage(with(req(nugetFakePath, null, "NUnit", null)));
      will(returnValue(CheckResult.succeeded(Arrays.asList(new SourcePackageInfo("src", "pkg", "5.6.87")))));

      oneOf(store).getValue("hash"); will(returnValue(null));
      oneOf(store).putValue(with(equal("hash")), with(any(String.class)));
      oneOf(store).flush();
    }});
    Assert.assertNull(checker.checkChanges(desr, store));

    m.assertIsSatisfied();
  }

  @Test
  public void test_check_first_time_should_not_trigger_linux() {
    myIsWindows = false;
    m.checking(new Expectations(){{
      oneOf(chk).checkPackage(with(reqTC(null, "NUnit")));
      will(returnValue(CheckResult.succeeded(Arrays.asList(new SourcePackageInfo("src", "pkg", "5.6.87")))));

      oneOf(store).getValue("hash"); will(returnValue(null));
      oneOf(store).putValue(with(equal("hash")), with(any(String.class)));
      oneOf(store).flush();
    }});
    Assert.assertNull(checker.checkChanges(desr, store));

    m.assertIsSatisfied();
  }

  @Test
  public void test_should_trigger_on_new_package() {
    final String source = "\\\\ServerNameRemoved\\NugetTest\\Repository";

    params.put(TriggerConstants.PACKAGE, "Common");
    params.put(TriggerConstants.SOURCE, source);
    m.checking(new Expectations(){{
      oneOf(chk).checkPackage(with(req(nugetFakePath, source, "Common", null)));
      will(returnValue(CheckResult.succeeded(Arrays.asList(new SourcePackageInfo(source, "Common", "1.0.0.21")))));

      oneOf(chk).checkPackage(with(req(nugetFakePath, source, "Common", null)));
      will(returnValue(CheckResult.succeeded(Arrays.asList(
              new SourcePackageInfo(source, "Common", "1.0.0.21"),
              new SourcePackageInfo(source, "Common", "2.0.0.22")
      ))));

      final String hash1 = "|s:\\\\ServerNameRemoved\\NugetTest\\Repository|p:Common|v:1.0.0.21";
      final String hash2 = "|s:\\\\ServerNameRemoved\\NugetTest\\Repository|p:Common|v:1.0.0.21|s:\\\\ServerNameRemoved\\NugetTest\\Repository|p:Common|v:2.0.0.22";

      oneOf(store).getValue("hash"); will(returnValue(null));
      oneOf(store).putValue("hash", hash1);
      oneOf(store).flush();

      oneOf(store).getValue("hash"); will(returnValue(hash1));
      oneOf(store).putValue("hash", hash2);
      oneOf(store).flush();

    }});
    Assert.assertNull(checker.checkChanges(desr, store));


    Assert.assertNotNull(checker.checkChanges(desr, store));

    m.assertIsSatisfied();
  }

  @Test
  public void test_should_trigger_on_new_package2() {
    final String source = "\\\\ServerNameRemoved\\NugetTest\\Repository";

    params.put(TriggerConstants.PACKAGE, "Common");
    params.put(TriggerConstants.SOURCE, source);
    m.checking(new Expectations(){{
      oneOf(chk).checkPackage(with(req(nugetFakePath, source, "Common", null)));
      will(returnValue(CheckResult.succeeded(Arrays.asList(new SourcePackageInfo(source, "Common", "1.0.0.21")))));

      oneOf(chk).checkPackage(with(req(nugetFakePath, source, "Common", null)));
      will(returnValue(CheckResult.succeeded(Arrays.asList(
              new SourcePackageInfo(source, "Common", "1.0.0.21"),
              new SourcePackageInfo(source, "Common", "2.0.0.22")
      ))));

      final String hash1 = "|s:\\\\ServerNameRemoved\\NugetTest\\Repository|p:Common|v:1.0.0.21";
      final String hash2 = "|s:\\\\ServerNameRemoved\\NugetTest\\Repository|p:Common|v:1.0.0.21|s:\\\\ServerNameRemoved\\NugetTest\\Repository|p:Common|v:2.0.0.22";

      oneOf(store).getValue("hash"); will(returnValue(hash1));
      oneOf(store).getValue("hash"); will(returnValue(hash1));
      oneOf(store).putValue("hash", hash2);
      oneOf(store).flush();

    }});
    Assert.assertNull(checker.checkChanges(desr, store));


    Assert.assertNotNull(checker.checkChanges(desr, store));

    m.assertIsSatisfied();
  }

  @Test
  public void test_should_sort_packages() {
    final String source = "\\\\ServerNameRemoved\\NugetTest\\Repository";

    params.put(TriggerConstants.PACKAGE, "Common");
    params.put(TriggerConstants.SOURCE, source);
    m.checking(new Expectations(){{
      oneOf(chk).checkPackage(with(req(nugetFakePath, source, "Common", null)));
      will(returnValue(CheckResult.succeeded(Arrays.asList(
              new SourcePackageInfo(source, "Common", "1.0.0.21"),
              new SourcePackageInfo(null, "Common", "2.0.0.22"),
              new SourcePackageInfo("s2", "C3ommon", "2.0.0.22"),
              new SourcePackageInfo(null, "C3ommon", "2.0.0.22"),
              new SourcePackageInfo(null, "C3o4mmon", "2.0.0.22"),
              new SourcePackageInfo("s4", "C3o3mmon", "2.0.0.22"),
              new SourcePackageInfo(null, "C3omm5on", "2.0.0.22")
      ))));

      final String hash1 = "|s:\\\\ServerNameRemoved\\NugetTest\\Repository|p:Common|v:1.0.0.21|s:s2|p:C3ommon|v:2.0.0.22|s:s4|p:C3o3mmon|v:2.0.0.22|p:C3o4mmon|v:2.0.0.22|p:C3omm5on|v:2.0.0.22|p:C3ommon|v:2.0.0.22|p:Common|v:2.0.0.22";
      oneOf(store).getValue("hash"); will(returnValue("foo"));
      oneOf(store).putValue("hash", hash1);
      oneOf(store).flush();

    }});
    Assert.assertNotNull(checker.checkChanges(desr, store));

    m.assertIsSatisfied();
  }

  @Test
  public void test_check_should_not_trigger_twice() {
    m.checking(new Expectations(){{
      oneOf(chk).checkPackage(with(req(nugetFakePath, null, "NUnit", null)));
      will(returnValue(CheckResult.succeeded(Arrays.asList(new SourcePackageInfo("src", "pkg", "5.6.87")))));

      oneOf(chk).checkPackage(with(req(nugetFakePath, null, "NUnit", null)));
      will(returnValue(CheckResult.succeeded(Arrays.asList(new SourcePackageInfo("src", "pkg", "5.6.87")))));

      oneOf(store).getValue("hash"); will(returnValue("aaa"));
      oneOf(store).putValue("hash", "|s:src|p:pkg|v:5.6.87");
      oneOf(store).getValue("hash"); will(returnValue("|s:src|p:pkg|v:5.6.87"));
      oneOf(store).flush();
    }});

    Assert.assertNotNull(checker.checkChanges(desr, store));
    Assert.assertNull(checker.checkChanges(desr, store));

    m.assertIsSatisfied();
  }

  @Test
  public void test_check_should_not_trigger_twice_linux() {
    myIsWindows = false;
    m.checking(new Expectations(){{
      oneOf(chk).checkPackage(with(reqTC(null, "NUnit")));
      will(returnValue(CheckResult.succeeded(Arrays.asList(new SourcePackageInfo("src", "pkg", "5.6.87")))));

      oneOf(chk).checkPackage(with(reqTC(null, "NUnit")));
      will(returnValue(CheckResult.succeeded(Arrays.asList(new SourcePackageInfo("src", "pkg", "5.6.87")))));

      oneOf(store).getValue("hash"); will(returnValue("aaa"));
      oneOf(store).putValue("hash", "|s:src|p:pkg|v:5.6.87");
      oneOf(store).getValue("hash"); will(returnValue("|s:src|p:pkg|v:5.6.87"));
      oneOf(store).flush();
    }});

    Assert.assertNotNull(checker.checkChanges(desr, store));
    Assert.assertNull(checker.checkChanges(desr, store));

    m.assertIsSatisfied();
  }

  @Test
  public void test_check_should_not_trigger_after_error() {
    m.checking(new Expectations(){{
      oneOf(chk).checkPackage(with(req(nugetFakePath, null, "NUnit", null)));
      will(returnValue(CheckResult.succeeded(Arrays.asList(new SourcePackageInfo("src", "pkg", "5.6.87")))));

      oneOf(chk).checkPackage(with(req(nugetFakePath)));
      will(throwException(new RuntimeException("Failed to execute command")));

      oneOf(chk).checkPackage(with(req(nugetFakePath, null, "NUnit", null)));
      will(returnValue(CheckResult.succeeded(Arrays.asList(new SourcePackageInfo("src", "pkg", "5.6.87")))));

      oneOf(store).getValue("hash"); will(returnValue("aaa"));
      oneOf(store).putValue("hash", "|s:src|p:pkg|v:5.6.87");
      oneOf(store).getValue("hash"); will(returnValue("|s:src|p:pkg|v:5.6.87"));
      oneOf(store).flush();
    }});

    Assert.assertNotNull(checker.checkChanges(desr, store));
    try {
      checker.checkChanges(desr, store);
      Assert.fail("should throw an exception");
    } catch (BuildTriggerException e) {
      //NOP
    }
    Assert.assertNull(checker.checkChanges(desr, store));

    m.assertIsSatisfied();
  }

  @Test
  public void test_check_should_fail_on_error() {
    m.checking(new Expectations() {{
      oneOf(chk).checkPackage(with(req(nugetFakePath)));
      will(throwException(new RuntimeException("Failed to execute command")));
    }});
    try {
      checker.checkChanges(desr, store);
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
      checker.checkChanges(desr, store);
      Assert.fail("should throw an exception");
    } catch (BuildTriggerException e) {
      //NOP
    }

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
