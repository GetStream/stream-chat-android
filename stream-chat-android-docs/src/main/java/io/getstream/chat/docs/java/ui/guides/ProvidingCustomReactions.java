package io.getstream.chat.docs.java.ui.guides;

import android.content.Context;

import androidx.core.content.ContextCompat;

import java.util.HashMap;
import java.util.Map;

import io.getstream.chat.android.ui.ChatUI;
import io.getstream.chat.android.ui.helper.SupportedReactions;
import io.getstream.chat.docs.R;

/**
 * [Providing Custom Reactions](https://getstream.io/chat/docs/sdk/android/ui/guides/providing-custom-reactions/)
 */
public class ProvidingCustomReactions {

    public void providingCustomReactions(Context context) {
        Map<String, SupportedReactions.ReactionDrawable> reactions = new HashMap<>();
        reactions.put(
                "thumbs_up", new SupportedReactions.ReactionDrawable(
                        ContextCompat.getDrawable(context, R.drawable.ic_thumb_up),
                        ContextCompat.getDrawable(context, R.drawable.ic_thumb_up_selected)
                )
        );

        reactions.put(
                "thumbs_down", new SupportedReactions.ReactionDrawable(
                        ContextCompat.getDrawable(context, R.drawable.ic_thumb_down),
                        ContextCompat.getDrawable(context, R.drawable.ic_thumb_down_selected)
                )
        );

        reactions.put(
                "mood_good", new SupportedReactions.ReactionDrawable(
                        ContextCompat.getDrawable(context, R.drawable.ic_mood_good),
                        ContextCompat.getDrawable(context, R.drawable.ic_mood_good_selected)
                )
        );

        reactions.put(
                "mood_bad", new SupportedReactions.ReactionDrawable(
                        ContextCompat.getDrawable(context, R.drawable.ic_mood_bad),
                        ContextCompat.getDrawable(context, R.drawable.ic_mood_bad_selected)
                )
        );

        ChatUI.setSupportedReactions(new SupportedReactions(context, reactions));
    }
}
