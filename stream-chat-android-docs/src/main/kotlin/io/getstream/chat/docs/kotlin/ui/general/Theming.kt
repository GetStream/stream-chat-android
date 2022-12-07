package io.getstream.chat.docs.kotlin.ui.general

import android.graphics.Color
import androidx.appcompat.app.AppCompatDelegate
import io.getstream.chat.android.ui.helper.StyleTransformer
import io.getstream.chat.android.ui.helper.TransformStyle

/**
 * [Theming](https://getstream.io/chat/docs/sdk/android/ui/general-customization/theming/)
 */
class Theming {

    fun styleTransformations() {
        TransformStyle.messageListItemStyleTransformer = StyleTransformer { defaultViewStyle ->
            defaultViewStyle.copy(
                messageBackgroundColorMine = Color.parseColor("#0277BD"),
                messageBackgroundColorTheirs = Color.parseColor("#2E7D32"),
                textStyleMine = defaultViewStyle.textStyleMine.copy(color = Color.WHITE),
                textStyleTheirs = defaultViewStyle.textStyleTheirs.copy(color = Color.WHITE),
            )
        }
    }

    fun chooseLightDarkTheme() {
        // Force Dark theme
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)

        // Force Light theme
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
    }
}
