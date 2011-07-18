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

package jetbrains.buildServer.nuget.agent.install.impl;

import com.intellij.openapi.diagnostic.Logger;
import jetbrains.buildServer.nuget.agent.install.NuGetPackagesCollector;
import jetbrains.buildServer.util.StringUtil;
import jetbrains.buildServer.util.XmlXppAbstractParser;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 18.07.11 22:45
 */
public class NuGetPackagesConfigParser {
  private static final Logger LOG = Logger.getInstance(NuGetPackagesConfigParser.class.getName());

  public void parseNuGetPackages(@NotNull final File packagesConfig,
                                 @NotNull final NuGetPackagesCollector callback) throws IOException {
    XmlXppAbstractParser parser = new XmlXppAbstractParser() {
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


            callback.addPackage(
                    id,
                    version,
                    xmlElementInfo.getAttribute("allowedVersions"));

            return xmlElementInfo.noDeep();
          }
        }, "packages", "package"));
      }
    };
    try {
      parser.parse(packagesConfig);
    } catch (IOException e) {
      LOG.warn("Failed to parse packages.config file: " + packagesConfig + ". " + e.getMessage(), e);
    }
  }
}
