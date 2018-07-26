/*
 * Copyright 2000-2017 JetBrains s.r.o.
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
import jetbrains.buildServer.util.StringUtil;
import jetbrains.buildServer.util.XmlUtil;
import org.jdom.Content;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 19.07.11 11:39
 */
public class PackageDependenciesStore {
  private static final String USED_PACKAGES_ELEMENT = "packages";
  private static final String CREATED_PACKAGES_ELEMENT = "created";
  private static final String PUBLISHED_PACKAGES_ELEMENT = "published";
  private static final String PACKAGE_ELEMENT = "package";
  private static final String PACKAGE_ID = "id";
  private static final String PACKAGE_VERSION = "version";
  private static final String PACKAGE_SOURCE = "source";

  @NotNull
  public PackageDependencies load(@NotNull final InputStream is) throws IOException {
    Element element;
    try {
      element = FileUtil.parseDocument(is, false);
    } catch (final JDOMException e) {
      throw new IOException("Failed to parse stream." + e.getMessage(), e);
    }

    return new PackageDependencies(
            readPackagesList(element.getChild(USED_PACKAGES_ELEMENT)),
            readPackagesList(element.getChild(CREATED_PACKAGES_ELEMENT)),
            readPackagesSourceList(element.getChild(PUBLISHED_PACKAGES_ELEMENT))
    );
  }

  @NotNull
  private List<NuGetPackageInfo> readPackagesList(@Nullable final Element packagesElement) {
    if (packagesElement == null) return Collections.emptyList();

    final List<NuGetPackageInfo> infos = new ArrayList<NuGetPackageInfo>();
    for (Object pkg : packagesElement.getChildren(PACKAGE_ELEMENT)) {
      Element el = (Element) pkg;
      final String id = el.getAttributeValue(PACKAGE_ID);
      final String version = el.getAttributeValue(PACKAGE_VERSION);
      if (id != null && version != null) {
        infos.add(new NuGetPackageInfo(id, version));
      }
    }

    return infos;
  }

  private void savePackagesList(@NotNull final Collection<NuGetPackageInfo> usedPackages,
                                @NotNull final Element root,
                                @NotNull final String containerName) {
    final Element container = new Element(containerName);
    for (NuGetPackageInfo info : usedPackages) {
      final Element pkg = new Element(PACKAGE_ELEMENT);
      pkg.setAttribute(PACKAGE_ID, info.getId());
      pkg.setAttribute(PACKAGE_VERSION, info.getVersion().toString());
      container.addContent((Content) pkg);
    }
    root.addContent((Content) container);
  }

  @NotNull
  private List<SourcePackageInfo> readPackagesSourceList(@Nullable final Element packagesElement) {
    if (packagesElement == null) return Collections.emptyList();

    final List<SourcePackageInfo> infos = new ArrayList<SourcePackageInfo>();
    for (Object pkg : packagesElement.getChildren(PACKAGE_ELEMENT)) {
      Element el = (Element) pkg;
      final String id = el.getAttributeValue(PACKAGE_ID);
      final String version = el.getAttributeValue(PACKAGE_VERSION);
      final String source = el.getAttributeValue(PACKAGE_SOURCE);
      if (id != null && version != null) {
        infos.add(new SourcePackageInfo(new NuGetPackageInfo(id, version), source));
      }
    }

    return infos;
  }

  private void savePackageSourceList(@NotNull final Collection<SourcePackageInfo> usedPackages,
                                     @NotNull final Element root,
                                     @NotNull final String containerName) {
    final Element container = new Element(containerName);
    for (SourcePackageInfo info : usedPackages) {
      final Element pkg = new Element(PACKAGE_ELEMENT);
      pkg.setAttribute(PACKAGE_ID, info.getPackageInfo().getId());
      pkg.setAttribute(PACKAGE_VERSION, info.getPackageInfo().getVersion().toString());
      if (!StringUtil.isEmptyOrSpaces(info.getSource())) {
        pkg.setAttribute(PACKAGE_SOURCE, info.getSource());
      }
      container.addContent((Content) pkg);
    }
    root.addContent((Content) container);
  }

  @NotNull
  public PackageDependencies load(@NotNull final File file) throws IOException {
    return load(new BufferedInputStream(new FileInputStream(file)));
  }

  public void save(@NotNull final PackageDependencies deps,
                   @NotNull final File file) throws IOException {
    final Element root = new Element("nuget-dependencies");

    savePackagesList(deps.getUsedPackages(), root, USED_PACKAGES_ELEMENT);
    savePackagesList(deps.getCreatedPackages(), root, CREATED_PACKAGES_ELEMENT);
    savePackageSourceList(deps.getPublishedPackages(), root, PUBLISHED_PACKAGES_ELEMENT);

    final Document doc = new Document(root);
    final OutputStream os = new BufferedOutputStream(new FileOutputStream(file));
    try {
      XmlUtil.saveDocument(doc, os);
    } finally {
      FileUtil.close(os);
    }
  }
}
