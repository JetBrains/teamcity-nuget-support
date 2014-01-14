/*
 * Copyright 2000-2014 JetBrains s.r.o.
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

package jetbrains.buildServer.nuget.tests.server.feed.reader;

import jetbrains.buildServer.BaseTestCase;
import jetbrains.buildServer.nuget.server.feed.reader.FeedPackage;
import jetbrains.buildServer.nuget.server.feed.reader.impl.PackagesFeedParser;
import jetbrains.buildServer.nuget.server.feed.reader.impl.PackagesFeedParserImpl;
import jetbrains.buildServer.nuget.tests.integration.Paths;
import jetbrains.buildServer.util.FileUtil;
import jetbrains.buildServer.util.TestFor;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;

import static com.intellij.openapi.util.text.StringUtil.isEmptyOrSpaces;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 12.08.11 13:44
 */
public class FeedPackagesParserTest extends BaseTestCase {
  private PackagesFeedParser myParser = new PackagesFeedParserImpl();

  @Test
  public void test_ParseRealFeed() throws JDOMException, IOException {
    String url = "feed/reader/feed-response.xml";
    final Collection<FeedPackage> feedPackages = readPackages(url, null);
    boolean hasLatest = false;
    for (FeedPackage feedPackage : feedPackages) {
      Assert.assertFalse(hasLatest && feedPackage.isLatestVersion(), "There could be only one latest");
      hasLatest |= feedPackage.isLatestVersion();
      System.out.println("feedPackage = " + feedPackage);
    }
  }

  @Test
  @TestFor(issues = "TW-21048")
  public void test_ParseNextLink() throws JDOMException, IOException {
    String nextUrl = "http://packages.nuget.org/v1/FeedService.svc/Packages?$skiptoken='Adam.JSGenerator','1.2.0.0'";
    Collection<FeedPackage> packages = readPackages("feed/reader/feed-next.xml", nextUrl);
    Assert.assertEquals(packages.size(), 100);
  }

  @Test
  public void test_ParseOnePackage() throws JDOMException, IOException {
    final Collection<FeedPackage> packages = readPackages("feed/reader/feed-one.xml", null);

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
  public void test_ParsePackagesNewFormat_2011_12_06() throws JDOMException, IOException {
    final Collection<FeedPackage> packages = readPackages("feed/reader/feed-new.xml", null);

    Assert.assertEquals(packages.size(), 21);
    for (FeedPackage pkg : packages) {
      Assert.assertFalse(isEmptyOrSpaces(pkg.getAtomId()));
      Assert.assertFalse(isEmptyOrSpaces(pkg.getDownloadUrl()));
      Assert.assertEquals(pkg.getInfo().getId(), "NuGet.CommandLine");
      Assert.assertTrue(pkg.getInfo().getVersion().startsWith("1."));
      Assert.assertTrue(pkg.getDescription().length() > 0, "package should have deseciription");
    }
  }

  @Test
  public void test_broken() {
    Element el = new Element("broken");
    final Collection<FeedPackage> feedPackages = new ArrayList<FeedPackage>();
    myParser.readPackages(el, feedPackages);
    Assert.assertTrue(feedPackages.isEmpty());
  }

  @NotNull
  private Collection<FeedPackage> readPackages(@NotNull String url, @Nullable final String nextUrl) throws JDOMException, IOException {
    final Element doc = FileUtil.parseDocument(Paths.getTestDataPath(url));
    final Collection<FeedPackage> feedPackages = new ArrayList<FeedPackage>();
    Assert.assertEquals(myParser.readPackages(doc, feedPackages), nextUrl);
    return feedPackages;
  }

}
