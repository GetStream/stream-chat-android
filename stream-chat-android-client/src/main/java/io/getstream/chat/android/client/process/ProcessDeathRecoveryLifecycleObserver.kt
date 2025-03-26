/*
 * Copyright (c) 2014-2025 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.client.process

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import io.getstream.chat.android.client.setup.state.ClientState
import io.getstream.log.taggedLogger

/**
 * Lifecycle observer that listens to the process lifecycle events and updates the
 * [ProcessDeathRecoveryStorage] accordingly.
 */
internal class ProcessDeathRecoveryLifecycleObserver(
    private val clientState: ClientState,
    private val storage: ProcessDeathRecoveryStorage,
) : DefaultLifecycleObserver {

    private val logger by taggedLogger("Chat:ProcessDeathRecoveryLifecycleObserver")

    override fun onStop(owner: LifecycleOwner) {
        logger.d { "Process onStop -> Candidate for process termination." }
        // Write the current user to the storage, as the process is stopped and can be killed by the system
        val currentUser = clientState.user.value
        if (currentUser != null) {
            logger.d { "Writing user to ProcessDeathRecoveryStorage." }
            storage.writeUser(currentUser)
        }
    }
}
