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
import com.intellij.execution.configurations.GeneralCommandLine;
import jetbrains.buildServer.NetworkUtil;
import jetbrains.buildServer.util.FileUtil;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 03.11.11 15:05
 */
public class ServerRunner {
  private final File myTeamCityHome;

  private final File myTeamCityPluginsHome;
  private int myPort;
  private final String myContext = "c" + (System.nanoTime() % 999);


  public ServerRunner(@NotNull final File teamCityHome, @NotNull final File teamCityPluginsHome) {
    myTeamCityHome = teamCityHome;
    myTeamCityPluginsHome = teamCityPluginsHome;
  }

  public void startServer() throws IOException, ExecutionException, InterruptedException {
    myPort = NetworkUtil.getFreePort(1111);
    ServerXmlPatcher.patchServerConfig(getServerXml(), myPort);

    if (!new File(getHome(), "webapps/ROOT").renameTo(new File(getHome(), "webapps/" + myContext))) {
      throw new RuntimeException("Failed to rename web application home directory");
    }

    AgentConfigPatcher.patchAgentConfig(new File(getHome(), "/buildAgent/conf/buildAgent.properties"), getTeamCityUrl());
    final Process process = createRunAllCommand("start").createProcess();
    closeAll(process);
    process.waitFor();
  }

  private void closeAll(Process process) {
    FileUtil.closeAll(process.getInputStream(), process.getOutputStream(), process.getErrorStream());
  }

  public void stopServer() throws InterruptedException, ExecutionException {
    final Process process = createRunAllCommand("stop").createProcess();
    closeAll(process);
    process.waitFor();
  }

  @NotNull
  private GeneralCommandLine createRunAllCommand(@NotNull final String command) {
    final GeneralCommandLine cmd =  new GeneralCommandLine();

    final Map<String, String> env = new HashMap<String, String>(System.getenv());
    env.put("JAVA_HOME", System.getProperty("java.home"));
    env.put("TEAMCITY_SERVER_OPTS", "");
    env.put("TEAMCITY_DATA_PATH", myTeamCityPluginsHome.getPath());

    cmd.setEnvParams(env);
    cmd.setExePath("cmd.exe");
    cmd.addParameter("/c");
    cmd.addParameter(new File(getHome(), "bin/runall.bat").getPath());

    cmd.addParameter(command);
    return cmd;
  }

  @NotNull
  private File getServerXml() {
    return new File(getHome(), "conf/server.xml");
  }

  @NotNull
  private File getHome() {
    return myTeamCityHome;
  }

  @NotNull
  public String getTeamCityUrl() {
    return "http://localhost:" + myPort + "/" + myContext;
  }

}
