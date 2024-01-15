

package jetbrains.buildServer.nuget.agent.util.fsScanner;

import org.jetbrains.annotations.NotNull;

/**
* @author Eugene Petrenko (eugene.petrenko@gmail.com)
*         Date: 25.08.11 11:56
*/
public class AntPatternStateMatch {
  private final MatchResult myResult;
  private final AntPatternState myState;

  public AntPatternStateMatch(@NotNull final MatchResult result,
                              @NotNull final AntPatternState state) {
    myResult = result;
    myState = state;
  }

  @NotNull
  public MatchResult getResult() {
    return myResult;
  }

  @NotNull
  public AntPatternState getState() {
    return myState;
  }
}
