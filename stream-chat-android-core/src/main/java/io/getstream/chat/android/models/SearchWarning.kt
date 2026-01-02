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

package io.getstream.chat.android.models

import androidx.compose.runtime.Immutable

/**
 * Represents a warning related to message search results. For example, if there are more
 * than 500 channels that match the channel filter.
 */
@Immutable
public data class SearchWarning(
    /**
     * Channel CIDs for the searched channels
     */
    val channelSearchCids: List<String>,

    /**
     * Number of channels searched
     */
    val channelSearchCount: Int,

    /**
     * Code corresponding to the warning
     */
    val warningCode: Int,

    /**
     * Description of the warning
     */
    val warningDescription: String,
)
