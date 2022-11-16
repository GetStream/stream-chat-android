package io.getstream.chat.docs.java.ui.general;

import androidx.appcompat.app.AppCompatDelegate;

import io.getstream.chat.android.ui.helper.TransformStyle;

/**
 * [Theming](https://getstream.io/chat/docs/sdk/android/ui/general-customization/theming/)
 */
public class Theming {

    public void styleTransformations() {
        TransformStyle.setMessageListItemStyleTransformer(source -> {
            // Customize the style
            return source;
        });
    }

    public void chooseLightDarkTheme() {
        // Force Dark theme
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);

        // Force Light theme
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
    }
}
