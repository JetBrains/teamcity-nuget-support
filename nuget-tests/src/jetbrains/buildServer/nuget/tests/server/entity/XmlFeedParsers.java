/*
 * Copyright 2000-2014 JetBrains s.r.o.
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
import jetbrains.buildServer.util.StringUtil;
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
import java.util.*;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 07.01.12 9:49
 */
public class XmlFeedParsers {
  @NotNull
  public static MetadataParseResult loadBeans_v1() throws JDOMException, IOException {
    final File data = Paths.getTestDataPath("feed/odata/metadata.v1.xml");
    Assert.assertTrue(data.isFile());

    return loadMetadataBeans(FileUtil.parseDocument(data));
  }

  @NotNull
  public static MetadataParseResult loadBeans_v2() throws JDOMException, IOException {
    final File data = Paths.getTestDataPath("feed/odata/metadata.v2.xml");
    Assert.assertTrue(data.isFile());

    return loadMetadataBeans(FileUtil.parseDocument(data));
  }

  @NotNull
  public static MetadataParseResult loadBeans_v3() throws JDOMException, IOException {
    final File data = Paths.getTestDataPath("feed/odata/metadata.v3.xml");
    Assert.assertTrue(data.isFile());

    return loadMetadataBeans(FileUtil.parseDocument(data));
  }

  @NotNull
  public static MetadataParseResult loadBeans_v4() throws JDOMException, IOException {
    final File data = Paths.getTestDataPath("feed/odata/metadata.v4.xml");
    Assert.assertTrue(data.isFile());

    return loadMetadataBeans(FileUtil.parseDocument(data));
  }

  @NotNull
  public static MetadataParseResult loadBeans_v5() throws JDOMException, IOException {
    final File data = Paths.getTestDataPath("feed/odata/metadata.v5.xml");
    Assert.assertTrue(data.isFile());

    return loadMetadataBeans(FileUtil.parseDocument(data));
  }

  @NotNull
  public static MetadataParseResult loadBeans_v6() throws JDOMException, IOException {
    final File data = Paths.getTestDataPath("feed/odata/metadata.v6.xml");
    Assert.assertTrue(data.isFile());
    return loadMetadataBeans(FileUtil.parseDocument(data));
  }

  @NotNull
  public static MetadataParseResult loadMetadataBeans(@NotNull final Element root) throws JDOMException {
    final Namespace edmx = Namespace.getNamespace("http://schemas.microsoft.com/ado/2007/06/edmx");
    final Namespace edm = Namespace.getNamespace("http://schemas.microsoft.com/ado/2006/04/edm");
    final Namespace m = Namespace.getNamespace("http://schemas.microsoft.com/ado/2007/08/dataservices/metadata");

    final XPath xKeys = XPath.newInstance("/x:Edmx/x:DataServices/m:Schema/m:EntityType[@Name='V2FeedPackage' or @Name='V1FeedPackage']/m:Key/m:PropertyRef/@Name");
    xKeys.addNamespace("m", edm.getURI());
    xKeys.addNamespace("x", edmx.getURI());

    final List<String> keyNames = new ArrayList<String>();
    for (Object o : xKeys.selectNodes(root)) {
      keyNames.add(((Attribute) o).getValue());
    }

    System.out.println("Selected keys: " + keyNames);
    final XPath xProps = XPath.newInstance("/x:Edmx/x:DataServices/m:Schema/m:EntityType[@Name='V2FeedPackage' or @Name='V1FeedPackage']/m:Property");
    xProps.addNamespace("m", edm.getURI());
    xProps.addNamespace("x", edmx.getURI());

    final List<MetadataBeanProperty> keys = new ArrayList<MetadataBeanProperty>();
    final List<MetadataBeanProperty> props = new ArrayList<MetadataBeanProperty>();
    for (Object o : xProps.selectNodes(root)) {
      Element el = (Element) o;
      System.out.println(XmlUtil.to_s(el));
      final String name = el.getAttributeValue("Name");
      final EdmSimpleType<?> type = EdmSimpleType.getSimple(el.getAttributeValue("Type"));
      final String atomPath = StringUtil.nullIfEmpty(el.getAttributeValue("FC_TargetPath", m));
      final MetadataBeanProperty prop = new MetadataBeanProperty(name, type, atomPath, Boolean.parseBoolean(el.getAttributeValue("Nullable")));
      if (keyNames.contains(prop.getName())) {
        keys.add(prop);
      }
      props.add(prop);
    }
    return new MetadataParseResult(keys, props);
  }

  @NotNull
  public static FeedParseResult loadFeedBeans(@NotNull final Element root) throws JDOMException {
    final Namespace a = Namespace.getNamespace("http://www.w3.org/2005/Atom");
    final Namespace m = Namespace.getNamespace("http://schemas.microsoft.com/ado/2007/08/dataservices/metadata");
    final Namespace d = Namespace.getNamespace("http://schemas.microsoft.com/ado/2007/08/dataservices");

    final XPath xKeys = XPath.newInstance("//m:properties");
    xKeys.addNamespace("m", m.getURI());

    final List<String> names = new ArrayList<String>();
    for (Object o : xKeys.selectNodes(root)) {
      Element e = (Element) o;
      for (Object c : e.getChildren()) {
        Element child = (Element) c;
        if (child.getNamespace().equals(d)) {
          names.add(child.getName());
        }
      }
    }

    final Map<String, String> properties = new HashMap<String, String>();
    for (String xpath : Arrays.asList("//a:entry/a:title", "//a:entry/a:summary", "//a:entry/a:author", "//a:entry/a:category", "//a:entry/a:content/@type")) {
      XPath xPath = XPath.newInstance(xpath);
      xPath.addNamespace("a", a.getURI());
      Object o = xPath.selectSingleNode(root);
      if (o != null) {
        if (o instanceof Element) {
          properties.put(xpath, XmlUtil.to_s((Element)o));
        } else if (o instanceof Attribute) {
          properties.put(xpath, ((Attribute) o).getValue());
        }
      }
    }


    return new FeedParseResult(names, properties);
  }

}
