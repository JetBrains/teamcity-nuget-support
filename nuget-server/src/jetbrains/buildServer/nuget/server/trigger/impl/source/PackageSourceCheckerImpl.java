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

package jetbrains.buildServer.nuget.server.trigger.impl.source;

import com.intellij.openapi.diagnostic.Logger;
import jetbrains.buildServer.nuget.common.FeedConstants;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 18.07.12 17:30
 */
public class PackageSourceCheckerImpl implements PackageSourceChecker {
  private static final Logger LOG = Logger.getInstance(PackageSourceCheckerImpl.class.getName());

  @Nullable
  public String checkSource(@NotNull final String source) {
    //skip this as NuGet used to report errors for the case
    if (source.startsWith("http://") || source.startsWith("https://")) return null;

    try {
      final File root = new File(source);
      if (!root.isDirectory()) {
        return "Package feed does not exists or inaccessible";
      }
      File[] files = root.listFiles(FeedConstants.PACKAGE_FILE_FILTER);
      if (files == null || files.length == 0) {
        return "Package feed is empty or inaccessible";
      }
    } catch (final Throwable e) {
      LOG.warnAndDebugDetails("Failed to connect to " + source + ". " + e.getMessage(), e);
      return "Package feed is empty or inaccessible. " + e.getMessage();
    }
    return null;
  }
}
