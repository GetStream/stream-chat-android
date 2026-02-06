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

package io.getstream.chat.android.ui.common.utils

import io.getstream.chat.android.core.internal.InternalStreamChatApi

/**
 * Constants related to polls.
 */
@InternalStreamChatApi
public object PollsConstants {
    /**
     * The minimum number of multiple answers allowed in a poll.
     */
    public const val MIN_NUMBER_OF_MULTIPLE_ANSWERS: Int = 2

    /**
     * The maximum number of multiple answers allowed in a poll.
     */
    public const val MAX_NUMBER_OF_MULTIPLE_ANSWERS: Int = 10

    /**
     * The maximum number of visible options in a poll message.
     */
    public const val MAX_NUMBER_OF_VISIBLE_OPTIONS: Int = 10
}
