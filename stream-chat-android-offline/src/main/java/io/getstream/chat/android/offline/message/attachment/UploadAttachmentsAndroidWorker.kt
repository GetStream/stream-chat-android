package io.getstream.chat.android.offline.message.attachment

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.offline.ChatDomain
import io.getstream.chat.android.offline.ChatDomainImpl

internal class UploadAttachmentsAndroidWorker(
    appContext: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val channelType: String = inputData.getString(DATA_CHANNEL_TYPE)!!
        val channelId: String = inputData.getString(DATA_CHANNEL_ID)!!
        val messageId = inputData.getString(DATA_MESSAGE_ID)!!

        return UploadAttachmentsWorker(applicationContext)
            .uploadAttachmentsForMessage(
                channelType,
                channelId,
                messageId,
                ChatDomain.instance() as ChatDomainImpl,
                ChatClient.instance()
            )
            .run { if (isSuccess) Result.success() else Result.failure() }
    }

    companion object {
        private const val DATA_MESSAGE_ID = "message_id"
        private const val DATA_CHANNEL_TYPE = "channel_type"
        private const val DATA_CHANNEL_ID = "channel_id"

        fun start(
            context: Context,
            channelType: String,
            channelId: String,
            messageId: String,
        ) {
            val uploadAttachmentsWorRequest = OneTimeWorkRequestBuilder<UploadAttachmentsAndroidWorker>()
                .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.NOT_ROAMING).build())
                .setInputData(
                    workDataOf(
                        DATA_CHANNEL_ID to channelId,
                        DATA_CHANNEL_TYPE to channelType,
                        DATA_MESSAGE_ID to messageId,
                    )
                )
                .build()

            WorkManager.getInstance(context).enqueueUniqueWork(
                "$channelId$messageId",
                ExistingWorkPolicy.KEEP,
                uploadAttachmentsWorRequest
            )
        }
    }
}
