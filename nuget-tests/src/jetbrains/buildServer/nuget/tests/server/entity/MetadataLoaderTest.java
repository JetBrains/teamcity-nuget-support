

package jetbrains.buildServer.nuget.tests.server.entity;

import jetbrains.buildServer.nuget.feedReader.NuGetFeedClient;
import jetbrains.buildServer.nuget.feedReader.impl.NuGetFeedHttpClientHolder;
import jetbrains.buildServer.util.CollectionsUtil;
import jetbrains.buildServer.util.XmlUtil;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.jdom.JDOMException;
import org.jetbrains.annotations.NotNull;
import org.testng.Assert;
import org.testng.annotations.Test;

import javax.net.ssl.SSLException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 07.01.12 9:50
 */
public class MetadataLoaderTest {
  @Test
  public void test_parses_properties() throws JDOMException, IOException {
    final MetadataParseResult result = XmlFeedParsers.loadBeans_v3();
    Assert.assertFalse(result.getData().isEmpty());
    Assert.assertFalse(result.getKey().isEmpty());

    for (MetadataBeanProperty property : result.getData()) {
      if (property.getAtomPath() != null) return;
    }
    Assert.fail("Property must have FC_AtomPath");
  }

  @Test
  public void test_parses_properties4() throws JDOMException, IOException {
    final MetadataParseResult result = XmlFeedParsers.loadBeans_v4();
    Assert.assertFalse(result.getData().isEmpty());
    Assert.assertFalse(result.getKey().isEmpty());

    for (MetadataBeanProperty property : result.getData()) {
      if (property.getAtomPath() != null) return;
    }
    Assert.fail("Property must have FC_AtomPath");
  }

  @Test
  public void generateEntitries_v1() throws IOException, JDOMException {
    final NuGetFeedClient fc = new NuGetFeedHttpClientHolder();
    final HttpGet get = new HttpGet("https://nuget.org/api/v1/$metadata");
    try {
      final HttpResponse execute = fc.execute(get);
      final ByteArrayOutputStream box = new ByteArrayOutputStream();
      final HttpEntity entity = execute.getEntity();
      entity.writeTo(box);
      final String source = box.toString("utf-8");
      System.out.println("source = " + source);

      final MetadataParseResult result = XmlFeedParsers.loadMetadataBeans(XmlUtil.from_s(source));
      Assert.assertFalse(result.getData().isEmpty());
      Assert.assertFalse(result.getKey().isEmpty());
    } catch (SSLException ignored) {
      // ignore SSL problems on agents with outdated JDK
      if (!ignored.getMessage().contains("hostname in certificate didn't match")) {
        throw ignored;
      }
    } finally {
      get.abort();
    }
  }

  @Test
  public void generateEntitries_v2() throws IOException, JDOMException {
    final MetadataParseResult result = fetchNuGetOrgMetadata_v2();
    Assert.assertFalse(result.getData().isEmpty());
    Assert.assertFalse(result.getKey().isEmpty());
  }

  @Test
  public void test_feed_api_not_changed() throws JDOMException, IOException {
    MetadataParseResult result = fetchNuGetOrgMetadata_v2();
    MetadataParseResult our = XmlFeedParsers.loadBeans_v5();

    assertSameDataReturned(result.getKey(), our.getKey());
    assertSameDataReturned(result.getData(), our.getData());
  }

  @SuppressWarnings("ConstantConditions")
  private void assertSameDataReturned(@NotNull Collection<MetadataBeanProperty> fetched,
                                      @NotNull Collection<MetadataBeanProperty> actual) {
    final Map<String, MetadataBeanProperty> fetchedM = map(fetched);
    final Map<String, MetadataBeanProperty> actualM = map(actual);

    final StringBuilder sb = new StringBuilder();
    final Set<String> allKeys = new TreeSet<String>(CollectionsUtil.join(fetchedM.keySet(), actualM.keySet()));
    for (String key : allKeys) {
      final MetadataBeanProperty fetchedKey = fetchedM.get(key);
      final MetadataBeanProperty actualKey = actualM.get(key);

      if (fetchedKey == null && actualKey == null) continue;
      if (fetchedKey == null && actualKey != null) {
        sb.append(actualKey).append(" was not fetched").append("\n");
        continue;
      }
      if (fetchedKey != null && actualKey == null) {
        sb.append(fetchedKey).append(" was not fetched").append("\n");
        continue;
      }

      if (fetchedKey.equals(actualKey)) continue;

      sb.append(key).append(":\n").append("  fetched: ").append(fetchedKey).append("\n   actual: ").append(actualKey).append("\n");
    }

    if (sb.length() > 0) {
      Assert.fail(sb.toString());
    }
  }

  private Map<String, MetadataBeanProperty> map(@NotNull Collection<MetadataBeanProperty> props) {
    final Map<String, MetadataBeanProperty> map = new TreeMap<String, MetadataBeanProperty>();
    for (MetadataBeanProperty p : props) {
      map.put(p.getName(), p);
    }
    return map;
  }

  @NotNull
  private MetadataParseResult fetchNuGetOrgMetadata_v2() throws IOException, JDOMException {
    return fetchNuGetMetadata("http://packages.nuget.org/api/v2/$metadata");
  }

  @NotNull
  private MetadataParseResult fetchNuGetMetadata(String uri) throws IOException, JDOMException {
    final NuGetFeedClient fc = new NuGetFeedHttpClientHolder();
    final HttpGet get = new HttpGet(uri);
    try {
      final HttpResponse execute = fc.execute(get);
      final ByteArrayOutputStream box = new ByteArrayOutputStream();
      final HttpEntity entity = execute.getEntity();
      entity.writeTo(box);
      final String source = box.toString("utf-8");
      System.out.println("source = " + source);

      return XmlFeedParsers.loadMetadataBeans(XmlUtil.from_s(source));
    } finally {
      get.abort();
    }
  }
}
