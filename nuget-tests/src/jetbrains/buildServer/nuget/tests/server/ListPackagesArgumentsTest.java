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

package jetbrains.buildServer.nuget.tests.server;

import jetbrains.buildServer.BaseTestCase;
import jetbrains.buildServer.nuget.server.exec.ListPackagesResult;
import jetbrains.buildServer.nuget.server.exec.SourcePackageInfo;
import jetbrains.buildServer.nuget.server.exec.SourcePackageReference;
import jetbrains.buildServer.nuget.server.exec.impl.ListPackagesArguments;
import jetbrains.buildServer.nuget.server.feed.FeedCredentials;
import jetbrains.buildServer.util.FileUtil;
import jetbrains.buildServer.util.StringUtil;
import jetbrains.buildServer.util.XmlUtil;
import org.jetbrains.annotations.NotNull;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 04.10.11 18:59
 */
public class ListPackagesArgumentsTest extends BaseTestCase {
  private ListPackagesArguments myArguments;

  @BeforeMethod
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    myArguments = new ListPackagesArguments();
  }

  @Test
  public void testSerialize_01() throws IOException {
    final File tmp = createTempFile();

    myArguments.encodeParameters(tmp, getFullSet());

    final String xml = new String(FileUtil.loadFileText(tmp, "utf-8"));
    final String reformatted = StringUtil.convertLineSeparators(XmlUtil.to_s(XmlUtil.from_s(xml)));
    final String gold = StringUtil.convertLineSeparators("<nuget-packages>\n" +
            "  <packages>\n" +
            "    <package id=\"2\" source=\"1\" versions=\"3\" />\n" +
            "    <package id=\"22\" versions=\"33\" />\n" +
            "    <package id=\"222\" />\n" +
            "  </packages>\n" +
            "</nuget-packages>");

    System.out.println(reformatted);
    Assert.assertEquals(reformatted, gold);
  }

  @Test
  public void testSerialize_isPrerelease() throws IOException {
    final File tmp = createTempFile();

    myArguments.encodeParameters(tmp, getFullSet());

    final String xml = new String(FileUtil.loadFileText(tmp, "utf-8"));
    final String reformatted = StringUtil.convertLineSeparators(XmlUtil.to_s(XmlUtil.from_s(xml)));
    final String gold = StringUtil.convertLineSeparators("<nuget-packages>\n" +
            "  <packages>\n" +
            "    <package id=\"2\" source=\"1\" versions=\"3\" />\n" +
            "    <package id=\"22\" versions=\"33\" />\n" +
            "    <package id=\"222\" />\n" +
            "  </packages>\n" +
            "</nuget-packages>");

    System.out.println(reformatted);
    Assert.assertEquals(reformatted, gold);
  }

  @Test
  public void testDeserialize_isPrerelease() throws IOException {
    final File tmp = createTempFile();

    SourcePackageReference ref = new SourcePackageReference(
            "http://some-source",
            new FeedCredentials("user", "pwww"),
            "package.id",
            "version-spec",
            true
    );
    myArguments.encodeParameters(tmp, Arrays.asList(ref));

    final String xml = new String(FileUtil.loadFileText(tmp, "utf-8"));
    final String reformatted = StringUtil.convertLineSeparators(XmlUtil.to_s(XmlUtil.from_s(xml)));

    final String gold = StringUtil.convertLineSeparators("<nuget-packages>\n" +
            "  <packages>\n" +
            "    <package id=\"package.id\" source=\"http://some-source\" username=\"user\" password=\"pwww\" versions=\"version-spec\" include-prerelease=\"true\" />\n" +
            "  </packages>\n" +
            "</nuget-packages>");

    System.out.println(reformatted);
    Assert.assertEquals(reformatted, gold);

    Map<SourcePackageReference, ListPackagesResult> map = myArguments.decodeParameters(tmp);
    Assert.assertEquals(map.size(), 1);

    final SourcePackageReference key = map.keySet().iterator().next();
    Assert.assertNotNull(key.getCredentials());
    assertPackageReferencesEqual(key, ref);

    final String reformatted2 = StringUtil.convertLineSeparators(XmlUtil.to_s(XmlUtil.from_s(xml)));
    Assert.assertEquals(reformatted, reformatted2);
  }

  private void assertPackageReferencesEqual(@NotNull SourcePackageReference actual,
                                            @NotNull SourcePackageReference expected) {
    Assert.assertEquals(actual.getPackageId(), expected.getPackageId());
    Assert.assertEquals(actual.getCredentials(), expected.getCredentials());
    Assert.assertEquals(actual.getSource(), expected.getSource());
    Assert.assertEquals(actual.getVersionSpec(), expected.getVersionSpec());
    Assert.assertEquals(actual.isIncludePrerelease(), expected.isIncludePrerelease());
  }

  @Test
  public void testDeserialize_01() throws IOException {
    final File tmp = createTempFile("<nuget-packages>\n" +
            "  <packages>\n" +
            "    <package id=\"2\" source=\"1\" versions=\"3\">" +
            "      <package-entries>" +
            "        <package-entry version='1.2.3'/>" +
            "        <package-entry version='11.22.33' />" +
            "      </package-entries>" +
            "    </package>\n" +
            "    <package id=\"22\" versions=\"33\" >\n" +
            "      <package-entries>" +
            "        <package-entry version='3.2.3'/>" +
            "        <package-entry version='31.22.33' />" +
            "      </package-entries>" +
            "    </package>\n" +
            "    <package id=\"222\" >\n" +
            "      <package-entries>" +
            "        <package-entry version='4.2.3'/>" +
            "        <package-entry version='41.22.33' />" +
            "      </package-entries>" +
            "    </package>\n" +
            "  </packages>\n" +
            "</nuget-packages>");

    final Map<SourcePackageReference, ListPackagesResult> map = myArguments.decodeParameters(tmp);
    Assert.assertEquals(map.size(), 3);
    for (SourcePackageReference sourcePackageReference : getFullSet()) {
      Assert.assertTrue(map.containsKey(sourcePackageReference), "must contain " + sourcePackageReference);
    }

    for (Map.Entry<SourcePackageReference, ListPackagesResult> e : map.entrySet()) {
      Assert.assertEquals(e.getValue().getCollectedInfos().size(), 2);
      Set<String> version = new HashSet<String>();
      if (e.getKey().getPackageId().equals("2")) {
        version.add("1.2.3");
        version.add("11.22.33");
      }
      if (e.getKey().getPackageId().equals("22")) {
        version.add("3.2.3");
        version.add("31.22.33");
      }
      if (e.getKey().getPackageId().equals("222")) {
        version.add("4.2.3");
        version.add("41.22.33");
      }

      for (SourcePackageInfo i : e.getValue().getCollectedInfos()) {
        Assert.assertEquals(i.getPackageId(), e.getKey().getPackageId());
        Assert.assertEquals(i.getSource(), e.getKey().getSource());

        Assert.assertTrue(version.remove(i.getVersion()));
      }
      Assert.assertTrue(version.isEmpty());
    }
  }

  @Test
  public void testDeserialize_02() throws Exception {
    final File tmp = createTempFile("<nuget-packages xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\">\n" +
            "  <packages>\n" +
            "    <package id=\"NUnit\">\n" +
            "      <package-entries>\n" +
            "        <package-entry version=\"2.5.10.11092\" />\n" +
            "        <package-entry version=\"2.5.7.10213\" />\n" +
            "        <package-entry version=\"2.5.9.10348\" />\n" +
            "      </package-entries>\n" +
            "    </package>\n" +
            "  </packages>\n" +
            "</nuget-packages>");

    final Map<SourcePackageReference, ListPackagesResult> map = myArguments.decodeParameters(tmp);
    Assert.assertEquals(map.size(), 1);
    final SourcePackageReference ref = new SourcePackageReference(null, "NUnit", null);
    final ListPackagesResult vs = map.get(ref);
    Assert.assertNotNull(vs);
    Assert.assertEquals(new HashSet<SourcePackageInfo>(vs.getCollectedInfos()), new HashSet<SourcePackageInfo>(Arrays.asList(ref.toInfo("2.5.10.11092"), ref.toInfo("2.5.7.10213"), ref.toInfo("2.5.9.10348"))));

  }

  @Test
  public void testDeserialize_03() throws Exception {
    final File tmp = createTempFile("<nuget-packages xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\">\n" +
            "  <packages>\n" +
            "    <package id=\"NUnit\">\n" +
            "      <error-message>this was a error</error-message>\n" +
            "      <package-entries>\n" +
            "        <package-entry version=\"2.5.10.11092\"></package-entry>\n" +
            "      </package-entries>\n" +
            "    </package>\n" +
            "  </packages>\n" +
            "</nuget-packages>");

    final Map<SourcePackageReference, ListPackagesResult> map = myArguments.decodeParameters(tmp);
    Assert.assertEquals(map.size(), 1);
    final SourcePackageReference ref = new SourcePackageReference(null, "NUnit", null);
    final ListPackagesResult vs = map.get(ref);
    Assert.assertNotNull(vs);
    Assert.assertEquals(new HashSet<SourcePackageInfo>(vs.getCollectedInfos()), new HashSet<SourcePackageInfo>(Arrays.asList(ref.toInfo("2.5.10.11092"))));
    Assert.assertEquals("this was a error", vs.getErrorMessage());
  }

  @NotNull
  private List<SourcePackageReference> getFullSet() {
    return Arrays.asList(new SourcePackageReference("1", "2", "3"), new SourcePackageReference(null, "22", "33"), new SourcePackageReference(null, "222", null));
  }

}
