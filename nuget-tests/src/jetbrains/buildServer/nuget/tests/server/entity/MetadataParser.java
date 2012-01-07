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

package jetbrains.buildServer.nuget.tests.server.entity;

import jetbrains.buildServer.nuget.tests.integration.Paths;
import jetbrains.buildServer.util.FileUtil;
import jetbrains.buildServer.util.XmlUtil;
import org.jdom.Attribute;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.xpath.XPath;
import org.jetbrains.annotations.NotNull;
import org.odata4j.edm.EdmSimpleType;
import org.testng.Assert;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 07.01.12 9:49
 */
public class MetadataParser {
  @NotNull
  public static ParseResult loadBeans() throws JDOMException, IOException {
    final File data = Paths.getTestDataPath("feed/odata/metadata.v2.xml");
    Assert.assertTrue(data.isFile());

    return loadBeans(FileUtil.parseDocument(data));
  }

  @NotNull
  public static ParseResult loadBeans(@NotNull final Element root) throws JDOMException {
    final Namespace edmx = Namespace.getNamespace("http://schemas.microsoft.com/ado/2007/06/edmx");
    final Namespace edm = Namespace.getNamespace("http://schemas.microsoft.com/ado/2006/04/edm");

    final XPath xKeys = XPath.newInstance("/x:Edmx/x:DataServices/m:Schema/m:EntityType[@Name='V2FeedPackage']/m:Key/m:PropertyRef/@Name");
    xKeys.addNamespace("m", edm.getURI());
    xKeys.addNamespace("x", edmx.getURI());

    final List<String> keyNames = new ArrayList<String>();
    for (Object o : xKeys.selectNodes(root)) {
      keyNames.add(((Attribute) o).getValue());
    }

    System.out.println("Selected keys: " + keyNames);
    final XPath xProps = XPath.newInstance("/x:Edmx/x:DataServices/m:Schema/m:EntityType[@Name='V2FeedPackage']/m:Property");
    xProps.addNamespace("m", edm.getURI());
    xProps.addNamespace("x", edmx.getURI());

    final List<Property> keys = new ArrayList<Property>();
    final List<Property> props = new ArrayList<Property>();
    for (Object o : xProps.selectNodes(root)) {
      Element el = (Element) o;
      System.out.println(XmlUtil.to_s(el));
      final Property prop = new Property(el.getAttributeValue("Name"), EdmSimpleType.getSimple(el.getAttributeValue("Type")));
      if (keyNames.contains(prop.getName())) {
        keys.add(prop);
      }
      props.add(prop);
    }
    return new ParseResult(keys, props);
  }

}
