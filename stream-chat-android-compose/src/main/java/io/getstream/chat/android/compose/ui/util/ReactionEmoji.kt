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

package io.getstream.chat.android.compose.ui.util

import io.getstream.chat.android.ui.common.helper.ReactionDefaults

internal object ReactionEmoji {
    /**
     * The default set of named reactions for pickers/menus.
     */
    // TODO [G.] if we move to full emojis, what about these cases that are already in prod? It's possible some messages
    //  will have both "like" and the thumbs up emoji so they'll be treated as different reactions.
    val defaultReactions: Map<String, String> = mapOf(
        ReactionDefaults.THUMBS_UP to "\uD83D\uDC4D",
        ReactionDefaults.LOVE to "❤\uFE0F",
        ReactionDefaults.LOL to "\uD83D\uDE02",
        ReactionDefaults.WUT to "\uD83D\uDE2E",
        ReactionDefaults.THUMBS_DOWN to "\uD83D\uDC4E",
    )
}
