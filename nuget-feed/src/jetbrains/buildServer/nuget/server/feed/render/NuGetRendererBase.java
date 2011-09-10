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
import java.io.Writer;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 10.09.11 20:45
 */
public class NuGetRendererBase {
  protected final String M = "http://schemas.microsoft.com/ado/2007/08/dataservices/metadata";
  protected final String D = "http://schemas.microsoft.com/ado/2007/08/dataservices";
  protected final String ATOM = "http://www.w3.org/2005/Atom";
  protected final String APP = "http://www.w3.org/2007/app";


  @NotNull
  protected XMLStreamWriter createWriter(@NotNull final Writer output) throws XMLStreamException {
    final XMLOutputFactory factory = XMLOutputFactory.newInstance();
    return factory.createXMLStreamWriter(output);
  }
}
