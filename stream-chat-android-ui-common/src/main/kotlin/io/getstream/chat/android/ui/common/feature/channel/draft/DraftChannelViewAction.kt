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

package io.getstream.chat.android.ui.common.feature.channel.draft

import io.getstream.chat.android.core.ExperimentalStreamChatApi

/**
 * Represents actions that can be performed in the draft channel view.
 */
@ExperimentalStreamChatApi
public sealed interface DraftChannelViewAction {

    /**
     * Represents the action of sending a message in the draft channel.
     */
    public data object MessageSent : DraftChannelViewAction
}
