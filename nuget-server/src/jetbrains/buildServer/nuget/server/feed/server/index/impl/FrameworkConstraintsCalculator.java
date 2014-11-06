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
import jetbrains.buildServer.nuget.common.PackageInfoLoaderBase;
import jetbrains.buildServer.serverSide.artifacts.BuildArtifact;
import jetbrains.buildServer.util.FileUtil;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * @author Evgeniy.Koshkin
 */
public class FrameworkConstraintsCalculator extends PackageInfoLoaderBase {
  private static final String NUSPEC_FILE_EXTENSION = ".nuspec";
  private static final String METADATA_ELEMENT = "metadata";
  private static final String FRAMEWORK_ASSEMBLIES_ELEMENT = "frameworkAssemblies";
  private static final String FRAMEWORK_ASSEMBLY_ELEMENT = "frameworkAssembly";
  private static final String TARGET_FRAMEWORK_ATTRIBUTE = "targetFramework";

  private static final Pattern SUBFOLDER_MATCHING_PATTERN = Pattern.compile("(content|lib|build|tools)\\/(\\S+)\\/.*");
  private static final Logger LOG = Logger.getInstance(FrameworkConstraintsCalculator.class.getName());

  @NotNull
  public Set<String> getPackageConstraints(@NotNull final BuildArtifact nugetPackage) {
    ZipInputStream zipInputStream = null;
    InputStream stream = null;
    final Set<String> constraints = new HashSet<String>();
    try {
      stream = nugetPackage.getInputStream();
      zipInputStream = new ZipInputStream(new BufferedInputStream(stream));
      ZipEntry zipEntry;
      while ((zipEntry = zipInputStream.getNextEntry()) != null) {
        if(zipEntry.isDirectory()) continue;
        final String zipEntryName = zipEntry.getName();
        if (zipEntryName.endsWith(NUSPEC_FILE_EXTENSION)) {
          final ZipInputStream finalZipInputStream = zipInputStream;
          try {
            constraints.addAll(extractConstraintsFromNuSpec(FileUtil.parseDocument(new InputStream() {
              @Override
              public int read() throws IOException {
                return finalZipInputStream.read();
              }

              @Override
              public void close() throws IOException {
                //do nothing, should avoid stream closing by xml parse util
              }
            }, false)));
          } catch (JDOMException e) {
            //LOG
          }
          zipInputStream.closeEntry();
        }
        else {
          final Matcher matcher = SUBFOLDER_MATCHING_PATTERN.matcher(zipEntryName);
          if(matcher.find()){
            constraints.add(matcher.toMatchResult().group(2).toLowerCase());
          }
        }
      }
    } catch (IOException e) {
      //LOG
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
    return constraints;
  }

  @NotNull
  private Collection<String> extractConstraintsFromNuSpec(Element nuspecRootElement) {
    final Collection<String> targetFrameworks = new HashSet<String>();
    for(Element frameworkAssemblyElement : getChildren(getChild(getChild(nuspecRootElement, METADATA_ELEMENT), FRAMEWORK_ASSEMBLIES_ELEMENT), FRAMEWORK_ASSEMBLY_ELEMENT)){
      final String targetFramework = frameworkAssemblyElement.getAttributeValue(TARGET_FRAMEWORK_ATTRIBUTE);
      if(targetFramework != null)
        targetFrameworks.add(targetFramework);
    }
    return targetFrameworks;
  }
}
