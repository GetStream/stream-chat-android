package io.getstream.chat.android.compose.ui.theme

import android.content.Context
import com.getstream.sdk.chat.utils.DateFormatter

/**
 * Central place for general app configuration options. Use this class to define how some of the features in the app
 * work.
 *
 * @param dateFormatter - The formatter used for timestamps across the app.
 * @param enforceUniqueReactions - Flag that enables or disables unique reactions on messages.
 * */
public class StreamConfiguration(
    public val dateFormatter: DateFormatter,
    public val enforceUniqueReactions: Boolean,
) {

    public companion object {
        /**
         * Provides the default configuration for the app.
         * */
        public fun defaultConfiguration(context: Context): StreamConfiguration = StreamConfiguration(
            DateFormatter.from(context),
            true
        )
    }
}
