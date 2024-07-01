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
import androidx.compose.runtime.Stable

/**
 * Indicates the error of the poll while configuring the options.
 *
 * @property message The error message of this option.
 */
@Stable
public sealed interface PollOptionError {
    public val message: String
}

/**
 * This error indicates that this poll creation contains a duplicated option item.
 *
 * @property message The error message of this option.
 */
@Immutable
public data class PollOptionDuplicated(
    override val message: String,
) : PollOptionError

/**
 * This error indicates that this poll creation input (number type) is exceed the maximum value.
 *
 * @property message The error message of this option.
 */
@Immutable
public data class PollOptionNumberExceed(
    override val message: String,
) : PollOptionError
