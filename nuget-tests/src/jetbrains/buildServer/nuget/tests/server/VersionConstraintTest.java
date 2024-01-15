

package jetbrains.buildServer.nuget.tests.server;

import jetbrains.buildServer.BaseTestCase;
import jetbrains.buildServer.nuget.common.version.SemanticVersion;
import jetbrains.buildServer.nuget.common.version.VersionConstraint;
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
            { "1.0", "1.0", true },
            { "(1.0,)", "1.0", false },
            { "1.0", "1.1", true },
            { "(1.0,)", "1.1", true },
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
    final VersionConstraint versionConstraint = VersionConstraint.valueOf(versionSpec);
    assertNotNull(versionConstraint);
    final SemanticVersion semanticVersion = SemanticVersion.valueOf(version);
    assertNotNull(semanticVersion);
    assertEquals(shouldMatch, versionConstraint.satisfies(semanticVersion));
  }
}
