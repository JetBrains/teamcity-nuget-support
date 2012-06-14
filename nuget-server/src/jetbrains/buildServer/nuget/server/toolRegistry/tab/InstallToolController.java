/*
 * Copyright 2000-2011 JetBrains s.r.o.
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

import com.intellij.openapi.diagnostic.Logger;
import jetbrains.buildServer.controllers.*;
import jetbrains.buildServer.nuget.server.toolRegistry.NuGetToolManager;
import jetbrains.buildServer.nuget.server.toolRegistry.ToolException;
import jetbrains.buildServer.nuget.server.toolRegistry.ToolsPolicy;
import jetbrains.buildServer.serverSide.auth.AccessDeniedException;
import jetbrains.buildServer.serverSide.auth.AuthorityHolder;
import jetbrains.buildServer.util.StringUtil;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import jetbrains.buildServer.web.openapi.WebControllerManager;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 11.08.11 12:12
 */
public class InstallToolController extends BaseController {
  private static final Logger LOG = Logger.getInstance(InstallToolController.class.getName());

  private final String myPath;
  private final NuGetToolManager myToolsManager;
  private final PluginDescriptor myDescriptor;

  public InstallToolController(@NotNull final AuthorizationInterceptor auth,
                               @NotNull final PermissionChecker checker,
                               @NotNull final WebControllerManager web,
                               @NotNull final NuGetToolManager toolsManager,
                               @NotNull final PluginDescriptor descriptor) {
    myToolsManager = toolsManager;
    myDescriptor = descriptor;
    myPath = descriptor.getPluginResourcesPath("tool/nuget-server-tab-install-tool.html");
    auth.addPathBasedPermissionsChecker(myPath, new RequestPermissionsChecker() {
      public void checkPermissions(@NotNull AuthorityHolder authorityHolder,
                                   @NotNull HttpServletRequest request) throws AccessDeniedException {
        checker.assertAccess(authorityHolder);
      }
    });
    web.registerController(myPath, this);
  }

  @NotNull
  public String getPath() {
    return myPath;
  }

  @Override
  protected ModelAndView doHandle(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response) throws Exception {
    if (isPost(request)) {
      return doPost(request, response);
    }

    if (isGet(request)) {
      return doGet(request, response);
    }

    //unknown request type
    return null;
  }

  private  ModelAndView doGet(@NotNull final HttpServletRequest request,
                              @NotNull final HttpServletResponse response) {
    final InstallToolBean bean = new InstallToolBean();
    final ToolsPolicy pol =
            StringUtil.isEmptyOrSpaces(request.getParameter("fresh"))
                    ? ToolsPolicy.ReturnCached
                    : ToolsPolicy.FetchNew;

    try {
      bean.setTools(myToolsManager.getAvailableTools(pol));
    } catch (Exception e) {
      bean.setErrorText(e.getMessage());
      LOG.warn("Failed to fetch NuGet.Commandline package versions. " + e.getMessage(), e);
    }

    final ModelAndView mv = new ModelAndView(myDescriptor.getPluginResourcesPath("tool/installTool.jsp"));
    mv.getModelMap().put("propertiesBean", new BasePropertiesBean(new HashMap<String, String>()));

    mv.getModel().put("installTools", bean);
    return mv;
  }

  protected ModelAndView doPost(@NotNull final HttpServletRequest request,
                                @NotNull final HttpServletResponse response) {
    final ActionErrors ae = new ActionErrors();
    doPost(request, ae);

    final ModelAndView mv = new ModelAndView(myDescriptor.getPluginResourcesPath("tool/installToolAjax.jsp"));

    final Element responseXml = XmlResponseUtil.newXmlResponse();
    XmlResponseUtil.writeErrors(responseXml, ae);
    final String responseText = new XMLOutputter(Format.getCompactFormat()).outputString(responseXml);

    mv.getModel().put("hasToolErrors", ae.hasErrors());
    mv.getModel().put("toolErrorsText", responseText);
    return mv;
  }

  private void doPost(@NotNull final HttpServletRequest request,
                      @NotNull final ActionErrors ae) {
    final String toolId = request.getParameter("toolId");

    if (StringUtil.isEmptyOrSpaces(toolId)) {
      ae.addError("toolId", "Select NuGet.Commandline package version to install");
      return;
    }

    final String whatToDo = request.getParameter("whatToDo");
    try {
      if ("install".equals(whatToDo)) {

        if ("custom".equals(toolId) && request instanceof MultipartHttpServletRequest) {
          LOG.debug("Processing NuGet commandline upload.");
        } else {
          myToolsManager.installTool(toolId);
        }
        return;
      }

      if ("remove".equals(whatToDo)) {
        myToolsManager.removeTool(toolId);
      }
    } catch (ToolException e) {
      ae.addError("toolId", "Failed to install package: " + e.getMessage());
    }
  }
}
