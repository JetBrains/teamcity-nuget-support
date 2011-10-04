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
import jetbrains.buildServer.nuget.server.exec.SourcePackageInfo;
import jetbrains.buildServer.nuget.server.exec.SourcePackageReference;
import jetbrains.buildServer.nuget.server.exec.impl.ListPackagesArguments;
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
  public void testDeserialize_01() throws IOException {
    final File tmp = createTempFile("<nuget-packages>\n" +
            "  <packages>\n" +
            "    <package id=\"2\" source=\"1\" versions=\"3\">" +
            "      <package-entry version='1.2.3'/>" +
            "      <package-entry version='11.22.33' />" +
            "    </package>\n" +
            "    <package id=\"22\" versions=\"33\" >\n" +
            "      <package-entry version='3.2.3'/>" +
            "      <package-entry version='31.22.33' />" +
            "    </package>\n" +
            "    <package id=\"222\" >\n" +
            "      <package-entry version='4.2.3'/>" +
            "      <package-entry version='41.22.33' />" +
            "    </package>\n" +
            "  </packages>\n" +
            "</nuget-packages>");

    final Map<SourcePackageReference,Collection<SourcePackageInfo>> map = myArguments.decodeParameters(tmp);
    Assert.assertEquals(map.size(), 3);
    for (SourcePackageReference sourcePackageReference : getFullSet()) {
      Assert.assertTrue(map.containsKey(sourcePackageReference), "must contain " + sourcePackageReference);
    }

    for (Map.Entry<SourcePackageReference, Collection<SourcePackageInfo>> e : map.entrySet()) {
      Assert.assertEquals(e.getValue().size(), 2);
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

      for (SourcePackageInfo i : e.getValue()) {
        Assert.assertEquals(i.getPackageId(), e.getKey().getPackageId());
        Assert.assertEquals(i.getSource(), e.getKey().getSource());

        Assert.assertTrue(version.remove(i.getVersion()));
      }
      Assert.assertTrue(version.isEmpty());
    }
  }

  @NotNull
  private List<SourcePackageReference> getFullSet() {
    return Arrays.asList(new SourcePackageReference("1", "2", "3"), new SourcePackageReference(null, "22", "33"), new SourcePackageReference(null, "222", null));
  }

}
