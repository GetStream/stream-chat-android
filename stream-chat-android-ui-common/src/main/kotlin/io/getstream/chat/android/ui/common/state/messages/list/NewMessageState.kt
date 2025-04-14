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

/**
 * Represents the state when a new message arrives to the channel.
 */
public sealed class NewMessageState

/**
 * If the message is our own (we sent it), we scroll to the bottom of the list.
 */
public data class MyOwn(val ts: Long?) : NewMessageState()

/**
 * If the message is someone else's (we didn't send it), we show a "New message" bubble.
 */
public data class Other(val ts: Long?) : NewMessageState()

/**
 * If it is a typing message, we scroll to the bottom of the list.
 */
public data object Typing : NewMessageState()
