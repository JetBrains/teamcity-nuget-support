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
import jetbrains.buildServer.nuget.server.feed.server.PackagesIndex;
import jetbrains.buildServer.nuget.server.feed.server.controllers.NuGetProducer;
import jetbrains.buildServer.nuget.tests.integration.Paths;
import jetbrains.buildServer.util.FileUtil;
import jetbrains.buildServer.util.XmlUtil;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jetbrains.annotations.NotNull;
import org.jmock.Mockery;
import org.odata4j.format.xml.EdmxFormatWriter;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 03.01.12 12:19
 */
public class NuGetProducerTest extends BaseTestCase {
  private Mockery m;
  private PackagesIndex myIndex;
  private NuGetProducer myProducer;

  @BeforeMethod
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    m = new Mockery();
    myIndex = m.mock(PackagesIndex.class);
    myProducer = new NuGetProducer(myIndex);
  }

  @Test
  public void dumpMetadata() {
    System.out.println(getMetadataString());
  }

  @Test
  public void assertMetadata() throws JDOMException, IOException {
    final File data = Paths.getTestDataPath("feed/odata/metadata.v2.xml");
    Assert.assertTrue(data.isFile());

    final Element root = FileUtil.parseDocument(data);

    Assert.assertEquals(getMetadataString(), XmlUtil.to_s(root));
  }

  @NotNull
  private String getMetadataString() {
    StringWriter sw = new StringWriter();
    EdmxFormatWriter.write(myProducer.getProducer().getMetadata(), sw);

    return XmlUtil.to_s(XmlUtil.from_s(sw.toString()));
  }
}
