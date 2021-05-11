package jetbrains.buildServer.nuget.feed.server.json

import jetbrains.buildServer.controllers.interceptors.RequestInterceptors
import jetbrains.buildServer.nuget.feed.server.NuGetFeedConstants
import org.springframework.beans.factory.BeanFactory
import org.springframework.util.AntPathMatcher
import org.springframework.web.context.request.async.WebAsyncUtils
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter
import javax.servlet.DispatcherType
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class CallableInterceptorRegistrar(
        private val interceptors: RequestInterceptors,
        private val beanFactory: BeanFactory
): HandlerInterceptorAdapter() {
    private val pathMatcher = AntPathMatcher()
    init {
        interceptors.addInterceptor(this)

        pathMatcher.setCachePatterns(true)
        pathMatcher.setCaseSensitive(false)
    }
    override fun preHandle(request: HttpServletRequest?, response: HttpServletResponse?, handler: Any?): Boolean {
        if (request?.pathInfo == null || request.dispatcherType == DispatcherType.ASYNC) return true
        if (!request.pathInfo.startsWith(APP_PATH_PREFIX)) return true

        val asyncManager = WebAsyncUtils.getAsyncManager(request)
        val loggingInterceptor = beanFactory.getBean(LoggingInterceptor::class.java)
        val timeoutInterceptor = beanFactory.getBean(TimeoutCallableInterceptor::class.java)

        asyncManager.registerCallableInterceptors(loggingInterceptor, timeoutInterceptor);

        return true
    }

    private companion object {
        val APP_PATH_PREFIX = "/app/nuget/feed/"
    }
}
