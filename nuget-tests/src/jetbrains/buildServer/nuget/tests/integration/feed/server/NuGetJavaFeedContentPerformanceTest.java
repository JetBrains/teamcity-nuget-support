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

import jetbrains.buildServer.nuget.server.feed.server.NuGetIndexEntry;
import jetbrains.buildServer.nuget.tests.integration.Paths;
import jetbrains.buildServer.util.XmlUtil;
import org.jetbrains.annotations.NotNull;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Iterator;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 13.01.12 20:25
 */
public class NuGetJavaFeedContentPerformanceTest extends NuGetJavaFeedIntegrationTestBase {

  private static <T> int count(Iterator<T> itz) {
    int c = 0;
    while(itz.hasNext()) {
      c++;
      itz.next();
    }
    return c;
  }

  @Test
  public void test_list_no_query_5000() throws IOException {
    do_test_list_packages(5000, 100, "");
  }

  @Test
  public void test_list_query_by_id_5000() throws IOException {
    do_test_list_packages(5000, 0.4, "?$filter=Id+eq+'Foo'");
  }


  public void do_test_list_packages(int sz, double time, @NotNull final String query) throws IOException {
    NuGetIndexEntry base = addPackage(Paths.getTestDataPath("/packages/CommonServiceLocator.1.0.nupkg"), false);
    for(int i = 1; i < sz; i ++) {
      addMockPackage(base, false);
    }

    Assert.assertEquals(count(myIndex.getNuGetEntries()), sz);

    assertTime(time, "aaa", 5, new Runnable() {
      public void run() {
        String req = openRequest("Packages()" + query);
        System.out.println("req.length() = " + req.length());
        System.out.println(req);
        System.out.println(XmlUtil.to_s(XmlUtil.from_s(req)));
      }
    });

  }
}

