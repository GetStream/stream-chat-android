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
import android.graphics.Color
import com.google.firebase.FirebaseApp
import io.getstream.android.push.firebase.FirebasePushDeviceGenerator
import io.getstream.android.push.xiaomi.XiaomiPushDeviceGenerator
import io.getstream.chat.android.client.ChatClient
import io.getstream.chat.android.client.logger.ChatLogLevel
import io.getstream.chat.android.client.notifications.handler.NotificationConfig
import io.getstream.chat.android.client.notifications.handler.NotificationHandlerFactory
import io.getstream.chat.android.markdown.MarkdownTextTransformer
import io.getstream.chat.android.models.Channel
import io.getstream.chat.android.models.Message
import io.getstream.chat.android.models.ReactionSortingByLastReactionAt
import io.getstream.chat.android.models.UploadAttachmentsNetworkType
import io.getstream.chat.android.offline.plugin.factory.StreamOfflinePluginFactory
import io.getstream.chat.android.state.plugin.config.StatePluginConfig
import io.getstream.chat.android.state.plugin.factory.StreamStatePluginFactory
import io.getstream.chat.android.ui.ChatUI
import io.getstream.chat.android.ui.feature.messages.list.adapter.viewholder.decorator.DecoratorProviderFactory
import io.getstream.chat.android.ui.feature.messages.list.adapter.viewholder.decorator.plus
import io.getstream.chat.android.ui.helper.StyleTransformer
import io.getstream.chat.android.ui.helper.TransformStyle
import io.getstream.chat.ui.sample.BuildConfig
import io.getstream.chat.ui.sample.debugger.CustomChatClientDebugger
import io.getstream.chat.ui.sample.feature.HostActivity
import io.getstream.chat.ui.sample.feature.chat.messagelist.decorator.CustomDecoratorProviderFactory

class ChatInitializer(
    private val context: Context,
    private val autoTranslationEnabled: Boolean,
) {

    @Suppress("UNUSED_VARIABLE")
    fun init(apiKey: String) {
        FirebaseApp.initializeApp(context)
        val notificationConfig =
            NotificationConfig(
                pushDeviceGenerators = listOf(
                    FirebasePushDeviceGenerator(
                        context = context,
                        providerName = "chat-android-firebase",
                    ),
                    XiaomiPushDeviceGenerator(
                        context = context,
                        appId = ApplicationConfigurator.XIAOMI_APP_ID,
                        appKey = ApplicationConfigurator.XIAOMI_APP_KEY,
                        providerName = "chat-android-xiaomi",
                    ),
                ),
                autoTranslationEnabled = autoTranslationEnabled,
            )
        val notificationHandler = NotificationHandlerFactory.createNotificationHandler(
            context = context,
            notificationConfig = notificationConfig,
            newMessageIntent = {
                    message: Message,
                    channel: Channel,
                ->
                HostActivity.createLaunchIntent(
                    context = context,
                    messageId = message.id,
                    parentMessageId = message.parentId,
                    channelType = channel.type,
                    channelId = channel.id,
                )
            },
            onPushMessage = { pushMessage ->
                ChatClient.instance()
                    .markMessageAsDelivered(messageId = pushMessage.messageId)
                    .enqueue()
                // Return false to let the SDK handle the push message and show a notification
                false
            },
        )
        val logLevel = if (BuildConfig.DEBUG) ChatLogLevel.ALL else ChatLogLevel.NOTHING

        val offlinePlugin = StreamOfflinePluginFactory(context)

        val statePluginFactory = StreamStatePluginFactory(
            config = StatePluginConfig(
                backgroundSyncEnabled = true,
                userPresence = true,
            ),
            appContext = context,
        )

        val client = ChatClient.Builder(apiKey, context)
            .loggerHandler(FirebaseLogger)
            .notifications(notificationConfig, notificationHandler)
            .logLevel(logLevel)
            .withPlugins(offlinePlugin, statePluginFactory)
            .uploadAttachmentsNetworkType(UploadAttachmentsNetworkType.NOT_ROAMING)
            .apply {
                if (BuildConfig.DEBUG) {
                    this.debugRequests(true)
                        .clientDebugger(CustomChatClientDebugger())
                }
            }
            .build()

        // Using markdown as text transformer
        val messageTranslator = MessageTranslator(client::getCurrentUser, autoTranslationEnabled)
        ChatUI.autoTranslationEnabled = autoTranslationEnabled
        ChatUI.messageTextTransformer = MarkdownTextTransformer(context, messageTranslator)
        ChatUI.draftMessagesEnabled = true

        TransformStyle.viewReactionsStyleTransformer = StyleTransformer { defaultStyle ->
            defaultStyle.copy(
                bubbleBorderColorMine = Color.YELLOW,
                reactionSorting = ReactionSortingByLastReactionAt,
            )
        }

        ChatUI.decoratorProviderFactory = CustomDecoratorProviderFactory() + DecoratorProviderFactory.defaultFactory()

        TransformStyle.messageComposerStyleTransformer = StyleTransformer { defaultStyle ->
            defaultStyle.copy(
                audioRecordingButtonVisible = true,
                audioRecordingButtonEnabled = true,
                audioRecordingButtonPreferred = true,
            )
        }
    }
}
