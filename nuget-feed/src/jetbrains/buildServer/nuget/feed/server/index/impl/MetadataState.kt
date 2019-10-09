package jetbrains.buildServer.nuget.feed.server.index.impl

enum class MetadataState {
    IsAbsent,

    Unsynchronized,

    Synchronized,
}
