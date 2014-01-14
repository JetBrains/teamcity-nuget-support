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

package jetbrains.buildServer.nuget.server.settings;

import org.jetbrains.annotations.NotNull;

/**
 * NuGet Settings writer interface for a compoenent
 *
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 30.10.11 14:14
 *
 * @see NuGetSettingsManager
 */
public interface NuGetSettingsWriter {
  void setStringParameter(@NotNull final String key, @NotNull String value);

  void setBooleanParameter(@NotNull final String key, boolean value);
  void setIntParameter(@NotNull final String key, int value);

  void removeParameter(@NotNull final String key);
}
