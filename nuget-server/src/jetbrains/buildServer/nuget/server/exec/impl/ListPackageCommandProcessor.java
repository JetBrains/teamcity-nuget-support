/*
 * Copyright 2000-2012 JetBrains s.r.o.
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

package jetbrains.buildServer.nuget.server.exec.impl;

import jetbrains.buildServer.messages.serviceMessages.ServiceMessage;
import jetbrains.buildServer.messages.serviceMessages.ServiceMessageParserCallback;
import jetbrains.buildServer.nuget.server.exec.NuGetOutputProcessorAdapter;
import jetbrains.buildServer.nuget.server.exec.SourcePackageInfo;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
* Created by Eugene Petrenko (eugene.petrenko@gmail.com)
* Date: 14.07.11 13:23
*/
public class ListPackageCommandProcessor extends NuGetOutputProcessorAdapter<Collection<SourcePackageInfo>> {
  private final String mySource;
  private final List<SourcePackageInfo> myPackages = new ArrayList<SourcePackageInfo>();

  public ListPackageCommandProcessor(@Nullable final String source, @NotNull final String commandName) {
    super(commandName);
    mySource = source;
  }

  public void onStdOutput(@NotNull String text) {
    super.onStdOutput(text);
    ServiceMessage.parse(text, new ServiceMessageParserCallback() {
      public void regularText(@NotNull String s) {
      }

      public void serviceMessage(@NotNull ServiceMessage serviceMessage) {
        if (!"nuget-package".equals(serviceMessage.getMessageName())) return;
        final String id = serviceMessage.getAttributes().get("Id");
        final String version = serviceMessage.getAttributes().get("Version");

        if (StringUtil.isEmptyOrSpaces(id)) return;
        if (StringUtil.isEmptyOrSpaces(version)) return;

        myPackages.add(new SourcePackageInfo(mySource, id, version));
      }

      public void parseException(@NotNull ParseException e, @NotNull String s) {
      }
    });
  }

  @NotNull
  public Collection<SourcePackageInfo> getResult() {
    return Collections.unmodifiableList(myPackages);
  }
}
