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

package jetbrains.buildServer.nuget.server.exec.impl;

import jetbrains.buildServer.nuget.feedReader.NuGetFeedCredentials;
import jetbrains.buildServer.nuget.server.exec.ListPackagesResult;
import jetbrains.buildServer.nuget.server.exec.SourcePackageInfo;
import jetbrains.buildServer.nuget.server.exec.SourcePackageReference;
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
import java.util.*;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 04.10.11 18:22
 */
public class ListPackagesArguments {
  private static final String NUGET_PACKAGES = "nuget-packages";
  private static final String PACKAGES = "packages";
  private static final String PACKAGE = "package";
  private static final String VERSIONS = "versions";
  private static final String INCLUDE_PRERELEASE = "include-prerelease";
  private static final String USERNAME = "username";
  private static final String PASSWORD = "password";
  private static final String SOURCE_ATTRIBUTE = "source";

  public void encodeParameters(@NotNull final File file,
                               @NotNull Collection<SourcePackageReference> refs) throws IOException {
    final Element root = new Element(NUGET_PACKAGES);
    final Element packages = new Element(PACKAGES);
    for (SourcePackageReference ref : refs) {
      final Element pkg = new Element(PACKAGE);
      pkg.setAttribute("id", ref.getPackageId());
      final String source = ref.getSource();
      final NuGetFeedCredentials credentials = ref.getCredentials();

      if (source != null) {
        pkg.setAttribute(SOURCE_ATTRIBUTE, source);
      }

      if (credentials != null) {
        pkg.setAttribute(USERNAME, credentials.getUsername());
        pkg.setAttribute(PASSWORD, credentials.getPassword());
      }

      final String spec = ref.getVersionSpec();
      if (spec != null) {
        pkg.setAttribute(VERSIONS, spec);
      }
      if (ref.isIncludePrerelease()) {
        pkg.setAttribute(INCLUDE_PRERELEASE, "true");
      }
      packages.addContent((Content)pkg);
    }
    root.addContent((Content)packages);

    final OutputStream os = new BufferedOutputStream(new FileOutputStream(file));
    try {
      XmlUtil.saveDocument(new Document(root), os);
    } finally {
      FileUtil.close(os);
    }
  }

  private static String trim(String s) {
    if (s == null || jetbrains.buildServer.util.StringUtil.isEmptyOrSpaces(s)) return null;
    return s;
  }

  public Map<SourcePackageReference, ListPackagesResult> decodeParameters(@NotNull final File file) throws IOException {
    final Element root;
    try {
      root = FileUtil.parseDocument(file);
    } catch (final JDOMException e) {
      throw new IOException("Failed to parse " + file + ". " + e.getMessage(), e);
    }

    if (!root.getName().equals(NUGET_PACKAGES)) throw new IOException("Invalid xml");
    final Element packages = root.getChild(PACKAGES);
    if (packages == null) throw new IOException("Invalid xml");

    final Map<SourcePackageReference, ListPackagesResult> result = new HashMap<SourcePackageReference, ListPackagesResult>();

    for (Object pElement : packages.getChildren(PACKAGE)) {
      final Element pkg = (Element) pElement;

      final String id = trim(pkg.getAttributeValue("id"));
      final String spec = trim(pkg.getAttributeValue(VERSIONS));
      final String source = trim(pkg.getAttributeValue(SOURCE_ATTRIBUTE));

      if (id == null) continue;
      final SourcePackageReference ref = new SourcePackageReference(source, parseCredentials(pkg), id, spec, parseIncludePrerelease(pkg));

      final Collection<SourcePackageInfo> versions = new ArrayList<SourcePackageInfo>();
      final String packageErrorMessage = pkg.getChildText("error-message");
      final Element pkgChild = pkg.getChild("package-entries");
      if (pkgChild != null) {
        for (Object pEntry : pkgChild.getChildren("package-entry")) {
          final Element entry = (Element) pEntry;

          final String version = entry.getAttributeValue("version");
          if (version == null || jetbrains.buildServer.util.StringUtil.isEmptyOrSpaces(version)) continue;

          versions.add(ref.toInfo(version));
        }
      }

      result.put(ref, new ListPackagesResult() {
        @Nullable
        public String getErrorMessage() {
          return packageErrorMessage;
        }

        @NotNull
        public Collection<SourcePackageInfo> getCollectedInfos() {
          return Collections.unmodifiableCollection(versions);
        }
      });
    }

    return result;
  }

  private boolean parseIncludePrerelease(@NotNull Element root) {
    final String include = root.getAttributeValue(INCLUDE_PRERELEASE);
    return Boolean.TRUE.equals(Boolean.parseBoolean(include));
  }

  @Nullable
  private NuGetFeedCredentials parseCredentials(@NotNull Element pkg) {
    final String username = trim(pkg.getAttributeValue(USERNAME));
    final String password = trim(pkg.getAttributeValue(PASSWORD));
    if (StringUtil.isEmptyOrSpaces(username)) return null;

    return new NuGetFeedCredentials(username, password);
  }
}
