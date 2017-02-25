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

package jetbrains.buildServer.nuget.agent.parameters;

import jetbrains.buildServer.RunBuildException;
import jetbrains.buildServer.nuget.common.PackagesPackDirectoryMode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Collection;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 23.08.11 11:17
 */
public interface NuGetPackParameters extends NuGetParameters {

  @NotNull
  Collection<String> getSpecFiles() throws RunBuildException;

  @NotNull
  Collection<String> getExclude();

  @NotNull
  Collection<String> getProperties();

  @NotNull
  Collection<String> getCustomCommandline();

  @NotNull
  File getOutputDirectory() throws RunBuildException;

  boolean cleanOutputDirectory() throws RunBuildException;

  @NotNull
  PackagesPackDirectoryMode getBaseDirectoryMode();

  @NotNull
  File getBaseDirectory() throws RunBuildException;

  @Nullable
  String getVersion() throws RunBuildException;

  boolean packSymbols();
  boolean packTool();

  boolean publishAsArtifacts();

  boolean preferProjectFileToNuSpec();
}
