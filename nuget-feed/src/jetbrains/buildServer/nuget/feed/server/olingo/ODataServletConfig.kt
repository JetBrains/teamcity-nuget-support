

package jetbrains.buildServer.nuget.feed.server.olingo

import java.util.*
import javax.servlet.ServletConfig
import javax.servlet.ServletContext

/**
 * Configuration for ODataService.
 */
class ODataServletConfig(private val parameters: Map<String, String>) : ServletConfig {
    override fun getServletName(): String {
        return "NuGet Feed"
    }

    override fun getServletContext(): ServletContext? {
        return null
    }

    override fun getInitParameter(name: String): String? {
        return parameters[name]
    }

    override fun getInitParameterNames(): Enumeration<String>? {
        return Vector(parameters.keys).elements()
    }
}
