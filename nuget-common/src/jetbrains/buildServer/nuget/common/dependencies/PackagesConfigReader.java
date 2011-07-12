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

package jetbrains.buildServer.nuget.common.dependencies;

import jetbrains.buildServer.util.StringUtil;
import jetbrains.buildServer.util.XmlXppAbstractParser;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Provide parsing for NuGet packages.config file
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 11.07.11 21:48
 */
public class PackagesConfigReader {
  private final FileSystem myFileSystem;

  public PackagesConfigReader(@NotNull final FileSystem fileSystem) {
    myFileSystem = fileSystem;
  }

  public interface Callback {
    /**
     * Called for each new found package usage in project
     * @param id nuget package Id
     * @param version nuget package version
     * @param allowedVersions nuget allowed version info, if specified
     */
    void packageFound(@NotNull String id, @NotNull String version, @Nullable String allowedVersions);
  }

  public void readConfig(@NotNull final RelativePath path,
                         @NotNull final Callback callback) throws IOException {
    myFileSystem.parseXml(path,
            new XmlXppAbstractParser() {
              @Override
              protected List<XmlHandler> getRootHandlers() {
                return Arrays.asList(elementsPath(new Handler() {
                  public XmlReturn processElement(@NotNull XmlElementInfo xmlElementInfo) {
                    String id = xmlElementInfo.getAttribute("id");
                    String version = xmlElementInfo.getAttribute("version");
                    if (id == null || StringUtil.isEmptyOrSpaces(id))
                      return xmlElementInfo.noDeep();

                    if (version == null || StringUtil.isEmptyOrSpaces(version))
                      return xmlElementInfo.noDeep();

                    callback.packageFound(
                            id,
                            version,
                            xmlElementInfo.getAttribute("allowedVersions"));

                    return xmlElementInfo.noDeep();
                  }
                }, "packages", "package"));
              }
            });
  }
}
