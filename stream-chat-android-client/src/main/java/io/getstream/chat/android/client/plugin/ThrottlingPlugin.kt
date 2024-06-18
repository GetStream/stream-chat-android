/*
 * Copyright (c) 2014-2023 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.client.plugin

import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.User
import io.getstream.log.StreamLog
import io.getstream.result.Error
import io.getstream.result.Result
import kotlin.reflect.KClass

internal class ThrottlingPlugin : Plugin {
    override val errorHandler = null
    private val lastMarkReadMap: MutableMap<String, Long> = mutableMapOf()

    override suspend fun onChannelMarkReadPrecondition(channelType: String, channelId: String): Result<Unit> {
        val now = System.currentTimeMillis()
        val deltaLastMarkReadAt = now - (lastMarkReadMap[channelId] ?: 0)
        return when {
            deltaLastMarkReadAt > MARK_READ_THROTTLE_MS -> Result.Success(Unit)
                .also { lastMarkReadMap[channelId] = now }
            else -> Result.Failure(Error.GenericError("Mark read throttled")).also {
                StreamLog.w("ThrottlingPlugin") { "[onChannelMarkReadPrecondition] read is ignored ($channelId)" }
            }
        }
    }

    override fun <T : Any> resolveDependency(klass: KClass<T>): T? = null
    override suspend fun onGetNewerRepliesResult(
        result: Result<List<Message>>,
        parentId: String,
        limit: Int,
        lastId: String?,
    ) { /* No-op */ }

    override fun onUserSet(user: User) { /* No-op */ }
    override fun onUserDisconnected() {
        lastMarkReadMap.clear()
    }

    companion object {
        const val MARK_READ_THROTTLE_MS = 3000L
    }
}
