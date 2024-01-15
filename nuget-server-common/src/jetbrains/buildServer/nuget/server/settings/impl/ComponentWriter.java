

package jetbrains.buildServer.nuget.server.settings.impl;

import jetbrains.buildServer.nuget.server.settings.NuGetSettingsComponent;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
* @author Eugene Petrenko (eugene.petrenko@gmail.com)
*         Date: 30.10.11 14:53
*/
public class ComponentWriter extends WriterBase {
  @NotNull private final NuGetSettingsComponent myComponent;
  private final Map<String, String> mySettings = new HashMap<String, String>();
  private final Set<String> myRemovedKeys = new HashSet<String>();

  public ComponentWriter(@NotNull final NuGetSettingsComponent component) {
    myComponent = component;
  }

  public void setStringParameter(@NotNull String key, @NotNull String value) {
    mySettings.put(key, value);
    myRemovedKeys.remove(key);
  }

  public void removeParameter(@NotNull String key) {
    mySettings.remove(key);
    myRemovedKeys.add(key);
  }

  @NotNull
  public NuGetSettingsComponent getComponent() {
    return myComponent;
  }

  @NotNull
  public Set<String> getRemovedKeys() {
    return myRemovedKeys;
  }

  @NotNull
  public Map<String, String> getSettings() {
    return Collections.unmodifiableMap(mySettings);
  }
}
