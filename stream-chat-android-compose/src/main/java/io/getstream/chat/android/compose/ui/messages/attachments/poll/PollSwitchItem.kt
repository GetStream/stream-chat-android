/*
 * Copyright (c) 2014-2024 Stream.io Inc. All rights reserved.
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
import androidx.compose.ui.text.input.KeyboardType
import java.util.UUID

/**
 * The option switch item for creating a poll.
 *
 * @property title The title of this poll item.
 * @property enabled Indicates if this switch is enabled or not.
 * @property key The key that identifies this poll item.
 * @property pollOptionError Indicates this option has an error.
 */
@Immutable
public data class PollSwitchItem(
    public val title: String,
    public val enabled: Boolean,
    public val key: String = UUID.randomUUID().toString(),
    public val pollSwitchInput: PollSwitchInput? = null,
    public val pollOptionError: PollOptionError? = null,
)

/**
 * The input information that will be used to create a poll switch item.
 *
 * @property value The default value of the switch.
 * @property description The description of the input in the switch (shown as hint/contentDescription).
 * @property maxValue The maximum vale of the switch. Normally, you can use the limit of the decimal format of the [value].
 * @property keyboardType The type of the input of the switch and decide the keyboard type of the input.
 */
public data class PollSwitchInput(
    public var value: Any,
    public val description: String = "",
    public val maxValue: Any? = null,
    public val keyboardType: KeyboardType = KeyboardType.Text,
)
