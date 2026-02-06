/*
 * Copyright (c) 2014-2026 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.client.internal.state.sync.internal

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
import io.getstream.chat.android.client.internal.state.extensions.internal.logic
import io.getstream.chat.android.client.internal.state.plugin.internal.StatePlugin
import io.getstream.chat.android.client.utils.internal.validateCid
import io.getstream.log.StreamLog

@Suppress("TooGenericExceptionCaught")
internal class SyncMessagesWork(
    appContext: Context,
    workerParams: WorkerParameters,
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val cid = inputData.getString(DATA_CID)!!
        val client = ChatClient.instance()

        StreamLog.i(TAG) { "[doWork] cid: $cid" }

        return try {
            val (type, id) = validateCid(cid).cidToTypeAndId()

            client.logic.channel(type, id) // Adds this channel to logic - Now it is an active channel

            val syncManager = client.resolveDependency<StatePlugin, SyncHistoryManager>()
            syncManager.sync()

            Result.success()
        } catch (e: Throwable) {
            StreamLog.e(TAG) { "[doWork] failed: $e" }
            Result.failure()
        }
    }

    companion object {
        private const val TAG = "Chat:SyncMessagesWork"
        private const val DATA_CID = "DATA_CID"
        private const val SYNC_MESSAGES_WORK_NAME = "SYNC_MESSAGES_WORK_NAME"

        fun start(context: Context, cid: String) {
            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
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
