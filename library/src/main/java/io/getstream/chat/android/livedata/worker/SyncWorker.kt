package io.getstream.chat.android.livedata.worker

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.livedata.ChatDomain

class SyncWorker(var context: Context, parameters: WorkerParameters) :
    CoroutineWorker(context, parameters) {
    override suspend fun doWork(): Result {
        // 1. Loads syncState from the room DB
        // 2. Queries events for all active channels
        // 3. Sync those events to room DB
        val channelId = inputData.getString("STREAM_CHANNEL_CID")!!
        val userId = inputData.getString("STREAM_USER_ID")!!

        val client = ChatClient.Builder("mykey", ApplicationProvider.getApplicationContext())
            .logLevel("ALL").build()

        val chatDomain = ChatDomain.Builder(context, client, User(userId)).recoveryDisabled().build()

        val result = chatDomain.useCases.replayEventsForActiveChannels(channelId).execute()

        return if (result.isSuccess) {
            Result.success()
        } else {
            Result.retry()
        }
    }
}
