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

package io.getstream.chat.android.client.notifications.handler

import android.content.Context
import io.getstream.chat.android.core.internal.InternalStreamChatApi

/**
 * Defaults that UI modules provide for the [NotificationHandler] built by [NotificationHandlerFactory], used when the
 * app doesn't pass its own.
 */
@InternalStreamChatApi
public object NotificationDefaults {

    /**
     * Creates the [UserIconBuilder]. When null, the icon is downloaded without caching.
     */
    @Volatile
    public var userIconBuilderFactory: ((Context) -> UserIconBuilder)? = null
}
