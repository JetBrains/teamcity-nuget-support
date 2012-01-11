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
import org.odata4j.core.OAtomEntity;
import org.odata4j.edm.EdmSimpleType;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 30.12.11 17:57
 */

public class EntityGenerator extends BaseTestCase {
  @Test
  public void generateEntityClasses() throws IOException, JDOMException {
    final String entity = "PackageEntityImpl";
    final String ientityV2 = "PackageEntityV2";

    final ParseResult V2 = MetadataParser.loadBeans_v2();

    new EntityInterfaceGenerator(ientityV2, V2.getKey(), V2.getData()).generateSimpleBean();
    new EntityBeanGenerator(entity, Arrays.asList(ientityV2), V2.getData()).generateSimpleBean();
  }

  private static class EntityBeanGenerator extends BeanGenerator {
    private final Collection<String> myIentities;
    private final Set<String> myExplicit = new HashSet<String>(Arrays.asList(
                "Created",
                "Published",
                "GalleryDetailsUrl",
                "Summary",
                "Title",
                "VersionDownloadCount",
                "DownloadCount"
        ));


    private EntityBeanGenerator(String entityName, List<String> ientity, Collection<Property> properties) {
      super(entityName, properties);
      myIentities = ientity;
    }

    @NotNull
    @Override
    protected Collection<String> getImplements() {
      final List<String> result = new ArrayList<String>();
      result.addAll(myIentities);
      result.add(OAtomEntity.class.getSimpleName());
      return result;
    }

    @Override
    protected void generateAfterContent(@NotNull PrintWriter wr) {
      super.generateAfterContent(wr);
      for (Property property : myProperties) {
        String path = property.getAtomPath();
        if (path == null) continue;
        path = path.substring("Syndication".length()).replace("AuthorName", "Author");

        wr.println();
        wr.println("  public final String getAtomEntity" + path + "() {");
        if (property.getType() == EdmSimpleType.DATETIME) {
          wr.println("    return InternalUtil.toString(get" + property.getName() + "().toDateTime(DateTimeZone.UTC));");
        } else {
          wr.println("    return get" + property.getName() + "();");
        }
        wr.println("  }");
        wr.println();
      }
    }

    @Override
    protected void generateProperty(@NotNull final PrintWriter w, @NotNull final Property p) {
      if (myExplicit.contains(p.getName())) return;
      super.generateProperty(w, p);
    }
  }

  private static class EntityInterfaceGenerator extends MethodsGenerator {
    private final Collection<Property> myKeys;

    private EntityInterfaceGenerator(String entityName, Collection<Property> keys, Collection<Property> properties) {
      super(entityName, properties);
      myKeys = keys;
    }

    @Override
    protected void generateAfterContent(@NotNull PrintWriter wr) {
      wr.println();
      wr.println("  String[] KeyPropertyNames = new String[] {");
      for (Property property : myKeys) {
        wr.println("    \"" + property.getName() + "\", ");
      }
      wr.println("  };");
      wr.println();
      wr.println();
    }

    @Override
    protected String getTypeKind() {
      return "interface";
    }

    @Override
    protected String getExtendsString() {
      return "";
    }

    @NotNull
    @Override
    protected String generatePropertyModifier(@NotNull Property p) {
      return "";
    }

    @Override
    protected void generatePropertyBody(@NotNull PrintWriter wr, @NotNull Property p) {
      wr.println(";");
    }

    @Override
    protected void generateBeforeContent(@NotNull PrintWriter wr) {
    }
  }
}
