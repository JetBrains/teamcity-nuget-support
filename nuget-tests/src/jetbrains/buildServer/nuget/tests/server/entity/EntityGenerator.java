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

import jetbrains.buildServer.BaseTestCase;
import org.jdom.JDOMException;
import org.jetbrains.annotations.NotNull;
import org.odata4j.core.OAtomEntity;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.*;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 30.12.11 17:57
 */

public class EntityGenerator extends BaseTestCase {
  public static void main(String[] args) throws IOException, JDOMException {
    new MetadataLoaderTest().test_feed_api_not_changed();

    final String entity = "PackageEntityImpl";
    final String ientityV2 = "PackageEntityV2";
    final String ientityV3 = "PackageEntityV3";

    final MetadataParseResult V2 = XmlFeedParsers.loadBeans_v3();
    final MetadataParseResult V3 = XmlFeedParsers.loadBeans_v4();

    new EntityInterfaceGenerator(ientityV2, V2.getKey(), V2.getData()).generateSimpleBean();
    new EntityInterfaceGenerator(ientityV3, V3.getKey(), V3.getData()).generateSimpleBean();

    final Set<MetadataBeanProperty> data = new LinkedHashSet<MetadataBeanProperty>();
    data.addAll(V2.getData());
    data.addAll(V3.getData());
    new EntityBeanGenerator(entity, Arrays.asList(ientityV2, ientityV3), data).generateSimpleBean();
  }

  private static class EntityBeanGenerator extends BeanGenerator {
    private final Collection<String> myIentities;
    private final Set<String> myExplicit = new HashSet<String>(Arrays.asList(
                "Created",
                "LastEdited",
                "Published",
                "GalleryDetailsUrl",
                "Summary",
                "Title",
                "VersionDownloadCount",
                "DownloadCount"
        ));


    private EntityBeanGenerator(String entityName, List<String> ientity, Collection<MetadataBeanProperty> properties) {
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
      for (MetadataBeanProperty property : myProperties) {
        String path = property.getAtomPath();
        if (path == null) continue;
        path = path.substring("Syndication".length()).replace("AuthorName", "Author");

        wr.println();
        wr.println("  public final " + property.getType().getCanonicalJavaType().getName() + " getAtomEntity" + path + "() {");
        wr.println("    return get" + property.getName() + "();");
        wr.println("  }");
        wr.println();
      }
    }

    @Override
    protected void generateProperty(@NotNull final PrintWriter w, @NotNull final MetadataBeanProperty p) {
      if (myExplicit.contains(p.getName())) return;
      super.generateProperty(w, p);
    }
  }

  private static class EntityInterfaceGenerator extends MethodsGenerator {
    private final Collection<MetadataBeanProperty> myKeys;

    private EntityInterfaceGenerator(String entityName, Collection<MetadataBeanProperty> keys, Collection<MetadataBeanProperty> properties) {
      super(entityName, properties);
      myKeys = keys;
    }

    @Override
    protected void generateAfterContent(@NotNull PrintWriter wr) {
      wr.println();
      wr.println("  String[] KeyPropertyNames = new String[] {");
      for (MetadataBeanProperty property : myKeys) {
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
    protected String generatePropertyModifier(@NotNull MetadataBeanProperty p) {
      return "";
    }

    @Override
    protected void generatePropertyBody(@NotNull PrintWriter wr, @NotNull MetadataBeanProperty p) {
      wr.println(";");
    }

    @Override
    protected void generateBeforeContent(@NotNull PrintWriter wr) {
    }
  }
}
