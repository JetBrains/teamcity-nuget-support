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

package jetbrains.buildServer.nuget.feed.server.tab;

import com.intellij.openapi.diagnostic.Logger;
import jetbrains.buildServer.controllers.*;
import jetbrains.buildServer.controllers.admin.projects.PluginPropertiesUtil;
import jetbrains.buildServer.log.Loggers;
import jetbrains.buildServer.nuget.feed.server.PermissionChecker;
import jetbrains.buildServer.serverSide.*;
import jetbrains.buildServer.serverSide.identifiers.IdentifiersUtil;
import jetbrains.buildServer.serverSide.packages.Repository;
import jetbrains.buildServer.serverSide.packages.RepositoryConstants;
import jetbrains.buildServer.serverSide.packages.RepositoryRegistry;
import jetbrains.buildServer.serverSide.packages.RepositoryType;
import jetbrains.buildServer.serverSide.packages.impl.RepositoryManager;
import jetbrains.buildServer.util.CollectionsUtil;
import jetbrains.buildServer.util.StringUtil;
import jetbrains.buildServer.web.openapi.ControllerAction;
import jetbrains.buildServer.web.openapi.PluginDescriptor;
import jetbrains.buildServer.web.openapi.WebControllerManager;
import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 01.11.11 17:56
 */
public class PackagesSettingsController extends BaseFormXmlController {
    private static final Logger LOG = Logger.getInstance(PackagesSettingsController.class.getName());

    private final PluginDescriptor myPluginDescriptor;
    private final ProjectManager myProjectManager;
    private final RepositoryRegistry myRepositoryRegistry;
    private final RepositoryManager myRepositoryManager;
    private final PropertiesProcessor myDefaultProcessor;
    private final List<ControllerAction> myPostActions;

    public PackagesSettingsController(@NotNull final AuthorizationInterceptor auth,
                                      @NotNull final PluginDescriptor pluginDescriptor,
                                      @NotNull final PermissionChecker checker,
                                      @NotNull final WebControllerManager web,
                                      @NotNull final ProjectManager projectManager,
                                      @NotNull final RepositoryRegistry repositoryRegistry,
                                      @NotNull final RepositoryManager repositoryManager) {
        myPluginDescriptor = pluginDescriptor;
        myProjectManager = projectManager;
        myRepositoryRegistry = repositoryRegistry;
        myRepositoryManager = repositoryManager;

        final String path = pluginDescriptor.getPluginResourcesPath("packages/settings.html");
        auth.addPathBasedPermissionsChecker(path, (authorityHolder, request) -> checker.assertAccess(getProject(request), authorityHolder));
        web.registerController(path, this);

        myDefaultProcessor = parameters -> {
            final List<InvalidProperty> invalidProperties = new ArrayList<>();
            notEmpty(parameters, RepositoryConstants.REPOSITORY_TYPE_KEY, "repository type", invalidProperties);
            notEmpty(parameters, RepositoryConstants.REPOSITORY_NAME_KEY, "name", invalidProperties);
            if (invalidProperties.isEmpty()) {
              String name = parameters.get(RepositoryConstants.REPOSITORY_NAME_KEY);
              try {
                IdentifiersUtil.validateExternalId(name, "Name");
              } catch (Exception e) {
                invalidProperties.add(new InvalidProperty(RepositoryConstants.REPOSITORY_NAME_KEY, e.getMessage()));
              }
            }
            return invalidProperties;
        };

        myPostActions = new ArrayList<>();
        myPostActions.add(new SaveRepositoryAction());
        myPostActions.add(new AddRepositoryAction());
        myPostActions.add(new DeleteRepositoryAction());
    }

    private void notEmpty(Map<String, String> parameters, String name, String displayName, List<InvalidProperty> invalidProperties) {
        if (StringUtil.isEmpty(parameters.get(name))) {
            invalidProperties.add(new InvalidProperty(name, String.format("The %s must be specified", displayName)));
        }
    }

