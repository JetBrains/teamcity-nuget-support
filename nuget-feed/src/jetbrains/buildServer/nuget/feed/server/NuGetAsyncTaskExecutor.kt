package jetbrains.buildServer.nuget.feed.server

import jetbrains.buildServer.serverSide.TeamCityProperties
import jetbrains.buildServer.util.executors.ExecutorsFactory
import org.springframework.core.task.AsyncTaskExecutor
import org.springframework.web.context.request.async.WebAsyncTask
import java.util.concurrent.Callable
import java.util.concurrent.ExecutorService
import java.util.concurrent.Future

class NuGetAsyncTaskExecutor: AsyncTaskExecutor {
    private var myExecutorService: ExecutorService

    init {
        myExecutorService = ExecutorsFactory.newFixedDaemonExecutor(
                "NuGet requests executor",
                0,
                TeamCityProperties.getInteger(NuGetFeedConstants.PROP_NUGET_FEED_MAX_REQUESTS, NuGetFeedConstants.NUGET_FEED_MAX_REQUESTS),
                TeamCityProperties.getInteger(NuGetFeedConstants.PROP_NUGET_FEED_ASYNC_REQUEST_QUEUE_CAPACITY, NuGetFeedConstants.NUGET_FEED_REQUEST_QUEUE_CAPACITY));
    }

    override fun submit(task: Runnable?): Future<*> {
        return myExecutorService.submit(task)
    }

    override fun <T : Any?> submit(task: Callable<T>?): Future<T> {
        return myExecutorService.submit(task)
    }

    override fun execute(task: Runnable?, startTimeout: Long) {
        return myExecutorService.execute(task)
    }

    override fun execute(task: Runnable) {
        return myExecutorService.execute(task)
    }

    fun <T> createAsyncTask(callable: () -> T): WebAsyncTask<T> {
        return WebAsyncTask(TeamCityProperties.getLong(NuGetFeedConstants.PROP_NUGET_FEED_ASYNC_REQUEST_TIMOEUT,30000), this, callable)
    }
}
