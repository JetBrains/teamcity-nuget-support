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
package jetbrains.buildServer.nuget.tests.util.fsScanner;

import jetbrains.buildServer.nuget.agent.runner.publish.fsScanner.Wildcard;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TestWildcard {
  @Test
  public void test_01() {
    Assert.assertFalse(new Wildcard("abcd", true).IsMatch("a"));
    Assert.assertFalse(new Wildcard("*.cs", true).IsMatch("hello.csc"));

    Assert.assertTrue(new Wildcard("", true).IsMatch(""));
    Assert.assertFalse(new Wildcard("", true).IsMatch("asdsad"));

    Assert.assertTrue(new Wildcard("*.cs", true).IsMatch("hello.cs"));
    Assert.assertTrue(new Wildcard("h*.cs", true).IsMatch("hello.cs"));
    Assert.assertFalse(new Wildcard("a*.cs", true).IsMatch("hello.cs"));
    Assert.assertTrue(new Wildcard("*.cs*", true).IsMatch("hello.cs"));
    Assert.assertTrue(new Wildcard("*.cs", true).IsMatch(".cs"));
    Assert.assertFalse(new Wildcard("*.cs", true).IsMatch("hello.s"));

    Assert.assertTrue(new Wildcard("*a*c", true).IsMatch("1221abcccac"));
    Assert.assertTrue(new Wildcard("a", true).IsMatch("a"));
    Assert.assertTrue(new Wildcard("?", true).IsMatch("z"));
    Assert.assertFalse(new Wildcard("*?*", true).IsMatch(""));
    Assert.assertTrue(new Wildcard("*?*", true).IsMatch("k"));

  }

  @Test
  public void test_02() {
    Assert.assertTrue(new Wildcard("*?**", true).IsMatch("k"));
    Assert.assertTrue(new Wildcard("*?*", true).IsMatch("111k"));
    Assert.assertTrue(new Wildcard("*?*", true).IsMatch("11k22"));
    Assert.assertTrue(new Wildcard("*?*m*", true).IsMatch("11mk22"));
    Assert.assertFalse(new Wildcard("*??*m*", true).IsMatch("1mk22"));
    Assert.assertTrue(
            new Wildcard("*non-greedy character*matching", true).IsMatch(
                    "non-greedy character matching compared to greedy character matching"));
    Assert.assertFalse(
            new Wildcard("*non-greedy character*matching2", true).IsMatch(
                    "non-greedy character matching compared to greedy character matching"));
  }
}
