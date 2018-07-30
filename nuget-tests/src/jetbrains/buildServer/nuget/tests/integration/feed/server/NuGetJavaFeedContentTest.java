/*
 * Copyright 2000-2017 JetBrains s.r.o.
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

package jetbrains.buildServer.nuget.tests.integration.feed.server;

import jetbrains.buildServer.nuget.feed.server.NuGetFeedConstants;
import jetbrains.buildServer.nuget.tests.integration.Paths;
import jetbrains.buildServer.nuget.tests.server.entity.FeedParseResult;
import jetbrains.buildServer.nuget.tests.server.entity.MetadataBeanProperty;
import jetbrains.buildServer.nuget.tests.server.entity.MetadataParseResult;
import jetbrains.buildServer.nuget.tests.server.entity.XmlFeedParsers;
import jetbrains.buildServer.util.CollectionsUtil;
import jetbrains.buildServer.util.FileUtil;
import jetbrains.buildServer.util.TestFor;
import jetbrains.buildServer.util.XmlUtil;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.http.HttpStatus;
import org.jdom.*;
import org.jdom.input.JDOMParseException;
import org.jdom.xpath.XPath;
import org.jetbrains.annotations.NotNull;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static jetbrains.buildServer.nuget.feedReader.NuGetPackageAttributes.ID;
import static jetbrains.buildServer.nuget.feedReader.NuGetPackageAttributes.VERSION;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 06.01.12 22:23
 */
public class NuGetJavaFeedContentTest extends NuGetJavaFeedIntegrationTestBase {
  private static final boolean LOCAL_DIFF_GOLD_AND_GENERATED = false;
  private static final Pattern NEXT_PAGE = Pattern.compile("<link href=\"([^\"]+)\" rel=\"next\"");
  private static final Pattern NEXT_PAGE2 = Pattern.compile("<link rel=\"next\" href=\"([^\"]+)\"");

  @Test(dataProvider = "nugetFeedLibrariesData")
  public void testMetadata_v1(final NugetFeedLibrary library) throws JDOMException, IOException {
    setODataSerializer(library);
    final String s = openRequest("$metadata");
    compareStringAsXml(s, "/feed/odata/metadata.v1.xml");
  }

  @Test(dataProvider = "nugetFeedLibrariesData")
  public void testMetadata_v2(final NugetFeedLibrary library) throws JDOMException, IOException {
    setODataSerializer(library);
    final String s = openRequest("$metadata");
    checkMetadata(s, "/feed/odata/metadata.v2.xml");
  }

  private void checkMetadata(@NotNull final String metadataXml, @NotNull final String gold) throws JDOMException, IOException {
    MetadataParseResult actual = XmlFeedParsers.loadMetadataBeans(parseXml(metadataXml));
    MetadataParseResult expected = XmlFeedParsers.loadMetadataBeans(parseGoldXml(gold));

    Assert.assertEquals(listProps(actual.getKey()), listProps(expected.getKey()));
    Assert.assertTrue(listProps(actual.getData()).containsAll(listProps(expected.getData())));

    compareStringAsXml(metadataXml, gold);
  }

  private void checkFeed(@NotNull final String feedXml, @NotNull final String gold) throws JDOMException, IOException {
    final Element root = parseXml(feedXml);

    final String base = root.getAttributeValue("base", Namespace.XML_NAMESPACE);
    Assert.assertTrue(base.startsWith(getNuGetServerUrl()), base);

    XPath xp = XPath.newInstance("//a:content/@src");
    xp.addNamespace("a", "http://www.w3.org/2005/Atom");
    for (Object o : xp.selectNodes(root)) {
      Attribute att = (Attribute) o;
      final URI downloadUrl = URI.create(att.getValue());
      final URI serverUrl = URI.create(getNuGetServerUrl());
      Assert.assertEquals(downloadUrl.getAuthority(), serverUrl.getAuthority());
    }

    FeedParseResult actual = XmlFeedParsers.loadFeedBeans(root);
    FeedParseResult expected = XmlFeedParsers.loadFeedBeans(parseGoldXml(gold));

    Assert.assertFalse(actual.getPropertyNames().isEmpty());
    Assert.assertFalse(expected.getPropertyNames().isEmpty());
    Assert.assertFalse(actual.getAtomProperties().isEmpty());
    Assert.assertFalse(expected.getAtomProperties().isEmpty());

    Assert.assertTrue(actual.getPropertyNames().containsAll(expected.getPropertyNames()));
    //Assert.assertEquals(actual.getAtomProperties().toString(), expected.getAtomProperties().toString());

    compareStringAsXml(feedXml, gold);
  }

