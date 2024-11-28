/*
 * Copyright (c) 2014-2024 Stream.io Inc. All rights reserved.
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

/**
 * Model holding data for a message moderated by Moderation V2.
 *
 * @property action The action taken by the moderation system.
 * @property originalText The original text of the message.
 * @property textHarms The list of harmful text detected in the message.
 * @property imageHarms The list of harmful images detected in the message.
 * @property blocklistMatched The blocklist matched by the message.
 * @property semanticFilterMatched The semantic filter matched by the message.
 * @property platformCircumvented true/false if the message triggered the platform circumvention model.
 */
public data class Moderation(
    val action: ModerationAction,
    val originalText: String,
    val textHarms: List<String>,
    val imageHarms: List<String>,
    val blocklistMatched: String?,
    val semanticFilterMatched: String?,
    val platformCircumvented: Boolean,
)

/**
 * The moderation action performed over the message.
 *
 * @property value The raw value (name) of the action.
 */
public data class ModerationAction(public val value: String) {

    public companion object {

        /**
         * Action 'bounce' - the message needs to be rephrased and sent again.
         */
        public val bounce: ModerationAction = ModerationAction(value = "bounce")

        /**
         * Action 'remove' - the message was removed by moderation policies.
         */
        public val remove: ModerationAction = ModerationAction(value = "remove")

        /**
         * Action 'flag' - the message was sent for review in the dashboard but it was still published.
         */
        public val flag: ModerationAction = ModerationAction(value = "flag")

        /**
         * Creates a [ModerationAction] from a raw value.
         */
        public fun fromValue(value: String): ModerationAction = when (value) {
            bounce.value -> bounce
            remove.value -> remove
            flag.value -> flag
            else -> ModerationAction(value = value)
        }
    }
}
