/*
 * Copyright 2000-2015 JetBrains s.r.o.
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
import jetbrains.buildServer.nuget.server.toolRegistry.*;
import jetbrains.buildServer.nuget.server.toolRegistry.impl.impl.DownloadableNuGetTool;
import jetbrains.buildServer.serverSide.auth.AccessDeniedException;
import jetbrains.buildServer.serverSide.auth.AuthorityHolder;
import jetbrains.buildServer.tools.ToolException;
import jetbrains.buildServer.util.FileUtil;
import jetbrains.buildServer.util.StringUtil;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import jetbrains.buildServer.web.openapi.WebControllerManager;
import org.apache.commons.io.FilenameUtils;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import static jetbrains.buildServer.nuget.server.toolRegistry.tab.WhatToDo.INSTALL;
import static jetbrains.buildServer.nuget.server.toolRegistry.tab.WhatToDo.UPLOAD;

/**
 * Created by Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 11.08.11 12:12
 */
public class InstallToolController extends BaseFormXmlController {
  private static final Logger LOG = Logger.getInstance(InstallToolController.class.getName());

  private final String myPath;
  private final NuGetToolManager myToolsManager;
  private final NuGetToolDownloader myToolsDownloader;
  private final PluginDescriptor myDescriptor;

  public InstallToolController(@NotNull final AuthorizationInterceptor auth,
                               @NotNull final PermissionChecker checker,
                               @NotNull final WebControllerManager web,
                               @NotNull final NuGetToolManager toolsManager,
                               @NotNull final NuGetToolDownloader toolsDownloader,
                               @NotNull final PluginDescriptor descriptor) {
    myToolsManager = toolsManager;
    myToolsDownloader = toolsDownloader;
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

  protected ModelAndView doGet(@NotNull final HttpServletRequest request,
                              @NotNull final HttpServletResponse response) {
    final InstallToolBean bean;

    if (!StringUtil.isEmptyOrSpaces(request.getParameter("uploadOnly"))) {
      bean = new InstallToolBean(UPLOAD);
    } else {
      bean = new InstallToolBean(INSTALL);
      final ToolsPolicy pol =
              StringUtil.isEmptyOrSpaces(request.getParameter("fresh"))
                      ? ToolsPolicy.ReturnCached
                      : ToolsPolicy.FetchNew;

      try {
        final FetchAvailableToolsResult fetchResult = myToolsManager.getAvailableTools(pol);
        bean.setTools(fetchResult.getFetchedTools());
        bean.setErrorText(fetchResult.getErrorsSummary());
      } catch (Exception e) {
        bean.setErrorText(e.getMessage());
        LOG.warn("Failed to fetch NuGet.Commandline package versions. " + e.getMessage(), e);
      }
    }

    final ModelAndView mv = new ModelAndView(myDescriptor.getPluginResourcesPath(bean.getView()));
    mv.getModel().put("propertiesBean", new BasePropertiesBean(new HashMap<String, String>()));
    mv.getModel().put("installTools", bean);
    return mv;
  }

  @Override
  protected void doPost(@NotNull final HttpServletRequest request,
                        @NotNull final HttpServletResponse response,
                        @NotNull final Element xmlResponse) {

    final ActionErrors ae = new ActionErrors();
    doPost(request, ae);

    ae.serialize(xmlResponse);
  }

  private void doPost(@NotNull final HttpServletRequest request,
                      @NotNull final ActionErrors ae) {
    final String toolId = request.getParameter("toolId");

    if (StringUtil.isEmptyOrSpaces(toolId)) {
      ae.addError("toolId", "Select NuGet.Commandline package version to install");
      return;
    }

    final WhatToDo whatToDo = WhatToDo.fromString(request.getParameter("whatToDo"));
    if (whatToDo == null)  {
      ae.addError("whatToDo", "Unknown action");
      return;
    }

    try {
      switch (whatToDo) {
        case INSTALL:
          final DownloadableNuGetTool tool = myToolsManager.findAvailableToolById(toolId);
          if (tool == null) throw new ToolException("Failed to find available tool by Id " + toolId);

          File uploadTarget;
          try {
            uploadTarget = FileUtil.createTempFile(tool.getId(), ".tmp");
            FileUtil.createParentDirs(uploadTarget);
          } catch (IOException e) {
            String msg = "Failed to create temp file";
            LOG.debug(e);
            throw new ToolException(msg);
          }
          FileUtil.delete(uploadTarget);

          myToolsDownloader.downloadTool(tool, uploadTarget);
          try {
            final NuGetTool downloadedTool = myToolsManager.installTool(tool.getId(), tool.getDestinationFileName(), uploadTarget);
            updateDefault(request, downloadedTool);
          } finally {
            FileUtil.delete(uploadTarget);
          }
          return;

        case DEFAULT:
          myToolsManager.setDefaultTool(toolId);
          return;

        case UPLOAD:
          LOG.debug("Processing NuGet commandline upload.");

          final String file = request.getParameter("nugetUploadControl");
          if (file == null) {
            throw new ToolException("No file was uploaded.");
          }
          final File tempFile = new File(file);

          try {
            final String uploadedFileName = tempFile.getName();
            final NuGetTool uploadedTool = myToolsManager.installTool(FilenameUtils.removeExtension(uploadedFileName), uploadedFileName, tempFile);
            updateDefault(request, uploadedTool);
          } finally {
            FileUtil.delete(tempFile);
          }
          return;

        case REMOVE:
          myToolsManager.removeTool(toolId);
      }
    } catch (ToolException e) {
      ae.addError("toolId", e.getMessage());
    }
  }

  private void updateDefault(@NotNull HttpServletRequest request,
                             @NotNull NuGetTool tool) {
    if (StringUtil.isEmptyOrSpaces(request.getParameter("setAsDefault"))) return;
    myToolsManager.setDefaultTool(tool.getId());
  }
}
