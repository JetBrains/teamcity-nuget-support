/*
 * Copyright 2000-2016 JetBrains s.r.o.
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

package jetbrains.buildServer.nuget.server.tool.impl;

import com.intellij.openapi.diagnostic.Logger;
import jetbrains.buildServer.tools.ToolException;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.zip.ZipFile;

import static jetbrains.buildServer.nuget.common.PackagesConstants.NUGET_TOOL_REL_PATH;

/**
 * @author Evgeniy.Koshkin
 */
public class NuGetPackageValidationUtil {

  private static final Logger LOG = Logger.getInstance(NuGetPackageValidationUtil.class.getName());

  public static void validatePackage(@NotNull final File pkg) throws ToolException {
    ZipFile file = null;
    try {
      file = new ZipFile(pkg);
      if (file.getEntry(NUGET_TOOL_REL_PATH) == null) {
        throw new ToolException("NuGet package must contain " + NUGET_TOOL_REL_PATH + " file");
      }
    } catch (IOException e) {
      String msg = "Failed to read NuGet package file. " + e.getMessage();
      LOG.warn(msg, e);
      throw new ToolException(msg);
    } finally {
      if (file != null) {
        try {
          file.close();
        } catch (IOException e) {
          //NOP
        }
      }
    }
  }

}
