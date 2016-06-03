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

import jetbrains.buildServer.nuget.server.runner.NuGetRunType;
import jetbrains.buildServer.serverSide.InvalidProperty;
import jetbrains.buildServer.serverSide.impl.BaseServerTestCase;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import junit.framework.Assert;
import org.jetbrains.annotations.NotNull;
import org.jmock.Mockery;
import org.testng.annotations.BeforeMethod;

import java.util.*;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 16.03.12 18:54
 */
public abstract class NuGetRunTypeTest<TRunType extends NuGetRunType> extends BaseServerTestCase {
  protected Mockery m;
  protected PluginDescriptor myDescriptor;
  protected NuGetToolManager myToolManager;
  protected TRunType myRunType;

  @BeforeMethod
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    m = new Mockery();
    myDescriptor = m.mock(PluginDescriptor.class);
    myToolManager = m.mock(NuGetToolManager.class);
    myRunType = createRunType();
  }

  @NotNull
  protected abstract TRunType createRunType();

  protected void doTestValidator(@NotNull Map<String, String> parameters, @NotNull Set<String> errors) {
    Collection<InvalidProperty> errs = myRunType.getRunnerPropertiesProcessor().process(parameters);

    Set<String> actualErrors = new TreeSet<String>();
    for (InvalidProperty err : errs) {
      actualErrors.add(err.getPropertyName());
    }

    Assert.assertEquals(new TreeSet<String>(errors), actualErrors);
  }

  @NotNull
  protected Map<String, String> m(String... kvs) {
    Map<String, String> m = new TreeMap<String, String>();
    for(int i = 0; i < kvs.length; i+=2) {
      m.put(kvs[i], kvs[i+1]);
    }
    return m;
  }

  @NotNull
  protected Set<String> s(String... kvs) {
    Set<String> m = new TreeSet<String>();
    Collections.addAll(m, kvs);
    return m;
  }

}
