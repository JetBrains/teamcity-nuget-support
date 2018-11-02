package jetbrains.buildServer.nuget.feed.server

import jetbrains.buildServer.RootUrlHolder
import jetbrains.buildServer.agent.AgentRuntimeProperties
import jetbrains.buildServer.agent.Constants
import jetbrains.buildServer.nuget.common.NuGetServerConstants
import jetbrains.buildServer.nuget.feed.server.index.NuGetIndexUtils
import jetbrains.buildServer.nuget.feed.server.packages.NuGetRepository
import jetbrains.buildServer.parameters.ReferencesResolverUtil
import jetbrains.buildServer.serverSide.BuildStartContext
import jetbrains.buildServer.serverSide.BuildStartContextProcessor
import jetbrains.buildServer.serverSide.ProjectManager
import jetbrains.buildServer.serverSide.packages.impl.RepositoryManager
import jetbrains.buildServer.web.util.WebUtil
import javax.ws.rs.core.UriBuilder

/**
 * Provides system parameters required for TeamCity NuGet credentials provider
 * to automatically authenticate TeamCity NuGet feeds in NuGet CLI without
 * need to add NuGet Feed Credentials build feature. Format:
 * `system.teamcity.nuget.feed.%projectExtId%.%feedId%.(url|publicUrl)`
 */
class NuGetFeedAuthParametersProvider(private val mySettings: NuGetServerSettings,
                                      private val myProjectManager: ProjectManager,
                                      private val myRepositoryManager: RepositoryManager,
                                      private val myRootUrlHolder: RootUrlHolder)
    : BuildStartContextProcessor {

    override fun updateParameters(context: BuildStartContext) {
        if (!mySettings.isNuGetServerEnabled) {
            return
        }

        val build = context.build

        // Check whether we should index NuGet packages for build
        val buildProject = myProjectManager.findProjectById(build.projectId)
        if (build.getBuildFeaturesOfType(NuGetFeedConstants.NUGET_INDEXER_TYPE).isNotEmpty() ||
            NuGetIndexUtils.isIndexingEnabledForBuild(build) &&
            NuGetIndexUtils.findFeedsWithIndexing(buildProject, myRepositoryManager).any()) {
            context.addSharedParameter(NuGetServerConstants.FEED_AGENT_SIDE_INDEXING, "true")
        }

        // Add NuGet feed references
        myProjectManager.findProjectById(build.projectId)?.let {
            val repositories = myRepositoryManager
                .getRepositories(it, true)
                .filterIsInstance<NuGetRepository>()

            repositories.forEach { repository ->
                val project = myProjectManager.findProjectById(repository.projectId) ?: return@forEach
                val feedPath = NuGetUtils.getProjectFeedPath(project.externalId, repository.name)
                val feedSuffix = "${project.externalId}.${repository.name}"
                val httpAuthFeedPath = WebUtil.combineContextPath(WebUtil.HTTP_AUTH_PREFIX, feedPath)
                val feedReferencePrefix = Constants.SYSTEM_PREFIX + NuGetServerConstants.FEED_REF_PREFIX
                context.addSharedParameter(
                        feedReferencePrefix + feedSuffix + NuGetServerConstants.FEED_REF_URL_SUFFIX,
                        ReferencesResolverUtil.makeReference(AgentRuntimeProperties.TEAMCITY_SERVER_URL) + httpAuthFeedPath
                )
                context.addSharedParameter(
                        feedReferencePrefix + feedSuffix + NuGetServerConstants.FEED_REF_PUBLIC_URL_SUFFIX,
                        UriBuilder.fromUri(myRootUrlHolder.rootUrl).replacePath(httpAuthFeedPath).build().toString()
                )
            }
        }
    }
}
