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

package jetbrains.buildServer.nuget.common.index;

import com.intellij.openapi.diagnostic.Logger;
import jetbrains.buildServer.nuget.common.version.VersionUtility;
import jetbrains.buildServer.nuget.spec.Dependencies;
import jetbrains.buildServer.nuget.spec.DependencyGroup;
import jetbrains.buildServer.nuget.spec.FrameworkAssembly;
import jetbrains.buildServer.nuget.spec.NuspecFileContent;
import jetbrains.buildServer.serverSide.TeamCityProperties;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Evgeniy.Koshkin
 */
public class FrameworkConstraintsCalculator implements NuGetPackageStructureAnalyser {

  public static final String MATCH_FROM_ROOT_PROP ="teamcity.nuget.index.package.targetframework.matchfromroot";

  private static final Pattern SUBFOLDER_MATCHING_PATTERN = Pattern.compile("(content|lib|build|tools)\\/([^\\/]+)\\/.*");
  private static final Pattern ROOT_FOLDER_MATCHING_PATTERN = Pattern.compile("^(?:runtime\\/.+\\/)?(content|lib|build|tools)\\/([^\\/]+)\\/.*$");

  private static final String LIB_FOLDER = "lib";

  private static final Logger LOG = Logger.getInstance(FrameworkConstraintsCalculator.class.getName());

  private final Set<String> myConstraints = new HashSet<String>();

  @NotNull
  public Set<String> getPackageConstraints() {
    return myConstraints;
  }

  public void analyseNuspecFile(@NotNull NuspecFileContent nuspecContent) {
    final Collection<String> constraintsFromNuspec = extractConstraintsFromNuspec(nuspecContent);
    if(constraintsFromNuspec.isEmpty())
      LOG.debug(String.format("No framework constraints were extracted from .nuspec file for NuGet package %s %s", nuspecContent.getId(), nuspecContent.getVersion()));
    else
      myConstraints.addAll(constraintsFromNuspec);
  }

  public void analyseEntry(@NotNull String entryName) {
    final String targetFramework = extractTargetFrameworkFromReferenceFilePath(entryName);
    if (targetFramework != null)
      myConstraints.add(targetFramework);
  }

  @Nullable
  private String extractTargetFrameworkFromReferenceFilePath(String path) {
    final Matcher matcher = getFolderMatchingPattern().matcher(path);
    if(!matcher.find()) return null;
    boolean strictMode = matcher.group(1).equalsIgnoreCase(LIB_FOLDER);
    final String frameworkString = matcher.group(2);
    if(strictMode || VersionUtility.isKnownFramework(frameworkString))
      return frameworkString.toLowerCase();
    else
      return null;
  }

  @NotNull
  private Pattern getFolderMatchingPattern() {
    return TeamCityProperties.getBooleanOrTrue(MATCH_FROM_ROOT_PROP)
      ? ROOT_FOLDER_MATCHING_PATTERN
      : SUBFOLDER_MATCHING_PATTERN;
  }

  @NotNull
  private Collection<String> extractConstraintsFromNuspec(@NotNull NuspecFileContent nuspecContent) {
    final Collection<String> targetFrameworks = new HashSet<String>();
    for(FrameworkAssembly frameworkAssembly : nuspecContent.getFrameworkAssemblies()){
      final String targetFramework = frameworkAssembly.getTargetFramework();
      if(!StringUtil.isEmptyOrSpaces(targetFramework))
        targetFrameworks.add(targetFramework.toLowerCase());
    }
    final Dependencies dependencies = nuspecContent.getDependencies();
    if(dependencies != null){
      for(DependencyGroup dependencyGroup : dependencies.getGroups()){
        final String targetFramework = dependencyGroup.getTargetFramework();
        if(!StringUtil.isEmptyOrSpaces(targetFramework))
          targetFrameworks.add(targetFramework.toLowerCase());
      }
    }
    return targetFrameworks;
  }
}
