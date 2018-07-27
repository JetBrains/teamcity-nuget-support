package jetbrains.buildServer.nuget.feed.server.json

data class JsonAutocompleteNames(
        val totalHits: Int,
        val data: List<String>
)
