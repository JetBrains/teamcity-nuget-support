package jetbrains.buildServer.nuget.feed.server.tab

import jetbrains.buildServer.controllers.admin.projects.EditProjectTab
import jetbrains.buildServer.serverSide.SProject
import jetbrains.buildServer.serverSide.crypt.RSACipher
import jetbrains.buildServer.serverSide.packages.RepositoryRegistry
import jetbrains.buildServer.serverSide.packages.impl.RepositoryManager
import jetbrains.buildServer.web.openapi.PagePlaces
import jetbrains.buildServer.web.openapi.PluginDescriptor
import jetbrains.buildServer.web.openapi.PositionConstraint
import javax.servlet.http.HttpServletRequest

class PackagesProjectTab(pagePlaces: PagePlaces,
                         private val myDescriptor: PluginDescriptor,
                         private val myRepositoryRegistry: RepositoryRegistry,
                         private val myRepositoryManager: RepositoryManager)
    : EditProjectTab(pagePlaces, "packages", myDescriptor.getPluginResourcesPath("packagesProjectTab.jsp"), FEEDS_TITLE) {

    init {
        setPosition(PositionConstraint.after("artifactsStorage", "versionedSettings"))
        addJsFile(myDescriptor.getPluginResourcesPath("feedServer.js"))
        addJsFile(myDescriptor.getPluginResourcesPath("packages.js"))
        addCssFile(myDescriptor.getPluginResourcesPath("feedServer.css"))
        register()
    }

    override fun fillModel(model: MutableMap<String, Any>, request: HttpServletRequest) {
        super.fillModel(model, request)
        model["includeUrl"] = myDescriptor.getPluginResourcesPath("packages/status.html")
        model["publicKey"] = RSACipher.getHexEncodedPublicKey()
    }

    override fun hasOwnSettings(project: SProject): Boolean {
        return myRepositoryManager.getRepositories(project, false).isNotEmpty()
    }

    override fun getTabTitle(request: HttpServletRequest): String {
        val numRepositories = getProject(request)?.let {
            myRepositoryManager.getRepositories(it, false).size
        } ?: 0
        return if (myRepositoryRegistry.types.size > 1) PACKAGES_TITLE else FEEDS_TITLE +
            if (numRepositories > 0) " ($numRepositories)" else ""
    }

    companion object {
        private const val PACKAGES_TITLE = "Packages"
        private const val FEEDS_TITLE = "NuGet Feeds"
    }
}
