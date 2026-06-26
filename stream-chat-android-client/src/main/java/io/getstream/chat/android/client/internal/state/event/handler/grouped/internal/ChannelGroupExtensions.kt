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

package io.getstream.chat.android.client.internal.state.event.handler.grouped.internal

import io.getstream.chat.android.models.Channel

/** Conventional `extraData` key under which a channel's group identifier is stored. */
internal const val DEFAULT_GROUP_FIELD_NAME: String = "group"

/** The channel's group identifier as stored in `extraData[DEFAULT_GROUP_FIELD_NAME]`. */
internal val Channel.group: String?
    get() = extraData[DEFAULT_GROUP_FIELD_NAME] as? String
