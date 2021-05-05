package jetbrains.buildServer.nuget.feed.server.json

import jetbrains.buildServer.nuget.feed.server.NuGetAsyncTaskExecutor
import jetbrains.buildServer.nuget.feed.server.NuGetFeedConstants
import jetbrains.buildServer.nuget.feed.server.NuGetFeedConstants.PROP_NUGET_FEED_ASYNC_REQUEST_TIMOEUT
import jetbrains.buildServer.serverSide.TeamCityProperties
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.context.request.async.WebAsyncTask
import java.net.URI
import java.util.concurrent.Callable
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@RestController
@RequestMapping(NuGetFeedConstants. NUGET_FEED_ASYNC_V3_PACKAGE_CONTENT)
class AsyncJsonPackageContentController(
        private val asyncTaskExecutor: NuGetAsyncTaskExecutor,
        private val packageSourceFactory: JsonPackageSourceFactory,
        private val adapterFactory: JsonPackageAdapterFactory
) {
    @RequestMapping("/{id}/{version}/{extension}", method = [RequestMethod.GET], produces = ["application/json; charset=UTF-8"])
    fun getVersions(
            @PathVariable("id") id: String,
            @PathVariable("version") version: String,
            @PathVariable("extension") extension: String,
            request: HttpServletRequest
    ): WebAsyncTask<ResponseEntity<String>> {
        val context = DispatcherUtils.getContext(request)
        return asyncTaskExecutor.createAsyncTask<ResponseEntity<String>>
        {
            if (context == null) {
                return@createAsyncTask ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal Server Error")
            }
            val packageSource = packageSourceFactory.create(context.feed)
            val results = packageSource.getPackages(id, version)
            if (!results.isEmpty()) {
                val adapter = adapterFactory.create(context)
                var redirectUrl = adapter.createDownloadContentUrl(results.first(), extension)
                if (redirectUrl.isNullOrEmpty()) {
                    return@createAsyncTask ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Unsupported format $extension")
                }
                return@createAsyncTask ResponseEntity.status(HttpStatus.MOVED_PERMANENTLY).location(URI(redirectUrl)).build<String>()
            } else {
                return@createAsyncTask ResponseEntity.status(HttpStatus.NOT_FOUND).body("Package $id:$version not found")
            }
        }
    }
}
