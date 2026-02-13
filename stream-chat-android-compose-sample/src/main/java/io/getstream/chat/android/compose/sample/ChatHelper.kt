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

package io.getstream.chat.android.compose.sample

import android.content.Context
import android.util.Log
import io.getstream.android.push.firebase.FirebasePushDeviceGenerator
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.api.StateConfig
import io.getstream.chat.android.client.logger.ChatLogLevel
import io.getstream.chat.android.client.notifications.handler.NotificationConfig
import io.getstream.chat.android.client.notifications.handler.NotificationHandlerFactory
import io.getstream.chat.android.compose.sample.data.UserCredentials
import io.getstream.chat.android.compose.sample.ui.StartupActivity
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.EventType
import io.getstream.chat.android.models.InitializationState
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.UploadAttachmentsNetworkType
import io.getstream.result.Error
import kotlinx.coroutines.flow.transformWhile

/**
 * A helper class that is responsible for initializing the SDK and connecting/disconnecting
 * a user. Under the hood, it persists the user so that we are able to connect automatically
 * next time the app is launched.
 */
object ChatHelper {

    private const val TAG = "ChatHelper"

    /**
     * Initializes the SDK with the given API key.
     */
    fun initializeSdk(context: Context, apiKey: String, baseUrl: String? = null) {
        Log.d(TAG, "[init] apiKey: $apiKey")
        val notificationConfig = NotificationConfig(
            pushDeviceGenerators = listOf(
                FirebasePushDeviceGenerator(
                    context = context,
                    providerName = "chat-android-firebase",
                ),
            ),
            autoTranslationEnabled = ChatApp.autoTranslationEnabled,
        )
        val notificationHandler = NotificationHandlerFactory.createNotificationHandler(
            context = context,
            notificationConfig = notificationConfig,
            newMessageIntent = { message: Message, channel: Channel ->
                StartupActivity.createIntent(
                    context = context,
                    channelId = "${channel.type}:${channel.id}",
                    messageId = message.id,
                    parentMessageId = message.parentId,
                )
            },
            onPushMessage = { pushMessage ->
                // Mark the message as delivered when a push for a new message is received
                if (EventType.MESSAGE_NEW == pushMessage.type) {
                    ChatClient.instance()
                        .markMessageAsDelivered(messageId = pushMessage.messageId)
                        .enqueue()
                }
                // Return false to let the SDK handle the push message and show a notification
                false
            },
        )

        val stateConfig = StateConfig(
            backgroundSyncEnabled = false,
            userPresence = true,
        )

        val logLevel = if (BuildConfig.DEBUG) ChatLogLevel.ALL else ChatLogLevel.NOTHING

        ChatClient.Builder(apiKey, context)
            .notifications(notificationConfig, notificationHandler)
            .stateConfig(stateConfig)
            .logLevel(logLevel)
            .uploadAttachmentsNetworkType(UploadAttachmentsNetworkType.NOT_ROAMING)
            .appName("Chat Sample Compose")
            .apply {
                baseUrl?.let {
                    if (it.startsWith("http://")) forceInsecureConnection()
                    baseUrl(it)
                }
            }
            .build()
    }

    /**
     * Initializes [ChatClient] with the given user and saves it to the persistent storage.
     */
    suspend fun connectUser(
        userCredentials: UserCredentials,
        onSuccess: () -> Unit = {},
        onError: (Error) -> Unit = {},
    ) {
        ChatClient.instance().run {
            clientState.initializationState
                .transformWhile {
                    emit(it)
                    it != InitializationState.COMPLETE
                }
                .collect {
                    if (it == InitializationState.NOT_INITIALIZED) {
                        connectUser(userCredentials.user, userCredentials.token)
                            .enqueue { result ->
                                result
                                    .onError(onError)
                                    .onSuccess {
                                        ChatApp.credentialsRepository.saveUserCredentials(userCredentials)
                                        ChatApp.sharedLocationService.start()
                                        onSuccess()
                                    }
                            }
                    }
                }
        }
    }

    /**
     * Logs out the user and removes their credentials from the persistent storage.
     */
    suspend fun disconnectUser() {
        ChatApp.sharedLocationService.stop()
        ChatApp.credentialsRepository.clearCredentials()

        ChatClient.instance().disconnect(flushPersistence = true).await()
    }
}
