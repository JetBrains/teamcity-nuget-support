package jetbrains.buildServer.nuget.feed.server.packages

import jetbrains.buildServer.controllers.BaseController
import jetbrains.buildServer.controllers.admin.projects.BuildTypeForm
import jetbrains.buildServer.nuget.feed.server.NuGetFeedConstants
import jetbrains.buildServer.nuget.feed.server.NuGetUtils
import jetbrains.buildServer.nuget.feed.server.index.NuGetFeedData
import jetbrains.buildServer.serverSide.*
import jetbrains.buildServer.serverSide.packages.impl.RepositoryManager
import jetbrains.buildServer.web.openapi.PluginDescriptor
import jetbrains.buildServer.web.openapi.WebControllerManager
import org.springframework.web.servlet.ModelAndView
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class NuGetIndexerFeature(pluginDescriptor: PluginDescriptor,
                          web: WebControllerManager,
                          private val myProjectManager: ProjectManager,
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
                model["feeds"] = myRepositoryManager.getRepositories(project, true)
                    .filterIsInstance<NuGetRepository>()
                    .mapNotNull { repository ->
                        myProjectManager.findProjectById(repository.projectId)?.let {
                            ProjectFeed(repository, it)
                        }
                    }

                return modelAndView
            }
        })

        myEditParametersUrl = html
    }

    override fun getType() = NuGetFeedConstants.NUGET_INDEXER_TYPE

    override fun getDisplayName() = "NuGet packages indexer"

    override fun getEditParametersUrl() = myEditParametersUrl

    override fun isMultipleFeaturesPerBuildTypeAllowed(): Boolean {
        return true
    }

    override fun isRequiresAgent() = false

    private fun getProject(request: HttpServletRequest): SProject {
        val buildTypeForm = request.getAttribute("buildForm") as BuildTypeForm
        return buildTypeForm.project
    }

    override fun describeParameters(parameters: MutableMap<String, String>): String {
        val feedName = parameters[NuGetFeedConstants.NUGET_INDEXER_FEED]?.let {
            NuGetUtils.feedIdToData(it)?.let { feed ->
                myProjectManager.findProjectByExternalId(feed.first)?.let {
                    if (feed.second == NuGetFeedData.DEFAULT_FEED_ID) "\"${it.name}\" project" else "\"${it.name}/${feed.second}\""
                }
            }
        } ?: "<Unknown>"

        return "Indexing packages into $feedName NuGet feed"
    }

    override fun getParametersProcessor(): PropertiesProcessor? {
        return PropertiesProcessor { parameters ->
            val invalidProperties = arrayListOf<InvalidProperty>()
            NuGetFeedConstants.NUGET_INDEXER_FEED.apply {
                if (parameters[this].isNullOrEmpty()) {
                    invalidProperties.add(InvalidProperty(this, "NuGet feed should not be empty"))
                }
            }
            return@PropertiesProcessor invalidProperties
        }
    }
}
