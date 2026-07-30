package jetbrains.buildServer.nuget.feed.server.index.impl

import com.intellij.openapi.diagnostic.Logger
import jetbrains.buildServer.nuget.feed.server.NuGetFeedConstants
import jetbrains.buildServer.nuget.feed.server.NuGetUtils
import jetbrains.buildServer.nuget.feed.server.index.NuGetFeedData
import jetbrains.buildServer.nuget.feed.server.packages.NuGetRepository
import jetbrains.buildServer.serverSide.ProjectManager
import jetbrains.buildServer.serverSide.SBuild
import jetbrains.buildServer.serverSide.SProject
import jetbrains.buildServer.serverSide.packages.Repository
import jetbrains.buildServer.serverSide.packages.impl.RepositoryManager

class NuGetFeedPermissionCheckerImpl(private val myProjectManager: ProjectManager,
                                     private val myRepositoryManager: RepositoryManager) : NuGetFeedPermissionChecker {

    override fun canWrite(build: SBuild, feed: NuGetFeedData): Boolean =
        canWrite(myProjectManager.findProjectById(build.projectId), feed)

    override fun canWrite(buildProject: SProject?, feed: NuGetFeedData): Boolean {
        if (buildProject == null) {
            return false
        }
        return myRepositoryManager.getRepositories(buildProject, true)
            .filterIsInstance<NuGetRepository>()
            .any { it.projectId == feed.projectId && it.name == feed.feedId }
    }
}
