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

package jetbrains.buildServer.nuget.feed.server;

import jetbrains.buildServer.nuget.common.NuGetServerConstants;
import jetbrains.buildServer.serverSide.Parameter;
import jetbrains.buildServer.serverSide.SBuild;
import jetbrains.buildServer.serverSide.SimpleParameter;
import jetbrains.buildServer.serverSide.parameters.types.PasswordsProvider;
import jetbrains.buildServer.util.StringUtil;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


/**
 * Hides tokens for built-in NuGet feed.
 */
public class NuGetFeedPasswordProvider implements PasswordsProvider {

  public NuGetFeedPasswordProvider() {
  }

  @NotNull
  @Override
  public Collection<Parameter> getPasswordParameters(@NotNull SBuild build) {
    final List<Parameter> passwords = new ArrayList<>();
    final String feedApiKey = build.getParametersProvider().get(NuGetServerConstants.FEED_REFERENCE_AGENT_API_KEY_PROVIDED);

    if (!StringUtil.isEmptyOrSpaces(feedApiKey)) {
      passwords.add(new SimpleParameter(NuGetServerConstants.FEED_REFERENCE_AGENT_API_KEY_PROVIDED, feedApiKey));
    }

    return passwords;
  }
}
