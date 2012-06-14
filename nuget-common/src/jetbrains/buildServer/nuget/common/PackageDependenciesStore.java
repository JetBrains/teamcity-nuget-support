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

package jetbrains.buildServer.nuget.common;

import jetbrains.buildServer.util.FileUtil;
import jetbrains.buildServer.util.XmlUtil;
import org.jdom.Content;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 19.07.11 11:39
 */
public class PackageDependenciesStore {

  public PackageDependencies load(@NotNull final InputStream is) throws IOException {
    Element element = null;
    try {
      element = FileUtil.parseDocument(is, false);
    } catch (final JDOMException e) {
      throw new IOException("Failed to parse stream." + e.getMessage()){{initCause(e);}};
    }

    final List<PackageInfo> infos = new ArrayList<PackageInfo>();

    Element packagesElement = element.getChild("packages");
    if (packagesElement != null) {
      for (Object pkg : packagesElement.getChildren("package")) {
        Element el = (Element) pkg;
        final String id = el.getAttributeValue("id");
        final String version = el.getAttributeValue("version");
        if (id != null && version != null) {
          infos.add(new PackageInfo(id, version));
        }
      }
    }
    return new PackageDependencies(infos);
  }

  public PackageDependencies load(@NotNull final File file) throws IOException {
    return load(new BufferedInputStream(new FileInputStream(file)));
  }

  public void save(@NotNull final PackageDependencies deps,
                   @NotNull final File file) throws IOException {
    Element root = new Element("nuget-dependencies");

    Element pkgs = new Element("packages");
    for (PackageInfo info : deps.getPackages()) {
      Element pkg = new Element("package");
      pkg.setAttribute("id", info.getId());
      pkg.setAttribute("version", info.getVersion());
      pkgs.addContent((Content) pkg);
    }

    root.addContent((Content) pkgs);

    Document doc = new Document(root);
    OutputStream os = new BufferedOutputStream(new FileOutputStream(file));
    try {
      XmlUtil.saveDocument(doc, os);
    } finally {
      FileUtil.close(os);
    }
  }
}
