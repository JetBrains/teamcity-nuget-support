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

package jetbrains.buildServer.nuget.feed.server.tab

import jetbrains.buildServer.RootUrlHolder
import jetbrains.buildServer.controllers.AuthorizationInterceptor
import jetbrains.buildServer.controllers.BaseController
import jetbrains.buildServer.nuget.feed.server.PermissionChecker
import jetbrains.buildServer.serverSide.ProjectManager
import jetbrains.buildServer.serverSide.auth.LoginConfiguration
import jetbrains.buildServer.serverSide.packages.RepositoryRegistry
import jetbrains.buildServer.serverSide.packages.impl.RepositoryManager
import jetbrains.buildServer.web.openapi.PluginDescriptor
import jetbrains.buildServer.web.openapi.WebControllerManager
import org.springframework.web.servlet.ModelAndView
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * @author Eugene Petrenko (eugene.petrenko@gmail.com)
 * Date: 26.10.11 19:21
 */
class PackagesController(auth: AuthorizationInterceptor,
                         checker: PermissionChecker,
                         web: WebControllerManager,
                         private val myDescriptor: PluginDescriptor,
                         private val myLoginConfiguration: LoginConfiguration,
                         private val myRootUrlHolder: RootUrlHolder,
                         private val myRepositoryRegistry: RepositoryRegistry,
                         private val myRepositoriesManager: RepositoryManager,
                         private val myProjectManager: ProjectManager) : BaseController() {

    private val myIncludePath: String = myDescriptor.getPluginResourcesPath("packages/status.html")
    private val mySettingsPath: String = myDescriptor.getPluginResourcesPath("packages/settings.html")

    init {
        auth.addPathBasedPermissionsChecker(myIncludePath) { authorityHolder, _ ->
            checker.assertAccess(authorityHolder)
        }
        web.registerController(myIncludePath, this)
    }

    override fun doHandle(request: HttpServletRequest,
                          response: HttpServletResponse): ModelAndView? {
        val mv = ModelAndView(myDescriptor.getPluginResourcesPath("packagesSettings.jsp"))

        val project = getProject(request)
        val repositories = myRepositoriesManager.getRepositories(project, false).map {
            val usages = myRepositoryRegistry.findUsagesProvider(it.type.type)
                    ?.getUsages(it, MAX_USAGES_COUNT)?.take(MAX_USAGES_COUNT)
                    ?: emptyList()
            ProjectRepository(it, project, myRootUrlHolder.rootUrl, usages)
        }
        mv.model["project"] = project
        mv.model["repositories"] = repositories
        mv.model["repositoryTypes"] = myRepositoryRegistry.types

        mv.model["statusRefreshUrl"] = myIncludePath
        mv.model["settingsPostUrl"] = (request.contextPath ?: "") + mySettingsPath
        mv.model["isGuestEnabled"] = myLoginConfiguration.isGuestLoginAllowed

        return mv
    }

    private fun getProject(request: HttpServletRequest) =
            myProjectManager.findProjectByExternalId(request.getParameter("projectId"))!!

    companion object {
        private const val MAX_USAGES_COUNT = 100
    }
}
