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

package jetbrains.buildServer.nuget.tests.server.entity;

import jetbrains.buildServer.BaseTestCase;
import jetbrains.buildServer.nuget.server.feed.FeedClient;
import jetbrains.buildServer.nuget.server.feed.impl.FeedHttpClientHolder;
import jetbrains.buildServer.nuget.tests.integration.Paths;
import jetbrains.buildServer.util.FileUtil;
import jetbrains.buildServer.util.XmlUtil;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.xpath.XPath;
import org.odata4j.edm.EdmSimpleType;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 30.12.11 17:57
 */
public class EntityGenerator extends BaseTestCase {

  @Test
  public void generateEntitries() throws IOException {
    final FeedClient fc = new FeedHttpClientHolder();
    final HttpGet get = new HttpGet("https://nuget.org/api/v2/$metadata");
    try {
      final HttpResponse execute = fc.execute(get);
      final ByteArrayOutputStream box = new ByteArrayOutputStream();
      final HttpEntity entity = execute.getEntity();
      entity.writeTo(box);
      final String source = box.toString("utf-8");

      System.out.println("source = " + source);
    } finally {
      get.abort();
    }
  }
  
  @Test
  public void test_parses_properties() throws JDOMException, IOException {
    Assert.assertFalse(generateBeans().isEmpty());
  }


  public List<Property> generateBeans() throws JDOMException, IOException {
    final File data = Paths.getTestDataPath("feed/odata/metadata.v2.xml");
    Assert.assertTrue(data.isFile());

    final Element root = FileUtil.parseDocument(data);
    final Namespace edmx = Namespace.getNamespace("http://schemas.microsoft.com/ado/2007/06/edmx");
    final Namespace edm = Namespace.getNamespace("http://schemas.microsoft.com/ado/2006/04/edm");

    final XPath xPath = XPath.newInstance("/x:Edmx/x:DataServices/m:Schema/m:EntityType[@Name='V2FeedPackage']/m:Property");

    xPath.addNamespace(edmx);
    xPath.addNamespace("m", edm.getURI());
    xPath.addNamespace("x", edmx.getURI());

    final List<Property> result = new ArrayList<Property>();
    for (Object o : xPath.selectNodes(root)) {
      Element el = (Element) o;
      System.out.println(XmlUtil.to_s(el));
      result.add(new Property(el.getAttributeValue("Name"), EdmSimpleType.getSimple(el.getAttributeValue("Type"))));
    }
    return result;
  }
  
  private static final class Property {
    private final String myName;
    private final EdmSimpleType myType;

    private Property(String name, EdmSimpleType type) {
      myName = name;
      myType = type;
    }
  }
  
}
