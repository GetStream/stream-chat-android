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

package io.getstream.chat.android.compose.sample.vm

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.compose.sample.ChatApp
import io.getstream.chat.android.models.User
import io.getstream.log.taggedLogger
import kotlinx.coroutines.launch
import java.util.Date

class SharedLocationViewModel(
    private val cid: String,
    private val chatClient: ChatClient = ChatClient.instance(),
) : ViewModel() {

    private val logger by taggedLogger("Chat:SharedLocationViewModel")

    val currentUser: User? = chatClient.getCurrentUser()

    fun sendStaticLocation(
        latitude: Double,
        longitude: Double,
    ) {
        viewModelScope.launch {
            chatClient.sendStaticLocation(
                cid = cid,
                latitude = latitude,
                longitude = longitude,
                deviceId = ChatApp.sharedLocationService.currentDeviceId,
            ).await()
                .onSuccess {
                    logger.d { "[sendStaticLocation] Success" }
                }
                .onError {
                    logger.e { "[sendStaticLocation] Failed: $it" }
                }
        }
    }

    fun startLiveLocationSharing(
        latitude: Double,
        longitude: Double,
        endAt: Date,
    ) {
        viewModelScope.launch {
            chatClient.startLiveLocationSharing(
                cid = cid,
                latitude = latitude,
                longitude = longitude,
                deviceId = ChatApp.sharedLocationService.currentDeviceId,
                endAt = endAt,
            ).await()
                .onSuccess {
                    logger.d { "[startLiveLocationSharing] Success" }
                }
                .onError {
                    logger.e { "[startLiveLocationSharing] Failed: $it" }
                }
        }
    }

    fun stopLiveLocationSharing(messageId: String) {
        viewModelScope.launch {
            chatClient.stopLiveLocationSharing(
                messageId = messageId,
                deviceId = ChatApp.sharedLocationService.currentDeviceId,
            ).await()
                .onSuccess {
                    logger.d { "[stopLiveLocationSharing] Success" }
                }
                .onError {
                    logger.e { "[stopLiveLocationSharing] Failed: $it" }
                }
        }
    }
}

class SharedLocationViewModelFactory(
    private val cid: String,
    private val chatClient: ChatClient = ChatClient.instance(),
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        require(modelClass == SharedLocationViewModel::class.java) {
            "SharedLocationViewModelFactory can only create instances of SharedLocationViewModel"
        }
        @Suppress("UNCHECKED_CAST")
        return SharedLocationViewModel(cid, chatClient) as T
    }
}
