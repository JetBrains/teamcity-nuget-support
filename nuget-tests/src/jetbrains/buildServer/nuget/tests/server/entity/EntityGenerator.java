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
import org.jdom.JDOMException;
import org.jetbrains.annotations.NotNull;
import org.odata4j.edm.EdmSimpleType;
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
    protected void fieldsGenerated(@NotNull PrintWriter wr) {
      super.fieldsGenerated(wr);
      wr.println("  public void visitFields(@NotNull final PackageFieldsVisitor visitor) {");
      wr.println("    String v;");
      for (Property property : myProperties) {
        wr.println("    v = myFields.get(\"" + property.getName() + "\");");
        if (property.getType() == EdmSimpleType.DATETIME) {
          wr.println("    v = jetbrains.buildServer.nuget.server.feed.server.index.ODataDataFormat.toODataString(v);");
        }
        wr.println("    if (v != null) visitor.visitPackageField(\"" + property.getName() + "\", v, \"" + property.getType().getFullyQualifiedTypeName() + "\");");

      }
      wr.println("  }");
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

      wr.println("  public " + myName + "(@NotNull final " + myEntityName + " entity) {");
      wr.println("    this(toMap(entity));");
      wr.println("  }");
      wr.println();
      wr.println("  @NotNull");
      wr.println("  private static Map<String, String> toMap(@NotNull final PackageEntity e) {");
      wr.println("    final Map<String, String> map = new HashMap<String, String>();");
      wr.println("    map.put(\"Id\", e.getId());");
      wr.println("    map.put(\"Version\", e.getVersion());");
      wr.println("    return map;");
      wr.println("  }");
    }
  }


}
