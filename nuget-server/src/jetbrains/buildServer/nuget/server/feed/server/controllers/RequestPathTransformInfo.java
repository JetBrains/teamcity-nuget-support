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

package jetbrains.buildServer.nuget.server.feed.server.controllers;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * @author Yegor.Yarko
 *         Date: 15.11.2009
 */
public class RequestPathTransformInfo implements PathTransformator {
  @NotNull private Map<String, String> myPathMapping;

  public RequestPathTransformInfo() {
  }

  public void setPathMapping(@Nullable final Map<String, String> pathMapping) {
    myPathMapping = pathMapping != null ? pathMapping : Collections.<String, String>emptyMap();
  }

  @Override
  public String toString() {
    return "Path mapping: " + myPathMapping;
  }

  @NotNull
  private static String getLargerstMatchingSubstring(@NotNull final String path, final Set<String> substrings) {
    String result = "";
    for (String substring : substrings) {
      boolean matches = path.contains(substring);
      if (matches && result.length() < substring.length()) {
        result = substring;
      }
    }
    return result;
  }

  @NotNull
  public String getTransformedPath(@NotNull final String path) {
    String matching = getLargerstMatchingSubstring(path, myPathMapping.keySet());
    if (matching.length() == 0){
      return path;
    }

    return replaceFirstSubstring(path, matching, myPathMapping.get(matching));
  }

  private static String replaceFirstSubstring(final String s, final String from, final String to) {
    final int i = s.indexOf(from);
    return s.substring(0, i) + to + s.substring(i+ from.length());
  }

  @NotNull
  public PathTransformator getReverseTransformator(@NotNull final String originalPath, final boolean prefixSupported) {
    final String matching = getLargerstMatchingSubstring(originalPath, myPathMapping.keySet());
    if (matching.length() == 0){
      return new PathTransformator(){
        @NotNull
        public String getTransformedPath(@NotNull final String path) {
          return path;
        }
      };
    }

    final String prefix = prefixSupported ? originalPath.substring(0, originalPath.indexOf(matching)) : "";
    final String prefixWithNewPart = prefix + myPathMapping.get(matching);
    return new PathTransformator(){
      @NotNull
      public String getTransformedPath(@NotNull final String path) {
        if (!path.startsWith(prefixWithNewPart)){
          return path;
        }
        return prefix + matching + path.substring(prefixWithNewPart.length());
      }
    };
  }
}
