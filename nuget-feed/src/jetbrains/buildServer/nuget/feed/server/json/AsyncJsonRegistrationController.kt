package jetbrains.buildServer.nuget.feed.server.json

import jetbrains.buildServer.nuget.feed.server.NuGetFeedConstants
import jetbrains.buildServer.serverSide.TeamCityProperties
import jetbrains.buildServer.util.executors.ExecutorsFactory
import org.springframework.core.task.AsyncTaskExecutor
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.context.request.async.WebAsyncTask
import java.util.concurrent.Callable
import java.util.concurrent.Future
import javax.servlet.http.HttpServletRequest

@RestController
@RequestMapping("nuget/v3/registrations")
class AsyncJsonRegistrationController(
        private val packageSourceFactory: JsonPackageSourceFactory,
        private val adapterFactory: JsonPackageAdapterFactory
) {
    private var myAsyncTaskExecutor: AsyncTaskExecutor

    init {
        val executorService = ExecutorsFactory.newFixedDaemonExecutor(
                "NuGet requests executor",
                0,
                TeamCityProperties.getInteger(NuGetFeedConstants.PROP_NUGET_FEED_MAX_REQUESTS, NuGetFeedConstants.NUGET_FEED_MAX_REQUESTS),
                TeamCityProperties.getInteger(NuGetFeedConstants.PROP_NUGET_FEED_ASYNC_REQUEST_QUEUE_CAPACITY, NuGetFeedConstants.NUGET_FEED_REQUEST_QUEUE_CAPACITY));

        myAsyncTaskExecutor = object : AsyncTaskExecutor {
            override fun submit(task: Runnable?): Future<*> {
                return executorService.submit(task)
            }

            override fun <T : Any?> submit(task: Callable<T>?): Future<T> {
                return executorService.submit(task)
            }

            override fun execute(task: Runnable?, startTimeout: Long) {
                return executorService.execute(task)
            }

            override fun execute(task: Runnable) {
                return executorService.execute(task)
            }
        }
    }

    @RequestMapping("/{id}/{version}", method = [RequestMethod.GET], produces = ["application/json; charset=UTF-8"])
    fun getRegistration(
        @PathVariable("id") id: String,
        @PathVariable("version") version: String,
        request: HttpServletRequest
    ): WebAsyncTask<ResponseEntity<String>> {
        return WebAsyncTask<ResponseEntity<String>>(1000, myAsyncTaskExecutor)
        {
            val context = request.getAttribute("nuget.feed.json.data") as JsonNuGetFeedContext
            val packageSource = packageSourceFactory.create(context.feed)
            val results = packageSource.getPackages(id, version)
            if (!results.isEmpty()) {
                val adapter = adapterFactory.create(context)
                ResponseEntity.ok(JsonExtensions.gson.toJson(adapter.createPackageResponse(results.first())))
            } else {
                ResponseEntity.status(HttpStatus.NOT_FOUND).body("Package $id:$version not found")
            }
        }
    }

    @RequestMapping("/{id}", method = [RequestMethod.GET], produces = ["application/json; charset=UTF-8"])
    fun getRegistrations(
            @PathVariable("id") id: String,
            request: HttpServletRequest
    ): WebAsyncTask<ResponseEntity<String>> {
        return WebAsyncTask<ResponseEntity<String>>(1000, myAsyncTaskExecutor)
        {
            val context = request.getAttribute("nuget.feed.json.data") as JsonNuGetFeedContext
            val packageSource = packageSourceFactory.create(context.feed)
            val results = packageSource.getPackages(id)
            if (!results.isEmpty()) {
                val adapter = adapterFactory.create(context)
                ResponseEntity.ok(JsonExtensions.gson.toJson(adapter.createPackagesResponse(id, results)))
            } else {
                ResponseEntity.status(HttpStatus.NOT_FOUND).body("Package $id not found")
            }
        }
    }
}
