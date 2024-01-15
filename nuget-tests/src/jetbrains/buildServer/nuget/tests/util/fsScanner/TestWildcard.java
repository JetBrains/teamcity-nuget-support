

package jetbrains.buildServer.nuget.tests.util.fsScanner;

import jetbrains.buildServer.nuget.agent.util.fsScanner.Wildcard;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TestWildcard {
  @Test
  public void test_01() {
    Assert.assertFalse(new Wildcard("abcd", true).isMatch("a"));
    Assert.assertFalse(new Wildcard("*.cs", true).isMatch("hello.csc"));

    Assert.assertTrue(new Wildcard("", true).isMatch(""));
    Assert.assertFalse(new Wildcard("", true).isMatch("asdsad"));

    Assert.assertTrue(new Wildcard("*.cs", true).isMatch("hello.cs"));
    Assert.assertTrue(new Wildcard("h*.cs", true).isMatch("hello.cs"));
    Assert.assertFalse(new Wildcard("a*.cs", true).isMatch("hello.cs"));
    Assert.assertTrue(new Wildcard("*.cs*", true).isMatch("hello.cs"));
    Assert.assertTrue(new Wildcard("*.cs", true).isMatch(".cs"));
    Assert.assertFalse(new Wildcard("*.cs", true).isMatch("hello.s"));

    Assert.assertTrue(new Wildcard("*a*c", true).isMatch("1221abcccac"));
    Assert.assertTrue(new Wildcard("a", true).isMatch("a"));
    Assert.assertTrue(new Wildcard("?", true).isMatch("z"));
    Assert.assertFalse(new Wildcard("*?*", true).isMatch(""));
    Assert.assertTrue(new Wildcard("*?*", true).isMatch("k"));

  }

  @Test
  public void test_02() {
    Assert.assertTrue(new Wildcard("*?**", true).isMatch("k"));
    Assert.assertTrue(new Wildcard("*?*", true).isMatch("111k"));
    Assert.assertTrue(new Wildcard("*?*", true).isMatch("11k22"));
    Assert.assertTrue(new Wildcard("*?*m*", true).isMatch("11mk22"));
    Assert.assertFalse(new Wildcard("*??*m*", true).isMatch("1mk22"));
    Assert.assertTrue(
            new Wildcard("*non-greedy character*matching", true).isMatch(
                    "non-greedy character matching compared to greedy character matching"));
    Assert.assertFalse(
            new Wildcard("*non-greedy character*matching2", true).isMatch(
                    "non-greedy character matching compared to greedy character matching"));
  }
}
