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

import jetbrains.buildServer.agent.AgentRunningBuild;
import jetbrains.buildServer.agent.artifacts.ArtifactsWatcher;
import jetbrains.buildServer.nuget.agent.install.PackageInfo;
import jetbrains.buildServer.util.FileUtil;
import jetbrains.buildServer.util.XmlUtil;
import org.jdom.Content;
import org.jdom.Document;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.Collection;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 18.07.11 23:02
 */
public class PackagesInfoUploader {
  private final ArtifactsWatcher myPublisher;

  public PackagesInfoUploader(@NotNull final ArtifactsWatcher publisher) {
    myPublisher = publisher;
  }

  public void uploadDepectedPackages(@NotNull final AgentRunningBuild build,
                                     @NotNull final Collection<PackageInfo> infos) throws IOException {
    File tmp = FileUtil.createTempDirectory("nuget", "packages", build.getBuildTempDirectory());

    Element root = new Element("packages");
    for (PackageInfo info : infos) {
      Element pkg = new Element("package");
      pkg.setAttribute("id", info.getId());
      pkg.setAttribute("version", info.getVersion());
      root.addContent((Content)pkg);
    }
    Document doc = new Document(root);

    final File content = new File(tmp, "nuget-packages.xml");
    OutputStream os = new BufferedOutputStream(new FileOutputStream(content));
    try {
      XmlUtil.saveDocument(doc, os);
    } finally {
      FileUtil.close(os);
    }

    myPublisher.addNewArtifactsPath(content.getPath() + " => .teamcity/nuget");
  }
}
