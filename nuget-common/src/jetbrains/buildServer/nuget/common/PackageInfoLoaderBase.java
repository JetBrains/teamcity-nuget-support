/*
 * Copyright 2000-2012 JetBrains s.r.o.
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
import jetbrains.buildServer.util.FileUtil;
import jetbrains.buildServer.util.StringUtil;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 15.06.12 16:10
 */
public abstract class PackageInfoLoaderBase {
  private static final Logger LOG = Logger.getInstance(PackageInfoLoaderBase.class.getName());
  public static final String NS = "http://schemas.microsoft.com/packaging/2010/07/nuspec.xsd";

  @NotNull
  protected <T> T loadPackage(@NotNull PackageHolder holder, @NotNull PackageInfoLoader<T> loader) throws PackageLoadException {
    final Element root = parseNuSpec(holder);
    if (root == null) {
      throw new PackageLoadException("Failed to fetch .nuspec from package");
    }

    final String id = parseProperty(root, "id");
    final String version = parseProperty(root, "version");

    if (id == null || StringUtil.isEmptyOrSpaces(id)) {
      throw new PackageLoadException("Invalid package. Failed to parse package Id for package: " + holder.getPackageName());
    }

    if (version == null || StringUtil.isEmptyOrSpaces(version)) {
      throw new PackageLoadException("Invalid package. Failed to parse package Version for package: " + holder.getPackageName());
    }

    return loader.createPackageInfo(root, id, version);
  }

  protected interface PackageHolder {
    @NotNull
    String getPackageName();

    @NotNull
    InputStream openPackage() throws IOException, PackageLoadException;
  }

  protected interface PackageInfoLoader<T> {
    @NotNull
    T createPackageInfo(@NotNull Element nuspec, @NotNull final String id, @NotNull final String version) throws PackageLoadException;
  }

  @Nullable
  protected static String parseProperty(@NotNull final Element root, final @NotNull String name) {
    final Element child = getChild(getChild(root, "metadata"), name);
    return child == null ? null : child.getTextNormalize();
  }

  @Nullable
  protected static Element getChild(@Nullable final Element root, final String child) {
    if (root == null) return null;
    Element metadata = root.getChild(child);
    if (metadata != null) return metadata;
    return root.getChild(child, root.getNamespace(NS));
  }

  @NotNull
  protected static List<Element> getChildren(@Nullable final Element root, final String child) {
    if (root == null) return Collections.emptyList();
    List<Element> result = new ArrayList<Element>();
    for (List list : Arrays.asList(root.getChildren(child), root.getChildren(child, root.getNamespace(NS)))) {
      for (Object o : list) {
        result.add((Element)o);
      }
    }
    return result;
  }

  protected static void addParameter(@NotNull final Map<String, String> map,
                            @NotNull final String key,
                            @Nullable final String value) {
    if (!StringUtil.isEmptyOrSpaces(value)) {
      map.put(key, value);
    }
  }

  @Nullable
  private static Element parseNuSpec(@NotNull final PackageHolder nupkg) throws PackageLoadException {
    ZipInputStream zos = null;
    InputStream stream = null;
    try {
      stream = nupkg.openPackage();
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
