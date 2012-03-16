/*
 * Copyright 2000-2012 JetBrains s.r.o.
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

import jetbrains.buildServer.BaseTestCase;
import jetbrains.buildServer.nuget.server.runner.install.PackagesInstallerRunType;
import jetbrains.buildServer.serverSide.InvalidProperty;
import jetbrains.buildServer.util.TestFor;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import junit.framework.Assert;
import org.jetbrains.annotations.NotNull;
import org.jmock.Mockery;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.*;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 16.03.12 13:38
 */
public class PackagesInstallerRunTypeTest extends BaseTestCase {
  private Mockery m;
  private PluginDescriptor myDescriptor;
  private PackagesInstallerRunType myRunType;

  @BeforeMethod
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    m = new Mockery();
    myDescriptor = m.mock(PluginDescriptor.class);
    myRunType = new PackagesInstallerRunType(myDescriptor);
  }

  private void doTestValidator(@NotNull Map<String, String> parameters, @NotNull Set<String> errors) {
    Collection<InvalidProperty> errs = myRunType.getRunnerPropertiesProcessor().process(parameters);

    Set<String> actualErrors = new TreeSet<String>();
    for (InvalidProperty err : errs) {
      actualErrors.add(err.getPropertyName());
    }

    Assert.assertEquals(new TreeSet<String>(errors), actualErrors);
  }


  @Test
  @TestFor(issues = "TW-20702")
  public void testParametersValidator_slnPath_reference() {
    doTestValidator(m("nuget.path", "fpo", "sln.path", "%solution%"), s());
  }

  @Test
  @TestFor(issues = "TW-20702")
  public void testParametersValidator_ok() {
    doTestValidator(m("nuget.path", "fpo", "sln.path", "file.sln"), s());
  }

  @Test
  @TestFor(issues = "TW-20702")
  public void testParametersValidator_not_sln() {
    doTestValidator(m("nuget.path", "fpo", "sln.path", "file.s"), s("sln.path"));
  }

  @Test
  @TestFor(issues = "TW-20702")
  public void testParametersValidator_empty() {
    doTestValidator(m(), s("nuget.path", "sln.path"));
  }

  @NotNull
  private Map<String, String> m(String... kvs) {
    Map<String, String> m = new TreeMap<String, String>();
    for(int i = 0; i < kvs.length; i+=2) {
      m.put(kvs[i], kvs[i+1]);
    }
    return m;
  }

  @NotNull
  private Set<String> s(String... kvs) {
    Set<String> m = new TreeSet<String>();
    Collections.addAll(m, kvs);
    return m;
  }

}