    @Override
    protected ModelAndView doGet(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response) {
        final String projectExternalId = request.getParameter("projectId");
        final SProject project = myProjectManager.findProjectByExternalId(projectExternalId);
        if (project == null) {
            throw new ProjectNotFoundException("Project with id [" + projectExternalId + " was not found");
        }

        String type = request.getParameter("type");
        if (StringUtil.isEmpty(type)) {
            throw new InvalidParameterException("Repository type must be specified");
        }
        RepositoryType repositoryType = myRepositoryRegistry.findType(type);
        if (repositoryType == null) {
            throw new RuntimeException("Repository type [" + type + "] could not be found");
        }

        final ModelAndView modelAndView = new ModelAndView(myPluginDescriptor.getPluginResourcesPath("editRepositoryParams.jsp"));
        final Map<String, Object> model = modelAndView.getModel();
        final String name = request.getParameter("name");
        final Map<String, String> properties;
        if (StringUtil.isEmpty(name)) {
            properties = repositoryType.getDefaultParameters();
        } else {
            final Repository repository = myRepositoryManager.getRepository(project, type, name);
            if (repository != null) {
                properties = repository.getParameters();
            } else {
                properties = repositoryType.getDefaultParameters();
            }
        }
        model.put("project", project);
        model.put("repositoryType", repositoryType);
        model.put("propertiesBean", new BasePropertiesBean(properties));

        return modelAndView;
    }

    @Override
    protected void doPost(@NotNull HttpServletRequest request,
                          @NotNull HttpServletResponse response,
                          @NotNull Element xmlResponse) {
        for (ControllerAction action : myPostActions) {
            if (action.canProcess(request)) {
                action.process(request, response, xmlResponse);
                return;
            }
        }
        try {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Request handler was not found");
        } catch (IOException e) {
            LOG.infoAndDebugDetails("Failed to set response status", e);
        }
    }

    final class SaveRepositoryAction implements ControllerAction {

        @Override
        public boolean canProcess(@NotNull HttpServletRequest request) {
            return "saveRepository".equals(request.getParameter("action"));
        }

        @Override
        public void process(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response, @Nullable Element xmlResponse) {
            final ActionErrors errors = new ActionErrors();
            try {
                final SProject project = getProject(request);
                if (project == null) {
                    errors.addError("projectNotFound", "Project was not found");
                    return;
                }

                if (project.isReadOnly()) {
                    errors.addError("cannotEditProject", "Project is read-only");
                    return;
                }

                if (PublicKeyUtil.isPublicKeyExpired(request)) {
                    if (xmlResponse != null) {
                        PublicKeyUtil.writePublicKeyExpiredError(xmlResponse);
                    }
                    return;
                }

                String name = request.getParameter("name");
                Map<String, String> properties;
                try {
                    properties = prepareProperties(request);
                } catch (Exception e) {
                    // In case of exception here, we don't know all possible errors, hence can't save the properties.
                    Loggers.SERVER.error(e.getMessage(), e);
                    errors.addError("invalidProperties", "Unable to read the properties");
                    return;
                }

                if (errors.hasNoErrors()) {
                    errors.fillErrors(myDefaultProcessor, properties);
                }

                String type = properties.get(RepositoryConstants.REPOSITORY_TYPE_KEY);
                if (type == null) {
                    errors.addError(RepositoryConstants.REPOSITORY_TYPE_KEY, "Repository type not found");
                    return;
                }

                RepositoryType registryType = myRepositoryRegistry.findType(type);
                if (registryType == null) {
                    errors.addError(RepositoryConstants.REPOSITORY_TYPE_KEY, "Repository type not found");
                    return;
                }

                PropertiesProcessor parametersProcessor = registryType.getParametersProcessor();
                if (parametersProcessor != null) {
                    errors.fillErrors(parametersProcessor, properties);
                }

                if (errors.hasNoErrors()) {
                    Repository repository = registryType.createRepository(project, properties);
                    try {
                        if (StringUtil.isEmpty(name)) {
                            myRepositoryManager.addRepository(project, repository);
                        } else {
                            myRepositoryManager.updateRepository(project, name, repository);
                        }
                    } catch (DuplicateIdException e) {
                        errors.addError(RepositoryConstants.REPOSITORY_NAME_KEY, registryType.getName() + " with the same name already exists");
                    } catch (Exception e) {
                        LOG.warnAndDebugDetails("Failed to save " + registryType.getName(), e);
                        errors.addError("saveFailure", e.getMessage());
                    }
                }
            } finally {
                if (xmlResponse != null) {
                    writeErrors(xmlResponse, errors);
                }
            }
        }
    }

    final class DeleteRepositoryAction implements ControllerAction {

