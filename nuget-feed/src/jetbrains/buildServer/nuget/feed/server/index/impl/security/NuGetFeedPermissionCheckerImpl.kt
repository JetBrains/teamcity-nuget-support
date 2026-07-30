package jetbrains.buildServer.nuget.feed.server.index.impl.security

import jetbrains.buildServer.nuget.feed.server.index.NuGetFeedData
import jetbrains.buildServer.nuget.feed.server.packages.NuGetRepository
import jetbrains.buildServer.serverSide.ProjectManager
import jetbrains.buildServer.serverSide.SBuild
import jetbrains.buildServer.serverSide.SProject
import jetbrains.buildServer.serverSide.packages.impl.RepositoryManager

class NuGetFeedPermissionCheckerImpl(private val myProjectManager: ProjectManager,
                                     private val myRepositoryManager: RepositoryManager) : NuGetFeedPermissionChecker {

    override fun canWrite(build: SBuild, feed: NuGetFeedData): Boolean =
        canWrite(myProjectManager.findProjectById(build.projectId), feed)

    override fun canWrite(buildProject: SProject?, feed: NuGetFeedData): Boolean =
        buildProject != null && getWritableFeeds(buildProject).contains(feed)

    override fun getWritableFeeds(buildProject: SProject): Set<NuGetFeedData> =
        myRepositoryManager.getRepositories(buildProject, true)
            .filterIsInstance<NuGetRepository>()
            .mapTo(hashSetOf()) { NuGetFeedData(it.projectId, it.name) }
}
