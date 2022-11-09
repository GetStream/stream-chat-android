package io.getstream.chat.docs.java.ui.general;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import io.getstream.chat.android.markdown.MarkdownTextTransformer;
import io.getstream.chat.android.ui.ChatUI;
import io.getstream.chat.android.ui.helper.SupportedReactions;
import io.getstream.chat.android.ui.navigation.ChatNavigator;
import io.getstream.chat.android.ui.font.ChatFonts;
import io.getstream.chat.android.ui.font.TextStyle;
import io.getstream.chat.android.ui.common.helper.DateFormatter;
import io.getstream.chat.android.ui.navigation.ChatNavigationHandler;
import io.getstream.chat.docs.R;

/**
 * [General Configuration](https://getstream.io/chat/docs/sdk/android/ui/general-customization/chatui/)
 */
public class Configuration {

    Context context;

    public void customReactions() {
        Drawable loveDrawable = ContextCompat.getDrawable(context, R.drawable.stream_ui_ic_reaction_love);
        Drawable loveDrawableSelected = ContextCompat.getDrawable(context, R.drawable.stream_ui_ic_reaction_love);
        loveDrawableSelected.setTint(Color.RED);
        Map<String, SupportedReactions.ReactionDrawable> supportedReactionsData = new HashMap<>();
        supportedReactionsData.put("love", new SupportedReactions.ReactionDrawable(loveDrawable, loveDrawableSelected));
        ChatUI.setSupportedReactions(new SupportedReactions(context, supportedReactionsData));
    }

    public void customMimeTypeIcons() {
        ChatUI.setMimeTypeIconProvider(mimeType -> {
            if (mimeType == null) {
                // Generic icon for missing MIME type
                return R.drawable.stream_ui_ic_file;
            } else if (mimeType.equals("application/vnd.ms-excel")) {
                // Special icon for XLS files
                return R.drawable.stream_ui_ic_file_xls;
            } else if (mimeType.contains("audio")) {
                // Generic icon for audio files
                return R.drawable.stream_ui_ic_file_mp3;
            } else if (mimeType.contains("video")) {
                // Generic icon for video files
                return R.drawable.stream_ui_ic_file_mov;
            } else {
                // Generic icon for other files
                return R.drawable.stream_ui_ic_file;
            }
        });
    }

    public void customizingImageHeaders() {
        ChatUI.setImageHeadersProvider(() -> {
            Map<String, String> headers = new HashMap<>();
            headers.put("token", "12345");

            return headers;
        });
    }

    public void changingTheDefaultFont() {
        ChatUI.setFonts(new ChatFonts() {
            @Override
            public void setFont(@NonNull TextStyle textStyle, @NonNull TextView textView) {
                textStyle.apply(textView);
            }

            @Override
            public void setFont(@NonNull TextStyle textStyle, @NonNull TextView textView, @NonNull Typeface defaultTypeface) {
                textStyle.apply(textView);
            }

            @Nullable
            @Override
            public Typeface getFont(@NonNull TextStyle textStyle) {
                return textStyle.getFont();
            }
        });
    }

    public void transformingMessageText() {
        ChatUI.setMessageTextTransformer((textView, messageItem) -> {
            textView.setText(messageItem.getMessage().getText().toUpperCase(Locale.ROOT));
        });
    }

    public void applyingMarkDown() {
        ChatUI.setMessageTextTransformer(new MarkdownTextTransformer(context));
    }

    public void customizingNavigator() {
        ChatNavigationHandler chatNavigatorHandler = destination -> {
            // Perform some custom action here
            return true;
        };

        ChatUI.setNavigator(new ChatNavigator(chatNavigatorHandler));
    }

    public void customizingChannelNameFormatter() {
        ChatUI.setChannelNameFormatter((channel, currentUser) -> channel.getName());
    }

    public void customizingMessagePreview() {
        ChatUI.setMessagePreviewFormatter((channel, message, currentUser) -> message.getText());
    }

    public void customizingDateFormatter() {
        ChatUI.setDateFormatter(
                new DateFormatter() {
                    private final DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
                    private final DateFormat timeFormat = new SimpleDateFormat("HH:mm");

                    public String formatDate(Date date) {
                        // Provide a way to format Date
                        return dateFormat.format(date);
                    }

                    public String formatTime(Date date) {
                        // Provide a way to format Time
                        return timeFormat.format(date);
                    }
                }
        );
    }
}
