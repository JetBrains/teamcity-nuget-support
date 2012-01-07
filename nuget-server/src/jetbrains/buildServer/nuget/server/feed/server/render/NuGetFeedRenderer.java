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

import org.jetbrains.annotations.NotNull;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.Writer;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 10.09.11 20:44
 */
public class NuGetFeedRenderer extends NuGetRendererBase {
  public void renderFeed(@NotNull final NuGetContext context,
                         @NotNull final Writer output) throws XMLStreamException {
    final XMLStreamWriter w = createWriter(output);
    w.writeStartDocument(context.getEncoding(), "1.0");
    w.writeStartElement("service");
    w.writeNamespace("atom", ATOM);
    w.writeNamespace("app", APP);

    w.setDefaultNamespace(APP);
    w.writeDefaultNamespace(APP);
    w.writeAttribute("xml:base", context.getBaseUri());

    w.writeStartElement("workspace");
    writeWorkspace(w, context);
    w.writeEndElement();//workspace

    w.writeEndElement();
    w.writeEndDocument();
    w.close();
  }

  private void writeWorkspace(XMLStreamWriter w, NuGetContext context) throws XMLStreamException {
    w.writeStartElement(ATOM, "title");
    w.writeCharacters("Default");
    w.writeEndElement();

    w.writeStartElement("collection");
    w.writeAttribute("href", "Packages");
    w.writeStartElement(ATOM, "title");
    w.writeCharacters("Packages");
    w.writeEndElement();
    w.writeEndElement();
  }
}
