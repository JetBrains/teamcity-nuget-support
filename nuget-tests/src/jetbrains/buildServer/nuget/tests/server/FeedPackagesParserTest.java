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

package jetbrains.buildServer.nuget.tests.server;

import jetbrains.buildServer.BaseTestCase;
import jetbrains.buildServer.nuget.server.feed.reader.FeedPackage;
import jetbrains.buildServer.nuget.server.feed.reader.impl.PackagesFeedParser;
import jetbrains.buildServer.nuget.server.feed.reader.impl.PackagesFeedParserImpl;
import jetbrains.buildServer.nuget.tests.integration.Paths;
import jetbrains.buildServer.util.FileUtil;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Collection;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 12.08.11 13:44
 */
public class FeedPackagesParserTest extends BaseTestCase {
  private PackagesFeedParser myParser = new PackagesFeedParserImpl();

  @Test
  public void test_ParseRealFeed() throws JDOMException, IOException {
    final Element doc = FileUtil.parseDocument(Paths.getTestDataPath("feed/reader/feed-response.xml"));
    boolean hasLatest = false;
    final Collection<FeedPackage> feedPackages = myParser.readPackages(doc);
    for (FeedPackage feedPackage : feedPackages) {
      Assert.assertFalse(hasLatest && feedPackage.isLatestVersion(), "There could be only one latest");
      hasLatest |= feedPackage.isLatestVersion();
      System.out.println("feedPackage = " + feedPackage);
    }
  }

  @Test
  public void test_ParseOnePackage() throws JDOMException, IOException {
    final Element doc = FileUtil.parseDocument(Paths.getTestDataPath("feed/reader/feed-one.xml"));
    final Collection<FeedPackage> packages = myParser.readPackages(doc);

    Assert.assertEquals(packages.size(), 1);
    final FeedPackage pkg = packages.iterator().next();

    Assert.assertEquals(pkg.getAtomId(), "http://packages.nuget.org/v1/FeedService.svc/Packages(Id='NuGet.CommandLine',Version='1.0.11220.26')");
    Assert.assertEquals(pkg.getDownloadUrl(), "http://packages.nuget.org/v1/Package/Download/NuGet.CommandLine/1.0.11220.26");
    Assert.assertEquals(pkg.getInfo().getId(), "NuGet.CommandLine");
    Assert.assertEquals(pkg.getInfo().getVersion(), "1.0.11220.26");
    Assert.assertEquals(pkg.isLatestVersion(), false);
    Assert.assertTrue(pkg.getDescription().length() > 0, "package should have deseciription");
  }

  @Test
  public void test_broken() {
    Element el = new Element("broken");
    final Collection<FeedPackage> feedPackages = myParser.readPackages(el);
    Assert.assertTrue(feedPackages.isEmpty());
  }
}
