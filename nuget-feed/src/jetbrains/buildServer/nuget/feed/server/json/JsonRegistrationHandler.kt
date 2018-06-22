package jetbrains.buildServer.nuget.feed.server.json

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.stream.JsonWriter
import jetbrains.buildServer.nuget.feed.server.controllers.NuGetFeedHandler
import jetbrains.buildServer.nuget.feed.server.index.NuGetFeed
import jetbrains.buildServer.nuget.feed.server.index.NuGetFeedData
import jetbrains.buildServer.nuget.feed.server.index.NuGetFeedFactory
import jetbrains.buildServer.nuget.feedReader.NuGetPackageAttributes
import jetbrains.buildServer.web.util.WebUtil
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

class JsonRegistrationHandler(private val feedFactory: NuGetFeedFactory) : NuGetFeedHandler {
  override fun handleRequest(feedData: NuGetFeedData, request: HttpServletRequest, response: HttpServletResponse) {
    REGISTRATION_URL.find(request.pathInfo)?.let {
      val (id, resource) = it.destructured
      val feed = feedFactory.createFeed(feedData)
      if (resource == "index") {
        getAllRegistrations(feed, request, response, id)
      } else {
        getRegistration(feed, request, response, id, resource)
      }
    }
  }

  private fun getRegistration(feed: NuGetFeed, request: HttpServletRequest, response: HttpServletResponse, id: String, version: String) {
    val results = feed.find(mapOf(
      NuGetPackageAttributes.ID to id,
      NuGetPackageAttributes.VERSION to version
    ))

    if (results.isEmpty()) {
      response.sendError(HttpServletResponse.SC_NOT_FOUND, "Package $id:$version not found")
      return
    }

    val entry = results.first()
    val rootUrl = WebUtil.getRootUrl(request)
    val packageResponse = JsonRegistrationPackageResponse(
      rootUrl + request.pathInfo,
      listOf("Package","catalog:Permalink"),
      rootUrl,
      entry
    )

    response.status = HttpServletResponse.SC_OK
    response.contentType = "application/json;charset=UTF-8"
    JsonWriter(response.writer).use {
      gson.toJson(packageResponse, JsonRegistrationPackageResponse::class.java, it)
    }
  }

  private fun getAllRegistrations(feed: NuGetFeed, request: HttpServletRequest, response: HttpServletResponse, id: String) {
    val results = feed.findPackagesById(id)
    val rootUrl = WebUtil.getRootUrl(request)
    val items = results.map {
      val registrationUrl = "$rootUrl/registration1/$id/${it.getVersion()}.json"
      JsonRegistrationPackage(
        registrationUrl,
        "Package",
        JsonRegistrationPackageResponse(
          registrationUrl,
          listOf("PackageDetails"),
          registrationUrl,
          it
        ),
        registrationUrl,
        registrationUrl
      )
    }
    val registrationPage = JsonRegistrationPage(
      rootUrl + request.pathInfo,
      results.size,
      lower = results.first().getVersion(),
      upper = results.last().getVersion(),
      items = items
    )
    val registration = JsonRegistrationResponse(
      rootUrl + request.pathInfo,
      listOf("catalog:CatalogRoot","PackageRegistration","catalog:Permalink"),
      1,
      listOf(registrationPage)
    )

    response.status = HttpServletResponse.SC_OK
    response.contentType = "application/json;charset=UTF-8"
    JsonWriter(response.writer).use {
      gson.toJson(registration, JsonRegistrationResponse::class.java, it)
    }
  }

  companion object {
    private val REGISTRATION_URL = Regex("\\/registration1\\/([^\\/]+)\\/([^\\/]+)\\.json")
    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()
  }
}