  private Set<String> listProps(Collection<MetadataBeanProperty> result) {
    return result.stream().map(MetadataBeanProperty::getName).collect(Collectors.toSet());
  }

  @Test(dataProvider = "nugetFeedLibrariesData")
  public void testRoot(final NugetFeedLibrary library) throws JDOMException, IOException {
    setODataSerializer(library);
    final String s = openRequest("");
    compareStringAsXml(s, "/feed/odata/root.v2.xml", true);
  }

  @Test(dataProvider = "nugetFeedLibrariesData")
  public void testPackages_v1(final NugetFeedLibrary library) throws JDOMException, IOException {
    setODataSerializer(library);
    addPackage(Paths.getTestDataPath("/packages/CommonServiceLocator.1.0.nupkg"), false);
    final String s = openRequest("Packages()");

    checkFeed(s, "/feed/odata/packages.v1.CommonServiceLocator.xml");
  }

  @Test(dataProvider = "nugetFeedLibrariesData")
  public void testPackages_v2(final NugetFeedLibrary library) throws JDOMException, IOException {
    setODataSerializer(library);
    addPackage(Paths.getTestDataPath("/packages/CommonServiceLocator.1.0.nupkg"), false);
    final String s = openRequest("Packages()");

    checkFeed(s, "/feed/odata/packages.v2.CommonServiceLocator.xml");
  }

  @Test(dataProvider = "nugetFeedLibrariesData")
  @TestFor(issues = "TW-26658")
  public void testPackages_title(final NugetFeedLibrary library) throws JDOMException, IOException {
    setODataSerializer(library);
    addPackage(Paths.getTestDataPath("/packages/YCM.Web.UI.1.0.20.7275.nupkg"), false);
    final String s = openRequest("Packages()");
    System.out.println(XmlUtil.to_s(XmlUtil.from_s(s)));

    Assert.assertTrue(s.contains("<d:Title>YorkNet UI Components</d:Title>"));
  }

  @Test(dataProvider = "nugetFeedLibrariesData")
  public void testPackages_count(final NugetFeedLibrary library) throws JDOMException, IOException {
    setODataSerializer(library);
    addPackage(Paths.getTestDataPath("/packages/CommonServiceLocator.1.0.nupkg"), false);
    final String s = openRequest("Packages()/$count");
    Assert.assertEquals(s, "1");
  }

  @Test(dataProvider = "nugetFeedLibrariesData")
  public void testGetById(final NugetFeedLibrary library) throws JDOMException, IOException {
    setODataSerializer(library);
    addPackage(Paths.getTestDataPath("/packages/CommonServiceLocator.1.0.nupkg"), false);
    final String s = openRequest("Packages(Id='CommonServiceLocator',Version='1.0')");
    Assert.assertTrue(s.contains("<title type=\"text\">CommonServiceLocator</title>"));
  }

  @Test(dataProvider = "nugetFeedLibrariesData")
  public void testGetByNormalizedVersion(final NugetFeedLibrary library) throws JDOMException, IOException {
    setODataSerializer(library);
    addMockPackage("foo", CollectionsUtil.asMap(ID, "foo", VERSION, "1.0.0"));

    final String s = openRequest("Packages(Id='foo',Version='1.0.0.0')");
    Assert.assertTrue(s.contains("<title type=\"text\">foo</title>"));

    assertStatusCode(HttpStatus.SC_OK, "Packages(Id='foo',Version='1.0')").run();
    assertStatusCode(HttpStatus.SC_OK, "Packages(Id='foo',Version='1.0.0')").run();
    assertStatusCode(HttpStatus.SC_OK, "Packages(Id='foo',Version='1.0.0.0')").run();
  }

  @Test(dataProvider = "nugetFeedLibrariesData")
  public void testGetByInvalidPath(final NugetFeedLibrary library) throws JDOMException, IOException {
    setODataSerializer(library);
    assertStatusCode(HttpStatus.SC_BAD_REQUEST, "Packages/CommonServiceLocator/1.0").run();
  }

