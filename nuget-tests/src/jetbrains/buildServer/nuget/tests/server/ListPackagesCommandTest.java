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
import jetbrains.buildServer.nuget.server.exec.*;
import jetbrains.buildServer.nuget.server.exec.impl.ListPackagesCommandImpl;
import jetbrains.buildServer.nuget.server.exec.impl.ListPackagesCommandProcessor;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
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
    cmd = new ListPackagesCommandImpl(exec);
  }

  private void allowCommandLineCall(final String... cmd) {
    final List<String> list = new ArrayList<String>(Arrays.<String>asList(cmd));
    m.checking(new Expectations(){{
      oneOf(exec).executeNuGet(with(any(File.class)), with(equal(list)), with(any(ListPackagesCommandProcessor.class)));
      will(returnValue(Collections.<SourcePackageInfo>emptyList()));
    }});
  }

  @Test
  public void test_run_no_version() {
    allowCommandLineCall(
            "TeamCity.List",
            "-Source",
            "source",
            "-Id",
            "package"
    );

    cmd.checkForChanges(new File("nuget"), "source", "package", null);
    m.assertIsSatisfied();
  }

  @Test
  public void test_run_version() {
    allowCommandLineCall(
            "TeamCity.List",
            "-Source",
            "source",
            "-Id",
            "package",
            "-Version",
            "version"
    );

    cmd.checkForChanges(new File("nuget"), "source", "package", "version");
    m.assertIsSatisfied();
  }
}
