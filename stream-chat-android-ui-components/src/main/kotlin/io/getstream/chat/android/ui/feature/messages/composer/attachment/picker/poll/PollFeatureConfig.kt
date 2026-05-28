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

package io.getstream.chat.android.ui.feature.messages.composer.attachment.picker.poll

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Configuration for individual poll entry feature.
 *
 * @param configurable Indicates whether the poll entry is configurable. When false, the UI element is hidden.
 * @param defaultValue Indicates the default value of the poll entry.
 */
@Parcelize
public data class PollFeatureConfig(
    val configurable: Boolean,
    val defaultValue: Boolean,
) : Parcelable {
    public companion object {
        /**
         * The default configuration for a poll entry. It will make it configurable and disabled by default.
         */
        public val Default: PollFeatureConfig = PollFeatureConfig(
            configurable = true,
            defaultValue = false,
        )

        /**
         * The feature should not be supported, so it is not configurable by the user and hidden from the UI.
         */
        public val NotConfigurable: PollFeatureConfig = PollFeatureConfig(
            configurable = false,
            defaultValue = false,
        )
    }
}