  @Test(dataProvider = "nugetFeedLibrariesData")
  public void testGetPackageByInvalidKey(final NugetFeedLibrary library) throws JDOMException, IOException {
    setODataSerializer(library);
    assertStatusCode(HttpStatus.SC_BAD_REQUEST, "Packages(Id='Fixie')").run();
  }

  @Test(dataProvider = "nugetFeedLibrariesData")
  public void testGetNonExistingPackage(final NugetFeedLibrary library) throws JDOMException, IOException {
    setODataSerializer(library);
    assertStatusCode(HttpStatus.SC_NOT_FOUND, "Packages(Id='Fixie',Version='1.0.0')").run();
  }

  @Test(dataProvider = "nugetFeedLibrariesData")
  public void testVSRequests(final NugetFeedLibrary library) {
    setODataSerializer(library);
    String[] reqs = {
            "Packages()",
            "Packages()/$count?$filter=((((Id%20ne%20null)%20and%20substringof('freerereeee',tolower(Id)))%20or%20((Description%20ne%20null)%20and%20substringof('freerereeee',tolower(Description))))%20or%20((Tags%20ne%20null)%20and%20substringof('%20freerereeee%20',tolower(Tags))))%20and%20IsLatestVersion",
            "Packages()/$count?$filter=((((Id%20ne%20null)%20and%20substringof('freerereeeeff',tolower(Id)))%20or%20((Description%20ne%20null)%20and%20substringof('freerereeeeff',tolower(Description))))%20or%20((Tags%20ne%20null)%20and%20substringof('%20freerereeeeff%20',tolower(Tags))))%20and%20IsLatestVersion",
            "Packages()/$count?$filter=((((Id%20ne%20null)%20and%20substringof('freerereeeeffdfg',tolower(Id)))%20or%20((Description%20ne%20null)%20and%20substringof('freerereeeeffdfg',tolower(Description))))%20or%20((Tags%20ne%20null)%20and%20substringof('%20freerereeeeffdfg%20',tolower(Tags))))%20and%20IsLatestVersion",
            "Packages()/$count?$filter=((((Id%20ne%20null)%20and%20substringof('freerereeeeffdfgdfg',tolower(Id)))%20or%20((Description%20ne%20null)%20and%20substringof('freerereeeeffdfgdfg',tolower(Description))))%20or%20((Tags%20ne%20null)%20and%20substringof('%20freerereeeeffdfgdfg%20',tolower(Tags))))%20and%20IsLatestVersion",
            "Packages()/$count?$filter=((((Id%20ne%20null)%20and%20substringof('freerereeeeffdfgdfgdfg',tolower(Id)))%20or%20((Description%20ne%20null)%20and%20substringof('freerereeeeffdfgdfgdfg',tolower(Description))))%20or%20((Tags%20ne%20null)%20and%20substringof('%20freerereeeeffdfgdfgdfg%20',tolower(Tags))))%20and%20IsLatestVersion",
            "Packages()/$count?$filter=((((Id%20ne%20null)%20and%20substringof('freerereeeeffdfgdfgdfgdf',tolower(Id)))%20or%20((Description%20ne%20null)%20and%20substringof('freerereeeeffdfgdfgdfgdf',tolower(Description))))%20or%20((Tags%20ne%20null)%20and%20substringof('%20freerereeeeffdfgdfgdfgdf%20',tolower(Tags))))%20and%20IsLatestVersion",
            "Packages()/$count?$filter=((((Id%20ne%20null)%20and%20substringof('freerereeeeffdfgdfgdfgdfgdfg',tolower(Id)))%20or%20((Description%20ne%20null)%20and%20substringof('freerereeeeffdfgdfgdfgdfgdfg',tolower(Description))))%20or%20((Tags%20ne%20null)%20and%20substringof('%20freerereeeeffdfgdfgdfgdfgdfg%20',tolower(Tags))))%20and%20IsLatestVersion",
            "Packages()/$count?$filter=IsLatestVersion",
            "Packages()?$filter=(tolower(Id)%20eq%20'castle.core')%20or%20(tolower(Id)%20eq%20'castle.windsor')&$orderby=Id&$skip=0&$top=30",
    };

    for (String req : reqs) {
      assert200(req).run();
    }
  }

