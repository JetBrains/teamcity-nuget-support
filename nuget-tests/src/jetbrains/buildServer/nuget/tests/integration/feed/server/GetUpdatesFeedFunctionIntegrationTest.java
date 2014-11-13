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

import org.testng.annotations.Test;

/**
 * @author Evgeniy.Koshkin
 */
public class GetUpdatesFeedFunctionIntegrationTest extends FeedFunctionIntegrationTestBase {

  @Test
  public void testVSRequest() throws Exception {
    final String feedResponse = openRequest("GetUpdates()?packageIds='Microsoft.Web.Infrastructure%7CRouteMagic%7Celmah%7Celmah.corelibrary%7Cxunit%7Cxunit.extensions%7CWebActivatorEx%7CNinject%7CMoq'&versions='1.0.0.0%7C1.2%7C1.2.2%7C1.2.2%7C1.9.2%7C1.9.2%7C2.0.2%7C2.2.1.4%7C4.1.1309.0919'&includePrerelease=true&includeAllVersions=false&targetFrameworks=''&versionConstraints=''");
  }

  @Test
  public void shouldHandleIncludePreReleaseParameter() throws Exception {
    fail();
  }

  @Test
  public void shouldHandleVersionConstraintsParameter() throws Exception {
    fail();
  }

  @Test
  public void shouldHandleTargetFrameworksParameter() throws Exception {
    fail();
  }

  @Test
  public void shouldHandleIncludeAllVersionsParameter() throws Exception {
    fail();
  }

  @Test
  public void shouldFailWhenNumberOfPackageIdsAndVersionDoNotMatch() throws Exception {
    fail();
  }
}
