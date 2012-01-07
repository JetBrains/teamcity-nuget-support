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

package jetbrains.buildServer.nuget.tests.feed;

import org.jdom.Attribute;
import org.jdom.Element;
import org.jdom.Namespace;
import org.jetbrains.annotations.NotNull;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 10.09.11 19:57
 */
public class CleanElement extends XmlPatchAction {
  public CleanElement(String XPath) {
    super(XPath);
  }

  @Override
  protected void action(@NotNull Element element) {
    //hack to use named namespace here.
    final Namespace namespace = findNamespace(element, XmlTestBase.Schemas.M);
    if (namespace != null) {
      final Attribute attr = element.getAttribute("null", namespace);
      if (attr != null) attr.detach();
    }
    final Attribute attr = element.getAttribute("null");
    if (attr != null) attr.detach();

    element.getContent().clear();
    element.setText("REMOVED");
  }
}