  @Test(dataProvider = "nugetFeedLibrariesData")
  @TestFor(issues = "TW-36083")
  public void testSemanticVersioning(final NugetFeedLibrary library) throws Exception {
    setODataSerializer(library);

    addMockPackage("foo", "1.0.0-Beta6");
    addMockPackage("foo", "1.0.0-Beta7");
    addMockPackage("foo", "1.0.0");

    String response = openRequest("Packages()?$filter=(Version%20gt%20%271.0.0-Beta1%27)");
    assertContainsPackageVersion(response, "1.0.0-Beta6");
    assertContainsPackageVersion(response, "1.0.0-Beta7");
    assertContainsPackageVersion(response, "1.0.0");

    response = openRequest("Packages()?$filter=(Version gt '1.0.0')");
    assertNotContainsPackageVersion(response, "1.0.0-Beta6");
    assertNotContainsPackageVersion(response, "1.0.0-Beta7");
    assertNotContainsPackageVersion(response, "1.0.0");
  }

  @Test(dataProvider = "nugetFeedLibrariesData")
  @TestFor(issues = "TW-40215")
  public void testSkipToken(final NugetFeedLibrary library) throws Exception {
    setODataSerializer(library);

    for (int i = 0; i <= NuGetFeedConstants.NUGET_FEED_PACKAGE_SIZE; i++) {
      addMockPackage("foo", "1.0." + i);
    }

    String response = openRequest("Packages()");
    Matcher matcher = NEXT_PAGE.matcher(response);
    if (!matcher.find()) {
      matcher = NEXT_PAGE2.matcher(response);
      Assert.assertTrue(matcher.find());
    }

    for (int i = 0; i < NuGetFeedConstants.NUGET_FEED_PACKAGE_SIZE; i++) {
      assertContainsPackageVersion(response, "1.0." + i);
    }
    assertNotContainsPackageVersion(response, "1.0." + NuGetFeedConstants.NUGET_FEED_PACKAGE_SIZE);

    String link = StringEscapeUtils.unescapeXml(matcher.group(1));
    String serverUrl = getNuGetServerUrl();
    if (link.startsWith(serverUrl)) {
      link = new URI(link).toString();
      link = link.substring(serverUrl.length());
    }

    response = openRequest(link);
    assertNotContainsPackageVersion(response, "1.0." + (NuGetFeedConstants.NUGET_FEED_PACKAGE_SIZE - 1));
    assertContainsPackageVersion(response, "1.0." + NuGetFeedConstants.NUGET_FEED_PACKAGE_SIZE);
  }

  @Test(dataProvider = "nugetFeedLibrariesData")
  @TestFor(issues = "TW-49634")
  public void testPsGetRequests(final NugetFeedLibrary library) {
    setODataSerializer(library);
    String[] reqs = {
      "Packages()?$filter=tolower(Id)+eq+%27packagename%27&$orderby=Id",
    };

    for (String req : reqs) {
      assert200(req).run();
    }
  }

  @Test(dataProvider = "nugetFeedLibrariesData")
  public void testNuGet28(final NugetFeedLibrary library) throws JDOMException, IOException {
    setODataSerializer(library);
    addMockPackage(
      "AdviserSpend.Web.UI", CollectionsUtil.asMap(
        ID, "AdviserSpend.Web.UI", VERSION, "1.0.0.42"),
      true);

    final String response = openRequest("Packages()?$filter=%28substringof%28%27adviser%27%2Ctolower%28Id%29%29%20or"+
      "%20%28%28Title%20ne%20null%29%20and%20substringof%28%27adviser%27%2Ctolower%28Title%29%29%29%29%20and%20%28"+
      "IsLatestVersion%20or%20IsAbsoluteLatestVersion%29&$top=20");

    assertContainsPackageVersion(response, "1.0.0.42.0");
  }

  @Test(dataProvider = "nugetFeedLibrariesData")
  public void testGetPackagesWithSemVer10(final NugetFeedLibrary library) throws Exception {
    setODataSerializer(library);
    enableDebug();
    addMockPackage("MyPackage", "1.0.0.0");
    addMockPackage("MyPackage", "1.0.0.1+metadata");

    String responseBody = openRequest("Packages?semVerLevel=1.0.0");
    assertContainsPackageVersion(responseBody, "1.0.0.0");
    assertNotContainsPackageVersion(responseBody, "1.0.0.1+metadata");
  }

