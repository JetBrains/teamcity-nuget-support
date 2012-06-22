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

package jetbrains.buildServer.nuget.tests.agent;

import jetbrains.buildServer.BaseTestCase;
import jetbrains.buildServer.nuget.agent.commands.impl.NuGetVersionFactoryImpl;
import junit.framework.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 22.06.12 18:12
 */
public class NuGetVersionFactoryTest extends BaseTestCase {
  private NuGetVersionFactoryImpl myImpl = new NuGetVersionFactoryImpl();

  @Test
  public void test_1_4() throws IOException {
    File file = createTempFile("1.4.223.04");
    Assert.assertFalse(myImpl.getFromVersionFile(file).supportAuth());
    Assert.assertFalse(myImpl.getFromVersionFile(file).supportInstallNoCache());
  }

  @Test
  public void test_1_7() throws IOException {
    File file = createTempFile("1.7.34");
    Assert.assertFalse(myImpl.getFromVersionFile(file).supportAuth());
    Assert.assertTrue(myImpl.getFromVersionFile(file).supportInstallNoCache());
  }

  @Test
  public void test_2_0() throws IOException {
    File file = createTempFile("2.0.34");
    Assert.assertTrue(myImpl.getFromVersionFile(file).supportAuth());
    Assert.assertTrue(myImpl.getFromVersionFile(file).supportInstallNoCache());
  }

  @Test
  public void test_2_x() throws IOException {
    File file = createTempFile("2.3");
    Assert.assertTrue(myImpl.getFromVersionFile(file).supportAuth());
    Assert.assertTrue(myImpl.getFromVersionFile(file).supportInstallNoCache());
  }

  @Test
  public void test_null() throws IOException {
    File file = null;
    Assert.assertFalse(myImpl.getFromVersionFile(file).supportAuth());
    Assert.assertFalse(myImpl.getFromVersionFile(file).supportInstallNoCache());
  }

  @Test
  public void test_broken() throws IOException {
    File file = createTempFile("lorn impsu\naas\n");
    Assert.assertFalse(myImpl.getFromVersionFile(file).supportAuth());
    Assert.assertFalse(myImpl.getFromVersionFile(file).supportInstallNoCache());
  }

  @Test
  public void test_broken2() throws IOException {
    File file = createTempFile("zooo,4,5");
    Assert.assertFalse(myImpl.getFromVersionFile(file).supportAuth());
    Assert.assertFalse(myImpl.getFromVersionFile(file).supportInstallNoCache());
  }

}
