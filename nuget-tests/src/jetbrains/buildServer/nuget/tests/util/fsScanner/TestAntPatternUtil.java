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


import jetbrains.buildServer.BaseTestCase;
import jetbrains.buildServer.nuget.agent.runner.publish.fsScanner.AntPatternUtil;
import jetbrains.buildServer.nuget.agent.runner.publish.fsScanner.Wildcard;
import jetbrains.buildServer.util.StringUtil;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

public class TestAntPatternUtil extends BaseTestCase {
  private static void AssertParseResult(String pattern, String expectedResult) {
    List<Wildcard> wildcards = AntPatternUtil.ParsePattern(pattern, false);
    StringBuilder sb = new StringBuilder();
    for (Wildcard w : wildcards) {
      sb.append(w == null ? "**" : w.Pattern());
      sb.append(":");
    }
    String result = StringUtil.trimEnd(sb.toString(), ":");
    Assert.assertEquals(expectedResult, result);
  }

  @Test
  public void IsFileNameMatch() {
    Assert.assertTrue(AntPatternUtil.IsFileNameMatch("a*/**b**", "abc/ss/aa/vv/ggbbhh/qq"));
    Assert.assertFalse(AntPatternUtil.IsFileNameMatch("a*/**b**", "abc/ss/aa/vv/gghh/qq"));
    Assert.assertTrue(AntPatternUtil.IsFileNameMatch("**/bin/**.exe", "My/BiN/Debug/program.exe"));
    Assert.assertFalse(AntPatternUtil.IsFileNameMatch("**/bin/**.exe", "My/BiN/Debug/program.dll"));
    Assert.assertTrue(AntPatternUtil.IsFileNameMatch("**/bin/**.exe", "bin/program.exe"));

    Assert.assertTrue(AntPatternUtil.IsFileNameMatch("**aaa**ddd***ccc**", "aaa/aaa/ddd/ddd/ccc/ccc"));
    Assert.assertFalse(AntPatternUtil.IsFileNameMatch("**aaa**ddd***ccc**", "aaa/aaa/ccc/ccc"));

    Assert.assertTrue(AntPatternUtil.IsFileNameMatch(".\\s.slN", "S.sln"));
    Assert.assertTrue(AntPatternUtil.IsFileNameMatch("s.slN", ".\\S.sln"));

    Assert.assertFalse(AntPatternUtil.IsFileNameMatch(".", ""));
  }

  @Test
  public void ParsePattern() {
    AssertParseResult("a///\\\\bcd", "a:bcd");
    AssertParseResult("//", "");
    AssertParseResult("*****/**.exe", "**:*.exe");
    AssertParseResult("**/bin/**.dll", "**:bin:**:*.dll"); // real-life example
    AssertParseResult("**a**b**/bin", "**:*a*:**:*b*:**:bin");
    AssertParseResult("a**a**b**", "a*:**:*a*:**:*b*:**");
    AssertParseResult("**a**b**d", "**:*a*:**:*b*:**:*d");

    AssertParseResult("../a/b/c", "a:b:c");
    AssertParseResult("a/../b/../c", "c");
    AssertParseResult("a/b/c/..", "a:b");
    AssertParseResult("a/../../c", "c");
    AssertParseResult("a/b/../../c", "c");
    AssertParseResult("a/b/c/../../../d", "d");
  }

  @Test
  public void TestParseLeadingSlash() {
    AssertParseResult("/aaa/bbb/ccc", "aaa:bbb:ccc");
  }

  @Test
  public void TestParseWindowsFullPath() {
    AssertParseResult("c:/aaa/bbb/ccc", "c::aaa:bbb:ccc");
  }

}
