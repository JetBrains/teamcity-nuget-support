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

import com.intellij.openapi.diagnostic.Logger;
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
  private static final Logger LOG = Logger.getInstance(SolutionParserImpl.class.getName());

  @NotNull
  public Collection<File> parseProjectFiles(@NotNull final File sln) throws IOException {
    final File root = sln.getParentFile();
    if (root == null) return Collections.emptyList();

    final Collection<String> lines = readFileText(sln);
    boolean isInsideProject = false;

    final List<File> files = new ArrayList<File>();
    for (String line : lines) {
      final Matcher projectStart = PROJECT_PATTERTN.matcher(line);
      if (projectStart.matches()) {
        isInsideProject = true;
        resolveAndAddFile(root, files, line, projectStart.group(1));
        continue;
      }

      if (PROJECT_END_PATTERTN.matcher(line).matches()) {
        isInsideProject = false;
        continue;
      }

      final Matcher slnRelPath = PROJECT_SLN_RELATIVE_PATH.matcher(line);
      if (isInsideProject && slnRelPath.matches()) {
        resolveAndAddFile(root, files, line, slnRelPath.group(1));
      }
    }

    return Collections.unmodifiableList(files);
  }

  private void resolveAndAddFile(File root, List<File> files, String line, String relPath) {
    if (relPath.contains("://") || relPath.trim().startsWith("\\\\")) {
      LOG.warn("Failed to resolve project path: " + line);
    } else {
      files.add(FileUtil.resolvePath(root, relPath));
    }
  }

  private final Pattern PROJECT_PATTERTN = Pattern.compile(
          "^\\s*Project\\(\"\\{[0-9A-Z\\-]+\\}\"\\)\\s*=\\s*\".*\"\\s*,\\s*\"(.*)\"\\s*,.*$",
          Pattern.CASE_INSENSITIVE);
  private final Pattern PROJECT_END_PATTERTN = Pattern.compile(
          "^\\s*EndProject\\s*$",
          Pattern.CASE_INSENSITIVE);
  private final Pattern PROJECT_SLN_RELATIVE_PATH = Pattern.compile(
          "^\\s*SlnRelativePath\\s*=\\s*\"(.*)\".*$",
          Pattern.CASE_INSENSITIVE);


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
