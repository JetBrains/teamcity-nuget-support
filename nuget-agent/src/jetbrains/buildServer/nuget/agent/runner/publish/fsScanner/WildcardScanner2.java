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

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 24.08.11 22:21
 */
public class WildcardScanner2 {
/*
  public Collection<File> matchWildcards(@NotNull final File root,
                                         @NotNull final Collection<String> includes,
                                         @NotNull final Collection<String> excludes) {

  }


  private static class MatchState {
    private final List<Wildcard> myUnmatchedWildcards;
    private final File myRoot;

    private MatchState(@NotNull final File root, List<Wildcard> unmatchedWildcards) {
      myUnmatchedWildcards = unmatchedWildcards;
    }

    @NotNull
    public MatchResult match() {
      assert myUnmatchedWildcards.size() > 0;
      final Wildcard wd = myUnmatchedWildcards.iterator().next();

      if (!wd.matches(name)) {
        return new MatchResult(false, Collections.<MatchState>emptyList());
      }

      List<Wildcard> next = new ArrayList<Wildcard>(myUnmatchedWildcards);
      List<MatchState> states = new ArrayList<MatchState>(2);

      if (wd.isFork()) {
        states.add(this);
      }
      next.remove(0);

      if (next.isEmpty()) {
        return new MatchResult(true, states);
      } else {
        states.add(new MatchState(next));
        return new MatchResult(false, states);
      }
    }
  }

  private static class MatchResult {
    private boolean myIsMatched;
    private Collection<MatchState> myMatches;

    private MatchResult(boolean isMatched, Collection<MatchState> matches) {
      myIsMatched = isMatched;
      myMatches = matches;
    }

    public boolean isIsMatched() {
      return myIsMatched;
    }

    public Collection<MatchState> getMatches() {
      return myMatches;
    }
  }

  private abstract static class Wildcard {
    protected final String myPattern;

    protected Wildcard(@NotNull final String pattern) {
      myPattern = pattern;
    }

    public abstract boolean matches(@NotNull final String name);
    public abstract boolean isPattern();
    public abstract boolean isFork();
  }

  private class NameWildcard extends Wildcard {
    private NameWildcard(@NotNull String pattern) {
      super(pattern);
    }

    @Override
    public boolean matches(@NotNull String name) {
      return myPattern.equals(name);
    }

    @Override
    public boolean isPattern() {
      return false;
    }

    @Override
    public boolean isFork() {
      return false;
    }
  }

  private class PatternWildcard extends Wildcard {
    private final Pattern myPattern;
    private PatternWildcard(@NotNull String pattern) {
      super(pattern);
      myPattern = Pattern.compile("$" +
              pattern.replaceAll("\\.", "\\.").replaceAll("\\*", ".*").replaceAll("\\?", ".?") + "^");
    }

    @Override
    public boolean matches(@NotNull String name) {
      return myPattern.matcher(name).matches();
    }

    @Override
    public boolean isPattern() {
      return true;
    }

    @Override
    public boolean isFork() {
      return false;
    }
  }

  private class DoubleStarWildcard extends Wildcard {
    private DoubleStarWildcard() {
      super("**");
    }

    @Override
    public boolean matches(@NotNull String name) {
      return true;
    }

    @Override
    public boolean isPattern() {
      return true;
    }

    @Override
    public boolean isFork() {
      return true;
    }
  }
*/
}
