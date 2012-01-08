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

package jetbrains.buildServer.nuget.tests.integration.feed.server;

import jetbrains.buildServer.nuget.tests.integration.Paths;
import jetbrains.buildServer.util.FileUtil;
import jetbrains.buildServer.util.XmlUtil;
import org.jdom.JDOMException;
import org.jetbrains.annotations.NotNull;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 06.01.12 22:23
 */
public class NuGetJavaFeedContentTest extends NuGetJavaFeedIntegrationTestBase {

  @Test
  public void testMetadata() throws JDOMException, IOException {
    final String s = openRequest("$metadata");
    final String actualText = XmlUtil.to_s(XmlUtil.from_s(s));
    System.out.println(actualText);

    final String goldText = XmlUtil.to_s(FileUtil.parseDocument(Paths.getTestDataPath("/feed/odata/metadata.v2.xml")));
    compareXml(actualText, goldText);
  }

  @Test
  public void testRoot() throws JDOMException, IOException {
    final String s = openRequest("");
    final String actualText = XmlUtil.to_s(XmlUtil.from_s(s));
    System.out.println(actualText);
    final String goldText = XmlUtil.to_s(FileUtil.parseDocument(Paths.getTestDataPath("/feed/odata/root.v2.xml")));
    compareXml(actualText, goldText);
  }
  
  private String replaceXml(@NotNull final String text) {
    return text
            .replace("http://nuget.org/api/v2/", "BASE_URI/")
            .replace(getNuGetServerUrl(), "BASE_URI/")
            .replaceAll("\\d+-\\d+-\\d+T\\d+:\\d+:\\d+Z", "TIME")
            ;
  }

  private void compareXml(String actualText, String goldText) {
    Assert.assertEquals(replaceXml(actualText), replaceXml(goldText));
  }

  @Test
  public void testPackages() throws JDOMException, IOException {
    addPackage(Paths.getTestDataPath("/packages/CommonServiceLocator.1.0.nupkg"));
    final String s = openRequest("Packages()");

    final String actualText = XmlUtil.to_s(XmlUtil.from_s(s));
    System.out.println(actualText);

    final String goldText = XmlUtil.to_s(FileUtil.parseDocument(Paths.getTestDataPath("/feed/odata/packages.v2.CommonServiceLocator.xml")));

    compareXml(actualText, goldText);
  }
}
