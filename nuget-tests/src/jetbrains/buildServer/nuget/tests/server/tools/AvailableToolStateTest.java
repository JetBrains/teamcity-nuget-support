/*
 * Copyright 2000-2015 JetBrains s.r.o.
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

package jetbrains.buildServer.nuget.tests.server.tools;

import com.google.common.collect.Lists;
import jetbrains.buildServer.BaseTestCase;
import jetbrains.buildServer.nuget.server.toolRegistry.FetchException;
import jetbrains.buildServer.nuget.server.toolRegistry.NuGetTool;
import jetbrains.buildServer.nuget.server.toolRegistry.ToolsPolicy;
import jetbrains.buildServer.nuget.server.toolRegistry.impl.AvailableToolsState;
import jetbrains.buildServer.nuget.server.toolRegistry.impl.impl.AvailableToolsFetcher;
import jetbrains.buildServer.nuget.server.toolRegistry.impl.impl.AvailableToolsStateImpl;
import jetbrains.buildServer.util.TestFor;
import jetbrains.buildServer.util.TimeService;
import org.jetbrains.annotations.NotNull;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Iterator;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com), Evgeniy Koshkin (evgeniy.koshkin@jetbrains.com)
 *         Date: 29.08.11 18:55
 */
public class AvailableToolStateTest extends BaseTestCase {
  private Mockery m;

  @BeforeMethod
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    m = new Mockery();

  }

  @Override
  @AfterMethod
  public void tearDown() throws Exception {
    m.assertIsSatisfied();
    super.tearDown();
  }

  @Test
  @TestFor(issues = "TW-30395")
  public void test_should_return_newer_first() throws IOException, FetchException {
    final AvailableToolsFetcher fetcher1 = m.mock(AvailableToolsFetcher.class, "fetcher1");
    final AvailableToolsFetcher fetcher2 = m.mock(AvailableToolsFetcher.class, "fetcher2");
    final TimeService time = m.mock(TimeService.class);

    m.checking(new Expectations(){{
      allowing(time).now(); will(returnValue(1000234L));
      allowing(fetcher1).fetchAvailable(); will(returnValue(Lists.newArrayList(toolOfVersion("1"), toolOfVersion("5"), toolOfVersion("3"))));
      allowing(fetcher2).fetchAvailable(); will(returnValue(Lists.newArrayList(toolOfVersion("6"), toolOfVersion("2"), toolOfVersion("4"))));
    }});

    final AvailableToolsState state = new AvailableToolsStateImpl(time, Lists.newArrayList(fetcher1, fetcher2));

    Iterator<? extends NuGetTool> tools = state.getAvailable(ToolsPolicy.FetchNew).iterator();
    assertEquals("6", tools.next().getVersion());
    assertEquals("5", tools.next().getVersion());
    assertEquals("4", tools.next().getVersion());
    assertEquals("3", tools.next().getVersion());
    assertEquals("2", tools.next().getVersion());
    assertEquals("1", tools.next().getVersion());
    assertFalse(tools.hasNext());
  }

  @Test
  public void test_find_tool() throws Exception {
    fail();
  }

  private static NuGetTool toolOfVersion(final String version){
    return new NuGetTool() {
      @NotNull
      public String getId() {
        return "nuget-exe-" + version;
      }

      @NotNull
      public String getVersion() {
        return version;
      }
    };
  }
}
