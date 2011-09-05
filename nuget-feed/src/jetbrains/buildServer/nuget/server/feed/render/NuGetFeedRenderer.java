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

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Collection;
import java.util.TimeZone;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 05.09.11 23:43
 */
public class NuGetFeedRenderer {

  public void renderFeed(@NotNull final NuGetContext context,
                         @NotNull final Collection<NuGetItem> items,
                         @NotNull final Writer output) throws IOException, XMLStreamException {
    final XMLOutputFactory factory = XMLOutputFactory.newInstance();
    final XMLStreamWriter w = factory.createXMLStreamWriter(output);
    w.writeStartDocument(context.getEncoding(),  "1.0");
    w.writeStartElement("feed");
    w.setDefaultNamespace("http://www.w3.org/2005/Atom");
    w.writeDefaultNamespace("http://www.w3.org/2005/Atom");
    w.writeNamespace("d", "http://schemas.microsoft.com/ado/2007/08/dataservices");
    w.writeNamespace("m", "http://schemas.microsoft.com/ado/2007/08/dataservices/metadata");
    w.writeAttribute("xml:base", context.getBaseUri());

    w.writeStartElement("title");
    w.writeAttribute("type", "text");
    w.writeCharacters("Packages");
    w.writeEndElement();

    w.writeStartElement("id");
    w.writeCharacters(context.getFeedId());
    w.writeEndElement();

    w.writeStartElement("updated");
    w.writeCharacters(formatDate(context));
    w.writeEndElement();

    w.writeStartElement("link");
    w.writeAttribute("rel", "self");
    w.writeAttribute("title", context.getTitle());
    w.writeAttribute("href", context.getTitle());
    w.writeEndElement();

    if (!items.isEmpty()) {
      w.writeStartElement("entry");
      for (NuGetItem item : items) {
        renderItem(context, item, w);
      }
      w.writeEndElement();
    }

    w.writeEndElement();
    w.writeEndDocument();
  }

  private String formatDate(NuGetContext context) {
    //TODO:fix timezon printing
    return Dates.formatDate(context.getUpdated(), "yyyy-MM-dd'T'HH:mm:ss'Z'", TimeZone.getTimeZone("GMT"));
  }

  private void renderItem(@NotNull NuGetContext context, @NotNull NuGetItem item, @NotNull XMLStreamWriter w) throws XMLStreamException {
    w.writeComment("Item " + item);
  }
}
