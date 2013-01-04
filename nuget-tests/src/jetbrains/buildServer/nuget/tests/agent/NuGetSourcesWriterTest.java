/*
 * Copyright 2000-2013 JetBrains s.r.o.
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
import jetbrains.buildServer.nuget.agent.commands.impl.NuGetSourcesWriter;
import jetbrains.buildServer.nuget.agent.parameters.PackageSource;
import jetbrains.buildServer.util.FileUtil;
import jetbrains.buildServer.util.StringUtil;
import jetbrains.buildServer.util.XmlUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

/**
 * Created 04.01.13 19:43
 *
 * @author Eugene Petrenko (eugene.petrenko@jetbrains.com)
 */
public class NuGetSourcesWriterTest extends BaseTestCase {
  private NuGetSourcesWriter myWriter = new NuGetSourcesWriter();

  @Test
  public void testSerialize_one() throws IOException {
    final File tmp = createTempFile();

    myWriter.writeNuGetSources(tmp, t(source("a", "b", "c")));

    final String xml = new String(FileUtil.loadFileText(tmp, "utf-8"));
    final String reformatted = StringUtil.convertLineSeparators(XmlUtil.to_s(XmlUtil.from_s(xml)));
    final String gold = StringUtil.convertLineSeparators("<sources>\n" +
            "  <source source=\"a\" username=\"b\" password=\"c\" />\n" +
            "</sources>");

    System.out.println(reformatted);
    Assert.assertEquals(reformatted, gold);
  }

  @Test
  public void testSerialize_some() throws IOException {
    final File tmp = createTempFile();

    myWriter.writeNuGetSources(tmp, t(source("a", "b", "c"), source("qqq", null, null)));

    final String xml = new String(FileUtil.loadFileText(tmp, "utf-8"));
    final String reformatted = StringUtil.convertLineSeparators(XmlUtil.to_s(XmlUtil.from_s(xml)));
    final String gold = StringUtil.convertLineSeparators("<sources>\n" +
            "  <source source=\"a\" username=\"b\" password=\"c\" />\n" +
            "  <source source=\"qqq\" />\n" +
            "</sources>");

    System.out.println(reformatted);
    Assert.assertEquals(reformatted, gold);
  }


  @NotNull
  private static <T> Collection<T> t(T... ts) {
    return Arrays.asList(ts);
  }

  @NotNull
  private PackageSource source(@NotNull final String feed,
                               @Nullable final String user,
                               @Nullable final String pass) {
    return new PackageSource() {
      @NotNull
      public String getSource() {
        return feed;
      }

      @Nullable
      public String getUsername() {
        return user;
      }

      @Nullable
      public String getPassword() {
        return pass;
      }
    };
  }
}