        @Override
        public boolean canProcess(@NotNull HttpServletRequest request) {
            return "deleteRepository".equals(request.getParameter("action"));
        }

        @Override
        public void process(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response, @Nullable Element xmlResponse) {
            final ActionErrors errors = new ActionErrors();
            final SProject project = getProject(request);
            try {
                if (project == null) {
                    errors.addError("projectNotFound", "Project was not found");
                    return;
                }

                if (project.isReadOnly()) {
                    errors.addError("cannotEditProject", "Project is read-only");
                    return;
                }

                final String type = request.getParameter("type");
                if (StringUtil.isEmpty(type)) {
                    errors.addError(RepositoryConstants.REPOSITORY_TYPE_KEY, "Repository type not found");
                    return;
                }

                RepositoryType registryType = myRepositoryRegistry.findType(type);
                if (registryType == null) {
                    errors.addError(RepositoryConstants.REPOSITORY_TYPE_KEY, "Repository type not found");
                    return;
                }

                final String name = request.getParameter("name");
                if (StringUtil.isEmpty(name)) {
                    errors.addError(RepositoryConstants.REPOSITORY_NAME_KEY, registryType.getName() + "name is not found");
                    return;
                }

                try {
                    myRepositoryManager.removeRepository(project, type, name);
                } catch (Exception e) {
                    LOG.infoAndDebugDetails(String.format("Failed to delete %s %s in project %s", registryType.getName(), name, project.getExternalId()), e);
                    errors.addError(RepositoryConstants.REPOSITORY_NAME_KEY, e.getMessage());
                }
            } finally {
                if (xmlResponse != null) {
                    writeErrors(xmlResponse, errors);
                }
            }
        }
    }

    final class AddRepositoryAction implements ControllerAction {

        @Override
        public boolean canProcess(@NotNull HttpServletRequest request) {
            return "addRepository".equals(request.getParameter("action"));
        }

        @Override
        public void process(@NotNull HttpServletRequest request, @NotNull HttpServletResponse response, @Nullable Element xmlResponse) {
            final ActionErrors errors = new ActionErrors();
            final SProject project = getProject(request);
            try {
                if (project == null) {
                    errors.addError("projectNotFound", "Project was not found");
                    return;
                }

                if (project.isReadOnly()) {
                    errors.addError("cannotEditProject", "Project is read-only");
                    return;
                }

                final String type = request.getParameter("type");
                if (StringUtil.isEmpty(type)) {
                    errors.addError(RepositoryConstants.REPOSITORY_TYPE_KEY, "Repository type not found");
                    return;
                }

                RepositoryType repositoryType = myRepositoryRegistry.findType(type);
                if (repositoryType == null) {
                    errors.addError(RepositoryConstants.REPOSITORY_TYPE_KEY, "Repository type not found");
                    return;
                }

                final String name = request.getParameter("name");
                if (StringUtil.isEmpty(name)) {
                    errors.addError(RepositoryConstants.REPOSITORY_NAME_KEY, repositoryType.getName() + "name is not found");
                    return;
                }

                final Repository repository = repositoryType.createRepository(project, CollectionsUtil.asMap(
                        RepositoryConstants.REPOSITORY_TYPE_KEY, type,
                        RepositoryConstants.REPOSITORY_NAME_KEY, name
                ));
                try {
                    myRepositoryManager.addRepository(project, repository);
                } catch (Exception e) {
                    LOG.infoAndDebugDetails(String.format("Failed to add %s %s in project %s", repositoryType.getName(), name, project.getExternalId()), e);
                    errors.addError(RepositoryConstants.REPOSITORY_NAME_KEY, e.getMessage());
                }
            } finally {
                if (xmlResponse != null) {
                    writeErrors(xmlResponse, errors);
                }
            }
        }
    }

    @Nullable
    private SProject getProject(@NotNull final HttpServletRequest request) {
      final String projectExternalId = request.getParameter("projectId");
      return myProjectManager.findProjectByExternalId(projectExternalId);
    }

    @NotNull
    private Map<String, String> prepareProperties(@NotNull final HttpServletRequest request) {
        final BasePropertiesBean bean = new BasePropertiesBean(null);
        PluginPropertiesUtil.bindPropertiesFromRequest(request, bean, true);
        return bean.getProperties();
    }
}
