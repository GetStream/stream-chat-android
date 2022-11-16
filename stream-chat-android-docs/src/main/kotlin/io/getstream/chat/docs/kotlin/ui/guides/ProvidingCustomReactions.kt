// ktlint-disable filename

package io.getstream.chat.docs.kotlin.ui.guides

import android.content.Context
import androidx.core.content.ContextCompat
import io.getstream.chat.android.ui.ChatUI
import io.getstream.chat.android.ui.helper.SupportedReactions
import io.getstream.chat.docs.R

/**
 * [Providing Custom Reactions](https://getstream.io/chat/docs/sdk/android/ui/guides/providing-custom-reactions/)
 */
private object ProvidingCustomReactionsSnippet {

    fun providingCustomReactions(context: Context) {
        val reactions = mapOf(
            "thumbs_up" to SupportedReactions.ReactionDrawable(
                inactiveDrawable = ContextCompat.getDrawable(context, R.drawable.ic_thumb_up)!!,
                activeDrawable = ContextCompat.getDrawable(context, R.drawable.ic_thumb_up_selected)!!
            ),
            "thumbs_down" to SupportedReactions.ReactionDrawable(
                inactiveDrawable = ContextCompat.getDrawable(context, R.drawable.ic_thumb_down)!!,
                activeDrawable = ContextCompat.getDrawable(context, R.drawable.ic_thumb_down_selected)!!
            ),
            "mood_good" to SupportedReactions.ReactionDrawable(
                inactiveDrawable = ContextCompat.getDrawable(context, R.drawable.ic_mood_good)!!,
                activeDrawable = ContextCompat.getDrawable(context, R.drawable.ic_mood_good_selected)!!
            ),
            "mood_bad" to SupportedReactions.ReactionDrawable(
                inactiveDrawable = ContextCompat.getDrawable(context, R.drawable.ic_mood_bad)!!,
                activeDrawable = ContextCompat.getDrawable(context, R.drawable.ic_mood_bad_selected)!!
            ),
        )

        ChatUI.supportedReactions = SupportedReactions(context, reactions)
    }
}