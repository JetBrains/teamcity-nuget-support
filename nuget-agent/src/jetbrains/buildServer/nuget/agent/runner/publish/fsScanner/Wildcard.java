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

import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class Wildcard {
  private final String myPattern;
  private final boolean myCaseSensitive;

  public Wildcard(String pattern, boolean caseSensitive) {
    pattern = pattern.replaceAll("[\\*]+", "*");
    myCaseSensitive = caseSensitive;

    myPattern = !caseSensitive ? pattern.toLowerCase() : pattern;
  }

  public String getPattern() {
    return myPattern;
  }

  public boolean isMatch(@NotNull String str) {
    String pattern = myPattern;

    if (StringUtil.isEmptyOrSpaces(pattern))
      return StringUtil.isEmptyOrSpaces(str);

    if (!myCaseSensitive)
      str = str.toLowerCase();

    int patlen = pattern.length();
    List<Integer> positions = new ArrayList<Integer>();
    positions.add(0);
    for (int j = 0; j < str.length(); j++) {
      char c = str.charAt(j);

      boolean hasPositionsToMatch = false;

      int len = positions.size();
      for (int i = 0; i < len; i++) {
        int pat = positions.get(i);
        if (pat >= patlen || pat < 0)
          continue;

        hasPositionsToMatch = true;

        if (pattern.charAt(pat) == '*') {
          if (pat == patlen - 1)
            return true;

          if (matchChars(pattern.charAt(pat + 1), c)) {
            if (pat == patlen - 2 && j == str.length() - 1)
              return true;
            positions.add(pat + 2);
          }
        } else {
          if (matchChars(pattern.charAt(pat), c)) {
            if (pat == patlen - 1 && j == str.length() - 1)
              return true;

            positions.set(i, positions.get(i) + 1);
          } else positions.set(i, -1);
        }
      }

      if (!hasPositionsToMatch)
        return false;
    }

    for (Integer x : positions) {
      if (x == patlen - 1 && pattern.charAt(x) == '*')
        return true;
    }
    return false;
  }

  private static boolean matchChars(char pat, char c) {
    return pat == '?' || pat == c;
  }

  @Override
  public String toString() {
    return "Wildcard{" + myPattern + "}";
  }

  public boolean containsNoPatterns() {
    return !myPattern.contains("?") && !myPattern.contains("*");
  }
}
