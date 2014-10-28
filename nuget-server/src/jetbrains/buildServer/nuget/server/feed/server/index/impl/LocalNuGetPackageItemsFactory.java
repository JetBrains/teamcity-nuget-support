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

package jetbrains.buildServer.nuget.server.feed.server.index.impl;

import com.intellij.openapi.util.text.StringUtil;
import jetbrains.buildServer.nuget.common.PackageInfoLoaderBase;
import jetbrains.buildServer.nuget.common.PackageLoadException;
import jetbrains.buildServer.serverSide.artifacts.BuildArtifact;
import jetbrains.buildServer.util.FileUtil;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import static jetbrains.buildServer.nuget.server.feed.server.PackageAttributes.*;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 06.09.11 22:03
 */
public class LocalNuGetPackageItemsFactory extends PackageInfoLoaderBase {

  @NotNull
  public Map<String, String> loadPackage(@NotNull final BuildArtifact nupkg,
                                         @NotNull final Date finishDate) throws PackageLoadException {
    return loadPackage(
            new PackageHolder() {
              @NotNull
              public String getPackageName() {
                return nupkg.toString();
              }

              @NotNull
              public InputStream openPackage() throws IOException, PackageLoadException {
                return nupkg.getInputStream();
              }
            },
            new PackageInfoLoader<Map<String, String>>() {
              @NotNull
              public Map<String, String> createPackageInfo(@NotNull final Element root,
                                                           @NotNull final String id,
                                                           @NotNull final String version) throws PackageLoadException {
                Map<String, String> map = new LinkedHashMap<String, String>();

                //The list is generated from
                //JetBrains.TeamCity.NuGet.Feed.Tests.DumpRequiredPackageParameters2()
                //not included here: addParameter(map, "TeamCityDownloadUrl", "");
                addParameter(map, ID, id);
                addParameter(map, VERSION, version);
                addParameter(map, "Title", parseProperty(root, "title"));
                addParameter(map, RELEASE_NOTES, parseProperty(root, "releaseNotes"));
                addParameter(map, AUTHORS, parseProperty(root, "authors"));
                addParameter(map, DEPENDENCIES, parseDependencies(root));
                addParameter(map, DESCRIPTION, parseProperty(root, "description"));
                addParameter(map, COPYRIGHT, parseProperty(root, "copyright"));
                addParameter(map, PROJECT_URL, parseProperty(root, "projectUrl"));
                addParameter(map, TAGS, parseProperty(root, "tags"));
                addParameter(map, ICON_URL, parseProperty(root, "iconUrl"));
                addParameter(map, LICENSE_URL, parseProperty(root, "licenseUrl"));
                addParameter(map, REQUIRE_LICENSE_ACCEPTANCE, parseProperty(root, "requireLicenseAcceptance"));
                addParameter(map, PACKAGE_HASH, sha512(nupkg));
                addParameter(map, PACKAGE_HASH_ALGORITHM, "SHA512");
                addParameter(map, PACKAGE_SIZE, String.valueOf(nupkg.getSize()));
                //addParameter(map, "IsLatestVersion", "");
                addParameter(map, LAST_UPDATED, ODataDataFormat.formatDate(finishDate));
                //addParameter(map, "Updated", formatDate(updated));
                addParameter(map, MIN_CLIENT_VERSION, parseMetadataAttribute(root, "minClientVersion"));

                return map;
              }

              @Nullable
              private String parseMetadataAttribute(@NotNull final Element root, @NotNull final String attribute) {
                final Element metadata = getChild(root, "metadata");
                if (metadata == null) return null;
                return metadata.getAttributeValue(attribute);
              }

              @NotNull
              private String parseDependencies(@NotNull final Element root) {
                final Element metadata = getChild(root, "metadata");
                final Element dependencies = getChild(metadata, "dependencies");
                if (dependencies == null) return "";
                final StringBuilder sb = new StringBuilder();
                processDependencies(dependencies, null, sb);

                for (Element group : getChildren(dependencies, "group")) {
                  processDependencies(group, group.getAttributeValue("targetFramework"), sb);
                }

                return sb.toString();
              }

              private void processDependencies(@NotNull final Element dependencies,
                                               @Nullable final String platform,
                                               @NotNull final StringBuilder sb) {
                for (Element dep : getChildren(dependencies, "dependency")) {
                  final String id = dep.getAttributeValue("id");
                  final String versionConstraint = dep.getAttributeValue("version");
                  if (sb.length() != 0) sb.append("|");
                  sb.append(id).append(":").append(versionConstraint);
                  if (!StringUtil.isEmptyOrSpaces(platform)) {
                    sb.append(":").append(platform);
                  }
                }
              }

              @NotNull
              private String sha512(@NotNull final BuildArtifact file) throws PackageLoadException {
                InputStream is = null;
                try {
                  is = new BufferedInputStream(file.getInputStream());
                  final byte[] hash = DigestUtils.sha512(is);
                  //Buggy commons.codes added unnecessary newlines
                  return Base64.encodeBase64String(hash).replaceAll("[\r\n]+", "");
                } catch (IOException e) {
                  throw new PackageLoadException("Failed to compute SHA-512 for " + file);
                } finally {
                  FileUtil.close(is);
                }
              }
            }
    );
  }
}
