package jetbrains.buildServer.nuget.feed.server.json

import jetbrains.buildServer.nuget.feed.server.NuGetAsyncTaskExecutor
import jetbrains.buildServer.nuget.feed.server.NuGetFeedConstants
import jetbrains.buildServer.nuget.feed.server.NuGetFeedConstants.*
import jetbrains.buildServer.serverSide.TeamCityProperties
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.context.request.async.WebAsyncTask
import java.util.concurrent.Callable
import javax.servlet.http.HttpServletRequest

@RestController
class AsyncJsonSearchPackagesController(
        private val asyncTaskExecutor: NuGetAsyncTaskExecutor,
        private val packageSourceFactory: JsonPackageSourceFactory,
        private val adapterFactory: JsonPackageAdapterFactory
) {
    @RequestMapping(NUGET_FEED_ASYNC_V3_PACKAGE_SEARCH, method = [RequestMethod.GET], produces = ["application/json; charset=UTF-8"])
    fun searchPackages(
            @RequestParam("q", required = false) query: String?,
            @RequestParam("skip", required = false) skipStr: String?,
            @RequestParam("take", required = false) takeStr: String?,
            @RequestParam("prerelease", required = false) prereleaseStr: String?,
            request: HttpServletRequest
    ): WebAsyncTask<ResponseEntity<String>> {
        val context = DispatcherUtils.getContext(request)
        return asyncTaskExecutor.createAsyncTask<ResponseEntity<String>>
        {
            if (context == null) {
                return@createAsyncTask ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal Server Error")
            }

            val skip = skipStr?.toIntOrNull()
            val take = takeStr?.toIntOrNull() ?: NuGetFeedConstants.NUGET_FEED_PACKAGE_SIZE
            val prerelease = prereleaseStr?.toBoolean() ?: false
            val includeSemVer2 = request.includeSemVer2()

            val packageSource = packageSourceFactory.create(context.feed)
            val packages = packageSource.searchPackages(query, prerelease, includeSemVer2)

            val adapter = adapterFactory.create(context)
            return@createAsyncTask ResponseEntity.ok(JsonExtensions.gson.toJson(adapter.createSearchPackagesResponse(packages, take, skip)))
        }
    }
}
