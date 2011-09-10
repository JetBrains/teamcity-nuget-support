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

package jetbrains.buildServer.nuget.tests.integration.feed;

import com.intellij.execution.configurations.GeneralCommandLine;
import jetbrains.buildServer.nuget.tests.integration.IntegrationTestBase;
import jetbrains.buildServer.nuget.tests.integration.NuGet;
import jetbrains.buildServer.nuget.tests.integration.ProcessRunner;
import jetbrains.buildServer.util.SimpleHttpServerBase;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Collections;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 10.09.11 20:33
 */
public class NuGetFeedIntegrationTest extends IntegrationTestBase {

  @Test
  public void doTest() throws IOException {
    SimpleHttpServerBase serverBase = new SimpleHttpServerBase(){
      @Override
      protected Response getResponse(String requestString) {
        System.out.println("Request: " + requestString);

        return createStreamResponse(STATUS_LINE_500, Collections.<String>emptyList(), new byte[0]);
      }
    };

    final String baseUrl = "/feed";
    final NuGet nuget = NuGet.NuGet_1_5;

    serverBase.start();
    try {

      GeneralCommandLine cmd = new GeneralCommandLine();
      cmd.setExePath(nuget.getPath().getPath());
      cmd.addParameter("list");
      cmd.addParameter("-Source");
      cmd.addParameter("http://localhost:" + serverBase.getPort() + baseUrl);

      ProcessRunner.runProces(cmd);
    } finally {
      serverBase.stop();
    }
  }
}
