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

import jetbrains.buildServer.nuget.server.feed.FeedClient;
import jetbrains.buildServer.nuget.server.feed.impl.FeedHttpClientHolder;
import jetbrains.buildServer.util.XmlUtil;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.jdom.JDOMException;
import org.jetbrains.annotations.NotNull;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 07.01.12 9:50
 */
public class MetadataLoaderTest {
  @Test
  public void test_parses_properties() throws JDOMException, IOException {
    final MetadataParseResult result = XmlFeedParsers.loadBeans_v2();
    Assert.assertFalse(result.getData().isEmpty());
    Assert.assertFalse(result.getKey().isEmpty());

    for (MetadataBeanProperty property : result.getData()) {
      if (property.getAtomPath() != null) return;
    }
    Assert.fail("Property must have FC_AtomPath");
  }

  @Test
  public void generateEntitries_v1() throws IOException, JDOMException {
    final FeedClient fc = new FeedHttpClientHolder();
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
    MetadataParseResult our = XmlFeedParsers.loadBeans_v2();

    assertDiff(result.getKey(), our.getKey());
    assertDiff(result.getData(), our.getData());
  }

  private <T> void assertDiff(@NotNull Collection<T> a, @NotNull Collection<T> b) {
    final HashSet<T> sa = new HashSet<T>(a);
    final HashSet<T> sb = new HashSet<T>(b);

    if (sa.equals(sb)) return;

    sa.removeAll(b);
    sb.removeAll(a);

    final StringBuilder s = new StringBuilder();
    if (!sa.isEmpty()) {
      s.append("a-b:").append(sa).append(", ");
    }

    if (!sb.isEmpty()) {
      s.append("b-a:").append(sb).append(", ");
    }

    if (sa.isEmpty() && sb.isEmpty()) {
      s.append("Incorrect comparison. Setts are not equal, but dirrerences are");
    }

    Assert.fail(s.toString());
  }

  @NotNull
  private MetadataParseResult fetchNuGetOrgMetadata_v2() throws IOException, JDOMException {
    return fetchNuGetMetadata("http://packages.nuget.org/api/v2/$metadata");
  }

  @NotNull
  private MetadataParseResult fetchNuGetMetadata(String uri) throws IOException, JDOMException {
    final FeedClient fc = new FeedHttpClientHolder();
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
