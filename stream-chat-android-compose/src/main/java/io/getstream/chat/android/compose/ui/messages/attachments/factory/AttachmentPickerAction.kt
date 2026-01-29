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

package io.getstream.chat.android.compose.ui.messages.attachments.factory

import io.getstream.chat.android.models.Command
import io.getstream.chat.android.models.PollConfig

/**
 * An user action event that occurs inside the attachment picker screen.
 */
public interface AttachmentPickerAction

/**
 * An user action that indicates a back button event.
 */
public data object AttachmentPickerBack : AttachmentPickerAction

/**
 * An user action that indicates that a poll is ready to be created.
 *
 * @param pollConfig The [PollConfig] object holding the data required to create the new poll.
 */
public data class AttachmentPickerPollCreation(public val pollConfig: PollConfig) : AttachmentPickerAction

/**
 * An user action that indicates an intention to create a poll.
 */
public data object AttachmentPickerCreatePollClick : AttachmentPickerAction

/**
 * An user action that indicates a command was clicked.
 */
public data class AttachmentPickerCommandClickClick(public val command: Command) : AttachmentPickerAction
