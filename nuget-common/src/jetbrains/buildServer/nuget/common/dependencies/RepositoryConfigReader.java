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

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 11.07.11 21:23
 */
public class RepositoryConfigReader {
  private final FileSystem myFileSystem;

  public RepositoryConfigReader(@NotNull final FileSystem fileSystem) {
    myFileSystem = fileSystem;
  }

  private interface Callback {
    /**
     * Called when packages.config is found
     *
     * @param configRelativePath relative path to NuGet configuration
     */
    void onPackagesConfigFound(@NotNull final RelativePath configRelativePath);
  }

  public void readConfigurations(@NotNull final RelativePath nugetRepsitoryConfig,
                                 @NotNull final Callback callback) throws IOException {
    myFileSystem.parseXml(
            nugetRepsitoryConfig,
            new XmlXppAbstractParser() {
              @Override
              protected List<XmlHandler> getRootHandlers() {
                return Arrays.asList(elementsPath(new Handler() {
                  public XmlReturn processElement(@NotNull XmlElementInfo xmlElementInfo) {
                    final String relPath = xmlElementInfo.getAttribute("path");
                    if (relPath != null && !StringUtil.isEmptyOrSpaces(relPath)) {
                      callback.onPackagesConfigFound(nugetRepsitoryConfig.getParent().createChild(relPath));
                    }
                    return xmlElementInfo.noDeep();
                  }
                }, "repositories", "repository"));
              }
            });
  }
}

