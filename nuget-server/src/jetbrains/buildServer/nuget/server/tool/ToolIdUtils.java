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

package jetbrains.buildServer.nuget.server.tool;

import jetbrains.buildServer.nuget.common.FeedConstants;
import jetbrains.buildServer.nuget.common.version.VersionUtility;
import jetbrains.buildServer.util.StringUtil;
import org.apache.commons.io.FilenameUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Evgeniy.Koshkin
 */
public class ToolIdUtils {

  private static Pattern NuGetPackageVersionPattern = Pattern.compile(
    String.format("%s\\.(.+)", FeedConstants.NUGET_COMMANDLINE),
    Pattern.CASE_INSENSITIVE
  );

  @NotNull
  public static String getPackageVersion(@NotNull File toolPackage) {
    final String toolPackageNameWithoutExtension = FilenameUtils.removeExtension(toolPackage.getName());
    final Matcher matcher = NuGetPackageVersionPattern.matcher(toolPackageNameWithoutExtension);
    if (matcher.matches()) {
      final String version = matcher.group(1);
      final String normalizedVersion = VersionUtility.normalizeVersion(version);
      return StringUtil.notEmpty(normalizedVersion, version);
    }
    return toolPackageNameWithoutExtension;
  }
}
