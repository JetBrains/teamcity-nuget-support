

package jetbrains.buildServer.nuget.server.trigger;

import org.jetbrains.annotations.NotNull;

/**
* Created by Eugene Petrenko (eugene.petrenko@gmail.com)
* Date: 14.07.11 15:57
*/
public class BuildStartReason {
  private final String myReason;

  public BuildStartReason(@NotNull final String reason) {
    myReason = reason;
  }

  @NotNull
  public String getReason() {
    return myReason;
  }
}
