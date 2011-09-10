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

package jetbrains.buildServer.nuget.tests.feed;

import jetbrains.buildServer.nuget.server.feed.render.NuGetContext;
import jetbrains.buildServer.nuget.server.feed.render.NuGetFeedRenderer;
import jetbrains.buildServer.nuget.server.feed.render.NuGetItem;
import jetbrains.buildServer.nuget.server.feed.render.impl.LocalNuGetPackageItemsFactory;
import jetbrains.buildServer.nuget.server.feed.render.impl.PackageLoadException;
import jetbrains.buildServer.nuget.tests.integration.Paths;
import org.jdom.JDOMException;
import org.jetbrains.annotations.NotNull;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 05.09.11 23:56
 */
public class NuGetFeedRendererTest extends XmlTestBase {
  private NuGetFeedRenderer myRenderer;

  @BeforeMethod
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    myRenderer = new NuGetFeedRenderer();
  }

  private String testRender(@NotNull Collection<NuGetItem> items) throws XMLStreamException, IOException {
    StringWriter sw = new StringWriter();
    myRenderer.renderFeed(new NuGetContext(), items, sw);
    return sw.toString();
  }

  @Test
  public void test_empty_feed() throws XMLStreamException, IOException, JDOMException {
    String s = testRender(Collections.<NuGetItem>emptyList());

    assertXml("reader/feed-empty.xml", s);
  }

  @Test
  public void test_one_package_ninject_mvc_2_2_2_0() throws XMLStreamException, IOException, JDOMException, PackageLoadException {
    LocalNuGetPackageItemsFactory f = new LocalNuGetPackageItemsFactory();
    final NuGetItem item = f.createPackage("teamcity url", Paths.getTestDataPath("packages/Ninject.MVC3.2.2.2.0.nupkg"), true, false);
    String s = testRender(Arrays.asList(item));

    assertXml("reader/feed-ninject.mvc3.latest.xml", s);
  }

  @Override
  protected void registerXmlPreprocessors(@NotNull Collection<XmlPatchAction> result) throws JDOMException {
    super.registerXmlPreprocessors(result);
    result.add(new SetContentXmlPatchAction("/x:feed/x:updated", ".*", "UPDATED"));
    result.add(new SetContentXmlPatchAction("/x:feed/x:entry/x:updated", ".*", "UPDATED"));
    result.add(new RemoveElement("/x:feed/x:author"));
    result.add(new RemoveElement("/x:feed/x:entry/x:link[@rel='edit']"));
    result.add(new RemoveElement("/x:feed/x:entry/x:link[@rel='edit-media']"));
    result.add(new RemoveElement("/x:feed/x:entry/x:link[@title='Screenshots']"));

    for (String name : Arrays.asList(
            "Copyright", "VersionRating", "VersionRatingsCount",
            "VersionDownloadCount",  "Created", "LastUpdated",
            "Published", "ExternalPackageUrl", "IconUrl",
            "Rating", "RatingsCount", "DownloadCount",
            "ReportAbuseUrl", "GalleryDetailsUrl")) {
      result.add(new CleanElement("/x:feed/x:entry/m:properties/d:" + name));
    }

    result.add(new CleanAttribute("/x:feed/x:entry/x:content", Schemas.X, "src"));
  }
}
