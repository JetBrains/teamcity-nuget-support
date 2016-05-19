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

package jetbrains.buildServer.nuget.server.feed.server.controllers.requests;

import jetbrains.buildServer.controllers.AuthorizationInterceptor;
import jetbrains.buildServer.controllers.BaseController;
import jetbrains.buildServer.controllers.RequestPermissionsChecker;
import jetbrains.buildServer.serverSide.auth.AccessDeniedException;
import jetbrains.buildServer.serverSide.auth.AuthUtil;
import jetbrains.buildServer.serverSide.auth.AuthorityHolder;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import jetbrains.buildServer.web.openapi.WebControllerManager;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.util.Collection;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 23.11.11 20:58
 */
public class NuGetFeedRequestsController extends BaseController {
  @NotNull
  private final RecentNuGetRequests myRequests;

  public NuGetFeedRequestsController(@NotNull final RecentNuGetRequests requests,
                                     @NotNull final WebControllerManager web,
                                     @NotNull final AuthorizationInterceptor auth,
                                     @NotNull final PluginDescriptor descriptor) {
    myRequests = requests;
    final String path = descriptor.getPluginResourcesPath("recent-packages.html");
    web.registerController(path, this);
    auth.addPathBasedPermissionsChecker(path, new RequestPermissionsChecker() {
      public void checkPermissions(@NotNull AuthorityHolder authorityHolder, @NotNull HttpServletRequest request) throws AccessDeniedException {
        if (!AuthUtil.isSystemAdmin(authorityHolder)) {
          throw new AccessDeniedException(authorityHolder, "Only SysAdmin may access the page");
        }
      }
    });
  }

  @Override
  protected ModelAndView doHandle(@NotNull final HttpServletRequest request, @NotNull final HttpServletResponse response) throws Exception {
    response.setContentType("text/plain");
    response.setCharacterEncoding("utf-8");
    final PrintWriter writer = response.getWriter();
    final Collection<String> data = myRequests.getRecentRequests();
    writer.write("Recently called " + data.size() + " NuGet requests:\r\n");
    for (String req : data) {
      writer.write(req);
      writer.write("\r\n");
    }
    return null;
  }
}
