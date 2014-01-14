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
import jetbrains.buildServer.TempFolderProvider;
import jetbrains.buildServer.nuget.server.exec.*;
import jetbrains.buildServer.nuget.server.exec.impl.ListPackagesCommandImpl;
import jetbrains.buildServer.nuget.server.exec.impl.ListPackageCommandProcessor;
import jetbrains.buildServer.util.StringUtil;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.jetbrains.annotations.NotNull;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 14.07.11 13:27
 */
public class ListPackagesCommandTest extends BaseTestCase {
  private Mockery m;
  private NuGetExecutor exec;
  private ListPackagesCommand cmd;

  @BeforeMethod
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    m = new Mockery();
    exec = m.mock(NuGetExecutor.class);
    cmd = new ListPackagesCommandImpl(exec, new TempFolderProvider() {
      @NotNull
      public File getTempDirectory() {
        try {
          return createTempDir();
        } catch (IOException e) {
          throw new RuntimeException(e);
        }
      }
    });
  }

  private <T> void allowCommandLineCall(final T result, final String... cmd) throws NuGetExecutionException {
    final List<String> list = new ArrayList<String>(Arrays.<String>asList(cmd));
    m.checking(new Expectations(){{
      oneOf(exec).executeNuGet(with(any(File.class)), with(new BaseMatcher<List<String>>() {
        public boolean matches(Object o) {
          final List<String> entries = (List<String>) o;

          if (entries.size() != list.size()) return false;

          final Iterator<String> actual = entries.iterator();
          final Iterator<String> gold = entries.iterator();
          while(actual.hasNext() && gold.hasNext()) {
            final String gN = gold.next();
            final String aN = actual.next();
            if (gN == null && aN == null) return false;
            if (gN != null && !gN.equals(aN)) return false;
          }

          return actual.hasNext() == gold.hasNext();
        }

        public void describeTo(Description description) {
          description.appendText("Expected commandline: " + StringUtil.join(", ", list));
        }
      }), with(any(ListPackageCommandProcessor.class)));
      will(returnValue(result));
    }});
  }

  @Test
  public void test_run_packages() throws NuGetExecutionException  {
    allowCommandLineCall(Collections.emptyMap(),

            "TeamCity.ListPackages", "-Request", null, "-Response", null);
    cmd.checkForChanges(new File("nuget"), Arrays.asList(new SourcePackageReference("source", "package", "version"),new SourcePackageReference("source2", "package2", "version2")));
  }
}
