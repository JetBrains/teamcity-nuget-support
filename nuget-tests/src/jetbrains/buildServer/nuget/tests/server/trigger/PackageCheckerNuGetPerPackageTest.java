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

package jetbrains.buildServer.nuget.tests.server.trigger;

import jetbrains.buildServer.nuget.server.exec.ListPackagesResult;
import jetbrains.buildServer.nuget.server.exec.NuGetExecutionException;
import jetbrains.buildServer.nuget.server.exec.SourcePackageReference;
import jetbrains.buildServer.nuget.server.trigger.impl.CheckResult;
import jetbrains.buildServer.nuget.server.trigger.impl.CheckablePackage;
import jetbrains.buildServer.nuget.server.trigger.impl.PackageCheckRequest;
import jetbrains.buildServer.nuget.server.trigger.impl.checker.PackageCheckerNuGetPerPackage;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 04.10.11 20:52
 */
public class PackageCheckerNuGetPerPackageTest extends PackageCheckerTestBase<PackageCheckerNuGetPerPackage> {

  @Override
  protected PackageCheckerNuGetPerPackage createChecker() {
    return new PackageCheckerNuGetPerPackage(myCommand, mySettings);
  }

  @Test
  public void test_available_01() throws IOException {
    m.checking(new Expectations() {{
      oneOf(mySettings).allowBulkMode(with(any(PackageCheckRequest.class)));
      will(returnValue(false));
    }});
    Assert.assertTrue(myChecker.accept(new PackageCheckRequest(nugetMode(), ref())));

    m.assertIsSatisfied();
  }

  @Test
  public void test_available_01x() throws IOException {
    Assert.assertFalse(myChecker.accept(new PackageCheckRequest(javaMode(), ref())));
  }

  @Test
  public void test_available_02() throws IOException {
    m.checking(new Expectations() {{
      oneOf(mySettings).allowBulkMode(with(any(PackageCheckRequest.class)));
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

      oneOf(myCommand).checkForChanges(with(any(File.class)), with(equalL(ref))); will(returnValue(Collections.emptyMap()));
    }});

    myChecker.update(myExecutor, Arrays.asList(task));

    m.assertIsSatisfied();
  }

  @Test
  public void test_bulk_success() throws NuGetExecutionException {
    final SourcePackageReference ref = ref();
    final ListPackagesResult result = fromCollection(ref.toInfo("aaa"));

    final CheckablePackage task = m.mock(CheckablePackage.class);
    m.checking(new Expectations(){{
      allowing(task).getPackage(); will(returnValue(ref));
      allowing(task).getMode(); will(returnValue(nugetMode()));

      oneOf(task).setExecuting();
      oneOf(task).setResult(CheckResult.fromResult(result));

      oneOf(myCommand).checkForChanges(with(any(File.class)), with(equalL(ref))); will(returnValue(Collections.singletonMap(ref, result)));
    }});

    myChecker.update(myExecutor, Arrays.asList(task));

    m.assertIsSatisfied();
  }

  @Test
  public void test_bulk_errorMessage() throws NuGetExecutionException {
    final SourcePackageReference ref = ref();
    final ListPackagesResult result = fromError("some error ");

    final CheckablePackage task = m.mock(CheckablePackage.class);
    m.checking(new Expectations(){{
      allowing(task).getPackage(); will(returnValue(ref));
      allowing(task).getMode(); will(returnValue(nugetMode()));

      oneOf(task).setExecuting();
      oneOf(task).setResult(CheckResult.failed("some error "));

      oneOf(myCommand).checkForChanges(with(any(File.class)), with(equalL(ref))); will(returnValue(Collections.singletonMap(ref, result)));
    }});

    myChecker.update(myExecutor, Arrays.asList(task));

    m.assertIsSatisfied();
  }

  @Test
  public void test_bulk_empty_result() throws NuGetExecutionException {
    final ListPackagesResult result = fromCollection();
    final SourcePackageReference ref = ref();
    final CheckablePackage task = m.mock(CheckablePackage.class);
    m.checking(new Expectations(){{
      allowing(task).getPackage(); will(returnValue(ref));
      allowing(task).getMode(); will(returnValue(nugetMode()));

      oneOf(task).setExecuting();
      oneOf(task).setResult(with(empty()));

      oneOf(myCommand).checkForChanges(with(any(File.class)), with(equalL(ref))); will(returnValue(Collections.singletonMap(ref,result)));
    }});

    myChecker.update(myExecutor, Arrays.asList(task));

    m.assertIsSatisfied();
  }

  @Test
  public void test_bulk_error() throws NuGetExecutionException {
    final SourcePackageReference ref = ref();
    final CheckablePackage task = m.mock(CheckablePackage.class);
    m.checking(new Expectations(){{
      allowing(task).getPackage(); will(returnValue(ref));
      allowing(task).getMode(); will(returnValue(nugetMode()));

      oneOf(task).setExecuting();
      oneOf(task).setResult(CheckResult.failed("aaa"));

      oneOf(myCommand).checkForChanges(with(any(File.class)), with(equalL(ref))); will(throwException(new NuGetExecutionException("aaa")));
    }});

    myChecker.update(myExecutor, Arrays.asList(task));

    m.assertIsSatisfied();
  }
}
