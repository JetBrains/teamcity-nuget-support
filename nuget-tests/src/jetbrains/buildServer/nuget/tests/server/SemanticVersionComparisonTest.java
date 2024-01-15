

package jetbrains.buildServer.nuget.tests.server;

import jetbrains.buildServer.BaseTestCase;
import jetbrains.buildServer.nuget.common.version.SemanticVersion;
import jetbrains.buildServer.util.CollectionsUtil;
import jetbrains.buildServer.util.Converter;
import org.jetbrains.annotations.NotNull;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.*;

/**
 * Created 18.03.13 15:58
 *
 * @author Eugene Petrenko (eugene.petrenko@jetbrains.com); re-written by evgeniy.koshkin
 */
public class SemanticVersionComparisonTest extends BaseTestCase {
  @Test
  public void test_simple_versions() {
    doTest("1.0.0", "2.0.0", "2.0.1", "2.1.0", "3.3.3", "4.0.0");
  }

  @Test
  public void test_simple_versions2() {
    doTest("1.0.0-beta", "1.0.0", "1.0.1+snapshot", "2.0.0", "2.0.1", "2.1.0", "3.3.3", "4.0.0");
  }

  @Test
  public void test_simple_versions3() {
    doTest("1.0.0-beta", "1.0.0");
  }

  @Test
  public void test_simple_versions3_1() {
    doTest("1.0.0-beta", "1.0.0-beta.4");
  }

  @Test
  public void test_simple_versions4() {
    doTest("1.0.0+beta", "1.0.1+beta.5");
  }

  @Test
  public void test_simple_4versions() {
    doTest("0.0.0.34", "1.0.0.0-beta", "1.0.0.0", "2.0.0.0", "4.0.0.3");
  }

  @Test
  public void rule_2() {
    /**
     * 2. A normal version number MUST take the form X.Y.Z where X, Y, and Z are
     * non-negative integers. X is the major version, Y is the minor version,
     * and Z is the patch version.
     * Each element MUST increase numerically by increments of one. For instance: 1.9.0 -> 1.10.0 -> 1.11.0.
     */
    doTest("1.9.0", "1.10.0", "1.11.0");
  }

  @Test
  public void rule_2x() {
    doTest("1.0009.0", "1.10.0", "1.000011.0");
  }

  @Test
  public void rule_3() {
    /**
     * 3. When a major version number is incremented, the minor version and patch
     * version MUST be reset to zero. When a minor version number is incremented,
     * the patch version MUST be reset to zero. For instance: 1.1.3 -> 2.0.0 and 2.1.7 -> 2.2.0.
     */
    doTest("1.1.3", "2.0.0");
    doTest("2.1.7", "2.2.0");
  }

  @Test
  public void rule_10() {
    /**
     * 10. A pre-release version MAY be denoted by appending a dash and a series of
     * dot separated identifiers immediately following the patch version. Identifiers MUST
     * be comprised of only ASCII alphanumerics and dash [0-9A-Za-z-]. Pre-release versions
     * satisfy but have a lower precedence than the associated normal version.
     * Examples: 1.0.0-alpha, 1.0.0-alpha.1, 1.0.0-0.3.7, 1.0.0-x.7.z.92.
     */
    doTest("1.0.0-alpha", "1.0.0");
    doTest("1.0.0-alpha.1", "1.0.0");
    doTest("1.0.0-0.3.7", "1.0.0");
    doTest("1.0.0-x.7.z.92", "1.0.0");
  }

  @Test
  public void simple_spec() {
    /**
     * 12. Precedence MUST be calculated by separating the version into major, minor, patch, pre-release,
     * and build identifiers in that order.
     * Major, minor, and patch versions are always compared numerically.
     * Pre-release and build version precedence MUST be determined by comparing each dot separated identifier as follows:
     *    - identifiers consisting of only digits are compared numerically
     *      and
     *    - identifiers with letters or dashes are compared lexically in ASCII sort order.
     * Numeric identifiers always have lower precedence than non-numeric identifiers.
     *
     * Example: 1.0.0-alpha < 1.0.0-alpha.1 < 1.0.0-beta.2 < 1.0.0-beta.11 < 1.0.0-rc.1 < 1.0.0-rc.1+build.1 < 1.0.0 < 1.0.0+0.3.7 < 1.3.7+build < 1.3.7+build.2.b8f12d7 < 1.3.7+build.11.e0f985a.
     */
    //from http://semver.org/
    doTest(
            "1.0.0-333",
            "1.0.0-333.44",
            "1.0.0-333.44.55",
            "1.0.0-333.44.z",
            "1.0.0-alpha",
            "1.0.0-alpha.1",
            "1.0.0-beta.2",
            "1.0.0-beta.11",
            "1.0.0-rc.1",
            "1.0.0",
            "1.3.7+build.z11.f0f985a"
            );
  }

  private void doTest(String... vs) {
    doTest(CollectionsUtil.convertCollection(Arrays.asList(vs), new Converter<SemanticVersion, String>() {
      public SemanticVersion createFrom(@NotNull String source) {
        final SemanticVersion semanticVersion = SemanticVersion.valueOf(source);
        assertNotNull("Failed to parse version " + source, semanticVersion);
        return semanticVersion;
      }
    }));
  }

  private void doTest(Collection<SemanticVersion> _versions) {
    final List<SemanticVersion> initial = new ArrayList<SemanticVersion>(_versions);

    for (int fromId = 0; fromId < initial.size(); fromId++) {
      final SemanticVersion from = initial.get(fromId);

      for (int toId = 0; toId < initial.size(); toId++) {
        final SemanticVersion to = initial.get(toId);

        int cmp = from.compareTo(to);
        final String msg = from + " : " + to + " => " + cmp;

        if (fromId == toId) Assert.assertEquals(cmp, 0, msg);
        if (from.equals(to)) Assert.assertEquals(cmp, 0, msg);
        if (fromId < toId) Assert.assertTrue(cmp < 0, msg);
        if (fromId > toId) Assert.assertTrue(cmp > 0, msg);
      }
    }

    List<SemanticVersion> copy = new ArrayList<SemanticVersion>(_versions);
    Collections.reverse(copy);
    Collections.sort(copy);
    Assert.assertEquals(copy, initial);

    for (int i = 0; i < 10; i++) {
      Collections.shuffle(copy);
      Collections.sort(copy);
      Assert.assertEquals(copy, initial);
    }
  }
}
