/*
 * Copyright 2000-2015 JetBrains s.r.o.
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

package jetbrains.buildServer.nuget.common;

import com.intellij.openapi.util.text.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Evgeniy.Koshkin
 */
public class ToolIdUtils {
  @NotNull
  public static String getVersionFromId(@NotNull final String id){
    if (id.toLowerCase().startsWith(FeedConstants.NUGET_COMMANDLINE.toLowerCase() + ".")) {
      return id.substring(FeedConstants.NUGET_COMMANDLINE.length() + 1);
    }
    return id;
  }

  @Nullable
  public static String normalizeToolId(@Nullable String toolId) {
    if(StringUtil.isEmptyOrSpaces(toolId)) return null;
    if(toolId.endsWith(FeedConstants.NUGET_EXTENSION)) return toolId.substring(0, toolId.length() - FeedConstants.NUGET_EXTENSION.length());
    return toolId;
  }
}
