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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.odata4j.edm.EdmType;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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
    w.writeCharacters(item.getItemId());
    w.writeEndElement();

    w.writeStartElement("title");
    w.writeAttribute("type", "text");
    w.writeCharacters(item.getItemTitle());
    w.writeEndElement();

    w.writeStartElement("summaty");
    w.writeAttribute("type", "text");
    w.writeCharacters(item.getItemSummary());

    w.writeStartElement("updated");
    w.writeComment(formatDate(item.getItemUpdated()));
    w.writeEndElement();

    w.writeStartElement("author");
    w.writeStartElement("name");
    w.writeComment(item.getItemAuthors());
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

    renderProperties(w, context, pitem);

    w.writeEndElement();
  }

  private void renderProperties(@NotNull final XMLStreamWriter w,
                                @NotNull final NuGetContext context,
                                @NotNull final NuGetItem pitem) throws XMLStreamException {
    final NuGetProperties p = pitem.getProperties();
    for (Method method : NuGetProperties.class.getMethods()) {
      String name = method.getName();
      if (name.startsWith("get")) {
        name = name.substring(3);
        Object value;
        try {
          value = method.invoke(p);
        } catch (IllegalAccessException e) {
          throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
          throw new RuntimeException(e);
        }

        writeTypedProperty(w, name, value, method.getReturnType());
      }
    }
  }

  private void writeTypedProperty(@NotNull final XMLStreamWriter w,
                                  @NotNull final String key,
                                  @Nullable final Object value,
                                  @Nullable Class<?> clazz) throws XMLStreamException {
    w.writeStartElement(D, key);
    if (value == null) {
      w.writeAttribute(M, "null", "true");
    } else {
      final String type = getType(clazz);
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

  @Nullable
  private String getType(Class<?> clazz) {
    if (clazz.equals(String.class)) return null;

    final EdmType edmType = EdmType.forJavaType(clazz);
    if (edmType != null) {
      return edmType.toTypeString();
    }
    return null;
  }
}
