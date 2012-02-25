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

import java.io.File;
import java.io.IOException;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 29.01.12 23:18
 */
public class NuGetServerMain {
  private static File outRoot;
  
  @NotNull
  public static File getPackagesRoot() {
    return outRoot;
  }
  
  public static void main(String[] args) throws IOException {
    System.out.println("NuGet Feed server");
    System.out.println("");

    final String appBaseUri = "http://localhost:9878/";
    final JerseyServer server = new JerseyServer(
            appBaseUri,
            NuGetApplication.class,
            RootApplication.class);

    if (args.length >= 1) {
      outRoot = new File(args[0]).getCanonicalFile();
    } else {
      outRoot = new File(".").getCanonicalFile();
    }
    
    System.out.println("Starting packages index from: " + outRoot);
    
    server.start();
    System.out.println("Server started at " + appBaseUri);
  }

}
