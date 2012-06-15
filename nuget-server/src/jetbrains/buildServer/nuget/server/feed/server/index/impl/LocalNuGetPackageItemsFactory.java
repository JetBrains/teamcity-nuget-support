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

package jetbrains.buildServer.nuget.server.feed.server.index.impl;

import jetbrains.buildServer.nuget.common.PackageInfoLoaderBase;
import jetbrains.buildServer.nuget.common.PackageLoadException;
import jetbrains.buildServer.serverSide.artifacts.BuildArtifact;
import jetbrains.buildServer.util.FileUtil;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.digest.DigestUtils;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

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
                addParameter(map, "Id", id);
                addParameter(map, "Version", version);
                //addParameter(map, "ReleaseNotes", ""); //TODO:!
                addParameter(map, "Authors", parseProperty(root, "authors"));
                addParameter(map, "Dependencies", parseDependencies(root));
                addParameter(map, "Description", parseProperty(root, "description"));
                //addParameter(map, "Copyright", ""); //TODO:
                addParameter(map, "ProjectUrl", parseProperty(root, "projectUrl"));
                addParameter(map, "Tags", parseProperty(root, "tags"));
                addParameter(map, "IconUrl", parseProperty(root, "iconUrl"));
                addParameter(map, "LicenseUrl", parseProperty(root, "licenseUrl"));
                addParameter(map, "RequireLicenseAcceptance", parseProperty(root, "requireLicenseAcceptance"));
                addParameter(map, "PackageHash", sha512(nupkg));
                addParameter(map, "PackageHashAlgorithm", "SHA512");
                addParameter(map, "PackageSize", String.valueOf(nupkg.getSize()));
                //addParameter(map, "IsLatestVersion", "");
                addParameter(map, "LastUpdated", ODataDataFormat.formatDate(finishDate));
                //addParameter(map, "Updated", formatDate(updated));

                return map;
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
