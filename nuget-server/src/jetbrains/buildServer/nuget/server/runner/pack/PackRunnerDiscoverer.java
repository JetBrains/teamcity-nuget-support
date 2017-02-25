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

package jetbrains.buildServer.nuget.server.runner.pack;

import jetbrains.buildServer.nuget.common.FeedConstants;
import jetbrains.buildServer.nuget.common.PackagesConstants;
import jetbrains.buildServer.nuget.server.tool.NuGetServerToolProvider;
import jetbrains.buildServer.serverSide.BuildTypeSettings;
import jetbrains.buildServer.serverSide.SBuildRunnerDescriptor;
import jetbrains.buildServer.serverSide.discovery.BreadthFirstRunnerDiscoveryExtension;
import jetbrains.buildServer.serverSide.discovery.DiscoveredObject;
import jetbrains.buildServer.tools.ToolVersionReference;
import jetbrains.buildServer.util.CollectionsUtil;
import jetbrains.buildServer.util.FileUtil;
import jetbrains.buildServer.util.browser.Browser;
import jetbrains.buildServer.util.browser.Element;
import jetbrains.buildServer.util.filters.Filter;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * @author Evgeniy.Koshkin
 */
public class PackRunnerDiscoverer extends BreadthFirstRunnerDiscoveryExtension {
  public static final String DEFAULT_OUT_DIR_PATH = "nuget-pack-out";

  @NotNull
  @Override
  protected List<DiscoveredObject> discoverRunnersInDirectory(@NotNull Element dir, @NotNull List<Element> filesAndDirs) {
    final List<String> foundNuSpecs = new ArrayList<String>();
    for(Element item : filesAndDirs){
      if(item.isLeaf() && item.getName().endsWith(FeedConstants.NUSPEC_FILE_EXTENSION) && item.isContentAvailable()){
        foundNuSpecs.add(item.getFullName());
      }
    }
    if(foundNuSpecs.isEmpty()) return Collections.emptyList();
    return Collections.singletonList(discover(getSpec(foundNuSpecs), getOutDirName(filesAndDirs)));
  }

  @NotNull
  @Override
  protected List<DiscoveredObject> postProcessDiscoveredObjects(@NotNull BuildTypeSettings settings, @NotNull Browser browser, @NotNull List<DiscoveredObject> discovered) {
    if(discovered.isEmpty()) return discovered;

    Set<String> configuredPaths = new HashSet<String>();
    for (SBuildRunnerDescriptor r: settings.getBuildRunners()) {
      if (r.getType().equals(PackagesConstants.PACK_RUN_TYPE)) {
        String path = r.getParameters().get(PackagesConstants.NUGET_PACK_SPEC_FILE);
        if (path != null) {
          configuredPaths.add(FileUtil.toSystemIndependentName(path));
        }
      }
    }
    if (configuredPaths.isEmpty()) return discovered;

    List<DiscoveredObject> res = new ArrayList<DiscoveredObject>();
    for (DiscoveredObject obj: discovered) {
      final String nuSpecPath = obj.getParameters().get(PackagesConstants.NUGET_PACK_SPEC_FILE);
      if (nuSpecPath != null && configuredPaths.contains(FileUtil.toSystemIndependentName(nuSpecPath))) continue;
      res.add(obj);
    }
    return res;
  }

  private String getOutDirName(Collection<Element> filesAndDirs) {
    return getOutDirPathRecursively(DEFAULT_OUT_DIR_PATH, filesAndDirs);
  }

  private String getOutDirPathRecursively(final String result, Collection<Element> filesAndDirs){
    final Element collision = CollectionsUtil.findFirst(filesAndDirs, new Filter<Element>() {
      public boolean accept(@NotNull Element data) {
        return data.getFullName().equalsIgnoreCase(result);
      }
    });
    if(collision == null) return result;
    return getOutDirPathRecursively(result + "-1", filesAndDirs);
  }

  private String getSpec(Collection<String> nuSpecs) {
    final StringBuilder sb = new StringBuilder();
    for(String nuSpecFilePath : nuSpecs){
      sb.append(nuSpecFilePath).append('\n');
    }
    return sb.toString().trim();
  }

  private DiscoveredObject discover(String nuSpecFilePath, String outDirName) {
    Map<String, String> parameters = NuGetPackRunnerDefaults.getRunnerProperties();
    parameters.put(PackagesConstants.NUGET_PATH, ToolVersionReference.getDefaultToolReference(NuGetServerToolProvider.NUGET_TOOL_TYPE.getType()).getReference());
    parameters.put(PackagesConstants.NUGET_PACK_SPEC_FILE, nuSpecFilePath);
    parameters.put(PackagesConstants.NUGET_PACK_OUTPUT_DIR, outDirName);
    return new DiscoveredObject(PackagesConstants.PACK_RUN_TYPE, parameters);
  }
}
