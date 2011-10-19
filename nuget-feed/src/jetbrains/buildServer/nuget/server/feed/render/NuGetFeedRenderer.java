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

package jetbrains.buildServer.nuget.server.feed.render;

import jetbrains.buildServer.util.Dates;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.odata4j.edm.EdmType;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.IOException;
import java.io.Writer;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 05.09.11 23:43
 */
public class NuGetFeedRenderer {
  private final String M = "http://schemas.microsoft.com/ado/2007/08/dataservices/metadata";
  private final String D = "http://schemas.microsoft.com/ado/2007/08/dataservices";

  public void renderFeed(@NotNull final NuGetContext context,
                         @NotNull final Collection<NuGetItem> items,
                         @NotNull final Writer output) throws IOException, XMLStreamException {
    final XMLOutputFactory factory = XMLOutputFactory.newInstance();
    final XMLStreamWriter w = factory.createXMLStreamWriter(output);
    w.writeStartDocument(context.getEncoding(),  "1.0");
    w.writeStartElement("feed");
    w.setDefaultNamespace("http://www.w3.org/2005/Atom");
    w.writeDefaultNamespace("http://www.w3.org/2005/Atom");
    w.writeNamespace("d", D);
    w.writeNamespace("m", M);
    w.writeAttribute("xml:base", context.getBaseUri());

    w.writeStartElement("title");
    w.writeAttribute("type", "text");
    w.writeCharacters("Packages");
    w.writeEndElement();

    w.writeStartElement("id");
    w.writeCharacters(context.getFeedId());
    w.writeEndElement();

    w.writeStartElement("updated");
    w.writeCharacters(formatDate(context.getUpdated()));
    w.writeEndElement();

    w.writeStartElement("link");
    w.writeAttribute("rel", "self");
    w.writeAttribute("title", context.getTitle());
    w.writeAttribute("href", context.getTitle());
    w.writeEndElement();

    for (NuGetItem item : items) {
      w.writeStartElement("entry");
      renderItem(context, item, w);
      w.writeEndElement();
    }

    w.writeEndElement();
    w.writeEndDocument();
  }

  private String formatDate(@NotNull Date date) {
    //TODO:fix timezon printing
    return Dates.formatDate(date, "yyyy-MM-dd'T'HH:mm:ss'Z'", TimeZone.getTimeZone("GMT"));
  }

