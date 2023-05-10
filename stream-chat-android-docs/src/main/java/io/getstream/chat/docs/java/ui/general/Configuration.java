package io.getstream.chat.docs.java.ui.general;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.getstream.sdk.chat.navigation.ChatNavigationHandler;
import com.getstream.sdk.chat.utils.DateFormatter;

import org.threeten.bp.LocalDateTime;
import org.threeten.bp.LocalTime;
import org.threeten.bp.format.DateTimeFormatter;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import io.getstream.chat.android.client.models.User;
import io.getstream.chat.android.markdown.MarkdownTextTransformer;
import io.getstream.chat.android.ui.ChatUI;
import io.getstream.chat.android.ui.SupportedReactions;
import io.getstream.chat.android.ui.avatar.AvatarBitmapFactory;
import io.getstream.chat.android.ui.avatar.AvatarStyle;
import io.getstream.chat.android.ui.common.navigation.ChatNavigator;
import io.getstream.chat.android.ui.common.style.ChatFonts;
import io.getstream.chat.android.ui.common.style.TextStyle;
import io.getstream.chat.docs.R;
import kotlin.coroutines.Continuation;

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

    public void defaultAvatar() {
        new AvatarBitmapFactory(context) {
            @Nullable
            @Override
            public Object createUserBitmap(@NonNull User user, @NonNull AvatarStyle style, int avatarSize, @NonNull Continuation<? super Bitmap> $completion) {
                return createDefaultUserBitmap(user, style, avatarSize, $completion);
            }
        };
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
        ChatUI.setDateFormatter(new DateFormatter() {
            private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yy MM dd");
            private DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm");

            @NonNull
            @Override
            public String formatDate(@Nullable LocalDateTime localDateTime) {
                if (localDateTime == null) {
                    return "";
                }
                return dateFormatter.format(localDateTime);
            }

            @NonNull
            @Override
            public String formatTime(@Nullable LocalTime localTime) {
                if (localTime == null) {
                    return "";
                }
                return dateTimeFormatter.format(localTime);
            }

            @NonNull
            @Override
            public String formatTime(@Nullable LocalDateTime localDateTime) {
                if (localDateTime == null) {
                    return "";
                }
                return formatTime(localDateTime.toLocalTime());
            }
        });
    }
}
