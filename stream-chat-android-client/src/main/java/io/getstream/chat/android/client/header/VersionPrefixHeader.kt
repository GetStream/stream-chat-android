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

package io.getstream.chat.android.client.header

import io.getstream.chat.android.core.internal.InternalStreamChatApi

/**
 * An enumeration used for tracking which SDK is being used.
 *
 * @param prefix Header for particular SDK.
 */
@InternalStreamChatApi
public enum class VersionPrefixHeader(public val prefix: String) {
    /**
     * Low-level client.
     */
    DEFAULT("stream-chat-android-"),

    /**
     * XML based UI components.
     */
    UI_COMPONENTS("stream-chat-android-ui-components-"),

    /**
     * Compose UI components.
     */
    COMPOSE("stream-chat-android-compose-"),
}
