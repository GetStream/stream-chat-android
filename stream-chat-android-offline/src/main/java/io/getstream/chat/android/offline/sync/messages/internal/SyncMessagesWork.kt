/*
 * Copyright (c) 2014-2022 Stream.io Inc. All rights reserved.
 *
 * Licensed under the Stream License;
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    https://github.com/GetStream/stream-chat-android/blob/main/LICENSE
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
 
package io.getstream.chat.android.offline.sync.messages.internal

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
import io.getstream.chat.android.client.extensions.cidToTypeAndId
import io.getstream.chat.android.offline.event.handler.internal.EventHandlerProvider
import io.getstream.chat.android.offline.extensions.internal.logic
import io.getstream.chat.android.offline.utils.internal.validateCidBoolean

internal class SyncMessagesWork(
    appContext: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val cid = inputData.getString(DATA_CID)!!
        val client = ChatClient.instance()

        return if (validateCidBoolean(cid)) {
            val (type, id) = cid.cidToTypeAndId()

            client.logic.channel(type, id) // Adds this channel to logic - Now it is an active channel
            EventHandlerProvider.eventHandler.replayEventsForActiveChannels()

            Result.success()
        } else {
            Result.failure()
        }
    }

    companion object {
        private const val DATA_CID = "DATA_CID"
        private const val SYNC_MESSAGES_WORK_NAME = "SYNC_MESSAGES_WORK_NAME"

        fun start(context: Context, cid: String) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.NOT_ROAMING)
                .build()

            val syncMessagesWork = OneTimeWorkRequestBuilder<SyncMessagesWork>()
                .setConstraints(constraints)
                .setInputData(workDataOf(DATA_CID to cid))
                .build()

            WorkManager
                .getInstance(context)
                .enqueueUniqueWork(
                    SYNC_MESSAGES_WORK_NAME,
                    ExistingWorkPolicy.REPLACE,
                    syncMessagesWork,
                )
        }

        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(SYNC_MESSAGES_WORK_NAME)
        }
    }
}
