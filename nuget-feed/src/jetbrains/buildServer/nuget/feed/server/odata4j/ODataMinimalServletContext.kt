package jetbrains.buildServer.nuget.feed.server.odata4j

import javax.servlet.*
import java.io.InputStream
import java.net.URL
import java.util.*
import javax.servlet.descriptor.JspConfigDescriptor

/**
 * TW-94080
 * Minimal ServletContext implementation required for Jersey 1.18.6+ compatibility with OData4j.
 *
 * PROBLEM:
 * Jersey 1.18.6 introduced stricter CDI (Contexts and Dependency Injection) requirements.
 * When initializing a ServletContainer, Jersey's CDIComponentProviderFactoryInitializer
 * expects a non-null ServletContext to look up CDI components. The original ODataServletConfig
 * returned null for getServletContext(), causing NullPointerException during servlet initialization.
 *
 * SOLUTION:
 * This class provides a minimal ServletContext implementation that:
 * - Satisfies Jersey's CDI requirements by providing a non-null ServletContext
 * - Returns safe defaults (null) for CDI lookups that aren't needed
 * - Implements only the essential ServletContext methods used by Jersey/OData4j
 * - Throws UnsupportedOperationException for advanced features not used by this legacy code
 *
 * This approach provides the minimal implementation needed without rewriting the legacy OData4j code.
 */
class ODataMinimalServletContext : ServletContext {
    private val attributes = mutableMapOf<String, Any>()
    private val initParameters = mutableMapOf<String, String>()

    override fun getAttribute(name: String): Any? = attributes[name]

    override fun getAttributeNames(): Enumeration<String> =
        Collections.enumeration(attributes.keys)

    override fun setAttribute(name: String, value: Any) {
        attributes[name] = value
    }

    override fun removeAttribute(name: String) {
        attributes.remove(name)
    }

    override fun getInitParameter(name: String): String? = initParameters[name]

    override fun getInitParameterNames(): Enumeration<String> =
        Collections.enumeration(initParameters.keys)

    override fun setInitParameter(name: String?, value: String?): Boolean {
        if (name == null || value == null) {
            return false
        }
        initParameters[name] = value
        return true
    }

    override fun getContextPath(): String = ""

    override fun getServletContextName(): String = "ODataMinimalServletContext"

    override fun getServerInfo(): String = "ODataMinimalServletContainer/1.0"

    override fun getContext(uripath: String): ServletContext = this

    override fun getMajorVersion(): Int = 3

    override fun getMinorVersion(): Int = 1

    override fun getEffectiveMajorVersion(): Int = 3

    override fun getEffectiveMinorVersion(): Int = 1

    override fun getMimeType(file: String?): String? = null

    override fun getResource(path: String?): URL? = null

    override fun getResourceAsStream(path: String?): InputStream? = null

    override fun getResourcePaths(path: String?): MutableSet<String>? = null

    override fun getRequestDispatcher(path: String?): RequestDispatcher? = null

    override fun getNamedDispatcher(name: String?): RequestDispatcher? = null

    @Deprecated("Not supported", level = DeprecationLevel.HIDDEN)
    override fun getServlet(name: String?): Servlet? = null

    @Deprecated("Not supported", level = DeprecationLevel.HIDDEN)
    override fun getServlets(): Enumeration<Servlet> = Collections.emptyEnumeration()

    @Deprecated("Not supported", level = DeprecationLevel.HIDDEN)
    override fun getServletNames(): Enumeration<String> = Collections.emptyEnumeration()

    override fun log(msg: String?) {}

    @Deprecated("Deprecated in Java")
    override fun log(exception: Exception?, msg: String?) {}

    override fun log(message: String?, throwable: Throwable?) {}

    override fun getRealPath(path: String?): String? = null

    override fun getVirtualServerName(): String = throw UnsupportedOperationException()
    override fun getSessionCookieConfig(): SessionCookieConfig = throw UnsupportedOperationException()
    override fun getDefaultSessionTrackingModes(): Set<SessionTrackingMode> = throw UnsupportedOperationException()
    override fun getEffectiveSessionTrackingModes(): Set<SessionTrackingMode> = throw UnsupportedOperationException()
    override fun setSessionTrackingModes(modes: MutableSet<SessionTrackingMode>?) = throw UnsupportedOperationException()
    override fun addServlet(servletName: String?, className: String?): ServletRegistration.Dynamic = throw UnsupportedOperationException()
    override fun addServlet(servletName: String?, servlet: Servlet?): ServletRegistration.Dynamic = throw UnsupportedOperationException()
    override fun addServlet(servletName: String?, servletClass: Class<out Servlet>?): ServletRegistration.Dynamic = throw UnsupportedOperationException()
    override fun <T : Servlet?> createServlet(c: Class<T>?): T = throw UnsupportedOperationException()
    override fun getServletRegistration(servletName: String?): ServletRegistration? = null
    override fun getServletRegistrations(): MutableMap<String, out ServletRegistration> = emptyMap<String, ServletRegistration>().toMutableMap()
    override fun addFilter(filterName: String?, className: String?): FilterRegistration.Dynamic = throw UnsupportedOperationException()
    override fun addFilter(filterName: String?, filter: Filter?): FilterRegistration.Dynamic = throw UnsupportedOperationException()
    override fun addFilter(filterName: String?, filterClass: Class<out Filter>?): FilterRegistration.Dynamic = throw UnsupportedOperationException()
    override fun <T : Filter?> createFilter(c: Class<T>?): T = throw UnsupportedOperationException()
    override fun getFilterRegistration(filterName: String?): FilterRegistration? = null
    override fun getFilterRegistrations(): MutableMap<String, out FilterRegistration> = emptyMap<String, FilterRegistration>().toMutableMap()
    override fun addListener(className: String?) = throw UnsupportedOperationException()
    override fun <T : EventListener?> addListener(t: T) = throw UnsupportedOperationException()
    override fun addListener(listenerClass: Class<out EventListener>?) = throw UnsupportedOperationException()
    override fun <T : EventListener?> createListener(c: Class<T>?): T = throw UnsupportedOperationException()
    override fun declareRoles(vararg roleNames: String?) = throw UnsupportedOperationException()
    override fun getJspConfigDescriptor(): JspConfigDescriptor? = null
    override fun getClassLoader(): ClassLoader? = null
}
