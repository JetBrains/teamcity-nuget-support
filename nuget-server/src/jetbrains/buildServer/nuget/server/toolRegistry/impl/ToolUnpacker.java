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

package jetbrains.buildServer.nuget.server.toolRegistry.impl;

import jetbrains.buildServer.util.ArchiveUtil;
import jetbrains.buildServer.util.FileUtil;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.zip.ZipInputStream;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 16.08.11 15:11
 */
public class ToolUnpacker {
  public void extractPackage(@NotNull final File pkg,
                             @NotNull final File dest) throws IOException {
    FileUtil.createDir(dest);
    final ZipInputStream zip = new ZipInputStream(new BufferedInputStream(new FileInputStream(pkg)));
    if (!ArchiveUtil.unpackZip(zip, dest)) {
      throw new IOException("Failed to unpack package " + pkg + " to " + dest);
    }
  }
}
