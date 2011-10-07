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

package jetbrains.buildServer.nuget.server.exec;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.List;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 14.07.11 13:23
 */
public interface NuGetExecutor {
  @NotNull
  <T> T executeNuGet(@NotNull File nugetExePath,
                     @NotNull List<String> arguments,
                     @NotNull NuGetOutputProcessor<T> listener) throws NuGetExecutionException;

  @NotNull
  NuGetServerHandle startNuGetServer(int port,
                                     @NotNull String serverUrl,
                                     @NotNull File artifactPaths,
                                     @NotNull File specs) throws NuGetExecutionException;
}
