

package jetbrains.buildServer.nuget.tests.util.fsScanner;

import jetbrains.buildServer.BaseTestCase;
import jetbrains.buildServer.nuget.agent.util.fsScanner.*;
import org.jetbrains.annotations.Nullable;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.List;

public class TestAntPatternState extends BaseTestCase {
  private static void AssertPatternState(String pattern, @Nullable List<Integer> pos, String enter, String expectedResult) {
    List<Wildcard> wildcards = AntPatternUtil.parsePattern(pattern, false);

    AntPatternState state = pos == null
            ? new AntPatternState(wildcards)
            : new AntPatternState(wildcards, pos);

    final AntPatternStateMatch e = state.enter(enter);
    MatchResult match = e.getResult();
    AntPatternState newState = e.getState();

    StringBuilder sb = new StringBuilder();
    sb.append(match);
    sb.append(" ");
    for (int p : newState.getPatternPositions()) {
      sb.append(p);
      sb.append(":");
    }

    String result = sb.toString();
    Assert.assertEquals(expectedResult, result);
  }

  @Test
  public void PatternState() {
    AssertPatternState("a*", null, "a", "YES ");
    AssertPatternState("a*", null, "abc", "YES ");
    AssertPatternState("a*", null, "b", "NO ");

    AssertPatternState("a*/**", null, "b", "NO ");
    AssertPatternState("a*/**", null, "a", "YES 1:");

    AssertPatternState("**", null, "b", "YES 0:");
    AssertPatternState("**/a", null, "b", "MAYBELATER 0:");
    AssertPatternState("**/a", null, "a", "YES 0:");
    AssertPatternState("**/a/**/a", Arrays.asList(0, 2), "a", "YES 0:2:2:"); // fix?

    AssertPatternState("**/a/**/b", Arrays.asList(0, 2), "a", "MAYBELATER 0:2:2:");
    AssertPatternState("**/a/**/b", Arrays.asList(0, 2), "b", "YES 0:2:");
    AssertPatternState("**/a/**/b", Arrays.asList(0, 2), "c", "MAYBELATER 0:2:");
  }
}
