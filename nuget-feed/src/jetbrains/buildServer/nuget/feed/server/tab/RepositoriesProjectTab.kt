package jetbrains.buildServer.nuget.feed.server.tab

import jetbrains.buildServer.controllers.admin.projects.EditProjectTab
import jetbrains.buildServer.nuget.feed.server.packages.RepositoryConstants
import jetbrains.buildServer.serverSide.SProject
import jetbrains.buildServer.web.openapi.PagePlaces
import jetbrains.buildServer.web.openapi.PluginDescriptor
import jetbrains.buildServer.web.openapi.PositionConstraint
import javax.servlet.http.HttpServletRequest

class RepositoriesProjectTab(pagePlaces: PagePlaces,
                             private val descriptor: PluginDescriptor)
    : EditProjectTab(pagePlaces, "repositories", descriptor.getPluginResourcesPath("repositoriesProjectTab.jsp"), FEEDS_TITLE) {

    init {
        setPosition(PositionConstraint.after("artifactsStorage", "versionedSettings"))
        addJsFile(descriptor.getPluginResourcesPath("feedServer.js"))
        addCssFile(descriptor.getPluginResourcesPath("feedServer.css"))
        register()
    }

    override fun fillModel(model: MutableMap<String, Any>, request: HttpServletRequest) {
        super.fillModel(model, request)
        model["includeUrl"] = descriptor.getPluginResourcesPath("packages/status.html")
    }

    override fun hasOwnSettings(project: SProject): Boolean {
        return project.getOwnFeaturesOfType(RepositoryConstants.PACKAGES_FEATURE_TYPE).isNotEmpty()
    }

    companion object {
        private const val FEEDS_TITLE = "NuGet Feeds"
    }
}
