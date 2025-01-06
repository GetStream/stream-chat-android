/*
 * Copyright (c) 2014-2024 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.state.plugin.listener.internal

import io.getstream.chat.android.client.plugin.listeners.BlockUserListener
import io.getstream.chat.android.models.UserBlock
import io.getstream.chat.android.state.plugin.state.global.internal.MutableGlobalState
import io.getstream.result.Result

/**
 * [BlockUserListener] implementation for the [StatePlugin].
 * Updates the global state with the result of the "Block User" operation.
 *
 * @param globalState The global state of the plugin.
 */
internal class BlockUserListenerState(private val globalState: MutableGlobalState) : BlockUserListener {

    override fun onBlockUserResult(result: Result<UserBlock>) {
        if (result is Result.Success) {
            val userId = result.value.userId
            val blockedUserIds = globalState.blockedUserIds.value
            if (!blockedUserIds.contains(userId)) {
                globalState.setBlockedUserIds(blockedUserIds + userId)
            }
        }
    }
}
