package io.getstream.chat.android.offline.repository.database.internal.debug

import java.util.concurrent.Executor

/**
 * Executor that runs tasks on the current thread.
 */
internal class CurrentThreadExecutor : Executor {
    override fun execute(command: Runnable) {
        command.run()
    }
}