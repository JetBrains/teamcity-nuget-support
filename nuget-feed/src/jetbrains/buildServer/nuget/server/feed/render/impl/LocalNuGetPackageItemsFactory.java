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

package jetbrains.buildServer.nuget.server.feed.render.impl;

import com.intellij.openapi.diagnostic.Logger;
import jetbrains.buildServer.nuget.server.feed.render.NuGetItem;
import jetbrains.buildServer.util.FileUtil;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 06.09.11 22:03
 */
public class LocalNuGetPackageItemsFactory {
  private static final Logger LOG = Logger.getInstance(LocalNuGetPackageItemsFactory.class.getName());

  @NotNull
  public NuGetItem createPackage(@NotNull final File nupkg) {
    final Element spec = parseNuSpec(nupkg);



    return null;
  }

  @Nullable
  private Element parseNuSpec(@NotNull final File nupkg) {
    //TODO: parse version number avay
    final String name = nupkg.getName();
    ZipInputStream zos = null;
    try {
      zos = new ZipInputStream(new BufferedInputStream(new FileInputStream(nupkg)));
      ZipEntry ze;
      while ((ze = zos.getNextEntry()) != null) {
        if (ze.getName().startsWith(name) && ze.getName().endsWith(".nuspec")) {
          try {
            return FileUtil.parseDocument(zos, false);
          } catch (JDOMException e) {
            LOG.warn("Failed to parse " + ze + " in " + nupkg);
          }
        }
      }
    } catch (IOException e) {
      LOG.warn("Failed to read " + nupkg + ". " + e.getMessage(), e);
    } finally {
      close(zos);
    }

    return null;
  }

  private void close(@Nullable final ZipInputStream zos) {
    if (zos != null) {
      try {
      zos.close();
      } catch (IOException e) {
        //NOP
      }
    }
  }
}
