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
package jetbrains.buildServer.nuget.agent.runner.publish.fsScanner;

import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/// <summary>
/// Nant-syntax wildcard matcher on file system trees
/// </summary>
public class DirectoryScanner {
  private static final Logger LOG = Logger.getInstance(DirectoryScanner.class.getName());

  public static Collection<File> FindFiles(@NotNull File root, Collection<String> includes, Collection<String> excludes) {
    return FindFiles(new RealFileSystem(), new RealDirectoryEntry(new FileSystemPath(root)), includes, excludes);
  }

  private static Collection<File> FindFiles(IFileSystem fs, IDirectoryEntry root, Collection<String> includes, Collection<String> excludes) {
    List<Wildcard> basePath = BuildSearchPrefix(root, fs.CaseSensitive());

    List<FileSystemPath> result = new ArrayList<FileSystemPath>();
    FindFilesRec(
            fs.Root(),
            result,
            ToAntPatternState(fs, basePath, fs.CaseSensitive(), includes),
            ToAntPatternState(fs, basePath, fs.CaseSensitive(), excludes)
    );

    List<File> foundFiles = new ArrayList<File>();
    for (FileSystemPath path : result) {
      foundFiles.add(path.FilePath());
    }
    return foundFiles;
  }

  private static List<Wildcard> BuildSearchPrefix(@NotNull IDirectoryEntry root, boolean caseSensitive) {
    List<Wildcard> wildcardPrefix = new ArrayList<Wildcard>();
    while (root.Parent() != null) {
      wildcardPrefix.add(new Wildcard(root.Name(), caseSensitive));
      root = root.Parent();
    }
    Collections.reverse(wildcardPrefix);

    return wildcardPrefix;
  }

  private static List<AntPatternState> ToAntPatternState(IFileSystem fs, List<Wildcard> wildcardPrefix, boolean caseSensitive, Collection<String> patterns) {
    List<AntPatternState> result = new ArrayList<AntPatternState>();
    for (String x : patterns) {
      result.add(new AntPatternState(ParsePattern(fs, wildcardPrefix, caseSensitive, x)));
    }
    return result;
  }

  private static List<Wildcard> ParsePattern(IFileSystem fs, List<Wildcard> rootPrefix, boolean caseSensitive, String pattern) {
    List<Wildcard> wildcards = AntPatternUtil.ParsePattern(pattern, caseSensitive);

    if (fs.IsPathAbsolute(pattern))
      return wildcards;

    List<Wildcard> result = new ArrayList<Wildcard>();
    result.addAll(rootPrefix);
    result.addAll(wildcards);

    return result;
  }

  private interface AnyPredicate {
    boolean matches(AntPatternState.MatchResult r);
  }

  private static boolean Any(List<AntPatternState> state, String component, AnyPredicate predicate, List<AntPatternState> newState) {
    boolean any = false;
    newState.clear();

    for (AntPatternState aState : state) {
      final AntPatternState.AntPatternStateMatch enter = aState.Enter(component);
      AntPatternState.MatchResult match = enter.getResult();
      newState.add(enter.getState());

      if (predicate.matches(match))
        any = true;
    }

    return any;
  }

  private static void FindFilesRec(IDirectoryEntry directory, List<FileSystemPath> result, List<AntPatternState> includeState, List<AntPatternState> excludeState) {
    LOG.debug("Scanning directory: " + directory.Name());
    for (IFileEntry file : directory.Files()) {
      List<AntPatternState> newState = new ArrayList<AntPatternState>();

      if (!Any(includeState, file.Name(), Predicate(false, AntPatternState.MatchResult.YES), newState))
        continue;

      newState.clear();
      if (Any(excludeState, file.Name(), Predicate(false, AntPatternState.MatchResult.YES), newState))
        continue;

      result.add(file.Path());
    }

    for (IDirectoryEntry subEntry : directory.Subdirectories()) {
      String name = subEntry.Name();

      List<AntPatternState> newIncludeState = new ArrayList<AntPatternState>();
      if (!Any(includeState, name, Predicate(true, AntPatternState.MatchResult.NO), newIncludeState))
        continue;

      List<AntPatternState> newExcludeState = new ArrayList<AntPatternState>();
      if (Any(excludeState, name, Predicate(false, AntPatternState.MatchResult.YES), newExcludeState))
        continue;

      FindFilesRec(subEntry, result, newIncludeState, newExcludeState);
    }
  }

  public static AnyPredicate Predicate(final boolean not, final AntPatternState.MatchResult state) {
    return new AnyPredicate() {
      public boolean matches(AntPatternState.MatchResult r) {
        return not ? state != r : state == r;
      }
    };
  }

}
