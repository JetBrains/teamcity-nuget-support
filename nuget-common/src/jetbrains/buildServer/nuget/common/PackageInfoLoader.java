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

package jetbrains.buildServer.nuget.common;

import com.intellij.openapi.diagnostic.Logger;
import jetbrains.buildServer.nuget.common.nuspec.NuspecFileContent;
import jetbrains.buildServer.util.FileUtil;
import jetbrains.buildServer.util.StringUtil;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 15.06.12 16:34
 */
public class PackageInfoLoader {

  private static final Logger LOG = Logger.getInstance(PackageInfoLoader.class.getName());

  @NotNull
  public PackageInfo loadPackageInfo(@NotNull final File pkg) throws PackageLoadException {

    final Element root = parseNuSpec(pkg);
    if (root == null) {
      throw new PackageLoadException("Failed to fetch .nuspec from package");
    }

    final NuspecFileContent nuspec = new NuspecFileContent(root);

    final String id = nuspec.getId();
    final String version = nuspec.getVersion();

    if (id == null || StringUtil.isEmptyOrSpaces(id)) {
      throw new PackageLoadException("Invalid package. Failed to parse package Id for package: " + pkg.getPath());
    }

    if (version == null || StringUtil.isEmptyOrSpaces(version)) {
      throw new PackageLoadException("Invalid package. Failed to parse package Version for package: " + pkg.getPath());
    }

    return new PackageInfo(id, version);
  }

  @Nullable
  private static Element parseNuSpec(@NotNull final File nupkg) throws PackageLoadException {
    ZipInputStream zos = null;
    InputStream stream = null;
    try {
      stream = new FileInputStream(nupkg);
      zos = new ZipInputStream(new BufferedInputStream(stream));
      ZipEntry ze;
      while ((ze = zos.getNextEntry()) != null) {
        if (ze.getName().endsWith(".nuspec")) {
          try {
            return FileUtil.parseDocument(zos, false);
          } catch (JDOMException e) {
            LOG.warn("Failed to parse " + ze + " in " + nupkg);
          }
        }
      }
    } catch (IOException e) {
      LOG.warn("Failed to read " + nupkg + ". " + e.getMessage(), e);
    } finally {
      close(zos);
      FileUtil.close(stream);
    }

    return null;
  }

  private static void close(@Nullable final ZipInputStream zos) {
    if (zos != null) {
      try {
        zos.close();
      } catch (IOException e) {
        //NOP
      }
    }
  }
}
