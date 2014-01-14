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

package jetbrains.buildServer.nuget.agent.commands.impl;

import jetbrains.buildServer.nuget.agent.parameters.PackageSource;
import jetbrains.buildServer.util.FileUtil;
import jetbrains.buildServer.util.XmlUtil;
import org.jdom.Content;
import org.jdom.Document;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.Collection;

/**
 * Created 04.01.13 19:33
 *
 * @author Eugene Petrenko (eugene.petrenko@jetbrains.com)
 */
public class NuGetSourcesWriter {
  public void writeNuGetSources(@NotNull final File file,
                                @NotNull final Collection<PackageSource> sources) throws IOException {
    final Element root = new Element("sources");
    for (PackageSource source : sources) {
      final Element sourceElement = new Element("source");
      sourceElement.setAttribute("source", source.getSource());

      final String username = source.getUsername();
      if (username != null) sourceElement.setAttribute("username", username);

      final String password = source.getPassword();
      if (password != null) sourceElement.setAttribute("password", password);

      root.addContent((Content)sourceElement);
    }

    final OutputStream os = new BufferedOutputStream(new FileOutputStream(file));
    try {
      XmlUtil.saveDocument(new Document(root), os);
    } finally {
      FileUtil.close(os);
    }

  }
}
