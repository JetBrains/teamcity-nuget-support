/*
 * Copyright 2000-2011 JetBrains s.r.o.
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

package jetbrains.buildServer.nuget.server.util;

import com.intellij.openapi.diagnostic.Logger;
import jetbrains.buildServer.util.Bitness;
import jetbrains.buildServer.util.Win32RegistryAccessor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 09.11.11 20:57
 */
public class SystemInfoImpl implements SystemInfo {
  private static final Logger LOG = Logger.getInstance(SystemInfoImpl.class.getName());
  
  private final Win32RegistryAccessor myRegistry;
  private Boolean myHasDotNetFramework;

  public SystemInfoImpl(@NotNull final Win32RegistryAccessor registry) {
    myRegistry = registry;
  }

  public boolean isWindows() {
    return com.intellij.openapi.util.SystemInfo.isWindows;
  }

  public boolean isDotNetFrameworkAvailable() {
    if (myHasDotNetFramework == null) {
      myHasDotNetFramework = checkDotNetFramework4Installed();
    }
    return myHasDotNetFramework;
  }

  private boolean checkDotNetFramework4Installed() {
    for (NetFrameworkInfo info : enumFrameworkVersions()) {
      LOG.debug("Detected .NET Framework " + info);
      if (info.getFullVersion().startsWith("4.0") && checkDotNetFrameworkFiles(info)) {
        LOG.debug("Found .NET Framework 4.0: " + info);
        return true;
      }
    }
    return false;
  }
  
  private boolean checkDotNetFrameworkFiles(@NotNull final NetFrameworkInfo info) {
    for (String name : new String[]{"msbuild.exe", "csc.exe", "mscorlib.dll", "MSBuild.exe", "System.Core.dll"}) {
      if (!new File(info.getPath(), name).isFile()) {
        LOG.debug("Detected corrupted .Net Framrwork " + info.getFullVersion() + " at " + info.getPath());
        return false;
      }
    }
    return true;
  }

  @Nullable
  private File getFrameworksInstallRoot(final Bitness arch) {
    return myRegistry.readRegistryFile(Win32RegistryAccessor.Hive.LOCAL_MACHINE, arch, "Software/Microsoft/.NetFramework", "InstallRoot");
  }

  @NotNull
  private List<NetFrameworkInfo> enumFrameworkVersions() {
    final String root = "Software/Microsoft/.NETFramework/Policy";
    final List<NetFrameworkInfo> fullVersions = new ArrayList<NetFrameworkInfo>();

    final Bitness bitness = Bitness.BIT32;

    final File installRoot = getFrameworksInstallRoot(bitness);
    if (installRoot == null) {
      return Collections.emptyList();
    }

    final Set<String> keys = myRegistry.listSubKeys(Win32RegistryAccessor.Hive.LOCAL_MACHINE, bitness, root);

    for (String key : keys) {
      if (!key.startsWith("v")) {
        continue;
      }

      for (String minorVersion : myRegistry.listValueNames(Win32RegistryAccessor.Hive.LOCAL_MACHINE, bitness, root + "/" + key)) {
        final String majorVersion = key.substring(1);
        final String fullVersion = majorVersion + "." + minorVersion;
        final File frameworkRoot = new File(installRoot, "v" + fullVersion);
        fullVersions.add(new NetFrameworkInfo(majorVersion, fullVersion, frameworkRoot, bitness));
      }
    }
    return fullVersions;
  }


  public class NetFrameworkInfo {
    private final String myMajorVersion;
    private final String myFullVersion;
    private final File myPath;
    private final Bitness myPlatform;

    public NetFrameworkInfo(@NotNull final String majorVersion,
                            @NotNull final String fullVersion,
                            @Nullable final File path,
                            @NotNull final Bitness platform) {
      myMajorVersion = majorVersion;
      myFullVersion = fullVersion;
      myPath = path;
      myPlatform = platform;
    }

    @NotNull
    public String getFullVersion() {
      return myFullVersion;
    }

    @Nullable
    public File getPath() {
      return myPath;
    }

    @Override
    public String toString() {
      return "NetFrameworkInfo{" +
              "myMajorVersion='" + myMajorVersion + '\'' +
              ", myFullVersion='" + myFullVersion + '\'' +
              ", myPath=" + myPath +
              ", myPlatform=" + myPlatform +
              '}';
    }
  }


}
