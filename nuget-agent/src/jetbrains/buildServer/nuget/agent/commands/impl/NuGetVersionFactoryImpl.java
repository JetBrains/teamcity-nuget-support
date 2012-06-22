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

package jetbrains.buildServer.nuget.agent.commands.impl;

import com.intellij.openapi.diagnostic.Logger;
import jetbrains.buildServer.nuget.agent.commands.NuGetVersion;
import jetbrains.buildServer.util.FileUtil;
import jetbrains.buildServer.util.VersionComparatorUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 22.06.12 17:53
 */
public class NuGetVersionFactoryImpl implements NuGetVersionFactory {
  private static final Logger LOG = Logger.getInstance(NuGetVersionFactoryImpl.class.getName());

  @NotNull
  private NuGetVersion getUnknownVersion() {
    return new NuGetVersion() {
      public boolean supportAuth() {
        return false;
      }

      public boolean supportInstallNoCache() {
        return false;
      }
    };
  }

  @NotNull
  public NuGetVersion getFromVersionFile(@Nullable File versionFile) {
    if (versionFile == null || !versionFile.isFile()) return getUnknownVersion();

    try {
      final List<String> file = FileUtil.readFile(versionFile);
      LOG.debug("Empty NuGet version file");
      if (file.isEmpty()) return getUnknownVersion();

      final String version = file.get(0).trim();

      return new NuGetVersion() {
        public boolean supportAuth() {
          return VersionComparatorUtil.compare(version, "2.0") > 0;
        }

        public boolean supportInstallNoCache() {
          return VersionComparatorUtil.compare(version, "1.7") > 0;
        }
      };
    } catch (IOException e) {
      LOG.warn("Failed to read version file. " + e.getMessage(), e);
      return getUnknownVersion();
    }
  }
}
