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

package jetbrains.buildServer.nuget.tests.server;

import jetbrains.buildServer.BaseTestCase;
import jetbrains.buildServer.nuget.server.util.SemanticVersion;
import jetbrains.buildServer.nuget.server.util.VersionConstraint;
import org.jetbrains.annotations.NotNull;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

/**
 * @author Evgeniy.Koshkin
 */
public class VersionConstraintTest extends BaseTestCase {

  @DataProvider(name = "versions-to-test")
  public Object[][] versionsProvider() {
    return new Object[][] {
            {"1.0", "1.0", true},
            {"(1.0,)", "1.0", false},
            {"1.0", "1.1", true},
            {"(1.0,)", "1.1", true},
            { "1.0", "0.1", false },
            { "(1.0,)", "0.1", false },
            { "[,1.0]", "1.0", true },
            { "(,1.0)", "1.0", false },
            { "[,1.0]", "0.1", true },
            { "(,1.0)", "0.1", true },
            { "[,1.0]", "1.1", false },
            { "(,1.0)", "1.1", false },
            { "(0.5,1.0]", "0.5", false },
            { "(0.5,1.0]", "0.7", true },
            { "(0.5,1.0]", "0.4", false },
            { "(0.5,1.0]", "1.4", false },
            { "(0.5,1.0]", "1.0", true },
            { "[0.5,1.0)", "1.0", false },
            { "[0.5,1.0)", "0.7", true },
            { "[0.5,1.0)", "0.4", false },
            { "[0.5,1.0)", "1.4", false },
            { "[0.5,1.0)", "0.5", true }
    };
  }

  @Test(dataProvider = "versions-to-test")
  public void test(@NotNull String versionSpec, @NotNull String version, boolean shouldMatch) throws Exception {
    final VersionConstraint versionConstraint = VersionConstraint.tryParse(versionSpec);
    assertNotNull(versionConstraint);
    final SemanticVersion semanticVersion = SemanticVersion.tryParse(version);
    assertNotNull(semanticVersion);
    assertEquals(shouldMatch, versionConstraint.satisfies(semanticVersion));
  }
}
