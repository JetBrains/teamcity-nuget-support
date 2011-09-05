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

import org.jetbrains.annotations.NotNull;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Collection;

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
    w.writeStartDocument();
    w.writeStartElement("feed");
    w.setDefaultNamespace("http://www.w3.org/2005/Atom");
    w.writeNamespace("base", context.getBaseUri());
    w.writeNamespace("d", "http://schemas.microsoft.com/ado/2007/08/dataservices");
    w.writeNamespace("m", "http://schemas.microsoft.com/ado/2007/08/dataservices/metadata");

    w.writeEndElement();
    w.writeEndDocument();
  }
}
