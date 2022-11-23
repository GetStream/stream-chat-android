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

package io.getstream.chat.android.ui.common.state.messages.list

import io.getstream.chat.android.models.Message

/**
 * Represents the list of actions users can take with ephemeral giphy messages.
 *
 * @param message The ephemeral giphy message.
 */
public sealed class GiphyAction(public val message: Message)

/**
 * Send the selected giphy message to the channel.
 */
public class SendGiphy(message: Message) : GiphyAction(message)

/**
 * Perform the giphy shuffle operation.
 */
public class ShuffleGiphy(message: Message) : GiphyAction(message)

/**
 * Cancel the ephemeral message.
 */
public class CancelGiphy(message: Message) : GiphyAction(message)
