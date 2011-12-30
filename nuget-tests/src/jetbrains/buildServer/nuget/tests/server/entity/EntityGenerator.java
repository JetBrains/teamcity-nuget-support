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
import org.jdom.Attribute;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.Namespace;
import org.jdom.xpath.XPath;
import org.odata4j.edm.EdmSimpleType;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 30.12.11 17:57
 */

@Test(enabled = false)
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
    Assert.assertFalse(generateBeans().myData.isEmpty());
    Assert.assertFalse(generateBeans().myKey.isEmpty());
  }

  @Test
  public void generateEntityClasses() throws IOException, JDOMException {
    new BeanGenerator("PackageEntity", generateBeans().myData).generateSimpleBean();
    new BeanGenerator("PackageKey", generateBeans().myKey).generateSimpleBean();
  }
  
  private static class BeanGenerator {
    private final String myName;
    private final Collection<Property> myProperties;

    private BeanGenerator(String name, Collection<Property> properties) {
      myName = name;
      myProperties = properties;
    }

    public void generateSimpleBean() throws IOException {
     
        final File file = new File("nuget-server/src/jetbrains/buildServer/nuget/server/feed/server/entity/" + myName + ".java");
        final String pkg = "jetbrains.buildServer.nuget.server.feed.server.entity";
        FileUtil.createParentDirs(file);
    
        PrintWriter wr = new PrintWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "utf-8")));
    
        wr.println("package " + pkg + ";");
        wr.println();
        wr.println("import java.util.*;");
        wr.println("import java.lang.*;");
        wr.println();
        wr.println("public class " + myName + " { ");
        wr.println("  private final Map<String, Object> myFields = new HashMap<String, Object>();");
        wr.println();
        for (Property p : myProperties) {
          wr.println();
          final String type = p.myType.getCanonicalJavaType().getName();
          final String name = p.myName;
          wr.println("  public " + type + " get" + name + "() { ");
          wr.println("    return " + type + ".class.cast(myFields.get(\"" + name + "\"));");
          wr.println("  }");
          wr.println();
          wr.println("  public void set" + name + "(final " + type + " v) { ");
          wr.println("    myFields.put(\"" + name + "\", v);");
          wr.println("  }");
          wr.println();
        }
    
        wr.println();
        wr.println(" public boolean isValid() { ");
        for (Property p : myProperties) {
          wr.println("    if (!myFields.containsKey(\"" + p.myName + "\")) return false;");
        }
        wr.println("    return true;");
        wr.println("  }");
        wr.println("}");
        wr.println();
    
        wr.flush();
        wr.close();
      }
      
  }



  public ParseResult generateBeans() throws JDOMException, IOException {
    final File data = Paths.getTestDataPath("feed/odata/metadata.v2.xml");
    Assert.assertTrue(data.isFile());

    final Element root = FileUtil.parseDocument(data);
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
      if (keyNames.contains(prop.myName)) {
        keys.add(prop);
      }
      props.add(prop);
    }
    return new ParseResult(keys, props);
  }
  
  private static final class ParseResult {
    private final Collection<Property> myKey;
    private final Collection<Property> myData;

    private ParseResult(Collection<Property> key, Collection<Property> data) {
      myKey = key;
      myData = data;
    }
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
