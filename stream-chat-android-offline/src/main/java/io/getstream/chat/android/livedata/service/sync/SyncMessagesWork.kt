package io.getstream.chat.android.livedata.service.sync

import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.logger.ChatLogger
import io.getstream.chat.android.client.models.User
import io.getstream.chat.android.livedata.ChatDomain

internal class SyncMessagesWork(
    appContext: Context,
    workerParams: WorkerParameters
) : Worker(appContext, workerParams) {

    override fun doWork(): Result {
        val cid = inputData.getString(DATA_CID)!!
        val userId = inputData.getString(DATA_USER_ID)!!
        val apiKey = inputData.getString(DATA_API_KEY)!!
        val userToken = inputData.getString(DATA_USER_TOKEN)!!

        val logger = ChatLogger.get("SyncMessagesWork")
        val user = User(id = userId)

        val client: ChatClient = initClient(
            context = applicationContext,
            user = user,
            userToken = userToken,
            apiKey = apiKey,
        )

        val domain = initDomain(applicationContext, user, client)
        val result = domain.replayEventsForActiveChannels(cid).execute()

        return if (result.isSuccess) {
            logger.logD("Sync success.")

            Result.success()
        } else {
            logger.logD("Sync failed.")

            Result.retry()
        }
    }

    private fun initDomain(context: Context, user: User, client: ChatClient): ChatDomain {
        return ChatDomain.Builder(context, client).build().apply {
            currentUser = user
        }
    }

    private fun initClient(
        context: Context,
        user: User,
        userToken: String,
        apiKey: String,
    ): ChatClient {
        return ChatClient.Builder(apiKey, context.applicationContext).build().apply {
            setUserWithoutConnecting(user, userToken)
        }
    }

    companion object {
        const val DATA_CID = "DATA_CID"
        const val DATA_USER_ID = "DATA_USER_ID"
        const val DATA_API_KEY = "DATA_API_KEY"
        const val DATA_USER_TOKEN = "DATA_USER_TOKEN"
    }
}
