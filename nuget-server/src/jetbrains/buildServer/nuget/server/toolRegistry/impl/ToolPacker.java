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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 15.08.11 20:51
 */
public class ToolPacker {

  public void packTool(@NotNull final File tool, @NotNull final File rootDir) throws IOException {
    FileUtil.createParentDirs(tool);
    final ZipOutputStream fos = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(tool)));
    try {
      fos.putNextEntry(new ZipEntry("teamcity-plugin.xml"));
      fos.write(TOOL_DESCRIPTOR.getBytes("utf-8"));
      fos.closeEntry();
      if (!ArchiveUtil.packZip(rootDir, fos)) {
        throw new IOException("Failed to create agent tool plugin");
      }
    } finally {
      FileUtil.close(fos);
    }
  }

  private static final String TOOL_DESCRIPTOR = "" +
          "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
          "<teamcity-agent-plugin xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
          "                       xsi:noNamespaceSchemaLocation=\"urn:shemas-jetbrains-com:teamcity-agent-plugin-v1-xml\">\n" +
          "  <tool-deployment />\n" +
          "</teamcity-agent-plugin>";
}
