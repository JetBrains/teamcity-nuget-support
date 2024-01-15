

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
    final String ientityV4 = "PackageEntityV4";

    final MetadataParseResult V2 = XmlFeedParsers.loadBeans_v3();
    final MetadataParseResult V3 = XmlFeedParsers.loadBeans_v4();
    final MetadataParseResult V4 = XmlFeedParsers.loadBeans_v5();

    new EntityInterfaceGenerator(ientityV2, V2.getKey(), V2.getData()).generateSimpleBean();
    new EntityInterfaceGenerator(ientityV3, V3.getKey(), V3.getData()).generateSimpleBean();
    new EntityInterfaceGenerator(ientityV4, V4.getKey(), V4.getData()).generateSimpleBean();

    final Set<MetadataBeanProperty> data = new LinkedHashSet<MetadataBeanProperty>();
    data.addAll(V2.getData());
    data.addAll(V3.getData());
    data.addAll(V4.getData());
    new EntityBeanGenerator(entity, Arrays.asList(ientityV2, ientityV3, ientityV4), data).generateSimpleBean();
  }

  private static class EntityBeanGenerator extends BeanGenerator {
    private final Collection<String> myIentities;
    private final Set<String> myExplicit = new HashSet<String>(Arrays.asList(
                "NormalizedVersion",
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
