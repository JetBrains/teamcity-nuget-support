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

package jetbrains.buildServer.nuget.agent.runner.pack;

import org.jetbrains.annotations.NotNull;

import java.io.File;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 02.12.11 15:09
 */
public interface TrackState {
  @NotNull
  CleanOutcome registerDirectoryClean(@NotNull File dir, boolean cleanEnabled);

  enum CleanOutcome {
    CLEAN,
    CLEANED_BEFORE,
    NOT_CLEANED_BEFORE,
    NO_CLEAN_REQUIRED,
    ;
  }
}
