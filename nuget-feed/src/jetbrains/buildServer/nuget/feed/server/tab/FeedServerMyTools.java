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

package jetbrains.buildServer.nuget.feed.server.tab;

import jetbrains.buildServer.nuget.feed.server.NuGetServerSettings;
import jetbrains.buildServer.web.openapi.PagePlaces;
import jetbrains.buildServer.web.openapi.PlaceId;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import jetbrains.buildServer.web.openapi.SimplePageExtension;
import org.jetbrains.annotations.NotNull;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 23.11.11 16:00
 */
public class FeedServerMyTools extends SimplePageExtension {
  @NotNull private final PluginDescriptor myDescriptor;
  @NotNull private final NuGetServerSettings mySettings;

  public FeedServerMyTools(@NotNull final PagePlaces pagePlaces,
                           @NotNull final PluginDescriptor descriptor,
                           @NotNull final NuGetServerSettings settings) {
    super(
            pagePlaces,
            PlaceId.MY_TOOLS_SECTION,
            descriptor.getPluginName() + "-myTools",
            descriptor.getPluginResourcesPath("feedMyTools.jsp")
    );
    myDescriptor = descriptor;
    mySettings = settings;
    register();
  }

  @NotNull
  @Override
  public List<String> getCssPaths() {
    return Collections.singletonList(myDescriptor.getPluginResourcesPath("feedServer.css"));
  }

  @Override
  public void fillModel(@NotNull Map<String, Object> model, @NotNull HttpServletRequest request) {
    super.fillModel(model, request);
    model.put("nugetPrivateUrl", mySettings.getNuGetHttpAuthFeedControllerPath());
    model.put("nugetPublicUrl", mySettings.getNuGetGuestAuthFeedControllerPath());
    model.put("imagesUrl", myDescriptor.getPluginResourcesPath("img"));
  }

  @Override
  public boolean isAvailable(@NotNull HttpServletRequest request) {
    return super.isAvailable(request) && mySettings.isNuGetServerEnabled();
  }
}
