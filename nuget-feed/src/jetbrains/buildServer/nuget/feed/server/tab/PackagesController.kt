

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
                    ?.getUsagesCount(it)
                    ?: 0
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
