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

package jetbrains.buildServer.nuget.tests.integration;

import com.intellij.execution.ExecutionException;
import jetbrains.buildServer.BaseTestCase;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 03.11.11 16:08
 */
public class PingServerTest extends BaseTestCase {

  @Test
  public void test_start_server_and_agent() throws IOException, ExecutionException, InterruptedException {
    ServerRunner runner = new ServerRunner(new File("E:\\ref\\TeamCity.tmp"), createTempDir());

    runner.startServer();

    System.out.println("Started TeamCity at " + runner.getTeamCityUrl());
    System.in.read();

    runner.stopServer();
  }
}
