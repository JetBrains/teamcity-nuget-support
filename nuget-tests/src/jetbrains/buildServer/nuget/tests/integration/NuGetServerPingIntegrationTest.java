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

package jetbrains.buildServer.nuget.tests.integration;

import jetbrains.buildServer.nuget.server.feed.impl.FeedHttpClientHolder;
import jetbrains.buildServer.nuget.server.feed.server.process.NuGetServerPing;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 02.11.11 14:28
 */
public class NuGetServerPingIntegrationTest extends NuGetServerIntegrationTestBase {
  @Test
  public void testPing() {
    Assert.assertTrue(new NuGetServerPing(myNuGetServerAddresses, new FeedHttpClientHolder(), myTokens).pingNuGetServer());
  }
}
