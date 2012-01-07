/*
 * Copyright 2000-2012 JetBrains s.r.o.
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

package jetbrains.buildServer.nuget.tests.feed;

import org.jdom.Element;
import org.jdom.Namespace;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;

/**
* @author Eugene Petrenko (eugene.petrenko@gmail.com)
*         Date: 02.11.11 15:51
*/
public abstract class XmlPatchAction {
  private final String myXPath;

  public XmlPatchAction(@NotNull final String XPath) {
    myXPath = XPath;
  }

  @NotNull
  public String getXPath() {
    return myXPath;
  }

  @Nullable
  protected Namespace findNamespace(@NotNull Element element, @NotNull XmlTestBase.Schemas schema) {
    for (Element el : Arrays.asList(element, element.getDocument().getRootElement())) {
      for (Object o : el.getAdditionalNamespaces()) {
        Namespace ns = (Namespace) o;
        try {
          if (new URI(ns.getURI()).equals(new URI(schema.getUrl()))) {
            return ns;
          }
        } catch (URISyntaxException e) {
          //NOP
        }
      }
    }
    return element.getNamespace(schema.getMappged());
  }

  protected abstract void action(@NotNull Element element);
}
