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
import org.jdom.JDOMException;
import org.jetbrains.annotations.NotNull;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.io.StringWriter;
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
}
