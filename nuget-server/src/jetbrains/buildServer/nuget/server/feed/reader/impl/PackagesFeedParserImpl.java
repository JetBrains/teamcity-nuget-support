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

package jetbrains.buildServer.nuget.server.feed.reader.impl;

import com.intellij.openapi.diagnostic.Logger;
import jetbrains.buildServer.nuget.common.PackageInfo;
import jetbrains.buildServer.nuget.server.feed.reader.FeedPackage;
import jetbrains.buildServer.util.StringUtil;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 12.08.11 13:42
 */
public class PackagesFeedParserImpl implements PackagesFeedParser {
  private static final Logger LOG = Logger.getInstance(PackagesFeedParserImpl.class.getName());

  private static final Namespace atom = Namespace.getNamespace("http://www.w3.org/2005/Atom");
  private static final Namespace metadata = Namespace.getNamespace("http://schemas.microsoft.com/ado/2007/08/dataservices/metadata");
  private static final Namespace services = Namespace.getNamespace("http://schemas.microsoft.com/ado/2007/08/dataservices");

  @Nullable
  public String readPackages(@NotNull final Element root, @NotNull final Collection<FeedPackage> result) {
    final List entries = root.getChildren("entry", atom);
    for (Object o : entries) {
      final Element entry = (Element) o;
      try {
        final FeedPackage e = parseOneEntry(entry);
        result.add(e);
      } catch (InvalidXmlException e) {
        LOG.debug("Failed to parse feed entry. " + e.getMessage());
      }
    }

    for (Object entry : root.getChildren("link", atom)) {
      final Element link = (Element)entry;
      if ("next".equals(link.getAttributeValue("rel"))) {
        String href = link.getAttributeValue("href");
        if (!StringUtil.isEmptyOrSpaces(href)) return href;
      }
    }
    return null;
  }

  @NotNull
  private FeedPackage parseOneEntry(@NotNull final Element entry) throws InvalidXmlException {
    final Element atomIdElement = getChild(entry, "id", atom);
    final String atomId = atomIdElement.getText();

    final String content = getChildAttribute(entry, "content", atom, "src");
    final Element props = getChild(entry, "properties", metadata);

    String packageId = getChildTextSafe(props, "Id", services);
    if (StringUtil.isEmptyOrSpaces(packageId)) {
      packageId = getChildText(entry, "title", atom);
    }
    final String version = getChildText(props, "Version", services);
    final String desription = getChildText(props, "Description", services);
    final boolean isLatestVersion = "true".equalsIgnoreCase(getChildTextSafe(props, "IsLatestVersion", services));

    return new FeedPackage(
            atomId,
            new PackageInfo(packageId, version),
            isLatestVersion,
            desription,
            content);
  }

  @NotNull
  private String getChildTextSafe(@NotNull final Element element,
                                  @NotNull final String name,
                                  @NotNull final Namespace ns) {
    try {
      return getChildText(element, name, ns);
    } catch (InvalidXmlException e) {
      return "";
    }
  }

  @NotNull
  private String getChildText(@NotNull final Element element,
                              @NotNull final String name,
                              @NotNull final Namespace ns) throws InvalidXmlException {
    final Element data = getChild(element, name, ns);
    final String text = data.getText();
    if (StringUtil.isEmptyOrSpaces(text)) {
      throw new InvalidXmlException("Element " + name + " must have a content. ");
    }
    return text;
  }

  @NotNull
  private String getChildAttribute(@NotNull final Element element,
                                   @NotNull final String name,
                                   @NotNull final Namespace ns,
                                   @NotNull final String attribute) throws InvalidXmlException {
    final Element data = getChild(element, name, ns);
    final String text = data.getAttributeValue(attribute);
    if (StringUtil.isEmptyOrSpaces(text)) {
      throw new InvalidXmlException("Element " + name + " must have non-empty attribute " + attribute + ". ");
    }
    return text;
  }

  @NotNull
  private Element getChild(@NotNull final Element element,
                           @NotNull final String name,
                           @NotNull final Namespace ns) throws InvalidXmlException {
    final Element data = element.getChild(name, ns);
    if (data == null) {
      throw new InvalidXmlException("Element " + name + " was not found. ");
    }
    return data;
  }

  private static class InvalidXmlException extends Exception {
    private InvalidXmlException(@NotNull final String message) {
      super(message);
    }
  }
}
