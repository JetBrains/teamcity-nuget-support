/*
 * Copyright 2000-2012 JetBrains s.r.o.
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

package jetbrains.buildServer.nuget.tests.agent;

import jetbrains.buildServer.BaseTestCase;
import jetbrains.buildServer.nuget.agent.parameters.PackageSource;
import jetbrains.buildServer.nuget.agent.parameters.impl.PackageSourceParser;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 25.06.12 18:17
 */
public class PackageSourceParserTest extends BaseTestCase {
  private PackageSourceParser myParser = new PackageSourceParser();

  @Test
  public void test_local_path() {
    doTest(t("\\\\aaa"), t("[null][null]\\\\aaa"));
    doTest(t("aaa"), t("[null][null]aaa"));
    doTest(t("file://aaa"), t("[null][null]file://aaa"));
    doTest(t("file://aaa:bbb@aaa"), t("[null][null]file://aaa:bbb@aaa"));
    doTest(t("http://aaa"), t("[null][null]http://aaa"));
    doTest(t("https://aaa/fff@"), t("[null][null]https://aaa/fff@"));
    doTest(t("http://aaa/fff@"), t("[null][null]http://aaa/fff@"));
    doTest(t("ftp://aaa/fff@"), t("[null][null]ftp://aaa/fff@"));
    doTest(t("http://jonny:aaa@fff"), t("[jonny][aaa]http://fff"));
    doTest(t("http://jonny:aaa@fff.local/@feed"), t("[jonny][aaa]http://fff.local/@feed"));
    doTest(t("https://jonny:aaa@fff"), t("[jonny][aaa]https://fff"));
    doTest(t("https://jonny:aaa@fff.local/@feed"), t("[jonny][aaa]https://fff.local/@feed"));
    doTest(t("https://jonny:a%20aa@fff.local/ddd"), t("[jonny][a aa]https://fff.local/ddd"));
    doTest(t("https://jonny:a%20aa@fff.l:oc:al/dd:d"), t("[jonny][a aa]https://fff.l:oc:al/dd:d"));
    doTest(t("https://jon:ny:a%20aa@fff.l:oc:al/dd:d"), t("[jon:ny][a aa]https://fff.l:oc:al/dd:d"));
  }

  private void doTest(String[] text, String[] gold) {
    final Collection<PackageSource> actual = myParser.parseSources(Arrays.asList(text));
    final Collection<String> goldData = new HashSet<String>(Arrays.asList(gold));

    for (PackageSource src : actual) {
      final String s = MessageFormat.format("[{0}][{1}]{2}", src.getUserName(), src.getPassword(), src.getSource());
      Assert.assertTrue(goldData.remove(s), "Must contain: " + s);
    }
  }


  protected static <T> T[] t(T...t) {
    return t;
  }

  protected static <T> Collection<T> c(T...t) {
    return Arrays.asList(t);
  }
}
