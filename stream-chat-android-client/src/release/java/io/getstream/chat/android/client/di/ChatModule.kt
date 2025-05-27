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

package io.getstream.chat.android.client.di

import android.content.Context
import androidx.lifecycle.Lifecycle
import io.getstream.chat.android.client.api.ChatClientConfig
import io.getstream.chat.android.client.debugger.ChatClientDebugger
import io.getstream.chat.android.client.interceptor.SendMessageInterceptor
import io.getstream.chat.android.client.notifications.handler.NotificationHandler
import io.getstream.chat.android.client.scope.ClientScope
import io.getstream.chat.android.client.scope.UserScope
import io.getstream.chat.android.client.token.TokenManager
import io.getstream.chat.android.client.transformer.ApiModelTransformers
import io.getstream.chat.android.client.uploader.FileTransformer
import io.getstream.chat.android.client.uploader.FileUploader
import okhttp3.OkHttpClient

/**
 * Release variant of [BaseChatModule].
 */
internal class ChatModule(
    appContext: Context,
    clientScope: ClientScope,
    userScope: UserScope,
    config: ChatClientConfig,
    notificationsHandler: NotificationHandler,
    apiModelTransformers: ApiModelTransformers,
    fileTransformer: FileTransformer,
    uploader: FileUploader?,
    sendMessageInterceptor: SendMessageInterceptor?,
    tokenManager: TokenManager,
    customOkHttpClient: OkHttpClient?,
    clientDebugger: ChatClientDebugger?,
    lifecycle: Lifecycle,
    appName: String?,
    appVersion: String?,
) : BaseChatModule(
    appContext,
    clientScope,
    userScope,
    config,
    notificationsHandler,
    apiModelTransformers,
    fileTransformer,
    uploader,
    sendMessageInterceptor,
    tokenManager,
    customOkHttpClient,
    clientDebugger,
    lifecycle,
    appName,
    appVersion,
)
