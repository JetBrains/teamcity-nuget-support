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

package jetbrains.buildServer.nuget.tests.server.trigger;

import jetbrains.buildServer.BaseTestCase;
import jetbrains.buildServer.nuget.server.trigger.impl.source.PackageSourceCheckerImpl;
import jetbrains.buildServer.util.FileUtil;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 18.07.12 17:37
 */
public class PackageSourceCheckerTest extends BaseTestCase {
  private PackageSourceCheckerImpl myCheck;

  @BeforeMethod
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    myCheck = new PackageSourceCheckerImpl();
  }

  @Test
  public void testSourcesCheck_http() {
    Assert.assertNull(myCheck.checkSource("http://foo"));
  }

  @Test
  public void testSourcesCheck_https() {
    Assert.assertNull(myCheck.checkSource("https://foo"));
  }

  @Test
  public void testSourcesCheck_local_empty() throws IOException {
    Assert.assertNotNull(myCheck.checkSource(createTempDir().getPath()));
  }

  @Test
  public void testSourcesCheck_local() throws IOException {
    final File dir = createTempDir();
    FileUtil.writeFileAndReportErrors(new File(dir, "foo.1.2.3.nupkg"), "ooo");
    Assert.assertNull(myCheck.checkSource(dir.getPath()));
  }

  @Test
  public void testSourcesCheck_network_share() throws IOException {
    //this test may only work in case we have a known network share.
    if (!"LABS".equals(System.getenv("USERDOMAIN"))) return;
    String result = myCheck.checkSource("\\\\top\\vss-6.0");
    Assert.assertNull(result, "but was: " + result);
  }

  @Test
  public void testSourcesCheck_network_share_fail() throws IOException {
    //this test may only work in case we have a known network share.
    if (!"LABS".equals(System.getenv("USERDOMAIN"))) return;
    Assert.assertNotNull(myCheck.checkSource("\\\\top\\vss26.0"));
  }
}
