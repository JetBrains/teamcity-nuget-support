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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static jetbrains.buildServer.nuget.common.FeedConstants.NUGET_COMMANDLINE;

/**
 * Created 27.12.12 15:46
 *
 * @author Eugene Petrenko (eugene.petrenko@jetbrains.com)
 * @since v0.9
 */
public class NuGetToolReferenceUtils {
  private static final String TOOL_REFERENCE_PREFIX = "?";
  private static final String TOOL_DEFAULT_NAME = NUGET_COMMANDLINE + ".DEFAULT";

  @NotNull
  public static String getToolReference(@NotNull final String id) {
    return TOOL_REFERENCE_PREFIX + id;
  }

  public static boolean isToolReference(@Nullable final String toolPath) {
    return toolPath != null && toolPath.startsWith(TOOL_REFERENCE_PREFIX);
  }

  @Nullable
  public static String normalizeToolReference(@Nullable String toolPath) {
    final String referredToolId = getReferredToolId(toolPath);
    if(referredToolId == null) return toolPath;
    return getToolReference(referredToolId);
  }

  @Nullable
  public static String getReferredToolId(@Nullable final String toolPath) {
    if (toolPath == null || toolPath.length() == 0) return null;
    if (isToolReference(toolPath)) {
      return ToolIdUtils.normalizeToolId(toolPath.substring(TOOL_REFERENCE_PREFIX.length()));
    }
    return null;
  }

  public static boolean isDefaultToolReference(@Nullable String toolPath) {
    return getDefaultToolId().equals(getReferredToolId(toolPath));
  }

  @NotNull
  public static String getDefaultToolId() {
    return TOOL_DEFAULT_NAME;
  }

  @NotNull
  public static String getDefaultToolReference() {
    return getToolReference(getDefaultToolId());
  }
}
