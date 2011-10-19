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

package jetbrains.buildServer.nuget.server.feed.server.index;

import com.intellij.openapi.diagnostic.Logger;
import jetbrains.buildServer.serverSide.artifacts.BuildArtifact;
import jetbrains.buildServer.util.Dates;
import jetbrains.buildServer.util.FileUtil;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 06.09.11 22:03
 */
public class LocalNuGetPackageItemsFactory {
  private static final Logger LOG = Logger.getInstance(LocalNuGetPackageItemsFactory.class.getName());
  public static final String NS = "http://schemas.microsoft.com/packaging/2010/07/nuspec.xsd";

  @NotNull
  private String sha512(@NotNull final BuildArtifact file) throws PackageLoadException {
    InputStream is = null;
    try {
      is = new BufferedInputStream(file.getInputStream());
      return Base64.encodeBase64String(DigestUtils.sha512(is));
    } catch (IOException e) {
      throw new PackageLoadException("Failed to compute SHA-512 for " + file);
    } finally {
      FileUtil.close(is);
    }
  }

  @NotNull
  public Map<String, String> loadPackage(@NotNull final BuildArtifact nupkg) throws PackageLoadException {
    final String sha = sha512(nupkg);
    final long size = nupkg.getSize();
    final Date updated = new Date(nupkg.getTimestamp());

    final Element root = parseNuSpec(nupkg);
    if (root == null) {
      throw new PackageLoadException("Failed to fetch .nuspec from package");
    }

    Map<String, String> map = new LinkedHashMap<String, String>();

    final String id = parseProperty(root, "id");
    final String version = parseProperty(root, "version");
    final String authors = parseProperty(root, "authors");
    final String requireLicenseAcceptanse = parseProperty(root, "requireLicenseAcceptance");
    final String description = parseProperty(root, "description");
    final String summary = parseProperty(root, "summary");
    final String licenseUrl = parseProperty(root, "licenseUrl");
    final String projectUrl = parseProperty(root, "projectUrl");
    final String iconUrl = parseProperty(root, "iconUrl");
    final String dependencies = parseDependencies(root);
    final String tags = parseProperty(root,"tags");

    map.put("Id", id);
    map.put("Version", version);
    map.put("Title", id); //???
    //Prerelease
    map.put("Authors", authors);
    map.put("Summary", summary);
    map.put("Description", description);
    //map.put("Copyright", description);
    map.put("PackageHashAlgorithm", "SHA512");
    map.put("PackageHash", sha);
    map.put("PackageSize", String.valueOf(size));
    map.put("RequireLicenseAcceptance", requireLicenseAcceptanse);
    //isLatestVersion
    //releaseNotes
    map.put("ProjectUrl", projectUrl);
    map.put("LicenseUrl", licenseUrl);
    map.put("IconUrl", iconUrl);
    //categories
    map.put("Tags", tags);
    map.put("Dependencies", dependencies);
//    map.put("DetailsUrl", detailsUrl);
//    map.put("GalleryDetailsUrl", detailsUrl);
    map.put("Updated", formatDate(updated));

    return map;
  }

  @NotNull
  private String formatDate(@NotNull Date date) {
    //TODO:fix timezon printing
    return Dates.formatDate(date, "yyyy-MM-dd'T'HH:mm:ss'Z'", TimeZone.getTimeZone("GMT"));
  }

  @Nullable
  private String parseProperty(@NotNull final Element root, final @NotNull String name) {
    final Element child = getChild(getChild(root, "metadata"), name);
    return child == null ? null : child.getTextNormalize();
  }

  @Nullable
  private Element getChild(@Nullable final Element root, final String child) {
    if (root == null) return null;
    Element metadata = root.getChild(child);
    if (metadata != null) return metadata;
    return root.getChild(child, root.getNamespace(NS));
  }

  @NotNull
  private List<Element> getChildren(@Nullable final Element root, final String child) {
    if (root == null) return Collections.emptyList();
    List<Element> result = new ArrayList<Element>();
    for (List list : Arrays.asList(root.getChildren(child), root.getChildren(child, root.getNamespace(NS)))) {
      for (Object o : list) {
        result.add((Element)o);
      }
    }
    return result;
  }

  private String parseDependencies(@NotNull final Element root) {
    final Element metadata = getChild(root, "metadata");
    final Element dependencies = getChild(metadata, "dependencies");
    final StringBuilder sb = new StringBuilder();
    for (Object _dependency : getChildren(dependencies, "dependency")) {
      Element dep = (Element) _dependency;
      final String id = dep.getAttributeValue("id");
      final String versionConstraint = dep.getAttributeValue("version");
      if (sb.length() != 0) sb.append("|");
      sb.append(id).append(":").append(versionConstraint);
    }
    return sb.toString();
  }

  @Nullable
  private Element parseNuSpec(@NotNull final BuildArtifact nupkg) {
    ZipInputStream zos = null;
    try {
      zos = new ZipInputStream(new BufferedInputStream(nupkg.getInputStream()));
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
    }

    return null;
  }

  private void close(@Nullable final ZipInputStream zos) {
    if (zos != null) {
      try {
      zos.close();
      } catch (IOException e) {
        //NOP
      }
    }
  }
}
