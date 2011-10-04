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
import jetbrains.buildServer.nuget.server.exec.impl.ListPackagesCommandProcessor;
import jetbrains.buildServer.nuget.server.exec.SourcePackageInfo;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Collection;
import java.util.Iterator;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 14.07.11 14:28
 */
public class ListPackagesCommandProcessorTest extends BaseTestCase {
  private  ListPackagesCommandProcessor p;

  @BeforeMethod
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    p = new ListPackagesCommandProcessor("source5");
  }

  @Test
  public void test_empty() {
    Assert.assertTrue(p.getResult().isEmpty());
  }

  @Test
  public void test_should_throw_on_exit_code() {
    try {
      p.onFinished(1);
    } catch (RuntimeException e) {
      return;
    }
    Assert.fail("Exception expected");
  }

  @Test
  public void test_some_output() {
    for(int i =0; i <100; i++) {
      p.onStdError("asdasd");
      p.onStdOutput("3434");
    }
    Assert.assertTrue(p.getResult().isEmpty());
  }

  @Test
  public void test_parse_service_message() {
    p.onStdOutput("##teamcity[nuget-package Id='NUnit' Version='2.5.10.11092']");

    Collection<SourcePackageInfo> result = p.getResult();
    Assert.assertEquals(result.size(), 1);
    SourcePackageInfo next = result.iterator().next();

    Assert.assertEquals(next.getSource(), "source5");
    Assert.assertEquals(next.getPackageId(), "NUnit");
    Assert.assertEquals(next.getVersion(), "2.5.10.11092");
  }

  @Test
  public void test_parse_service_message_no_source() {
    p = new ListPackagesCommandProcessor(null);
    p.onStdOutput("##teamcity[nuget-package Id='NUnit' Version='2.5.10.11092']");

    Collection<SourcePackageInfo> result = p.getResult();
    Assert.assertEquals(result.size(), 1);
    SourcePackageInfo next = result.iterator().next();

    Assert.assertEquals(next.getSource(), null);
    Assert.assertEquals(next.getPackageId(), "NUnit");
    Assert.assertEquals(next.getVersion(), "2.5.10.11092");
  }

  @Test
  public void test_parse_service_message_multiple() {
    p.onStdOutput("##teamcity[nuget-package Id='NUnit' Version='2.5.10.11092']");
    p.onStdOutput("##teamcity[nuget-package Id='JUnit' Version='1.2.0.92']");

    Collection<SourcePackageInfo> result = p.getResult();
    Assert.assertEquals(result.size(), 2);
    Iterator<SourcePackageInfo> it = result.iterator();
    SourcePackageInfo next = it.next();

    Assert.assertEquals(next.getSource(), "source5");
    Assert.assertEquals(next.getPackageId(), "NUnit");
    Assert.assertEquals(next.getVersion(), "2.5.10.11092");

    next = it.next();
    Assert.assertEquals(next.getSource(), "source5");
    Assert.assertEquals(next.getPackageId(), "JUnit");
    Assert.assertEquals(next.getVersion(), "1.2.0.92");
  }

}
