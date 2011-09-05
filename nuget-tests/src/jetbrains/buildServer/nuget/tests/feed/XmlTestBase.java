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

import jetbrains.buildServer.BaseTestCase;
import jetbrains.buildServer.util.FileUtil;
import jetbrains.buildServer.util.XmlUtil;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.xpath.XPath;
import org.jetbrains.annotations.NotNull;
import org.testng.Assert;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 06.09.11 1:02
 */
public abstract class XmlTestBase extends BaseTestCase {
  protected void assertXml(String gold, String actual) throws JDOMException, IOException {
    final String xml = p(actual);

    System.out.println(xml);

    File goldFile = getTestDataPath(gold);
    File tmp = new File(goldFile.getPath() + ".tmp");
    FileUtil.writeFile(tmp, xml);

    final String expected = p(new String(FileUtil.loadFileText(goldFile, "utf-8")));

    Assert.assertEquals(xml, expected);
    FileUtil.delete(tmp);
  }

  protected String p(String s) throws JDOMException {
    Element el = XmlUtil.from_s(s);
    preprocessXml(el);
    return XmlUtil.to_s(el).trim().replaceAll("[\r\n]+", "\n");
  }


  protected void preprocessXml(Element el) throws JDOMException {
    Collection<XmlPatchAction> repaces = Arrays.asList(
            new SetContentXmlPatchAction("/x:feed/x:updated", ".*", "UPDATED"),
            new RemoveElement("/x:feed/x:author")
//            new SetContentXmlPatchAction("/x:feed/x:id", "http://(feed.jonnyzzz.name/path/Packages)|(http://packages.nuget.org/v1/FeedService.svc/Packages)", "URL"),
//            new RemoveElement("/x:feed/x:entry/x:link[@rel='edit']"),
//            new RemoveElement("/x:feed/x:entry/x:link[@rel='edit-media']")
    );

    for (XmlPatchAction replace : repaces) {
      XPath xPath = XPath.newInstance(replace.getXPath());
      xPath.addNamespace("x", "http://www.w3.org/2005/Atom");

      List list = xPath.selectNodes(el);
      for (Object o : list) {
        if (o instanceof Element) {
          replace.action((Element) o);
        }
      }
    }
  }


  @NotNull
  protected File getTestDataPath(String prefix) {
    return FileUtil.getCanonicalFile(new File("./nuget-tests/testData/feed", prefix));
  }
}
