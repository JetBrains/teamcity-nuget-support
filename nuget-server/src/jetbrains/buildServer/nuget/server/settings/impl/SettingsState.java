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

package jetbrains.buildServer.nuget.server.settings.impl;

import jetbrains.buildServer.nuget.server.settings.NuGetSettingsComponent;
import jetbrains.buildServer.util.StringUtil;
import org.jdom.Content;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;

import java.util.*;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 30.10.11 14:45
 */
public class SettingsState {
  private final Map<NuGetSettingsComponent, Map<String, String>> myState;

  public SettingsState(@NotNull SettingsState state, @NotNull ComponentWriter patch) {
    final Map<NuGetSettingsComponent, Map<String, String>> map = new HashMap<NuGetSettingsComponent, Map<String, String>>();

    for (Map.Entry<NuGetSettingsComponent, Map<String, String>> e : state.myState.entrySet()) {
      map.put(e.getKey(), Collections.unmodifiableMap(new HashMap<String, String>(e.getValue())));
    }

    final NuGetSettingsComponent component = patch.getComponent();
    Map<String, String> values = new HashMap<String, String>(state.getComponentMap(component));
    values.putAll(patch.getSettings());
    for (String key : patch.getRemovedKeys()) {
      values.remove(key);
    }
    map.put(component, Collections.unmodifiableMap(values));

    myState = Collections.unmodifiableMap(map);
  }

  public SettingsState() {
    myState = Collections.emptyMap();
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
    return new SettingsState(this, patch);
  }

  @NotNull
  public Element toXml() {
    final Element root = new Element("nuget-settings");
    for (Map.Entry<NuGetSettingsComponent, Map<String, String>> e : myState.entrySet()) {
      Element component = new Element("component");
      component.setAttribute("type", e.getKey().getId());

      for (Map.Entry<String, String> m : e.getValue().entrySet()) {
        Element par = new Element("param");
        par.setAttribute("key", m.getKey());
        par.setText(m.getValue());

        component.addContent((Content)par);
      }

      root.addContent((Content)component);
    }
    return root;
  }


  public static SettingsState load(@NotNull Element root) {
    SettingsState state = new SettingsState();

    if (!"nuget-settings".equals(root.getName())) {
      return state;
    }

    for (Object o : root.getChildren("component")) {
      final Element comp = (Element) o;
      final NuGetSettingsComponent component = NuGetSettingsComponent.parse(comp.getAttributeValue("type"));
      if (component == null) continue;

      final ComponentWriter cw = new ComponentWriter(component);

      for (Object op : comp.getChildren("param")) {
        final Element param = (Element) op;

        final String key = param.getAttributeValue("key");
        final String value = param.getTextTrim();

        if (StringUtil.isEmptyOrSpaces(key)) continue;
        if (value == null) continue;

        cw.setStringParameter(key, value);
      }

      state = state.update(cw);
    }

    return state;
  }

  @NotNull
  public Collection<NuGetSettingsComponent> getComponents() {
    return new HashSet<NuGetSettingsComponent>(myState.keySet());
  }
}
