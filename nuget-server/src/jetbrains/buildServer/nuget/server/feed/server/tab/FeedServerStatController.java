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

package jetbrains.buildServer.nuget.server.feed.server.tab;

import jetbrains.buildServer.controllers.BaseController;
import jetbrains.buildServer.nuget.server.feed.server.index.NuGetServerStatisticsProvider;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import jetbrains.buildServer.web.openapi.WebControllerManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Evgeniy.Koshkin
 */
public class FeedServerStatController extends BaseController {

  @NotNull private final PluginDescriptor myDescriptor;
  @NotNull private final NuGetServerStatisticsProvider myStatisticsProvider;
  @NotNull private final String myPath;

  public FeedServerStatController(@NotNull PluginDescriptor descriptor,
                                  @NotNull NuGetServerStatisticsProvider statisticsProvider,
                                  @NotNull final WebControllerManager web) {
    myDescriptor = descriptor;
    myStatisticsProvider = statisticsProvider;
    myPath = descriptor.getPluginResourcesPath("feed/stat.html");
    web.registerController(myPath, this);
  }

  @NotNull
  public String getPath() {
    return myPath;
  }

  @Nullable
  @Override
  protected ModelAndView doHandle(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response) throws Exception {
    final ModelAndView mv = new ModelAndView(myDescriptor.getPluginResourcesPath("server/feedServerStatistics.jsp"));
    mv.getModel().put("packagesIndexStat", myStatisticsProvider.getIndexStatistics());
    return mv;
  }
}
