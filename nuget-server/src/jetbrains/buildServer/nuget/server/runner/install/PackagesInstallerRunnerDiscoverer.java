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

package jetbrains.buildServer.nuget.server.runner.install;

import jetbrains.buildServer.nuget.common.NuGetTools;
import jetbrains.buildServer.nuget.common.PackagesConstants;
import jetbrains.buildServer.serverSide.BuildTypeSettings;
import jetbrains.buildServer.serverSide.SBuildRunnerDescriptor;
import jetbrains.buildServer.serverSide.discovery.BreadthFirstRunnerDiscoveryExtension;
import jetbrains.buildServer.serverSide.discovery.DiscoveredObject;
import jetbrains.buildServer.util.CollectionsUtil;
import jetbrains.buildServer.util.Converter;
import jetbrains.buildServer.util.FileUtil;
import jetbrains.buildServer.util.browser.Browser;
import jetbrains.buildServer.util.browser.Element;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * @author Evgeniy.Koshkin
 */
public class PackagesInstallerRunnerDiscoverer extends BreadthFirstRunnerDiscoveryExtension {

  private static final String PACKAGES_CONFIG = "packages.config";
  private static final String NUGET_DIR_NAME = ".nuget";
  private static final String SLN_FILE_EXTENSION = ".sln";
  public static final String CHECKED = "checked";

  @NotNull
  @Override
  protected List<DiscoveredObject> discoverRunnersInDirectory(@NotNull Element dir, @NotNull List<Element> filesAndDirs) {
    List<String> foundSlns = new ArrayList<String>();
    boolean nugetUsageFound = false;

    for(Element item : filesAndDirs){
      final String name = item.getName();
      final boolean isLeaf = item.isLeaf();

      if(isLeaf && name.endsWith(SLN_FILE_EXTENSION) && item.isContentAvailable())
        foundSlns.add(item.getFullName());

      if(nugetUsageFound) continue;
      nugetUsageFound = (isLeaf && name.equalsIgnoreCase(PACKAGES_CONFIG)) || (!isLeaf && name.equalsIgnoreCase(NUGET_DIR_NAME));
    }

    if (foundSlns.isEmpty() || !nugetUsageFound) return Collections.emptyList();

    return CollectionsUtil.convertCollection(foundSlns, new Converter<DiscoveredObject, String>() {
      public DiscoveredObject createFrom(@NotNull String source) {
        return discover(source);
      }
    });
  }

  @NotNull
  @Override
  protected List<DiscoveredObject> postProcessDiscoveredObjects(@NotNull BuildTypeSettings settings, @NotNull Browser browser, @NotNull List<DiscoveredObject> discovered) {
    if(discovered.isEmpty()) return discovered;

    Set<String> configuredPaths = new HashSet<String>();
    for (SBuildRunnerDescriptor r: settings.getBuildRunners()) {
      if (r.getType().equals(PackagesConstants.INSTALL_RUN_TYPE)) {
        String path = r.getParameters().get(PackagesConstants.SLN_PATH);
        if (path != null) {
          configuredPaths.add(FileUtil.toSystemIndependentName(path));
        }
      }
    }
    if (configuredPaths.isEmpty()) return discovered;

    List<DiscoveredObject> res = new ArrayList<DiscoveredObject>();
    for (DiscoveredObject obj: discovered) {
      final String slnPath = obj.getParameters().get(PackagesConstants.SLN_PATH);
      if (slnPath != null && configuredPaths.contains(FileUtil.toSystemIndependentName(slnPath))) continue;
      res.add(obj);
    }
    return res;
  }

  private DiscoveredObject discover(String slnPath) {
    Map<String, String> parameters = new HashMap<String, String>();
    parameters.put(PackagesConstants.NUGET_PATH, NuGetTools.getDefaultToolPath());
    parameters.put(PackagesConstants.SLN_PATH, slnPath);
    parameters.put(PackagesConstants.NUGET_USE_RESTORE_COMMAND, CHECKED);
    return new DiscoveredObject(PackagesConstants.INSTALL_RUN_TYPE, parameters);
  }
}
