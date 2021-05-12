package jetbrains.buildServer.nuget.feed.server.json

import jetbrains.buildServer.nuget.feed.server.NuGetFeedConstants
import jetbrains.buildServer.nuget.feed.server.controllers.NuGetFeedHandler
import jetbrains.buildServer.nuget.feed.server.index.NuGetFeedData
import jetbrains.buildServer.nuget.feed.server.index.NuGetFeedFactory
import jetbrains.buildServer.serverSide.TeamCityProperties
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class JsonRegistrationHandler(private val feedFactory: NuGetFeedFactory,
                              private val packageSourceFactory: JsonPackageSourceFactory,
                              private val adapterFactory: JsonPackageAdapterFactory) : NuGetFeedHandler {
    override fun handleRequest(feedData: NuGetFeedData, request: HttpServletRequest, response: HttpServletResponse) {
        val matchResult = REGISTRATION_URL.find(request.pathInfo)
        if (matchResult != null) {
            val (id, resource) = matchResult.destructured
            val feed = feedFactory.createFeed(feedData)
            val context = JsonNuGetFeedContext(feed, request)

            if (resource == "index") {
                if (DispatcherUtils.isAsyncEnabled()) {
                    DispatcherUtils.dispatchGetRegistrations(request, response, context, id)
                } else {
                    getAllRegistrations(response, context, id)
                }
            } else {
                if (DispatcherUtils.isAsyncEnabled()) {
                    DispatcherUtils.dispatchGetRegistration(request, response, context, id, resource)
                } else {
                    getRegistration(response, context, id, resource)
                }
            }
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Requested resource not found")
        }
    }

    private fun getRegistration(response: HttpServletResponse, context: JsonNuGetFeedContext, id: String, version: String) {
        val packageSource = packageSourceFactory.create(context.feed)
        val results = packageSource.getPackages(id)

        if (!results.isEmpty()) {
            val adapter = adapterFactory.create(context)
            response.writeJson(adapter.createPackagesResponse(id, results))
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Package $id:$version not found")
        }
    }

    private fun getAllRegistrations(response: HttpServletResponse, context: JsonNuGetFeedContext,id: String) {
        val packageSource = packageSourceFactory.create(context.feed)
        val results = packageSource.getPackages(id)

        if (!results.isEmpty()) {
            val adapter = adapterFactory.create(context)
            response.writeJson(adapter.createPackagesResponse(id, results))
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Package $id not found")
        }
    }

    companion object {
        private val REGISTRATION_URL = Regex("\\/registration1\\/([^\\/]+)\\/([^\\/]+)\\.json")
    }
}

