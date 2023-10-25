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

package io.getstream.chat.ui.sample.application

import android.content.Context
import com.google.firebase.FirebaseApp
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.logger.ChatLogLevel
import io.getstream.chat.android.client.notifications.handler.NotificationConfig
import io.getstream.chat.android.client.notifications.handler.NotificationHandlerFactory
import io.getstream.chat.android.markdown.MarkdownTextTransformer
import io.getstream.chat.android.offline.plugin.configuration.Config
import io.getstream.chat.android.offline.plugin.factory.StreamOfflinePluginFactory
import io.getstream.chat.android.pushprovider.firebase.FirebasePushDeviceGenerator
import io.getstream.chat.android.pushprovider.huawei.HuaweiPushDeviceGenerator
import io.getstream.chat.android.pushprovider.xiaomi.XiaomiPushDeviceGenerator
import io.getstream.chat.android.ui.ChatUI
import io.getstream.chat.ui.sample.BuildConfig
import io.getstream.chat.ui.sample.debugger.CustomChatClientDebugger
import io.getstream.chat.ui.sample.feature.HostActivity

class ChatInitializer(private val context: Context) {

    @Suppress("UNUSED_VARIABLE")
    fun init(apiKey: String) {
        FirebaseApp.initializeApp(context)
        val notificationConfig =
            NotificationConfig(
                pushDeviceGenerators = listOf(
                    FirebasePushDeviceGenerator(providerName = "Firebase"),
                    HuaweiPushDeviceGenerator(context, ApplicationConfigurator.HUAWEI_APP_ID, providerName = "Huawei"),
                    XiaomiPushDeviceGenerator(
                        context,
                        ApplicationConfigurator.XIAOMI_APP_ID,
                        ApplicationConfigurator.XIAOMI_APP_KEY,
                        providerName = "Xiaomi"
                    ),
                ),
                requestPermissionOnAppLaunch = { true }
            )
        val notificationHandler = NotificationHandlerFactory.createNotificationHandler(
            context = context,
            newMessageIntent = {
                    messageId: String,
                    channelType: String,
                    channelId: String,
                ->
                HostActivity.createLaunchIntent(context, messageId, channelType, channelId)
            },
            notificationConfig = notificationConfig,
        )
        val logLevel = if (BuildConfig.DEBUG) ChatLogLevel.ALL else ChatLogLevel.NOTHING

        val offlinePlugin = StreamOfflinePluginFactory(
            Config(
                userPresence = true,
                persistenceEnabled = true,
                useSequentialEventHandler = true
            ),
            context
        )

        val client = ChatClient.Builder(apiKey, context)
            .loggerHandler(FirebaseLogger)
            .notifications(notificationConfig, notificationHandler)
            .logLevel(logLevel)
            .withPlugin(offlinePlugin)
            .apply {
                if (BuildConfig.DEBUG) {
                    this.debugRequests(true)
                        .clientDebugger(CustomChatClientDebugger())
                }
            }
            .build()

        // Using markdown as text transformer
        ChatUI.messageTextTransformer = MarkdownTextTransformer(context)
    }
}
