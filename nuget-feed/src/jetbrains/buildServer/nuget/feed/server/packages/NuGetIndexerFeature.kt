package jetbrains.buildServer.nuget.feed.server.packages

import jetbrains.buildServer.controllers.BaseController
import jetbrains.buildServer.controllers.admin.projects.BuildTypeForm
import jetbrains.buildServer.nuget.feed.server.NuGetFeedConstants
import jetbrains.buildServer.serverSide.BuildFeature
import jetbrains.buildServer.serverSide.InvalidProperty
import jetbrains.buildServer.serverSide.PropertiesProcessor
import jetbrains.buildServer.serverSide.SProject
import jetbrains.buildServer.serverSide.packages.impl.RepositoryManager
import jetbrains.buildServer.web.openapi.PluginDescriptor
import jetbrains.buildServer.web.openapi.WebControllerManager
import org.springframework.web.servlet.ModelAndView
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class NuGetIndexerFeature(pluginDescriptor: PluginDescriptor,
                          web: WebControllerManager,
                          private val myRepositoryManager: RepositoryManager) : BuildFeature() {

    private val myEditParametersUrl: String

    init {
        val jsp = pluginDescriptor.getPluginResourcesPath("editNuGetIndexerFeature.jsp")
        val html = pluginDescriptor.getPluginResourcesPath("nugetIndexerSettings.html")

        web.registerController(html, object : BaseController() {
            override fun doHandle(request: HttpServletRequest, response: HttpServletResponse): ModelAndView? {
                val modelAndView = ModelAndView(jsp)
                val model = modelAndView.model

                val project = getProject(request)
                model["feeds"] = myRepositoryManager.getRepositories(project, true).filter {
                    it.type is NuGetRepositoryType
                }

                return modelAndView
            }
        })

        myEditParametersUrl = html
    }

    override fun getType() = NuGetFeedConstants.NUGET_INDEXER_TYPE

    override fun getDisplayName() = "NuGet Packages Indexer"

    override fun getEditParametersUrl() = myEditParametersUrl

    override fun isMultipleFeaturesPerBuildTypeAllowed(): Boolean {
        return true
    }

    override fun isRequiresAgent(): Boolean {
        return true
    }

    private fun getProject(request: HttpServletRequest): SProject {
        val buildTypeForm = request.getAttribute("buildForm") as BuildTypeForm
        return buildTypeForm.project
    }

    override fun describeParameters(parameters: MutableMap<String, String>): String {
        return "Indexing packages into ${parameters[NuGetFeedConstants.NUGET_INDEXER_FEED_ID]} NuGet Feed"
    }

    override fun getParametersProcessor(): PropertiesProcessor? {
        return PropertiesProcessor { parameters ->
            val invalidProperties = arrayListOf<InvalidProperty>()
            NuGetFeedConstants.NUGET_INDEXER_FEED_ID.apply {
                if (parameters[this].isNullOrEmpty()) {
                    invalidProperties.add(InvalidProperty(this, "NuGet feed should not be empty"))
                }
            }
            return@PropertiesProcessor invalidProperties
        }
    }
}
