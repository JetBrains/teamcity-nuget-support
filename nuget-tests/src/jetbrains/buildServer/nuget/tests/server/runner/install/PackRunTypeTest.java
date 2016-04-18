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

package jetbrains.buildServer.nuget.tests.server.runner.install;

import jetbrains.buildServer.nuget.server.runner.pack.PackRunType;
import org.jetbrains.annotations.NotNull;
import org.testng.annotations.Test;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 16.03.12 18:59
 */
public class PackRunTypeTest extends NuGetRunTypeTest<PackRunType> {
  @NotNull
  @Override
  protected PackRunType createRunType() {
    return new PackRunType(myDescriptor, myToolManager, myFixture.getProjectManager());
  }

  @Test
  public void test_no_parameters() {
    doTestValidator(m(), s("nuget.pack.output.directory", "nuget.pack.specFile", "nuget.path"));
  }

  @Test
  public void test_all_parameters() {
    doTestValidator(
            m(
                    "nuget.pack.output.directory", "dir",
                    "nuget.pack.specFile", "spec",
                    "nuget.pack.version", "123.2",
                    "nuget.path", "foo.exe"),
            s());
  }

  @Test
  public void test_no_version() {
    doTestValidator(
            m(
                    "nuget.pack.output.directory", "dir",
                    "nuget.pack.specFile", "spec",
                    "nuget.path", "foo.exe"),
            s());
  }

  @Test
  public void test_no_output() {
    doTestValidator(
            m(
                    "nuget.pack.output.directory", "",
                    "nuget.pack.specFile", "fff",
                    "nuget.pack.version", "123.2",
                    "nuget.path", "foo.exe"),
            s("nuget.pack.output.directory"));
  }

  @Test
  public void test_no_spec() {
    doTestValidator(
            m(
                    "nuget.pack.output.directory", "dir",
                    "nuget.pack.specFile", "    ",
                    "nuget.pack.version", "123.2",
                    "nuget.path", "foo.exe"),
            s("nuget.pack.specFile"));
  }

  @Test
  public void test_no_nuget() {
    doTestValidator(
            m(
                    "nuget.pack.output.directory", "dir",
                    "nuget.pack.specFile", "spec",
                    "nuget.pack.version", "123.2",
                    "nuget.path", ""),
            s("nuget.path"));
  }

  @Test
  public void test_short_version() {
    doTestValidator(
            m(
                    "nuget.pack.output.directory", "dir",
                    "nuget.pack.specFile", "spec",
                    "nuget.pack.version", "1",
                    "nuget.path", "foo.exe"),
            s("nuget.pack.version"));
  }

  @Test
  public void test_sem_version() {
    doTestValidator(
            m(
                    "nuget.pack.output.directory", "dir",
                    "nuget.pack.specFile", "spec",
                    "nuget.pack.version", "1.2.3-zoo",
                    "nuget.path", "foo.exe"),
            s());
  }

  @Test
  public void test_ref_version() {
    doTestValidator(
            m(
                    "nuget.pack.output.directory", "dir",
                    "nuget.pack.specFile", "spec",
                    "nuget.pack.version", "%f1ff%",
                    "nuget.path", "foo.exe"),
            s());
  }


}
