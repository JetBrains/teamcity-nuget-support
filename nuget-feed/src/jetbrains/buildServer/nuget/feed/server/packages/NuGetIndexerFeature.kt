package jetbrains.buildServer.nuget.feed.server.packages

import jetbrains.buildServer.controllers.BaseController
import jetbrains.buildServer.controllers.admin.projects.BuildTypeForm
import jetbrains.buildServer.nuget.common.index.PackageConstants
import jetbrains.buildServer.nuget.feed.server.NuGetFeedConstants
import jetbrains.buildServer.nuget.feed.server.NuGetUtils
import jetbrains.buildServer.nuget.feed.server.index.NuGetFeedData
import jetbrains.buildServer.serverSide.*
import jetbrains.buildServer.serverSide.packages.RepositoryConstants
import jetbrains.buildServer.serverSide.packages.impl.RepositoryManager
import jetbrains.buildServer.web.openapi.PluginDescriptor
import jetbrains.buildServer.web.openapi.WebControllerManager
import org.springframework.web.servlet.ModelAndView
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class NuGetIndexerFeature(pluginDescriptor: PluginDescriptor,
                          web: WebControllerManager,
                          eventDispatcher: ProjectsModelEventDispatcher,
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
                        myProjectManager.findProjectByExternalId(repository.projectId)?.let {
                            ProjectFeed(repository, it)
                        }
                    }

                return modelAndView
            }
        })

        eventDispatcher.addListener(object : ProjectsModelListenerAdapter() {
            override fun projectFeatureChanged(project: SProject, before: SProjectFeatureDescriptor, after: SProjectFeatureDescriptor) {
                if (before.type != RepositoryConstants.PACKAGES_FEATURE_TYPE) {
                    return
                }
                if (before.parameters[RepositoryConstants.REPOSITORY_TYPE_KEY] != PackageConstants.NUGET_PROVIDER_ID) {
                    return
                }

                val newName = after.parameters[RepositoryConstants.REPOSITORY_NAME_KEY]
                val oldName = before.parameters[RepositoryConstants.REPOSITORY_NAME_KEY]
                if (oldName == newName || oldName == null || newName == null) {
                    return
                }

                val oldReference = NuGetFeedData(project.externalId, oldName)
                val newReference = NuGetFeedData(project.externalId, newName)

                updateFeedReferences(project, oldReference, newReference)
                project.projects.forEach {
                    updateFeedReferences(it, oldReference, newReference)
                }
            }

            override fun projectFeatureRemoved(project: SProject, feature: SProjectFeatureDescriptor) {
                if (feature.type != RepositoryConstants.PACKAGES_FEATURE_TYPE) {
                    return
                }
                if (feature.parameters[RepositoryConstants.REPOSITORY_TYPE_KEY] != PackageConstants.NUGET_PROVIDER_ID) {
                    return
                }

                val name = feature.parameters[RepositoryConstants.REPOSITORY_NAME_KEY] ?: return
                val reference = NuGetFeedData(project.externalId, name)

                deleteFeedReferences(project, reference)
                project.projects.forEach {
                    deleteFeedReferences(it, reference)
                }
            }

            override fun projectRemoved(project: SProject) {
                project.getAvailableFeaturesOfType(RepositoryConstants.PACKAGES_FEATURE_TYPE).forEach {feature ->
                    if (feature.parameters[RepositoryConstants.REPOSITORY_TYPE_KEY] != PackageConstants.NUGET_PROVIDER_ID) {
                        return
                    }

                    val name = feature.parameters[RepositoryConstants.REPOSITORY_NAME_KEY] ?: return
                    val reference = NuGetFeedData(project.externalId, name)

                    deleteFeedReferences(project, reference)
                    project.projects.forEach {
                        deleteFeedReferences(it, reference)
                    }
                }
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

    override fun isRequiresAgent() = false

    private fun getProject(request: HttpServletRequest): SProject {
        val buildTypeForm = request.getAttribute("buildForm") as BuildTypeForm
        return buildTypeForm.project
    }

    override fun describeParameters(parameters: MutableMap<String, String>): String {
        val feedName = parameters[NuGetFeedConstants.NUGET_INDEXER_FEED]?.let {
            NuGetUtils.feedIdToData(it)?.let { feed ->
                myProjectManager.findProjectByExternalId(feed.projectId)?.let {
                    if (feed.feedId == NuGetFeedData.DEFAULT_FEED_ID) "\"${it.name}\" project" else "\"${it.name}/${feed.feedId}\""
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

    private fun updateFeedReferences(project: SProject, oldReference: NuGetFeedData, newReference: NuGetFeedData) {
        processFeedReferences(project,  oldReference, { buildType, feature ->
            val parameters = feature.parameters.toMutableMap()
            parameters[NuGetFeedConstants.NUGET_INDEXER_FEED] = newReference.toString()
            buildType.updateBuildFeature(feature.id, feature.type, parameters)
        })
    }

    private fun deleteFeedReferences(project: SProject, oldReference: NuGetFeedData) {
        processFeedReferences(project,  oldReference, { buildType, feature ->
            buildType.removeBuildFeature(feature.id)
        })
    }

    private fun processFeedReferences(project: SProject, reference: NuGetFeedData, action: (SBuildType, SBuildFeatureDescriptor) -> Unit) {
        project.buildTypes.forEach {buildType ->
            var updated = false
            buildType.getBuildFeaturesOfType(NuGetFeedConstants.NUGET_INDEXER_TYPE).forEach {feature ->
                feature.parameters[NuGetFeedConstants.NUGET_INDEXER_FEED]?.let {
                    NuGetUtils.feedIdToData(it)?.let {
                        if (it == reference) {
                            action(buildType, feature)
                            updated = true
                        }
                    }
                }
            }
            if (updated) {
                buildType.persist()
            }
        }
    }
}
