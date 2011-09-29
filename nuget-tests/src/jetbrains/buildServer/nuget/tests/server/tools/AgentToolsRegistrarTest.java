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

package jetbrains.buildServer.nuget.tests.server.tools;

import com.intellij.openapi.util.SystemInfo;
import jetbrains.buildServer.BaseTestCase;
import jetbrains.buildServer.nuget.server.toolRegistry.impl.NuGetAgentToolHolder;
import jetbrains.buildServer.nuget.server.toolRegistry.impl.ToolPaths;
import jetbrains.buildServer.nuget.server.toolRegistry.impl.ToolPathsImpl;
import jetbrains.buildServer.serverSide.ServerPaths;
import jetbrains.buildServer.util.FileUtil;
import jetbrains.buildServer.util.TestFor;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 22.09.11 11:48
 */
@TestFor(issues = "TW-18271")
public class AgentToolsRegistrarTest extends BaseTestCase {

  @Test
  public void testPath() throws IOException {
    if (!SystemInfo.isWindows) return;

    final File home = new File(createTempDir(), "aAbB");

    final ToolPaths paths = new ToolPathsImpl(new ServerPaths(home.getPath().toUpperCase()));
    NuGetAgentToolHolder h = new NuGetAgentToolHolder(paths);

    FileUtil.writeFile(new File(paths.getAgentPluginsPath(), "plugin.zip"), "this is a plugin");

    Assert.assertNotNull(h.getPluginFile("plugin.zip"));
  }
}
