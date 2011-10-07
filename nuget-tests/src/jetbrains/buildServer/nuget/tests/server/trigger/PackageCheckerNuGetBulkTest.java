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

import jetbrains.buildServer.nuget.server.exec.NuGetExecutionException;
import jetbrains.buildServer.nuget.server.exec.SourcePackageInfo;
import jetbrains.buildServer.nuget.server.exec.SourcePackageReference;
import jetbrains.buildServer.nuget.server.trigger.impl.CheckResult;
import jetbrains.buildServer.nuget.server.trigger.impl.CheckablePackage;
import jetbrains.buildServer.nuget.server.trigger.impl.PackageCheckRequest;
import jetbrains.buildServer.nuget.server.trigger.impl.PackageCheckerNuGetBulk;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 04.10.11 19:49
 */
public class PackageCheckerNuGetBulkTest extends PackageCheckerTestBase<PackageCheckerNuGetBulk> {
  @Override
  protected PackageCheckerNuGetBulk createChecker() {
    return new PackageCheckerNuGetBulk(myCommand, myCalculator, mySettings);
  }

  @Test
  public void test_available_01() throws IOException {
    m.checking(new Expectations() {{
      oneOf(mySettings).allowBulkMode(with(any(PackageCheckRequest.class)));
      will(returnValue(false));
    }});
    Assert.assertFalse(myChecker.accept(new PackageCheckRequest(nugetMode(), ref())));
  }

  @Test
  public void test_available_02() throws IOException {
    m.checking(new Expectations() {{
      oneOf(mySettings).allowBulkMode(with(any(PackageCheckRequest.class)));
      will(returnValue(true));
    }});
    Assert.assertTrue(myChecker.accept(new PackageCheckRequest(nugetMode(), ref())));
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

      oneOf(myCommand).checkForChanges(with(any(File.class)), with(col(ref))); will(returnValue(Collections.emptyMap()));
    }});

    myChecker.update(myExecutor, Arrays.asList(task));

    m.assertIsSatisfied();
  }

  @Test
  public void test_bulk_success() throws NuGetExecutionException {
    final SourcePackageReference ref = ref();
    final Collection<SourcePackageInfo> aaa = Arrays.asList(ref.toInfo("aaa"));
    final Map<SourcePackageReference, Collection<SourcePackageInfo>> result = Collections.singletonMap(ref, aaa);
    final CheckablePackage task = m.mock(CheckablePackage.class);
    m.checking(new Expectations(){{
      allowing(task).getPackage(); will(returnValue(ref));
      allowing(task).getMode(); will(returnValue(nugetMode()));

      oneOf(task).setExecuting();
      oneOf(task).setResult(CheckResult.succeeded(aaa));

      oneOf(myCommand).checkForChanges(with(any(File.class)), with(col(ref))); will(returnValue(result));
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

      oneOf(myCommand).checkForChanges(with(any(File.class)), with(col(ref))); will(throwException(new NuGetExecutionException("aaa")));
    }});

    myChecker.update(myExecutor, Arrays.asList(task));

    m.assertIsSatisfied();
  }

  private class Expectations extends org.jmock.Expectations {
    public Matcher<Collection<SourcePackageReference>> col(final SourcePackageReference... args) {
      return new BaseMatcher<Collection<SourcePackageReference>>() {
        public boolean matches(Object o) {
          final List<SourcePackageReference> c = new ArrayList<SourcePackageReference>((Collection<SourcePackageReference>) o);
          if (c.size() != args.length) return false;

          for (int i = 0; i < args.length; i++) {
            SourcePackageReference arg = args[i];
            if (!arg.equals(c.get(i))) return false;
          }
          return true;
        }

        public void describeTo(Description description) {
          description.appendValue(new ArrayList<SourcePackageReference>(Arrays.asList(args)));
        }
      };
    }
  }
}
