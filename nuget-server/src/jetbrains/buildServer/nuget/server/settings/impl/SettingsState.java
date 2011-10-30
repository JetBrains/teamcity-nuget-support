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

package jetbrains.buildServer.nuget.server.settings.impl;

import jetbrains.buildServer.nuget.server.settings.NuGetSettingsComponent;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 30.10.11 14:45
 */
public class SettingsState {
  private final Map<NuGetSettingsComponent, Map<String, String>> myState = new HashMap<NuGetSettingsComponent, Map<String, String>>();

  public SettingsState(@NotNull SettingsState state) {
    this();

    for (Map.Entry<NuGetSettingsComponent, Map<String, String>> e : myState.entrySet()) {
      myState.put(e.getKey(), new HashMap<String, String>(e.getValue()));
    }
  }

  public SettingsState() {
  }

  public ReaderBase read(@NotNull final NuGetSettingsComponent component) {
    final Map<String, String> map = getComponentMap(component);
    return new ReaderBase() {
      public String getStringParameter(@NotNull String key) {
        return map.get(key);
      }
    };
  }

  @NotNull
  public Map<String, String> getComponentMap(@NotNull NuGetSettingsComponent component) {
    final Map<String, String> map = myState.get(component);
    return map != null ? Collections.unmodifiableMap(map) : Collections.<String, String>emptyMap();
  }

  @NotNull
  public SettingsState update(@NotNull final ComponentWriter patch) {
    SettingsState state = new SettingsState(this);
    final NuGetSettingsComponent component = patch.getComponent();

    Map<String, String> values = new HashMap<String, String>(state.getComponentMap(component));
    values.putAll(patch.getSettings());
    state.myState.put(component, values);
    return state;
  }

}
