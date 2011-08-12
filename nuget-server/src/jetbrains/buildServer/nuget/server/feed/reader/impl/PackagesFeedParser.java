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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 12.08.11 13:42
 */
public class PackagesFeedParser {
  private static final Logger LOG = Logger.getInstance(PackagesFeedParser.class.getName());

  private static final Namespace atom = Namespace.getNamespace("http://www.w3.org/2005/Atom");
  private static final Namespace metadata = Namespace.getNamespace("http://schemas.microsoft.com/ado/2007/08/dataservices/metadata");
  private static final Namespace services = Namespace.getNamespace("http://schemas.microsoft.com/ado/2007/08/dataservices");

  public Collection<FeedPackage> readPackages(@NotNull Element root) {
    final List<FeedPackage> result = new ArrayList<FeedPackage>();

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
    Collections.sort(result);
    return result;
  }

  @NotNull
  private FeedPackage parseOneEntry(Element entry) throws InvalidXmlException {
    final Element atomIdElement = getChild(entry, "id", atom);
    final String atomId = atomIdElement.getText();

    final String content = getChildAttribute(entry, "content", atom, "src");
    final Element props = getChild(entry, "properties", metadata);

    final String packageId = getChildText(props, "Id", services);
    final String version = getChildText(props, "Version", services);
    final String desription = getChildText(props, "Description", services);
    final String summary = getChildText(props, "Summary", services);
    final boolean isLatestVersion = "true".equalsIgnoreCase(getChildTextSafe(props, "IsLatestVersion", services));

    return new FeedPackage(
            atomId,
            new PackageInfo(packageId, version),
            isLatestVersion,
            StringUtil.isEmptyOrSpaces(desription) ? summary : desription,
            content);
  }

  @NotNull
  private String getChildTextSafe(@NotNull Element element,
                                  @NotNull String name,
                                  @NotNull Namespace ns) {
    try {
      return getChildText(element, name, ns);
    } catch (InvalidXmlException e) {
      return "";
    }
  }

  @NotNull
  private String getChildText(@NotNull Element element,
                              @NotNull String name,
                              @NotNull Namespace ns) throws InvalidXmlException {
    final Element data = getChild(element, name, ns);
    final String text = data.getText();
    if (StringUtil.isEmptyOrSpaces(text)) {
      throw new InvalidXmlException("Element " + name + " must have a content. ");
    }
    return text;
  }

  @NotNull
  private String getChildAttribute(@NotNull Element element,
                                   @NotNull String name,
                                   @NotNull Namespace ns,
                                   @NotNull String attribute) throws InvalidXmlException {
    final Element data = getChild(element, name, ns);
    final String text = data.getAttributeValue(attribute);
    if (StringUtil.isEmptyOrSpaces(text)) {
      throw new InvalidXmlException("Element " + name + " must have non-empty attribute " + attribute + ". ");
    }
    return text;
  }

  @NotNull
  private Element getChild(Element element, String name, Namespace ns) throws InvalidXmlException {
    final Element data = element.getChild(name, ns);
    if (data == null) {
      throw new InvalidXmlException("Element " + name + " was not found. ");
    }
    return data;
  }

  private static class InvalidXmlException extends Exception {
    private InvalidXmlException(String message) {
      super(message);
    }
  }
}