  private void renderItem(@NotNull NuGetContext context,
                          @NotNull NuGetItem pitem,
                          @NotNull XMLStreamWriter w) throws XMLStreamException {
    final NuGetAtomItem item = pitem.getAtomItem();

    w.writeStartElement("id");
    w.writeCharacters(context.createId(item.getItemName(), item.getItemVersion()));
    w.writeEndElement();

    w.writeStartElement("title");
    w.writeAttribute("type", "text");
    w.writeCharacters(item.getItemTitle());
    w.writeEndElement();

    w.writeStartElement("summary");
    w.writeAttribute("type", "text");
    w.writeCharacters(item.getItemSummary());
    w.writeEndElement();

    w.writeStartElement("updated");
    w.writeCharacters(formatDate(item.getItemUpdated()));
    w.writeEndElement();

    w.writeStartElement("author");
    w.writeStartElement("name");
    w.writeCharacters(item.getItemAuthors());
    w.writeEndElement();
    w.writeEndElement();

    //link tags omitted

    w.writeStartElement("category");
    w.writeAttribute("term", "Gallery.Infrastructure.FeedModels.PublishedPackage");
    w.writeAttribute("scheme", "http://schemas.microsoft.com/ado/2007/08/dataservices/scheme");
    w.writeEndElement();

    w.writeStartElement("content");
    w.writeAttribute("type", "application/zip");
    w.writeAttribute("src", context.resolveUrl(item.getDownloadPath()));
    w.writeEndElement();


    w.writeStartElement(M, "properties");
    w.writeNamespace("m", M);
    w.writeNamespace("d", D);


    final NuGetProperties p = pitem.getProperties();
    writeTypedProperty(w, "Id", p.getId());
    writeTypedProperty(w, "Version", p.getVersion());
    writeTypedProperty(w, "Title", p.getTitle());
    writeTypedProperty(w, "Authors", p.getAuthors());
    writeTypedProperty(w, "PackageType", "Packages");
    writeTypedProperty(w, "Summary", p.getSummary());
    writeTypedProperty(w, "Description", p.getDescription());
    writeTypedProperty(w, "Copyright", p.getCopyright());
    writeTypedProperty(w, "PackageHashAlgorithm", p.getPackageHashAlgorithm());
    writeTypedProperty(w, "PackageHash", p.getPackageHash());
    writeTypedProperty(w, "PackageSize", p.getPackageSize());
    writeTypedProperty(w, "Price", BigDecimal.ZERO);
    writeTypedProperty(w, "RequireLicenseAcceptance", p.getRequireLicenseAcceptance());
    writeTypedProperty(w, "IsLatestVersion", p.getIsLatestVersion());
    writeTypedProperty(w, "ReleaseNotes", p.getReleaseNotes());
    writeTypedProperty(w, "Prerelease", p.getPrerelease());
    writeTypedProperty(w, "VersionRating", 4.5);
    writeTypedProperty(w, "VersionRatingsCount", 1);
    writeTypedProperty(w, "VersionDownloadCount", 0);
    writeTypedProperty(w, "Created", p.getCreated());
    writeTypedProperty(w, "LastUpdated", p.getCreated());
    writeTypedProperty(w, "Published", p.getCreated());
    writeTypedProperty(w, "ExternalPackageUrl", p.getExternalPackageUrl());
    writeTypedProperty(w, "ProjectUrl", p.getProjectUrl());
    writeTypedProperty(w, "LicenseUrl", p.getLicenseUrl());
    writeTypedProperty(w, "IconUrl", p.getIconUrl());
    writeTypedProperty(w, "Rating", 4.333);
    writeTypedProperty(w, "RatingsCount", 1);
    writeTypedProperty(w, "DownloadCount", 1);
    writeTypedProperty(w, "Categories", p.getCategories());

    writeTags(w, p.getTags());

    writeTypedProperty(w, "Dependencies", p.getDependencies());
    writeTypedProperty(w, "ReportAbuseUrl", p.getReportAbuseUrl());
    writeTypedProperty(w, "GalleryDetailsUrl", p.getGalleryDetailsUrl());

    w.writeEndElement();
  }

  private void writeTypedProperty(@NotNull final XMLStreamWriter w,
                                  @NotNull final String key,
                                  @Nullable final Object value) throws XMLStreamException {
    w.writeStartElement(D, key);
    if (value == null) {
      w.writeAttribute(M, "null", "true");
    } else {
      final String type = getType(value.getClass());
      if (type != null) {
        w.writeAttribute(M, "type", type);
      }
      if (value instanceof Date) {
        w.writeCharacters(formatDate((Date) value));
      } else {
        w.writeCharacters(value.toString());
      }
    }
    w.writeEndElement();
  }

  private void writeTags(@NotNull final XMLStreamWriter w,
                                  @Nullable final String value) throws XMLStreamException {
    w.writeStartElement(D, "Tags");
    if (value != null && !StringUtil.isEmptyOrSpaces(value)) {
      w.writeAttribute("xml:space", "preserve");
      w.writeCharacters(" " + value.trim() + " ");
    } else {
      w.writeAttribute(M, "null", "true");
    }
    w.writeEndElement();
  }

  @Nullable
  private String getType(Class<?> clazz) {
    if (clazz.equals(String.class)) return null;

    final EdmType edmType = EdmType.forJavaType(clazz);
    if (edmType != null) {
      return edmType.toTypeString();
    }

    if (Date.class.isAssignableFrom(clazz)) {
      return EdmType.DATETIME.toTypeString();
    }

    return null;
  }
}
