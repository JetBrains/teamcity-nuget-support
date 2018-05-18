package jetbrains.buildServer.nuget.feed.server.packages

abstract class Repository(private val params: Map<String, String>) {

    val name: String
        get() {
            return parameters[RepositoryConstants.REPOSITORY_NAME_KEY]!!
        }

    val description: String
        get() {
            return parameters[RepositoryConstants.REPOSITORY_DESCRIPTION_KEY]!!
        }

    open val parameters: Map<String, String>
        get() = params

    abstract val urlPaths: List<String>
}
