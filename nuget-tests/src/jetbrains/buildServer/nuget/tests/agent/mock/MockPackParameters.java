/*
 * Copyright 2000-2013 JetBrains s.r.o.
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

package jetbrains.buildServer.nuget.tests.agent.mock;

import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.nuget.agent.parameters.NuGetPackParameters;
import jetbrains.buildServer.nuget.common.PackagesPackDirectoryMode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

/**
 * Created 18.03.13 13:16
 *
 * @author Eugene Petrenko (eugene.petrenko@jetbrains.com)
 */
public class MockPackParameters implements NuGetPackParameters {
  private File myNuGetExe;
  private final Collection<String> mySpecFiles = new ArrayList<String>();
  private final Collection<String> myExclude = new ArrayList<String>();
  private final Collection<String> myProperties = new ArrayList<String>();
  private final Collection<String> myCommandLine = new ArrayList<String>();
  private File myOutput;
  private boolean myCleanOutput = false;
  private PackagesPackDirectoryMode myPackMode = PackagesPackDirectoryMode.EXPLICIT_DIRECTORY;
  private File myBaseDir;
  private String myVersion = "45.239.32.12";
  private boolean myPackSymbols = false;
  private boolean myPackTool = false;
  private boolean myPublishArtifacts = false;
  private boolean myPreferProjectFile = false;


  public void setNuGetExe(File nuGetExe) {
    myNuGetExe = nuGetExe;
  }

  public void setOutput(@NotNull File output) {
    myOutput = output;
  }

  public void setSpecFiles(@NotNull String... files) {
    mySpecFiles.clear();
    mySpecFiles.addAll(Arrays.asList(files));
  }
  public void setSpecFiles(@NotNull Collection<String> files) {
    mySpecFiles.clear();
    mySpecFiles.addAll(files);
  }

  public void setSpecFiles(@NotNull File... files) {
    mySpecFiles.clear();
    for (File file : files) {
      mySpecFiles.add(file.getPath());
    }
  }

  public void addProperty(@NotNull String... s) {
    myProperties.addAll(Arrays.asList(s));
  }

  public void setCleanOutput(boolean cleanOutput) {
    myCleanOutput = cleanOutput;
  }

  public void addCmdParameters(@NotNull String... s) {
    myCommandLine.addAll(Arrays.asList(s));
  }

  public void addExcludes(@NotNull String... s) {
    myExclude.addAll(Arrays.asList(s));
  }

  public void setBaseDirMode(@NotNull PackagesPackDirectoryMode packMode) {
    myPackMode = packMode;
  }

  public void setBaseDir(@NotNull File baseDir) {
    myBaseDir = baseDir;
  }

  public void setVersion(@Nullable String version) {
    myVersion = version;
  }

  public void setPackSymbols(boolean packSymbols) {
    myPackSymbols = packSymbols;
  }

  public void setPackTool(boolean packTool) {
    myPackTool = packTool;
  }

  public void setPublishArtifacts(boolean publishArtifacts) {
    myPublishArtifacts = publishArtifacts;
  }

  public void setPreferProjectFile(boolean preferProjectFile) {
    myPreferProjectFile = preferProjectFile;
  }

  @NotNull
  public Collection<String> getSpecFiles() throws RunBuildException {
    return mySpecFiles;
  }

  @NotNull
  public Collection<String> getExclude() {
    return myExclude;
  }

  @NotNull
  public Collection<String> getProperties() {
    return myProperties;
  }

  @NotNull
  public Collection<String> getCustomCommandline() {
    return myCommandLine;
  }

  @NotNull
  public File getOutputDirectory() throws RunBuildException {
    return myOutput;
  }

  public boolean cleanOutputDirectory() throws RunBuildException {
    return myCleanOutput;
  }

  @NotNull
  public PackagesPackDirectoryMode getBaseDirectoryMode() {
    return myPackMode;
  }

  @NotNull
  public File getBaseDirectory() throws RunBuildException {
    return myBaseDir;
  }

  @Nullable
  public String getVersion() throws RunBuildException {
    return myVersion;
  }

  public boolean packSymbols() {
    return myPackSymbols;
  }

  public boolean packTool() {
    return myPackTool;
  }

  public boolean publishAsArtifacts() {
    return myPublishArtifacts;
  }

  public boolean preferProjectFileToNuSpec() {
    return myPreferProjectFile;
  }

  @NotNull
  public File getNuGetExeFile() throws RunBuildException {
    return myNuGetExe;
  }
}
