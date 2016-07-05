/*
 * Copyright 2000-2014 JetBrains s.r.o.
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

package jetbrains.buildServer.nuget.server.runner.pack;

import jetbrains.buildServer.nuget.common.FeedConstants;
import jetbrains.buildServer.nuget.common.PackagesConstants;
import jetbrains.buildServer.nuget.common.PackagesPackDirectoryMode;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collection;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 22.08.11 20:32
 */
public class PackBean {
  public String getNuGetPathKey() { return PackagesConstants.NUGET_PATH; }
  public String getPackOutputDirectory() { return PackagesConstants.NUGET_PACK_OUTPUT_DIR; }
  public String getPackOutputClear() { return PackagesConstants.NUGET_PACK_OUTPUT_CLEAR; }
  public String getPackBaseDirectory() { return PackagesConstants.NUGET_PACK_BASE_DIR;}
  public String getPackBaseDirectoryMode() { return PackagesConstants.NUGET_PACK_BASE_DIRECTORY_MODE; }
  public String getPackVersion() { return PackagesConstants.NUGET_PACK_VERSION; }
  public String getPackSpecFile() { return PackagesConstants.NUGET_PACK_SPEC_FILE;}
  public String getPackExcludePatterns() { return PackagesConstants.NUGET_PACK_EXCLUDE_FILES; }
  public String getPackProperties() { return PackagesConstants.NUGET_PACK_PROPERTIES; }
  public String getPackCustomCommandline() { return PackagesConstants.NUGET_PACK_CUSOM_COMMANDLINE; }
  public String getPackAsTool() { return PackagesConstants.NUGET_PACK_AS_TOOL; }
  public String getPackSources() { return PackagesConstants.NUGET_PACK_INCLUDE_SOURCES; }
  public String getPackAsArtifact() { return PackagesConstants.NUGET_PACK_PUBLISH_ARTIFACT; }
  public String getPackPreferProject() { return PackagesConstants.NUGET_PACK_PREFER_PROJECT; }

  @NotNull
  public Collection<PackagesPackDirectoryMode> getPackBaseDirectoryModes() {
    return Arrays.asList(PackagesPackDirectoryMode.values());
  }

  public String getNugetToolTypeName() {return FeedConstants.NUGET_COMMANDLINE;}
}
