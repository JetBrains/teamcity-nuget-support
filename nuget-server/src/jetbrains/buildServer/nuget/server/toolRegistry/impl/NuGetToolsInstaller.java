/*
 * Copyright 2000-2015 JetBrains s.r.o.
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

package jetbrains.buildServer.nuget.server.toolRegistry.impl;

import jetbrains.buildServer.tools.ToolException;
import org.jetbrains.annotations.NotNull;

import java.io.File;

/**
 * Created 27.12.12 18:46
 *
 * @author Eugene Petrenko (eugene.petrenko@jetbrains.com)
 */
public interface NuGetToolsInstaller {
  void installNuGet(@NotNull String toolFileName, @NotNull File toolFile) throws ToolException;
}
