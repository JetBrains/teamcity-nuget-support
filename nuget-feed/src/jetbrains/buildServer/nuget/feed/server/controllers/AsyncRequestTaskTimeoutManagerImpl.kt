package jetbrains.buildServer.nuget.feed.server.controllers

import java.util.concurrent.ConcurrentHashMap

class AsyncRequestTaskTimeoutManagerImpl : AsyncRequestTaskTimeoutManager {
    private val tasks = ConcurrentHashMap.newKeySet<AsyncRequestTask<*>>();

    override fun registerTask(task: AsyncRequestTask<*>) {
        tasks.add(task)
    }

    override fun processTasks() {
        val expiredTasksPhaseOne = tasks.filter { it.state.isExpired && it.state.canBeCancelled }.toList()
        for (task in expiredTasksPhaseOne) {
            if (task.isDone) {
                tasks.remove(task)
            } else {
                task.cancel()
            }
        }

        val expiredTasksPhaseTwo = tasks.filter { it.state.isCancellingExpired && it.state.canBeInterrupted }.toList()
        for (task in expiredTasksPhaseTwo) {
            if (tasks.remove(task)) {
                if (!task.isDone) {
                    task.interrupt()
                }
            }
        }

        val cancellingTasks = tasks.filter { it.state.isCancelling }.toList()
        for (task in cancellingTasks) {
            tasks.remove(task)
        }
    }
}
