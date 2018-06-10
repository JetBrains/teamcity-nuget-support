package jetbrains.buildServer.nuget.feed.server.json

data class JsonSearchResponse(
        val totalHits: Int,
        val data: List<JsonPackage>
)
