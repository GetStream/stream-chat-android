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

package io.getstream.chat.android.compose.ui.messages.attachments.poll

import androidx.compose.runtime.Immutable
import java.util.UUID

/**
 * The option item for creating a poll.
 *
 * @property title The title of this poll item.
 * @property key The key that identifies this poll item.
 * @property pollOptionError Indicates this option has an error.
 */
@Immutable
public data class PollOptionItem(
    public val title: String,
    public val key: String = UUID.randomUUID().toString(),
    public val pollOptionError: PollOptionError? = null,
)
