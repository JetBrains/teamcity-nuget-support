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

import com.intellij.openapi.diagnostic.Logger;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 04.10.11 18:51
 */
public abstract class NuGetOutputProcessorAdapter<T> implements NuGetOutputProcessor<T> {
  protected final Logger LOG = Logger.getInstance(getClass().getName());
  private final String myCommandName;

  protected NuGetOutputProcessorAdapter(@NotNull final String commandName) {
    myCommandName = commandName;
  }

  public void onStdOutput(@NotNull String text) {
    if (LOG.isDebugEnabled()) {
      LOG.debug(text);
    }

  }

  public void onStdError(@NotNull String text) {
    if (!StringUtil.isEmptyOrSpaces(text)) {
      LOG.warn(text);
    }
  }

  public void onFinished(int exitCode) throws NuGetExecutionException {
    if (LOG.isDebugEnabled()) {
      LOG.debug("NuGet " + myCommandName + " command exited with " + exitCode);
    }
    if (exitCode != 0) {
      throw new NuGetExecutionException("Failed to execute NuGet " + myCommandName + " command. Exited code was " + exitCode);
    }
  }
}
