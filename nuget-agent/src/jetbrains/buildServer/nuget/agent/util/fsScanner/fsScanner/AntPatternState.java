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
package jetbrains.buildServer.nuget.agent.util.fsScanner.fsScanner;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class AntPatternState {
  private final List<Wildcard> myPatternParts;
  //NDA state of mathing of pattern. Every ** produces new state
  private final List<Integer> myPatternPositions;

  public AntPatternState(List<Wildcard> patternParts, List<Integer> patternPositions) {
    myPatternParts = patternParts;
    myPatternPositions = patternPositions;
  }

  public AntPatternState(List<Wildcard> patternParts) {
    this(patternParts, initialList());
  }

  private static List<Integer> initialList() {
    List<Integer> list = new ArrayList<Integer>();
    list.add(0);
    return list;
  }

  public List<Integer> getPatternPositions() {
    return myPatternPositions;
  }

  /**
   * @return list of next tokends that are matching or null of there are at least one ** in pattern
   */
  @Nullable
  public Collection<String> nextTokes() {
    List<String> result = new ArrayList<String>();
    for (int position : myPatternPositions) {
      Wildcard wd = myPatternParts.get(position);
      //** is here
      if (wd == null) return null;

      if (wd.containsNoPatterns()) {
        result.add(wd.getPattern());
      } else {
        return null;
      }
    }
    return result;
  }

  public boolean hasLastState() {
    final int totalStates = myPatternParts.size();

    for (int position : myPatternPositions) {
      if (position == totalStates - 1) return true;
    }

    return false;
  }


  public AntPatternStateMatch enter(@NotNull final String component) {
    if (myPatternParts.size() == 0) {
      return new AntPatternStateMatch(MatchResult.NO, this);
    }

    MatchResult match = MatchResult.MAYBELATER;

    //TODO:replace list with array operations

    final List<Integer> newPositions = new ArrayList<Integer>(myPatternPositions.size());

    for (int pos : myPatternPositions) {
      final Wildcard wildcard = myPatternParts.get(pos);

      if (wildcard != null) {
        if (wildcard.isMatch(component)) {
          if (pos == myPatternParts.size() - 1) {
            match = MatchResult.YES;
          } else if (pos == myPatternParts.size() - 2 && myPatternParts.get(pos + 1) == null) {
            match = MatchResult.YES;
            newPositions.add(pos + 1);
          } else {
            newPositions.add(pos + 1);
          }
        } else if (match == MatchResult.MAYBELATER) {
          match = MatchResult.NO;
        }
      } else {
        // **
        newPositions.add(pos);

        if (pos == myPatternParts.size() - 1) {
          match = MatchResult.YES;
        } else {
          if (myPatternParts.get(pos + 1).isMatch(component)) {
            if (pos == myPatternParts.size() - 2) {
              match = MatchResult.YES;
            } else {
              newPositions.add(pos + 2);
            }
          }
        }
      }
    }

    return new AntPatternStateMatch(
            match,
            new AntPatternState(myPatternParts, newPositions));
  }
}