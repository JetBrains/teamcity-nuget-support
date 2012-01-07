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

package jetbrains.buildServer.nuget.server.feed.server.render;

import jetbrains.buildServer.nuget.server.feed.server.entity.PackageEntity;
import jetbrains.buildServer.nuget.server.feed.server.entity.PackageFieldsVisitor;
import jetbrains.buildServer.util.Dates;
import jetbrains.buildServer.util.ExceptionUtil;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joda.time.LocalDateTime;
import org.odata4j.edm.EdmSimpleType;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 05.09.11 23:43
 */
public class NuGetPackagesFeedRenderer extends NuGetRendererBase {

  public void renderFeed(@NotNull final NuGetContext context,
                         @NotNull final Collection<PackageEntity> items,
                         @NotNull final Writer output) throws IOException, XMLStreamException {
    final XMLStreamWriter w = createWriter(output);

    w.writeStartDocument(context.getEncoding(), "1.0");
    w.writeStartElement("feed");
    w.setDefaultNamespace(ATOM);
    w.writeDefaultNamespace(ATOM);
    w.writeNamespace("d", D);
    w.writeNamespace("m", M);
    w.writeAttribute("xml:base", context.getBaseUri());

    w.writeStartElement("title");
    w.writeAttribute("type", "text");
    w.writeCharacters("Packages");
    w.writeEndElement();

    w.writeStartElement("id");
    w.writeCharacters(context.getBaseUri() + "/Packages");
    w.writeEndElement();

    w.writeStartElement("updated");
    w.writeCharacters(formatDate(context.getUpdated()));
    w.writeEndElement();

    w.writeStartElement("link");
    w.writeAttribute("rel", "self");
    w.writeAttribute("title", "Packages");
    w.writeAttribute("href", "Packages");
    w.writeEndElement();

    for (PackageEntity item : items) {
      w.writeStartElement("entry");
      renderItem(context, item, w);
      w.writeEndElement();
    }

    w.writeEndElement();
    w.writeEndDocument();
    w.close();
  }


  private String formatDate(@NotNull Date date) {
    //TODO:fix timezone printing
    return Dates.formatDate(date, "yyyy-MM-dd'T'HH:mm:ss'Z'", TimeZone.getTimeZone("GMT"));
  }

  private String formatDate(@NotNull LocalDateTime date) {
    //TODO:fix timezone printing
    return date.toString("yyyy-MM-dd'T'HH:mm:ss'Z'");
  }

  private void renderItem(@NotNull final NuGetContext context,
                          @NotNull final PackageEntity pitem,
                          @NotNull final XMLStreamWriter w) throws XMLStreamException {
    w.writeStartElement("id");
    w.writeCharacters(context.getBaseUri() + "/Packages(Id='" + pitem.getId() + ", Version=" + pitem.getVersion());
    w.writeEndElement();

    w.writeStartElement("title");
    w.writeAttribute("type", "text");
    w.writeCharacters(pitem.getId());
    w.writeEndElement();

    w.writeStartElement("summary");
    w.writeAttribute("type", "text");
    w.writeCharacters(pitem.getSummary());
    w.writeEndElement();

    w.writeStartElement("updated");
    w.writeCharacters(formatDate(pitem.getLastUpdated()));
    w.writeEndElement();

    w.writeStartElement("author");
    w.writeStartElement("name");
    w.writeCharacters(pitem.getAuthors());
    w.writeEndElement();
    w.writeEndElement();

    //link tags omitted

    w.writeStartElement("category");
    w.writeAttribute("term", "NuGetGallery.V2FeedPackage");
    w.writeAttribute("scheme", "http://schemas.microsoft.com/ado/2007/08/dataservices/scheme");
    w.writeEndElement();

    w.writeStartElement("content");
    w.writeAttribute("type", "application/zip");
    w.writeAttribute("src", context.getDownloadUrl(pitem));
    w.writeEndElement();

    w.writeStartElement(M, "properties");
    w.writeNamespace("m", M);
    w.writeNamespace("d", D);

    pitem.visitFields(new PackageFieldsVisitor() {
      public void visitPackageField(@NotNull String key, @Nullable String value, @NotNull String type) {
        try {
          writeProperty(w, key, value, type);
        } catch (XMLStreamException e) {
          ExceptionUtil.rethrowAsRuntimeException(e);
        }
      }
    });

/*
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
*/

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

  private void writeProperty(@NotNull final XMLStreamWriter w,
                             @NotNull final String key,
                             @Nullable final String value,
                             @NotNull final String type) throws XMLStreamException {
    w.writeStartElement(D, key);
    if (value == null) {
      w.writeAttribute(M, "null", "true");
    } else {
      w.writeAttribute(M, "type", type);
      w.writeCharacters(value);
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

    final EdmSimpleType edmType = EdmSimpleType.forJavaType(clazz);
    if (edmType != null) {
      return edmType.getFullyQualifiedTypeName();
    }

    if (Date.class.isAssignableFrom(clazz)) {
      return EdmSimpleType.DATETIME.getFullyQualifiedTypeName();
    }

    return null;
  }
}
