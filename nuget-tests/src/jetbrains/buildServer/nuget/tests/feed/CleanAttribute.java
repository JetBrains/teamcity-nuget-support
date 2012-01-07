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

import org.jdom.Attribute;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jetbrains.annotations.NotNull;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 10.09.11 20:22
 */
public class CleanAttribute extends XmlPatchAction {
  private final XmlTestBase.Schemas mySchema;
  private final String myAttributeName;

  public CleanAttribute(String XPath, XmlTestBase.Schemas schema, String attributeName) {
    super(XPath);
    mySchema = schema;
    myAttributeName = attributeName;
  }

  @Override
  protected void action(@NotNull Element element) {
    final Namespace namespace = findNamespace(element, mySchema);
    if (namespace == null) {
      final Attribute attribute = element.getAttribute(myAttributeName);
      if (attribute != null) attribute.setValue("REMOVED");
    } else {
      final Attribute attribute = element.getAttribute(myAttributeName, namespace);
      if (attribute != null) attribute.setValue("REMOVED");
    }
  }
}
