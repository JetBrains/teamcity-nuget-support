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

package jetbrains.buildServer.nuget.server.feed.server.index.impl;

import com.intellij.openapi.diagnostic.Logger;
import jetbrains.buildServer.nuget.common.FeedConstants;
import jetbrains.buildServer.nuget.common.PackageLoadException;
import jetbrains.buildServer.nuget.common.nuspec.NuspecFileContent;
import jetbrains.buildServer.serverSide.artifacts.BuildArtifact;
import jetbrains.buildServer.util.FileUtil;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * @author Evgeniy.Koshkin
 */
public class NuGetPackageStructureVisitor {

  private static final Logger LOG = Logger.getInstance(NuGetPackageStructureVisitor.class.getName());

  @NotNull private final Collection<NuGetPackageStructureAnalyser> myAnalysers;

  public NuGetPackageStructureVisitor(@NotNull Collection<NuGetPackageStructureAnalyser> analysers) {
    myAnalysers = analysers;
  }

  public void visit(@NotNull BuildArtifact artifact) throws PackageLoadException {
    if(myAnalysers.isEmpty()) return;
    ZipInputStream zipInputStream = null;
    InputStream stream = null;
    final String nugetPackageName = artifact.getName();
    try {
      stream = artifact.getInputStream();
      zipInputStream = new ZipInputStream(new BufferedInputStream(stream));
      ZipEntry zipEntry;
      while ((zipEntry = zipInputStream.getNextEntry()) != null) {
        if(zipEntry.isDirectory()) continue;
        final String zipEntryName = zipEntry.getName();
        for(NuGetPackageStructureAnalyser analyser : myAnalysers){
          analyser.analyseEntry(zipEntryName);
        }
        if (zipEntryName.endsWith(FeedConstants.NUSPEC_FILE_EXTENSION)) {
          LOG.debug(String.format("Nuspec file found on path %s in NuGet package %s", zipEntryName, nugetPackageName));
          final NuspecFileContent nuspecContent = readNuspecFileContent(zipInputStream);
          if (nuspecContent == null)
            LOG.warn("Failed to read .nuspec file content from NuGet package " + nugetPackageName);
          else {
            for(NuGetPackageStructureAnalyser analyser : myAnalysers){
              analyser.analyseNuspecFile(nuspecContent);
            }
          }
          zipInputStream.closeEntry();
        }
      }
    } catch (IOException e) {
      LOG.warn("Failed to read content of NuGet package " + nugetPackageName);
      if(zipInputStream != null){
        try {
          zipInputStream.close();
        } catch (IOException ex) {
          //NOP
        }
      }
    } finally {
      FileUtil.close(stream);
    }
  }

  @Nullable
  private NuspecFileContent readNuspecFileContent(final ZipInputStream finalZipInputStream) throws IOException {
    try {
      final Element document = FileUtil.parseDocument(new InputStream() {
        @Override
        public int read() throws IOException {
          return finalZipInputStream.read();
        }

        @Override
        public void close() throws IOException {
          //do nothing, should avoid stream closing by xml parse util
        }
      }, false);
      return new NuspecFileContent(document);
    } catch (JDOMException e) {
      LOG.debug(e);
      return null;
    }
  }
}
