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
import jetbrains.buildServer.nuget.server.exec.ListPackagesCommand;
import jetbrains.buildServer.nuget.server.exec.SourcePackageInfo;
import jetbrains.buildServer.nuget.server.trigger.NamedPackagesUpdateChecker;
import jetbrains.buildServer.nuget.server.trigger.TriggerConstants;
import jetbrains.buildServer.nuget.tests.integration.Paths;
import jetbrains.buildServer.serverSide.CustomDataStorage;
import junit.framework.Assert;
import org.jmock.Expectations;
import org.jmock.Mockery;
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
  private ListPackagesCommand cmd;
  private NamedPackagesUpdateChecker checker;
  private BuildTriggerDescriptor desr;
  private CustomDataStorage store;
  private Map<String, String> params;
  private File nugetFakePath;

  @BeforeMethod
  @Override
  protected void setUp() throws Exception {
    super.setUp();

    m = new Mockery();
    cmd = m.mock(ListPackagesCommand.class);
    desr = m.mock(BuildTriggerDescriptor.class);
    store = m.mock(CustomDataStorage.class);
    params = new TreeMap<String, String>();

    checker = new NamedPackagesUpdateChecker(cmd);

    m.checking(new Expectations(){{
      allowing(desr).getProperties(); will(returnValue(params));
    }});
    nugetFakePath = Paths.getNuGetRunnerPath();

    params.put(TriggerConstants.NUGET_EXE, nugetFakePath.getPath());
    params.put(TriggerConstants.PACKAGE, "NUnit");
  }

  @Test
  public void test_check_first_time_should_not_trigger() {
    m.checking(new Expectations(){{
      oneOf(cmd).checkForChanges(nugetFakePath, null, "NUnit", null);
      will(returnValue(Arrays.asList(new SourcePackageInfo("src", "pkg", "5.6.87"))));

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
      oneOf(cmd).checkForChanges(nugetFakePath, source, "Common", null);
      will(returnValue(Arrays.asList(new SourcePackageInfo(source, "Common", "1.0.0.21"))));

      oneOf(cmd).checkForChanges(nugetFakePath, source, "Common", null);
      will(returnValue(Arrays.asList(
              new SourcePackageInfo(source, "Common", "1.0.0.21"),
              new SourcePackageInfo(source, "Common", "2.0.0.22")
              )));

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
      oneOf(cmd).checkForChanges(nugetFakePath, source, "Common", null);
      will(returnValue(Arrays.asList(new SourcePackageInfo(source, "Common", "1.0.0.21"))));

      oneOf(cmd).checkForChanges(nugetFakePath, source, "Common", null);
      will(returnValue(Arrays.asList(
              new SourcePackageInfo(source, "Common", "1.0.0.21"),
              new SourcePackageInfo(source, "Common", "2.0.0.22")
              )));

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
      oneOf(cmd).checkForChanges(nugetFakePath, source, "Common", null);
      will(returnValue(Arrays.asList(
              new SourcePackageInfo(source, "Common", "1.0.0.21"),
              new SourcePackageInfo(null, "Common", "2.0.0.22"),
              new SourcePackageInfo("s2", "C3ommon", "2.0.0.22"),
              new SourcePackageInfo(null, "C3ommon", "2.0.0.22"),
              new SourcePackageInfo(null, "C3o4mmon", "2.0.0.22"),
              new SourcePackageInfo("s4", "C3o3mmon", "2.0.0.22"),
              new SourcePackageInfo(null, "C3omm5on", "2.0.0.22")
              )));

      final String hash1 = "|p:Common|v:2.0.0.22|s:\\\\ServerNameRemoved\\NugetTest\\Repository|p:Common|v:1.0.0.21|s:s2|p:C3ommon|v:2.0.0.22|s:s4|p:C3o3mmon|v:2.0.0.22|p:C3ommon|v:2.0.0.22|p:C3o4mmon|v:2.0.0.22|p:C3omm5on|v:2.0.0.22";
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
      oneOf(cmd).checkForChanges(nugetFakePath, null, "NUnit", null);
      will(returnValue(Arrays.asList(new SourcePackageInfo("src", "pkg", "5.6.87"))));

      oneOf(cmd).checkForChanges(nugetFakePath, null, "NUnit", null);
      will(returnValue(Arrays.asList(new SourcePackageInfo("src", "pkg", "5.6.87"))));

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
  public void test_check_should_fail_on_error() {
    m.checking(new Expectations() {{
      oneOf(cmd).checkForChanges(with(equal(nugetFakePath)), with(any(String.class)), with(any(String.class)), with(any(String.class)));
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
}
