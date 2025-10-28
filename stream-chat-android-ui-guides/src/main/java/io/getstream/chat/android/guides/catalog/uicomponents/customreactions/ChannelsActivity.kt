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

package io.getstream.chat.android.guides.catalog.uicomponents.customreactions

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.core.content.ContextCompat
import io.getstream.chat.android.guides.R
import io.getstream.chat.android.guides.cleanup
import io.getstream.chat.android.ui.ChatUI
import io.getstream.chat.android.ui.feature.channels.ChannelListActivity
import io.getstream.chat.android.ui.helper.SupportedReactions

/**
 * An Activity representing a self-contained channel list screen with custom attachment factories.
 */
class ChannelsActivity : ChannelListActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val reactions = mapOf(
            "thumbs_up" to SupportedReactions.ReactionDrawable(
                inactiveDrawable = ContextCompat.getDrawable(this, R.drawable.ic_thumb_up)!!,
                activeDrawable = ContextCompat.getDrawable(this, R.drawable.ic_thumb_up_selected)!!,
            ),
            "thumbs_down" to SupportedReactions.ReactionDrawable(
                inactiveDrawable = ContextCompat.getDrawable(this, R.drawable.ic_thumb_down)!!,
                activeDrawable = ContextCompat.getDrawable(this, R.drawable.ic_thumb_down_selected)!!,
            ),
            "mood_good" to SupportedReactions.ReactionDrawable(
                inactiveDrawable = ContextCompat.getDrawable(this, R.drawable.ic_mood_good)!!,
                activeDrawable = ContextCompat.getDrawable(this, R.drawable.ic_mood_good_selected)!!,
            ),
            "mood_bad" to SupportedReactions.ReactionDrawable(
                inactiveDrawable = ContextCompat.getDrawable(this, R.drawable.ic_mood_bad)!!,
                activeDrawable = ContextCompat.getDrawable(this, R.drawable.ic_mood_bad_selected)!!,
            ),
        )
        ChatUI.supportedReactions = SupportedReactions(this, reactions)
    }

    override fun onDestroy() {
        super.onDestroy()
        ChatUI.cleanup(applicationContext)
    }

    companion object {
        /**
         * Creates an [Intent] to start [ChannelsActivity].
         *
         * @param context The context used to create the intent.
         * @return The [Intent] to start [ChannelsActivity].
         */
        fun createIntent(context: Context): Intent = Intent(context, ChannelsActivity::class.java)
    }
}
