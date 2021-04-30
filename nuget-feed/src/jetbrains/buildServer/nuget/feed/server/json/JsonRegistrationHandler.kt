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
            request.setAttribute(NuGetFeedConstants.NUGET_FEED_ASYNC_DATA_CONTEXT, context)

            val asyncEnabled = TeamCityProperties.getBoolean(NuGetFeedConstants.PROP_NUGET_FEED_ASYNC_REQUEST_ENABLED)
            if (resource == "index") {
                if (asyncEnabled) {
                    val dispatcher = request.getRequestDispatcher("/app/${NuGetFeedConstants.NUGET_FEED_ASYNC_V3_REGISTRATIONS}/${id}")
                    dispatcher.forward(request, response)
                } else {
                    getAllRegistrations(context, response, id)
                }
            } else {
                if (asyncEnabled) {
                    val dispatcher = request.getRequestDispatcher("/app/${NuGetFeedConstants.NUGET_FEED_ASYNC_V3_REGISTRATIONS}/${id}/${resource}")
                    dispatcher.forward(request, response)
                } else {
                    getRegistration(context, response, id, resource)
                }
            }
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Requested resource not found")
        }
    }

    private fun getRegistration(context: JsonNuGetFeedContext, response: HttpServletResponse, id: String, version: String) {
        val packageSource = packageSourceFactory.create(context.feed)
        val results = packageSource.getPackages(id)

        if (!results.isEmpty()) {
            val adapter = adapterFactory.create(context)
            response.writeJson(adapter.createPackagesResponse(id, results))
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Package $id:$version not found")
        }
    }

    private fun getAllRegistrations(context: JsonNuGetFeedContext, response: HttpServletResponse, id: String) {
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

