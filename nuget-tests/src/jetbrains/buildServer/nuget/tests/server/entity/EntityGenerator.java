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
import jetbrains.buildServer.nuget.server.feed.server.index.ODataDataFormat;
import jetbrains.buildServer.util.FileUtil;
import jetbrains.buildServer.util.StringUtil;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.jdom.JDOMException;
import org.jetbrains.annotations.NotNull;
import org.odata4j.edm.EdmSimpleType;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.*;
import java.util.Collection;
import java.util.Collections;

import static jetbrains.buildServer.nuget.tests.server.entity.MetadataParser.loadBeans;

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
    Assert.assertFalse(loadBeans().getData().isEmpty());
    Assert.assertFalse(loadBeans().getKey().isEmpty());
  }

  @Test
  public void generateEntityClasses() throws IOException, JDOMException {
    final String key = "PackageKey";
    final String entity = "PackageEntity";
    new EntityBeanGenerator(key, entity, loadBeans().getData()).generateSimpleBean();
    new KeyBeanGenerator(key, entity, loadBeans().getKey()).generateSimpleBean();
  }

  private static class EntityBeanGenerator extends BeanGenerator {
    private final String myKeyName;
    private EntityBeanGenerator(String keyName, String entityName, Collection<Property> properties) {
      super(entityName, properties);
      myKeyName = keyName;
    }

    @Override
    protected String getExtends() {
      return myKeyName;
    }

    @Override
    protected void generateConstructor(PrintWriter wr) {
      wr.println("    super(data); ");
    }

    @Override
    protected void generateFields(PrintWriter wr) {
    }
  }

  private static class KeyBeanGenerator extends BeanGenerator {
    private final String myEntityName;

    private KeyBeanGenerator(String name, String entityName, Collection<Property> properties) {
      super(name, properties);
      myEntityName = entityName;
    }

    @Override
    protected Collection<String> getImplements() {
      return Collections.singleton("OEntityId");
    }

    @Override
    protected void fieldsGenerated(@NotNull PrintWriter wr) {
      super.fieldsGenerated(wr);
      
      wr.println();
      wr.println("  public OEntityKey getEntityKey() {");
      wr.println("    return OEntityKey.create(\"Id\", getId(), \"Version\", getVersion());");
      wr.println("  }");
      wr.println();
      wr.println("  public String getEntitySetName() {");
      wr.println("    return \"Packages\";");
      wr.println("  }");
      wr.println();
    }
  }

  private static class BeanGenerator {
    protected final String myName;
    protected final Collection<Property> myProperties;

    private BeanGenerator(String name, Collection<Property> properties) {
      myName = name;
      myProperties = properties;
    }

    public void generateSimpleBean() throws IOException {

      final File file = new File("nuget-server/src/jetbrains/buildServer/nuget/server/feed/server/entity/" + myName + ".java");
      final String pkg = "jetbrains.buildServer.nuget.server.feed.server.entity";
      FileUtil.createParentDirs(file);

      PrintWriter wr = new PrintWriter(new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "utf-8")));
      wr.println("/****");
      wr.println("****");
      wr.println("**** THIS CODE IS GENERATED BY " + getClass().getName());
      wr.println("**** DO NOT CHANGE!");
      wr.println("*****/");
      wr.println("package " + pkg + ";");
      wr.println();
      wr.println("import java.util.*;");
      wr.println("import java.lang.*;");
      wr.println("import org.odata4j.core.*;");
      wr.println();
      wr.println("import org.jetbrains.annotations.NotNull;");
      wr.println();

      String ext = getExtends();
      if (!StringUtil.isEmptyOrSpaces(ext)) {
        ext = " extends " + ext;
      }

      final Collection<String> impl = getImplements();
      if (!impl.isEmpty()) {
        ext += " implements " + StringUtil.join(", ", impl);
      }

      wr.println("public class " + myName + ext + " { ");
      generateFields(wr);
      wr.println();
      wr.println("  public " + myName + "(@NotNull final Map<String, String> data) {");
      generateConstructor(wr);
      wr.println("  }");
      wr.println();
      for (Property p : myProperties) {
        wr.println();
        final String type = p.getType().getCanonicalJavaType().getName();
        final String name = p.getName();
        wr.println("  public " + type + " get" + name + "() { ");
        wr.println("    final String v = myFields.get(\"" + name + "\");");
        if (p.getType() == EdmSimpleType.STRING) {
          wr.println("    return v;");
        } else if (p.getType() == EdmSimpleType.BOOLEAN){
          wr.println("    return Boolean.valueOf(v);");
        } else if (p.getType() == EdmSimpleType.INT32){
          wr.println("    return Integer.parseInt(v);");
        } else if (p.getType() == EdmSimpleType.INT64){
          wr.println("    return Long.parseLong(v);");
        } else if (p.getType() == EdmSimpleType.DATETIME){
          wr.println("    return " + ODataDataFormat.class.getName() + ".parseDate(v);");
        } else {
          wr.println("    UnsupportedTypeError");
        }
        wr.println("  }");
        wr.println();
      }

      wr.println();
      wr.println(" public boolean isValid() { ");
      for (Property p : myProperties) {
        wr.println("    if (!myFields.containsKey(\"" + p.getName() + "\")) return false;");
      }
      wr.println("    return true;");
      wr.println("  }");
      fieldsGenerated(wr);
      wr.println("}");
      wr.println();

      wr.flush();
      wr.close();
    }

    protected void generateConstructor(PrintWriter wr) {
      wr.println("    myFields = data;");
    }

    protected void generateFields(PrintWriter wr) {
      wr.println("  protected final Map<String, String> myFields;");
    }

    protected void fieldsGenerated(@NotNull final PrintWriter wr) {

    }

    protected String getExtends() {
      return "";
    }

    protected Collection<String> getImplements() {
      return Collections.emptyList();
    }
  }


}
