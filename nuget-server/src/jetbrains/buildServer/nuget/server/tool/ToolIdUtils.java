/*
 * Copyright 2000-2016 JetBrains s.r.o.
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

package jetbrains.buildServer.nuget.server.tool;

import com.intellij.openapi.util.text.StringUtil;
import jetbrains.buildServer.nuget.common.FeedConstants;
import org.apache.commons.io.FilenameUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;

/**
 * @author Evgeniy.Koshkin
 */
public class ToolIdUtils {
  @NotNull
  public static String getIdForPackage(@NotNull File toolPackage) {
    return FilenameUtils.removeExtension(toolPackage.getName());
  }

  @NotNull
  public static String getPackageVersion(@NotNull File toolPackage) {
    final String toolPackageNameWithoutExtension = FilenameUtils.removeExtension(toolPackage.getName());
    if (toolPackageNameWithoutExtension.startsWith(FeedConstants.NUGET_COMMANDLINE + ".")) {
      return toolPackageNameWithoutExtension.substring(FeedConstants.NUGET_COMMANDLINE.length() + 1);
    }
    return toolPackageNameWithoutExtension;
  }
}
