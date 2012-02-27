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

package jetbrains.buildServer.nuget.standalone.server;

import org.jetbrains.annotations.NotNull;
import org.odata4j.jersey.producer.server.JerseyServer;
import org.odata4j.producer.resources.RootApplication;

import java.io.IOException;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 29.01.12 23:18
 */
public class NuGetServerMain {
  private static ServerSettings outSettings = new DefaultSettings();
  
  @NotNull
  public static ServerSettings getSettings() {
    return outSettings;
  }
  
  public static void main(String[] args) throws IOException {
    System.out.println("NuGet Java Feed Server");
    System.out.println("");
    System.out.println("Starting...");

    try {
      outSettings = new CommandlineSettings(args);
    } catch (CommandlineException e) {
      System.out.println();
      System.err.println("Invalid commandline: " + e.getMessage());
      e.dumpUsage();
      System.exit(2);
      return;
    }

    final String appBaseUri = getSettings().getServerUrl();
    final JerseyServer server = new JerseyServer(
            appBaseUri,
            NuGetApplication.class,
            RootApplication.class);

    System.out.println("Starting packages index from: " + outSettings.getPackagesFolder());
    System.out.println("Starting server at " + outSettings.getServerUrl() + "...");

    startServer(server);
    System.out.println("Server started at " + outSettings.getServerUrl());
    System.out.println();
    System.out.println();
  }

  private static void startServer(@NotNull final JerseyServer server) {
    try {
      server.start();
    } catch (Exception e) {
      System.out.println("Failed to start server. " + e.getMessage());
      System.out.println();
      e.printStackTrace();
      System.exit(4);
    }
  }
}
