package jetbrains.buildServer.nuget.feed.server

import com.intellij.openapi.diagnostic.Logger
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
        return myExecutorService.submit(wrapRunnable(task))
    }

    override fun <T : Any?> submit(task: Callable<T>?): Future<T> {
        return myExecutorService.submit(wrapCallable(task))
    }

    override fun execute(task: Runnable?, startTimeout: Long) {
        return myExecutorService.execute(wrapRunnable(task))
    }

    override fun execute(task: Runnable) {
        return myExecutorService.execute(wrapRunnable(task))
    }

    fun <T> createAsyncTask(callable: () -> T): WebAsyncTask<T> {
        return WebAsyncTask(TeamCityProperties.getLong(NuGetFeedConstants.PROP_NUGET_FEED_ASYNC_REQUEST_TIMOEUT,30000), this, callable)
    }

    private fun wrapRunnable(runnable: Runnable?) : Runnable {
        return Runnable {
            try {
                runnable!!.run()
            } catch (throwable: Throwable) {
                LOG.warnAndDebugDetails("Error in NuGet requests executor thread", throwable)
                throw throwable;
            }
        }
    }

    private fun <T> wrapCallable(callable: Callable<T>?) : Callable<T> {
        return Callable {
            try {
                return@Callable callable!!.call()
            } catch (throwable: Throwable) {
                LOG.warnAndDebugDetails("Error in NuGet requests executor thread", throwable)
                throw throwable
            }
        }
    }

    private companion object {
        val LOG = Logger.getInstance(NuGetAsyncTaskExecutor::class.java.name)
    }
}
