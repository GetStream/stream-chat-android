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

package io.getstream.chat.android.compose.state.messages

/**
 * Determines if the list is loading data to prepare for scroll to a certain part of the list or is currently scrolling
 * to it.
 */
public sealed class ScrollToPositionState

/**
 * State when the focused message is not in the current list and that the data is being loaded form the API.
 */
public object LoadFocusedMessageData : ScrollToPositionState()

/**
 * State when the focused message is inside the list and it should scroll to the focused message.
 */
public object ScrollToFocusedMessage : ScrollToPositionState()

/**
 * State when the loaded data does not contain the newest messages and it is loading data for it.
 */
public object LoadNewestMessages : ScrollToPositionState()

/**
 * State when the newest messages inside the list and should scroll to them.
 */
public object ScrollToNewestMessages : ScrollToPositionState()

/**
 * State when we do not do any automatic scrolling.
 */
public object Idle : ScrollToPositionState()
