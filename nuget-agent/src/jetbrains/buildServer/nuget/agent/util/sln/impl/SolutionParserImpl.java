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

package jetbrains.buildServer.nuget.agent.util.sln.impl;

import com.intellij.openapi.util.io.StreamUtil;
import jetbrains.buildServer.nuget.agent.util.sln.SolutionFileParser;
import jetbrains.buildServer.util.FileUtil;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 15.06.12 20:05
 */
public class SolutionParserImpl implements SolutionFileParser {
  @NotNull
  public Collection<File> parseProjectFiles(@NotNull final File sln) throws IOException {
    final File root = sln.getParentFile();
    if (root == null) Collections.emptyList();

    final Collection<String> lines = readFileText(sln);
    final Pattern pt = Pattern.compile("^\\s*Project\\(\".*\"\\)\\s*");

    final List<File> files = new ArrayList<File>();
    for (String line : lines) {
      final Matcher matcher = pt.matcher(line);
      if (matcher.matches()) {
        files.add(new File(root, matcher.group(1)));
      }
    }

    return Collections.unmodifiableList(files);
  }

  @NotNull
  private Collection<String> readFileText(@NotNull final File file) throws IOException {
    final FileInputStream is = new FileInputStream(file);
    try {
      final String text = StreamUtil.readText(is, "utf-8");
      return Arrays.asList(text.split("[\\r\\n]+"));
    } finally {
      FileUtil.close(is);
    }
  }
}
