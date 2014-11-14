/*
 * Copyright 2000-2014 JetBrains s.r.o.
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

/**
 * @author Evgeniy.Koshkin
 */
public abstract class FeedFunctionIntegrationTestBase extends NuGetJavaFeedIntegrationTestBase {
  protected void assertContainsPackageVersion(String responseBody, String version){
    assertContains(responseBody, "<d:Version>" + version + "</d:Version>");
  }

  protected void assertNotContainsPackageVersion(String responseBody, String version){
    assertNotContains(responseBody, "<d:Version>" + version + "</d:Version>", false);
  }

  protected void assertPackageVersionsOrder(String responseBody, String... versions) {
    int prevVersionPosition = 0;
    for (String version : versions){
      final int i = responseBody.indexOf("<d:Version>" + version + "</d:Version>");
      if(i == -1) fail("Response doesn't contain package version " + version);
      assertGreater(i, prevVersionPosition);
      prevVersionPosition = i;
    }
  }
}
