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

package io.getstream.chat.android.ui.common.state.messages.list

/**
 * Represents the message focus state, in case the user jumps to a message.
 */
public sealed class MessageFocusState

/**
 * Represents the state when the message is currently being focused.
 */
public object MessageFocused : MessageFocusState() { override fun toString(): String = "MessageFocused" }

/**
 * Represents the state when we've removed the focus from the message.
 */
public object MessageFocusRemoved : MessageFocusState() { override fun toString(): String = "MessageFocusRemoved" }
