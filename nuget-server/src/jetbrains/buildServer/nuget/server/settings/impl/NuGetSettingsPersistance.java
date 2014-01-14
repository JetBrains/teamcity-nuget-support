/*
 * Copyright 2000-2014 JetBrains s.r.o.
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

import jetbrains.buildServer.util.FileUtil;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 30.10.11 15:30
 */
public class NuGetSettingsPersistance {
  private final NuGetSettingsManagerConfiguration myConfig;

  public NuGetSettingsPersistance(@NotNull final NuGetSettingsManagerConfiguration config) {
    myConfig = config;
  }

  public void saveSettings(@NotNull final SettingsState state) throws IOException {
    final File file = myConfig.getNuGetConfigXml();
    final Element data = state.toXml();

    FileUtil.saveDocument(new Document(data), file);
  }

  @NotNull
  public SettingsState loadSettings() throws IOException {
    final File file = myConfig.getNuGetConfigXml();
    if (!file.isFile() || file.length() < 10) {
      return new SettingsState();
    }

    final Element element;
    try {
      element = FileUtil.parseDocument(file);
    } catch (JDOMException e) {
      throw new IOException(e);
    }
    return SettingsState.load(element);
  }
}
