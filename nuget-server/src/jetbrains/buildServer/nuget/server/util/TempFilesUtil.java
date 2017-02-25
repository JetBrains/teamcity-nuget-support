/*
 * Copyright 2000-2017 JetBrains s.r.o.
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

package jetbrains.buildServer.nuget.server.util;

import jetbrains.buildServer.nuget.server.exec.NuGetExecutionException;
import jetbrains.buildServer.util.FileUtil;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;

/**
 * Created by Evgeniy.Koshkin on 17.12.2015
 */
public class TempFilesUtil {
  public static File createTempFile(@NotNull File location, @NotNull String infix) throws NuGetExecutionException {
    try {
      location.mkdirs();
      return FileUtil.createTempFile(location, "nuget", infix + ".xml", true);
    } catch (IOException e) {
      throw new NuGetExecutionException("Failed to create temp file at " + location + ". " + e.getMessage(), e);
    }
  }
}
