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

package jetbrains.buildServer.nuget.tests.server;

import jetbrains.buildServer.BaseTestCase;
import jetbrains.buildServer.nuget.server.exec.SourcePackageInfo;
import jetbrains.buildServer.nuget.server.trigger.PackagesHashCalculator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.security.SecureRandom;
import java.util.*;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 16.11.11 14:34
 */
public class PackagesHashCalculatorTest extends BaseTestCase {
  private PackagesHashCalculator myCalculator;

  @BeforeMethod
  @Override
  protected void setUp() throws Exception {
    super.setUp();
    myCalculator = new PackagesHashCalculator();
  }

  @Test
  public void testHash1() {
    doCalculatorTest("|s:a|p:b|v:c", p("a", "b", "c"));
  }

  @Test
  public void testHash2() {
    doCalculatorTest("|p:b|v:c", p(null, "b", "c"));
  }

  @Test
  public void testMultipleHash_defaultSOource() {
    doCalculatorTest("|p:p1|v:v1|p:p1|v:v2|p:p2|v:v1", p(null, "p1", "v1"), p(null, "p2", "v1"), p(null, "p1", "v2"));
  }

  @Test
  public void testMultipleHash_customSource() {
    doCalculatorTest("|s:c|p:p1|v:v1|s:c|p:p1|v:v2|s:c|p:p2|v:v1", p("c", "p1", "v1"), p("c", "p2", "v1"), p("c", "p1", "v2"));
  }

  @Test
  public void testMultipleHash_mix() {
    doCalculatorTest("|s:c|p:p1|v:v1|s:c|p:p1|v:v2|s:c|p:p2|v:v1|p:p1|v:v1|p:p1|v:v2|p:p2|v:v1", p("c", "p1", "v1"), p("c", "p2", "v1"), p("c", "p1", "v2"), p(null, "p1", "v1"), p(null, "p2", "v1"), p(null, "p1", "v2"));
  }

  @Test
  public void testMultipleHash_big() {
    final String[] sources = {null, "s1", "s2"};
    final String[] packages = {"p1", "q", };
    final String[] versions = {"2.3.5", "0.5.4"};

    List<SourcePackageInfo> infos = new ArrayList<SourcePackageInfo>();
    for (String source : sources) {
      for (String aPackage : packages) {
        for (String version : versions) {
          infos.add(p(source, aPackage, version));
        }
      }
    }

    //12! is too much to check
    final Random r = new SecureRandom();
    final String hash = "v2|s:s1|p:p1|v:0.5.4|s:s1|p:p1|v:2.3.5|s:s1|p:q|v:0.5.4|s:s1|p:q|v:2.3.5|s:s2|p:p1|v:0.5.4|s:s2|p:p1|v:2.3.5|s:s2|p:q|v:0.5.4|s:s2|p:q|v:2.3.5|p:p1|v:0.5.4|p:p1|v:2.3.5|p:q|v:0.5.4|p:q|v:2.3.5";
    for (int i = 0; i < 1000; i++) {
      Collections.shuffle(infos, r);
      assertHash(hash, infos);
    }
  }

  private SourcePackageInfo p(@Nullable String source, @NotNull String pkgId, @NotNull String version) {
    return new SourcePackageInfo(source, pkgId, version);
  }

  private void doCalculatorTest(@NotNull String hash, SourcePackageInfo... infos) {
    hash = "v2" + hash;
    assertHash(hash, Arrays.asList(infos));
    for (Collection<SourcePackageInfo> pkgs : allPermutations(Arrays.asList(infos))) {
      assertHash(hash, pkgs);
    }
  }

  private void assertHash(String hash, Collection<SourcePackageInfo> pkgs) {
    Assert.assertEquals(myCalculator.serializeHashcode(pkgs), hash, "Packages: " + new ArrayList<SourcePackageInfo>(pkgs));
  }


  @NotNull
  private <T> Collection<Collection<T>> allPermutations(Collection<T> initial) {
    if (initial.size() == 0) return Collections.emptyList();
    if (initial.size() == 1) return Collections.singleton(initial);

    Collection<Collection<T>> result = new ArrayList<Collection<T>>();

    for (T first : initial) {
      List<T> data = new LinkedList<T>(initial);
      data.remove(first);

      final Collection<Collection<T>> ref = allPermutations(data);

      for (Collection<T> ts : ref) {
        List<T> rr = new ArrayList<T>();
        rr.add(first);
        rr.addAll(ts);
        result.add(rr);
      }
    }
    return result;
  }

}