  @Test(dataProvider = "nugetFeedLibrariesData")
  public void testGetPackagesWithSemVer20(final NugetFeedLibrary library) throws Exception {
    setODataSerializer(library);
    enableDebug();
    addMockPackage("MyPackage", "1.0.0.0");
    addMockPackage("MyPackage", "1.0.0.1+metadata");

    String responseBody = openRequest("Packages?semVerLevel=2.0.0");
    assertContainsPackageVersion(responseBody, "1.0.0.0");
    assertContainsPackageVersion(responseBody, "1.0.0.1+metadata");
  }

  private String replaceXml(@NotNull final String text) {
    return text
            .replace("http://nuget.org/api/v2/", "BASE_URI/")
            .replace(getNuGetServerUrl(), "BASE_URI/")
            .replace(" xmlns:app=\"http://www.w3.org/2007/app\"", "")
            .replaceAll("\\d+-\\d+-\\d+T\\d+:\\d+:\\d+(\\.\\d+)?Z?", "TIME")
            ;
  }

  private void compareStringAsXml(String actualXml, String goldPath) throws JDOMException, IOException {
    compareStringAsXml(actualXml, goldPath, false);
  }

  private void compareStringAsXml(String actualXml, String goldPath, boolean checkUnderTeamCity) throws JDOMException, IOException {
    //disable comparison check for TeamCity
    if (!checkUnderTeamCity && System.getenv("TEAMCITY_VERSION") != null) return;
    //noinspection PointlessBooleanExpression
    if (!checkUnderTeamCity && !LOCAL_DIFF_GOLD_AND_GENERATED) return;
    System.out.println("actualXml = " + actualXml);
    compareXmlWithGold(parseXml(actualXml), goldPath);
  }

  private Element parseXml(String text) throws JDOMException, IOException {
    try {
      return FileUtil.parseDocument(new StringReader(text), false);
    } catch (JDOMParseException e) {
      System.err.println("Failed to parse document: " + text);
      throw e;
    }
  }

  private void compareXmlWithGold(Element actual, String goldPath) throws JDOMException, IOException {
    System.out.println("actual: \r\n" + XmlUtil.to_s(actual) + "\r\n\r\n");
    Element gold = parseGoldXml(goldPath);
    compareXml(actual, gold);
  }

  private Element parseGoldXml(String goldPath) throws IOException, JDOMException {
    final File file = Paths.getTestDataPath(goldPath);
    final String text = new String(FileUtil.loadFileText(file, "utf-8"));
    return parseXml(text);
  }

  private void compareXml(Element actual, Element gold) throws JDOMException {
    String actualText = XmlUtil.to_s(sortProperties(sortEntityTypeProperties(actual)));
    String goldText = XmlUtil.to_s(sortProperties(sortEntityTypeProperties(gold)));

    Assert.assertEquals(replaceXml(actualText), replaceXml(goldText));
  }

  private Element sortEntityTypeProperties(Element el) throws JDOMException {
    XPath xp = XPath.newInstance("//x:EntityType");
    xp.addNamespace("x", "http://schemas.microsoft.com/ado/2006/04/edm");

    for (Object pNode : xp.selectNodes(el)) {
      Element node = (Element) pNode;
      final List<Element> props = new ArrayList<>();
      for (Object pChild : node.getChildren()) {
        Element child = (Element) pChild;
        if (child.getName().equals("Property")) {
          props.add((Element) child.clone());
          child.detach();
        }
      }

      Collections.sort(props, (o1, o2) -> o1.getAttributeValue("Name").compareTo(o2.getAttributeValue("Name")));

      for (Element prop : props) {
        node.addContent((Content)prop);
      }
    }
    return el;
  }

  private Element sortProperties(Element el) throws JDOMException {
    XPath xp = XPath.newInstance("//m:properties");
    xp.addNamespace("m", "http://schemas.microsoft.com/ado/2007/08/dataservices/metadata");

    for (Object pNode : xp.selectNodes(el)) {
      Element node = (Element) pNode;
      final List<Element> props = new ArrayList<>();
      for (Object pChild : node.getChildren()) {
        Element child = (Element) pChild;
        props.add((Element) child.clone());
        child.detach();
      }

      Collections.sort(props, (o1, o2) -> o1.getName().compareTo(o2.getName()));

      for (Element prop : props) {
        node.addContent((Content)prop);
      }
    }
    return el;
  }

}
