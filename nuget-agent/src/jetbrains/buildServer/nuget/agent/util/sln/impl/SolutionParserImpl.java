

package jetbrains.buildServer.nuget.agent.util.sln.impl;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.util.io.StreamUtil;
import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.agent.BuildProgressLogger;
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
  public Collection<File> parseProjectFiles(@NotNull final BuildProgressLogger logger,
                                            @NotNull final File sln) throws RunBuildException {
    final File root = sln.getParentFile();
    if (root == null) return Collections.emptyList();

    final Collection<String> lines = readFileText(sln);
    boolean isInsideProject = false;

    final List<File> files = new ArrayList<File>();
    for (String line : lines) {
      final Matcher projectStart = PROJECT_PATTERTN.matcher(line);
      if (projectStart.matches()) {
        isInsideProject = true;
        resolveAndAddFile(logger, root, line, projectStart.group(1), files);
        continue;
      }

      if (PROJECT_END_PATTERTN.matcher(line).matches()) {
        isInsideProject = false;
        continue;
      }

      final Matcher slnRelPath = PROJECT_SLN_RELATIVE_PATH.matcher(line);
      if (isInsideProject && slnRelPath.matches()) {
        resolveAndAddFile(logger, root, line, slnRelPath.group(1), files);
      }
    }

    return Collections.unmodifiableList(files);
  }

  private void resolveAndAddFile(@NotNull final BuildProgressLogger logger,
                                 @NotNull final File root,
                                 @NotNull final String line,
                                 @NotNull final String relativePath,
                                 @NotNull final List<File> files) {
    final String relPath = relativePath.replace("\\", File.separator);
    if (relPath.contains("://") || relPath.trim().startsWith("\\\\")) {
      String msg = "Failed to resolve project reference from solution file: " + line;
      LOG.warn(msg);
      logger.warning(msg);
    } else {
      final File path = FileUtil.resolvePath(root, relPath);
      LOG.debug("Found project: " + path);
      files.add(path);
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
  private Collection<String> readFileText(@NotNull final File file) throws RunBuildException {
    FileInputStream is = null;

    try {
      is = new FileInputStream(file);
      final String text = StreamUtil.readText(is, "utf-8");
      return Arrays.asList(text.split("[\\r\\n]+"));
    } catch (IOException e) {
      throw new RunBuildException("Failed to open solution file: " + file);
    } finally {
      FileUtil.close(is);
    }
  }
}
