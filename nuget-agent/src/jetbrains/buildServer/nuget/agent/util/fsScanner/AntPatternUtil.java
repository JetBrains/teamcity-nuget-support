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

import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class AntPatternUtil {
  private static String normalizePatternString(@NotNull String pattern) {
    pattern = pattern.trim();
    pattern = pattern.replace('\\', '/');
    pattern = pattern.replaceAll("[//]+", "/");
    pattern = pattern.replaceAll("\\*\\*[\\*]+", "**");
    pattern = pattern.replaceAll("\\*\\*/\\*\\*", "**");
    if (pattern.startsWith("/")) pattern = pattern.substring(1);
    return pattern;
  }

  public static boolean isFileNameMatch(@NotNull final String pattern, @NotNull String name) {
    name = name.trim();
    name = name.replace('\\', '/');
    name = name.replaceAll("[//]+", "/");

    List<Wildcard> wildcards = parsePattern(pattern, false);
    AntPatternState state = new AntPatternState(wildcards);

    List<String> splitted = new ArrayList<String>(Arrays.asList(name.split("/")));
    List<String> nameComponents = new ArrayList<String>();
    for (String s : splitted) {
      if (!StringUtil.isEmptyOrSpaces(s)) nameComponents.add(s);
    }

    MatchResult matchResult = MatchResult.NO;

    for (int i = 0; i < nameComponents.size(); i++) {
      if (nameComponents.get(i).equals(".") && i < nameComponents.size() - 1)
        continue;

      final AntPatternStateMatch enter = state.enter(nameComponents.get(i));
      matchResult = enter.getResult();
      state = enter.getState();
    }

    return matchResult == MatchResult.YES;
  }

  @NotNull
  public static List<Wildcard> parsePattern(@NotNull String pattern, boolean caseSensitive) {
    pattern = normalizePatternString(pattern);
    if (pattern.length() == 0)
      return Collections.emptyList();

    List<Wildcard> result = new ArrayList<Wildcard>();
    for (String component : pattern.split("/")) {
      int astIndex = component.indexOf("**");
      if (astIndex < 0) {
        result.add(new Wildcard(component, caseSensitive));
        continue;
      }

      if (astIndex > 0) {
        result.add(new Wildcard(component.substring(0, astIndex) + "*", caseSensitive));
      }

      int cur = astIndex;
      while (astIndex >= 0) {
        if (astIndex - cur > 0)
          result.add(new Wildcard("*" + component.substring(cur, astIndex) + "*", caseSensitive));

        result.add(null);

        cur = astIndex + 2;
        astIndex = component.indexOf("**", cur);
      }

      if (cur < component.length()) {
        result.add(new Wildcard("*" + component.substring(cur), caseSensitive));
      }
    }

    removeTwoDots(result);
    removeOneDot(result);

    return result;
  }

  private static void removeOneDot(@NotNull final List<Wildcard> wildcards) {
    int i = 0;
    while (i < wildcards.size() - 1) {
      if (wildcards.get(i) != null && wildcards.get(i).getPattern().equals(".")) {
        wildcards.remove(i);
      } else {
        i++;
      }
    }
  }

  private static void removeTwoDots(List<Wildcard> wildcards) {
    int i = 0;
    while (i < wildcards.size() - 1) {
      if (wildcards.get(i + 1) != null && wildcards.get(i + 1).getPattern().equals("..")) {
        wildcards.remove(i);
        wildcards.remove(i);

        if (i > 0) i--;
      } else {
        i++;
      }
    }

    while (wildcards.size() > 0 && wildcards.get(0) != null && wildcards.get(0).getPattern().equals("..")) {
      wildcards.remove(0);
    }
  }
}
