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

package jetbrains.buildServer.nuget.tests.server.trigger;

import jetbrains.buildServer.BaseTestCase;
import jetbrains.buildServer.nuget.server.exec.ListPackagesCommand;
import jetbrains.buildServer.nuget.server.exec.NuGetExecutionException;
import jetbrains.buildServer.nuget.server.exec.SourcePackageInfo;
import jetbrains.buildServer.nuget.server.exec.SourcePackageReference;
import jetbrains.buildServer.nuget.server.trigger.impl.*;
import jetbrains.buildServer.util.TimeService;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.api.Invocation;
import org.jmock.lib.action.CustomAction;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.ExecutorService;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 04.10.11 20:52
 */
public class PackageCheckerNuGetPerPackageTest extends BaseTestCase {

  private Mockery m;
  private ListPackagesCommand myCommand;
  private NuGetPathCalculator myCalculator;
  private PackageCheckerSettings mySettings;
  private PackageCheckerNuGetPerPackage myChecker;
  private ExecutorService myExecutor;
  private TimeService myTime;

  @BeforeMethod
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    m = new Mockery();
    myCommand = m.mock(ListPackagesCommand.class);
    myCalculator = m.mock(NuGetPathCalculator.class);
    mySettings = m.mock(PackageCheckerSettings.class);
    myExecutor = m.mock(ExecutorService.class);
    myChecker = new PackageCheckerNuGetPerPackage(myCommand, myCalculator, mySettings);

    m.checking(new Expectations(){{
      allowing(myCalculator).getNuGetPath(with(any(CheckRequestMode.class))); will(returnValue(new File("aaa")));
      allowing(myExecutor).submit(with(any(Runnable.class))); will(new CustomAction("Execute task in same thread") {
        public Object invoke(Invocation invocation) throws Throwable {
          Runnable action = (Runnable) invocation.getParameter(0);
          action.run();
          return null;
        }
      });
    }});
  }

  @Test
  public void test_available_01() throws IOException {
    m.checking(new Expectations() {{
      oneOf(mySettings).alowBulkMode(with(any(PackageCheckRequest.class)));
      will(returnValue(false));
    }});
    Assert.assertTrue(myChecker.accept(new PackageCheckRequest(nugetMode(), ref())));

    m.assertIsSatisfied();
  }

  @Test
  public void test_available_02() throws IOException {
    m.checking(new Expectations() {{
      oneOf(mySettings).alowBulkMode(with(any(PackageCheckRequest.class)));
      will(returnValue(true));
    }});
    Assert.assertFalse(myChecker.accept(new PackageCheckRequest(nugetMode(), ref())));

    m.assertIsSatisfied();
  }


  @Test
  public void test_bulk() throws NuGetExecutionException {
    final SourcePackageReference ref = ref();
    final CheckablePackage task = m.mock(CheckablePackage.class);
    m.checking(new Expectations(){{
      allowing(task).getPackage(); will(returnValue(ref));
      allowing(task).getMode(); will(returnValue(nugetMode()));

      oneOf(task).setExecuting();
      oneOf(task).setResult(with(any(CheckResult.class)));

      oneOf(myCommand).checkForChanges(with(any(File.class)), with(equal(ref))); will(returnValue(Collections.emptyList()));
    }});

    myChecker.update(myExecutor, Arrays.asList(task));

    m.assertIsSatisfied();
  }

  @Test
  public void test_bulk_success() throws NuGetExecutionException {
    @SuppressWarnings({"unchecked"})
    final Collection<SourcePackageInfo> result = m.mock(Collection.class);
    final SourcePackageReference ref = ref();
    final CheckablePackage task = m.mock(CheckablePackage.class);
    m.checking(new Expectations(){{
      allowing(task).getPackage(); will(returnValue(ref));
      allowing(task).getMode(); will(returnValue(nugetMode()));

      oneOf(task).setExecuting();
      oneOf(task).setResult(CheckResult.succeeded(result));

      oneOf(myCommand).checkForChanges(with(any(File.class)), with(equal(ref))); will(returnValue(result));
    }});

    myChecker.update(myExecutor, Arrays.asList(task));

    m.assertIsSatisfied();
  }

  @Test
  public void test_bulk_error() throws NuGetExecutionException {
    @SuppressWarnings({"unchecked"})
    final SourcePackageReference ref = ref();
    final CheckablePackage task = m.mock(CheckablePackage.class);
    m.checking(new Expectations(){{
      allowing(task).getPackage(); will(returnValue(ref));
      allowing(task).getMode(); will(returnValue(nugetMode()));

      oneOf(task).setExecuting();
      oneOf(task).setResult(CheckResult.failed("aaa"));

      oneOf(myCommand).checkForChanges(with(any(File.class)), with(equal(ref))); will(throwException(new NuGetExecutionException("aaa")));
    }});

    myChecker.update(myExecutor, Arrays.asList(task));

    m.assertIsSatisfied();
  }

  private SourcePackageReference ref() {
    return new SourcePackageReference("a","a", "a");
  }

  private CheckRequestModeNuGet nugetMode() {
    return new CheckRequestModeNuGet(new File("bbb"));
  }

}
