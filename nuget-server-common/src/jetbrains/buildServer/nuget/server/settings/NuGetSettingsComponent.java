

package jetbrains.buildServer.nuget.server.settings;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * NuGet settings component.
 *
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 30.10.11 14:14
 */
public enum NuGetSettingsComponent {
  SERVER("server"),
  NUGET("nuget"),
  ;

  private final String myId;

  private NuGetSettingsComponent(@NotNull final String id) {
    myId = id;
  }

  @NotNull
  public String getId() {
    return myId;
  }

  @Nullable
  public static NuGetSettingsComponent parse(@Nullable final String id) {
    for (NuGetSettingsComponent e : values()) {
      if (e.getId().equals(id)) return e;
    }
    return null;
  }
}
