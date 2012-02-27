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

import jetbrains.buildServer.util.FileUtil;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

/**
* Created by Eugene Petrenko (eugene.petrenko@gmail.com)
* Date: 27.02.12 11:45
*/
public class CommandlineSettings extends DefaultSettings {
  private final File myOutRoot;
  private final String myUrl;

  public CommandlineSettings(@NotNull String[] args) throws CommandlineException {
    if (args.length == 0) {
      throw new CommandlineException("No packages folder specified. ");
    }
    myOutRoot = FileUtil.getCanonicalFile(new File(args[0]));

    String url = super.getServerUrl();
    for (String arg : args) {
      if (arg.startsWith("/url:")) {
        final String pUrl = arg.substring(5);
        try {
          if (!"http".equalsIgnoreCase(new URL(pUrl).getProtocol())) {
            throw new CommandlineException("Server url must use HTTP protocol.");
          }
        } catch (MalformedURLException e) {
          throw new CommandlineException("Failed to parse server url: " + pUrl);
        }
        url = pUrl;
      }
    }

    myUrl = url;
  }

  public static void dumpUsage() {
    System.out.println("java -jar standalonge-nuget-feed.jar <packages folder> [/url:<server url>]");
    System.out.println(" where: ");
    System.out.println(" <packages folder>    is path to .nupkg files");
    System.out.println(" <server url>         is url to start server for, i.e. " + new DefaultSettings().getServerUrl());
    System.out.println();
  }

  @NotNull
  @Override
  public File getPackagesFolder() {
    return myOutRoot == null
            ? super.getPackagesFolder()
            : myOutRoot;
  }

  @Override
  public long getPackagesRefreshInterval() {
    return super.getPackagesRefreshInterval();
  }

  @NotNull
  @Override
  public String getServerUrl() {
    return myUrl;
  }
}
