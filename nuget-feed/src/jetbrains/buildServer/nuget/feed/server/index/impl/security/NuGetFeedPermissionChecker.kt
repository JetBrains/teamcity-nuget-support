package jetbrains.buildServer.nuget.feed.server.index.impl.security

import jetbrains.buildServer.nuget.feed.server.index.NuGetFeedData
import jetbrains.buildServer.serverSide.SBuild
import jetbrains.buildServer.serverSide.SProject

/**
 * Authorizes NuGet package publishing: a project may write only to feeds available to it.
 */
interface NuGetFeedPermissionChecker {
    fun canWrite(build: SBuild, feed: NuGetFeedData): Boolean

    fun canWrite(buildProject: SProject?, feed: NuGetFeedData): Boolean

    fun getWritableFeeds(buildProject: SProject): Set<NuGetFeedData>
}
