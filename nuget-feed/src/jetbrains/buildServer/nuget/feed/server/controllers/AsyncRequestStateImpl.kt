package jetbrains.buildServer.nuget.feed.server.controllers

import java.time.Clock
import java.time.LocalDateTime
import java.util.concurrent.CancellationException
import java.util.concurrent.atomic.AtomicInteger

class AsyncRequestStateImpl : AsyncRequestState, AsyncRequestStateController {
    @Volatile
    private var myTimeoutDateTime: LocalDateTime? = null

    @Volatile
    private var myCancellingTimeoutDateTime: LocalDateTime? = null

    private val myState = AtomicInteger(NORMAL)

    override val canBeCancelled: Boolean
        get() = myState.get() == NORMAL

    override val canBeInterrupted: Boolean
        get() = myState.get() == CANCELLATION_REQUESTED

    override val isExpired: Boolean
        get() = myTimeoutDateTime != null && myTimeoutDateTime!! <= getCurrentDateTime()

    override val isCancellingExpired: Boolean
        get() = myCancellingTimeoutDateTime != null && myCancellingTimeoutDateTime!! <= getCurrentDateTime()

    override val isCancellationRequested: Boolean
        get() = myState.get() >= CANCELLATION_REQUESTED

    override val isCancelling: Boolean
        get() = myState.get() >= CANCELLING

    override fun acceptCancelling() {
        myState.compareAndSet(CANCELLATION_REQUESTED, CANCELLING)
    }

    override fun throwIfCancellationRequested() {
        myState.compareAndSet(CANCELLATION_REQUESTED, CANCELLING)
        if (isCancellationRequested) {
            throw CancellationException()
        }
    }

    override fun cancelIfAllowed(action: () -> Unit) {
        if (myState.compareAndSet(NORMAL, CANCELLATION_REQUESTED)) {
            myCancellingTimeoutDateTime = getCurrentDateTime().plusSeconds(1)
            action()
        }
    }

    override fun interruptIfAllowed(action: () -> Unit) {
        if (myState.compareAndSet(CANCELLATION_REQUESTED, INTERRUPTING)) {
            action()
        }
    }

    override fun setTimeout(timeoutDateTime: LocalDateTime) {
        myTimeoutDateTime = timeoutDateTime
    }

    private fun getCurrentDateTime(): LocalDateTime = LocalDateTime.now(Clock.systemUTC())


    companion object {
        private val NORMAL = 0
        private val CANCELLATION_REQUESTED = 1
        private val CANCELLING = 2
        private val INTERRUPTING = 3

        public val NoAsyncState : AsyncRequestState = object : AsyncRequestState {
            override val isExpired: Boolean
                get() = false

            override val isCancellationRequested: Boolean
                get() = false

            override val isCancelling: Boolean
                get() = false

            override val isCancellingExpired: Boolean
                get() = false

            override val canBeCancelled: Boolean
                get() = false

            override val canBeInterrupted: Boolean
                get() = false

            override fun throwIfCancellationRequested() {
            }

            override fun acceptCancelling() {
            }
        }
    }
}
