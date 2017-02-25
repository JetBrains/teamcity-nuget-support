/*
 * Copyright 2000-2017 JetBrains s.r.o.
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

import jetbrains.buildServer.BaseTestCase;
import jetbrains.buildServer.nuget.server.tool.NuGetServerToolPreProcessor;
import jetbrains.buildServer.serverSide.ServerPaths;
import jetbrains.buildServer.tools.installed.ToolPaths;
import org.jmock.Mockery;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Created by Evgeniy.Koshkin.
 */
public class NuGetServerToolPreProcessorTest extends BaseTestCase {

  private Mockery m;
  private NuGetServerToolPreProcessor myToolPreProcessor;

  @Override
  @BeforeMethod
  public void setUp() throws Exception {
    super.setUp();
    m = new Mockery();
    final ToolPaths toolPaths = m.mock(ToolPaths.class);
    myToolPreProcessor = new NuGetServerToolPreProcessor(new ServerPaths(createTempDir()), toolPaths, null, null, null);
  }

  @Test
  public void testNoTools() throws Exception {
    myToolPreProcessor.doBeforeServerStartup();
    myToolPreProcessor.doAfterServerStartup();
  }

  @Test
  public void testSimple() throws Exception {
    fail();
  }
}
