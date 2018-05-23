package jetbrains.buildServer.nuget.feed.server.packages

import jetbrains.buildServer.serverSide.SProject

data class ProjectFeed(val repository: NuGetRepository, val project: SProject)
