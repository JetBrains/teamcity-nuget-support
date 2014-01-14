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

package jetbrains.buildServer.nuget.server.toolRegistry.tab;

import jetbrains.buildServer.nuget.server.toolRegistry.NuGetTool;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 *         Date: 14.06.12 14:02
 */
public class InstallToolBean {
  private final WhatToDo myWhatToDo;
  private String myErrorText = null;
  private final Collection<NuGetTool> myTools = new ArrayList<NuGetTool>();

  public InstallToolBean(@NotNull WhatToDo whatToDo) {
    myWhatToDo = whatToDo;
  }

  public void setErrorText(@NotNull String errorText) {
    myErrorText = errorText;
  }

  public void setTools(@NotNull Collection<? extends NuGetTool> tools) {
    myTools.clear();
    myTools.addAll(tools);
  }

  @Nullable
  public String getErrorText() {
    return myErrorText;
  }

  @NotNull
  public Collection<NuGetTool> getTools() {
    return myTools;
  }

  public boolean isShowErrors() {
    return getErrorText() != null || getTools().isEmpty();
  }

  @NotNull
  public String getWhatToDo() {
    return myWhatToDo.getName();
  }

  @NotNull
  public String getView() {
    switch (myWhatToDo) {
      case INSTALL: return "tool/installTool.jsp";
      case UPLOAD: return "tool/uploadTool.jsp";
      default:
        throw new RuntimeException("Unexpected action: " + getWhatToDo());
    }
  }
}
