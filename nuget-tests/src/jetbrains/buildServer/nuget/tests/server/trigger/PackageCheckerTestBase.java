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
import jetbrains.buildServer.nuget.server.exec.SourcePackageReference;
import jetbrains.buildServer.nuget.server.trigger.impl.*;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.core.IsEqual;
import org.jmock.Mockery;
import org.jmock.api.Invocation;
import org.jmock.lib.action.CustomAction;
import org.testng.annotations.BeforeMethod;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutorService;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 04.10.11 21:07
 */
public abstract class PackageCheckerTestBase<T extends PackageChecker> extends BaseTestCase {
  protected Mockery m;
  protected ListPackagesCommand myCommand;
  protected NuGetPathCalculator myCalculator;
  protected PackageCheckerSettings mySettings;
  protected ExecutorService myExecutor;

  protected T myChecker;


  @BeforeMethod
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    m = new Mockery();
    myCommand = m.mock(ListPackagesCommand.class);
    myCalculator = m.mock(NuGetPathCalculator.class);
    mySettings = m.mock(PackageCheckerSettings.class);
    myExecutor = m.mock(ExecutorService.class);
    myChecker = createChecker();

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

  protected abstract T createChecker();


  protected SourcePackageReference ref() {
    return new SourcePackageReference("a","a", "a");
  }

  protected CheckRequestModeNuGet nugetMode() {
    return new CheckRequestModeNuGet(new File("bbb"));
  }

  protected Matcher<CheckResult> failed(final String... contains) {
    return new BaseMatcher<CheckResult>() {
      public boolean matches(Object o) {
        CheckResult r = (CheckResult) o;
        final String err = r.getError();
        if (err == null) return false;
        for (String contain : contains) {
          if (!err.contains(contain)) return false;
        }
        return true;
      }

      public void describeTo(Description description) {
        description.appendText("Failed, containing").appendValueList("", "", "", contains);
      }
    };
  }

  protected Matcher<CheckResult> equal(final CheckResult result) {
    return new IsEqual<CheckResult>(result);
  }

  protected static class Expectations extends org.jmock.Expectations {

    protected <T> Matcher<Collection<T>> equalL(final T t) {
      return new BaseMatcher<Collection<T>>() {
        public boolean matches(Object o) {
          if (o instanceof Collection) {
            Object i = ((Collection) o).iterator().next();
            return i.equals(t);
          }
          return false;
        }

        public void describeTo(Description description) {
          description.appendText("equals [" + t + "]");
        }
      };
    }

    protected <T> Matcher<Collection<T>> sz(Class<T> clazz, final int sz) {
      return new BaseMatcher<Collection<T>>() {
        public boolean matches(Object o) {
          Collection c = (Collection) o;
          return c.size() <= sz;
        }

        public void describeTo(Description description) {
          description.appendText("Collection size == ").appendValue(sz);
        }
      };
    }

    protected Matcher<Collection<SourcePackageReference>> col(final SourcePackageReference... args) {
      return col(Arrays.asList(args));
    }

    protected Matcher<Collection<SourcePackageReference>> col(final List<SourcePackageReference> _args) {
      final List<SourcePackageReference> args = new ArrayList<SourcePackageReference>(_args);
      return new BaseMatcher<Collection<SourcePackageReference>>() {
        public boolean matches(Object o) {
          @SuppressWarnings("unchecked")
          final List<SourcePackageReference> c = new ArrayList<SourcePackageReference>((Collection<SourcePackageReference>) o);
          if (c.size() != args.size()) return false;

          for (int i = 0; i < args.size(); i++) {
            SourcePackageReference arg = args.get(i);
            if (!arg.equals(c.get(i))) return false;
          }
          return true;
        }

        public void describeTo(Description description) {
          description.appendValue(args);
        }
      };
    }

  }

}
