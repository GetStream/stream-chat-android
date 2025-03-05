/*
 * Copyright (c) 2014-2025 Stream.io Inc. All rights reserved.
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

package io.getstream.chat.android.compose.ui.chats

import android.content.Context
import io.getstream.chat.android.compose.viewmodel.messages.MessagesViewModelFactory

/**
 * A lambda function that provides a [MessagesViewModelFactory] for managing messages within a selected channel.
 */
public typealias MessagesViewModelFactoryProvider =
    (context: Context, selection: MessageSelection) -> MessagesViewModelFactory?
