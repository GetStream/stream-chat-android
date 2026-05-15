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
 * The configuration for the various poll features. It determines if the user can or cannot enable
 * certain poll features.
 *
 * @param multipleVotes Configuration for allowing multiple votes in a poll.
 * @param anonymousPoll Configuration for enabling anonymous polls.
 * @param suggestAnOption Configuration for allowing users to suggest options in a poll.
 * @param allowComments Configuration for adding comments to a poll.
 * @param questionTextLimit Optional character limit for the poll question. Null means no limit.
 * @param optionTextLimit Optional character limit for poll answer options. Null means no limit.
 */
@Parcelize
public data class PollsConfig(
    val multipleVotes: PollFeatureConfig = PollFeatureConfig.Default,
    val anonymousPoll: PollFeatureConfig = PollFeatureConfig.Default,
    val suggestAnOption: PollFeatureConfig = PollFeatureConfig.Default,
    val allowComments: PollFeatureConfig = PollFeatureConfig.Default,
    val questionTextLimit: Int? = null,
    val optionTextLimit: Int? = null,
) : Parcelable {

   init {
        require(multipleVotes.configurable || !multipleVotes.defaultValue) {
            "Invalid PollsConfig: multipleVotes cannot have defaultValue=true while " +
                "configurable=false as the user would be unable to set maxVotesAllowed."
        }
    }

    public companion object {
        /**
         * The default configuration for polls. All features are configurable and disabled by default.
         */
        public val Default: PollsConfig = PollsConfig()
    }
}
