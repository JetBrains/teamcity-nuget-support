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
package jetbrains.buildServer.nuget.agent.util.fsScanner;

import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.*;

/// <summary>
/// Nant-syntax wildcard matcher on file system trees
/// </summary>
public class DirectoryScanner {
  private static final Logger LOG = Logger.getInstance(DirectoryScanner.class.getName());

  public static Collection<File> findFiles(@NotNull File root,
                                           @NotNull final Collection<String> includes,
                                           @NotNull final Collection<String> excludes) {
    return findFiles(new RealFileSystem(), new RealDirectoryEntry(new FileSystemPath(root)), includes, excludes);
  }

  public static Collection<File> findFiles(@NotNull final FileSystem fs,
                                           @NotNull final DirectoryEntry root,
                                           @NotNull final Collection<String> includes,
                                           @NotNull final Collection<String> excludes) {
    List<Wildcard> basePath = buildSearchPrefix(root, fs.caseSensitive());

    List<FileSystemPath> result = new ArrayList<FileSystemPath>();
    findFilesRec(
            fs.getRoot(),
            result,
            toAntPatternState(fs, basePath, fs.caseSensitive(), includes),
            toAntPatternState(fs, basePath, fs.caseSensitive(), excludes)
    );

    Set<File> foundFiles = new TreeSet<File>();
    for (FileSystemPath path : result) {
      foundFiles.add(path.getFilePath());
    }
    return foundFiles;
  }

  private static List<Wildcard> buildSearchPrefix(@NotNull DirectoryEntry root, boolean caseSensitive) {
    List<Wildcard> wildcardPrefix = new ArrayList<Wildcard>();
    while (true) {
      final DirectoryEntry parent = root.getParent();
      if (parent == null) break;

      wildcardPrefix.add(new Wildcard(root.getName(), caseSensitive));
      root = parent;
    }
    Collections.reverse(wildcardPrefix);

    return wildcardPrefix;
  }

  @NotNull
  private static List<AntPatternState> toAntPatternState(@NotNull final FileSystem fs,
                                                         @NotNull final List<Wildcard> wildcardPrefix,
                                                         boolean caseSensitive,
                                                         @NotNull final Collection<String> patterns) {
    List<AntPatternState> result = new ArrayList<AntPatternState>();
    for (String x : patterns) {
      result.add(new AntPatternState(ParsePattern(fs, wildcardPrefix, caseSensitive, x)));
    }
    return result;
  }

  @NotNull
  private static List<Wildcard> ParsePattern(@NotNull final FileSystem fs,
                                             @NotNull final List<Wildcard> rootPrefix,
                                             boolean caseSensitive,
                                             @NotNull final String pattern) {
    List<Wildcard> wildcards = AntPatternUtil.parsePattern(pattern, caseSensitive);

    if (fs.isPathAbsolute(pattern))
      return wildcards;

    List<Wildcard> result = new ArrayList<Wildcard>();
    result.addAll(rootPrefix);
    result.addAll(wildcards);
    return result;
  }

  private static boolean any(@NotNull final List<AntPatternState> state,
                             @NotNull final String component,
                             @NotNull final AnyPredicate predicate,
                             @NotNull final List<AntPatternState> newState) {
    boolean any = false;
    newState.clear();

    for (AntPatternState aState : state) {
      final AntPatternStateMatch enter = aState.enter(component);
      MatchResult match = enter.getResult();
      newState.add(enter.getState());

      if (predicate.matches(match))
        any = true;
    }

    return any;
  }

  private static void findFilesRec(@NotNull final DirectoryEntry directory,
                                   @NotNull final List<FileSystemPath> result,
                                   @NotNull final List<AntPatternState> includeState,
                                   @NotNull final List<AntPatternState> excludeState) {
    LOG.debug("Scanning directory: " + directory.getName());

    boolean mayContainFiles = false;
    Collection<String> explicits = new ArrayList<String>();
    for (AntPatternState state : includeState) {
      final Collection<String> nextTokens = state.nextTokes();
      if (nextTokens == null) {
        explicits = null;
        mayContainFiles = true;
        break;
      }
      if (!mayContainFiles) {
        mayContainFiles = state.hasLastState();
      }
      explicits.addAll(nextTokens);
    }

    if (mayContainFiles) {
      for (FileEntry file : explicits != null ? directory.getFiles(explicits) : directory.getFiles()) {
        List<AntPatternState> newState = new ArrayList<AntPatternState>();

        if (!any(includeState, file.getName(), equal(MatchResult.YES), newState))
          continue;

        if (any(excludeState, file.getName(), equal(MatchResult.YES), newState))
          continue;

        result.add(file.getPath());
      }
    }


    for (DirectoryEntry subEntry : explicits != null ? directory.getSubdirectories(explicits) : directory.getSubdirectories()) {
      String name = subEntry.getName();

      List<AntPatternState> newIncludeState = new ArrayList<AntPatternState>();
      if (!any(includeState, name, notEqual(MatchResult.NO), newIncludeState))
        continue;

      List<AntPatternState> newExcludeState = new ArrayList<AntPatternState>();
      if (any(excludeState, name, equal(MatchResult.YES), newExcludeState))
        continue;

      findFilesRec(subEntry, result, newIncludeState, newExcludeState);
    }
  }

  private interface AnyPredicate {
    boolean matches(@NotNull MatchResult r);
  }

  private static AnyPredicate notEqual(@NotNull final MatchResult result) {
    return new AnyPredicate() {
      public boolean matches(@NotNull MatchResult r) {
        return result != r;
      }
    };
  }

  private static AnyPredicate equal(@NotNull final MatchResult result) {
    return new AnyPredicate() {
      public boolean matches(@NotNull MatchResult r) {
        return result == r;
      }
    };
  }

}
