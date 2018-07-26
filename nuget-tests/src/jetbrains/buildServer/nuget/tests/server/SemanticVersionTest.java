/*
 * Copyright 2000-2017 JetBrains s.r.o.
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
import jetbrains.buildServer.nuget.common.version.SemVerLevel;
import jetbrains.buildServer.nuget.common.version.SemanticVersion;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * @author Evgeniy.Koshkin
 */
public class SemanticVersionTest extends BaseTestCase {
  @Test
  public void testCanHandleShortVersions() {
    assertEquals(SemanticVersion.valueOf("1.0.0"), SemanticVersion.valueOf("1"));
    assertEquals(SemanticVersion.valueOf("1.0.0"), SemanticVersion.valueOf("1.0"));
    assertEquals(SemanticVersion.valueOf("1.0.0"), SemanticVersion.valueOf("1.0.0.0"));
  }

  @Test
  public void testSemVer10() {
    Assert.assertEquals(SemanticVersion.valueOf("1.0.0-beta2").getLevel(), SemVerLevel.V1);
    Assert.assertEquals(SemanticVersion.valueOf("1.0.0-beta2").getLevel(), SemVerLevel.V1);
    Assert.assertEquals(SemanticVersion.valueOf("2.4.1").getLevel(), SemVerLevel.V1);
  }

  @Test
  public void testSemVer20() {
    Assert.assertEquals(SemanticVersion.valueOf("1.0.0-beta.20.5").getLevel(), SemVerLevel.V2);
    Assert.assertEquals(SemanticVersion.valueOf("2.4.1-pre.0.134+git.hash.5aa7fa8324af609bcdb43a90e54ee076d1a6b067").getLevel(), SemVerLevel.V2);
    Assert.assertEquals(SemanticVersion.valueOf("2.4.1-pre.0.134+buildagent.12.date.2016.3.31.config.debug").getLevel(), SemVerLevel.V2);
    Assert.assertEquals(SemanticVersion.valueOf("1.0.1+security.patch.2349").getLevel(), SemVerLevel.V2);
  }
}
