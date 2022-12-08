package io.getstream.chat.docs.java.ui.general;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import io.getstream.chat.android.markdown.MarkdownTextTransformer;
import io.getstream.chat.android.ui.ChatUI;
import io.getstream.chat.android.ui.common.helper.DateFormatter;
import io.getstream.chat.android.ui.feature.messages.composer.attachment.preview.AttachmentPreviewFactoryManager;
import io.getstream.chat.android.ui.feature.messages.list.adapter.viewholder.attachment.AttachmentFactoryManager;
import io.getstream.chat.android.ui.feature.messages.list.adapter.viewholder.attachment.QuotedAttachmentFactoryManager;
import io.getstream.chat.android.ui.font.ChatFonts;
import io.getstream.chat.android.ui.font.TextStyle;
import io.getstream.chat.android.ui.helper.SupportedReactions;
import io.getstream.chat.android.ui.navigation.ChatNavigationHandler;
import io.getstream.chat.android.ui.navigation.ChatNavigator;
import io.getstream.chat.docs.R;

/**
 * [General Configuration](https://getstream.io/chat/docs/sdk/android/ui/general-customization/chatui/)
 */
public class Configuration {

    Context context;

    /**
     * [Custom Reactions](https://getstream.io/chat/docs/sdk/android/ui/general-customization/chatui/#custom-reactions)
     */
    public void customReactions() {
        // Create a drawable for the non-selected reaction option
        Drawable loveDrawable = ContextCompat.getDrawable(context, R.drawable.stream_ui_ic_reaction_love);
        // Create a drawable for the selected reaction option and set a tint to it
        Drawable loveDrawableSelected = ContextCompat.getDrawable(context, R.drawable.stream_ui_ic_reaction_love);
        loveDrawableSelected.setTint(Color.RED);

        // Create a map of reactions
        Map<String, SupportedReactions.ReactionDrawable> supportedReactionsData = new HashMap<>();
        supportedReactionsData.put("love", new SupportedReactions.ReactionDrawable(loveDrawable, loveDrawableSelected));

        // Replace the default reactions with your custom reactions
        ChatUI.setSupportedReactions(new SupportedReactions(context, supportedReactionsData));
    }

    /**
     * [Custom MIME Type Icons](https://getstream.io/chat/docs/sdk/android/ui/general-customization/chatui/#custom-mime-type-icons)
     */
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

    /**
     * [Customizing Image Headers](https://getstream.io/chat/docs/sdk/android/ui/general-customization/chatui/#adding-extra-headers-to-image-requests)
     */
    public void customizingImageHeaders() {
        ChatUI.setImageHeadersProvider(() -> {
            Map<String, String> headers = new HashMap<>();
            headers.put("token", "12345");

            return headers;
        });
    }

    /**
     * [Changing the Default Font](https://getstream.io/chat/docs/sdk/android/ui/general-customization/chatui/#changing-the-default-font)
     */
    private class ChangingTheDefaultFont extends AppCompatActivity {

        /**
         * Holds no significant value, it's just so that
         * we can simply use 'context' inside the tutorials
         * without specifying where it's coming from.
         */
        Context context = getApplicationContext();

        public void changingTheDefaultFont() {
            ChatUI.setFonts(new ChatFonts() {

                // Fetch the font you want to use
                final Typeface font = ResourcesCompat.getFont(context, R.font.stream_roboto_regular);

                @Override
                public void setFont(@NonNull TextStyle textStyle, @NonNull TextView textView) {
                    textView.setTypeface(font, Typeface.BOLD);
                }

                @Override
                public void setFont(@NonNull TextStyle textStyle, @NonNull TextView textView, @NonNull Typeface defaultTypeface) {
                    textView.setTypeface(font, Typeface.BOLD);
                }

                @Nullable
                @Override
                public Typeface getFont(@NonNull TextStyle textStyle) {
                    return font;
                }
            });
        }
    }

    /**
     * [Transforming Message Text](https://getstream.io/chat/docs/sdk/android/ui/general-customization/chatui/#transforming-message-text)
     */
    public void transformingMessageText() {
        ChatUI.setMessageTextTransformer((textView, messageItem) -> {
            textView.setText(messageItem.getMessage().getText().toUpperCase(Locale.ROOT));
        });
    }

    /**
     * [Applying Markdown](https://getstream.io/chat/docs/sdk/android/ui/general-customization/chatui/#markdown)
     */
    public void applyingMarkDown() {
        ChatUI.setMessageTextTransformer(new MarkdownTextTransformer(context));
    }

    /**
     * [Customizing Navigator](https://getstream.io/chat/docs/sdk/android/ui/general-customization/chatui/#navigator)
     */
    public void customizingNavigator() {
        ChatNavigationHandler chatNavigatorHandler = destination -> {
            // Perform a custom action here
            return true;
        };

        ChatUI.setNavigator(new ChatNavigator(chatNavigatorHandler));
    }

    /**
     * [Customizing Channel Name Formatter](https://getstream.io/chat/docs/sdk/android/ui/general-customization/chatui/#customizing-channelnameformatter)
     */
    public void customizingChannelNameFormatter() {
        ChatUI.setChannelNameFormatter((channel, currentUser) -> channel.getName());
    }

    public void customizingMessagePreview() {
        ChatUI.setMessagePreviewFormatter((channel, message, currentUser) -> message.getText());
    }

    /**
     * [Customizing Date Formatter](https://getstream.io/chat/docs/sdk/android/ui/general-customization/chatui/#customizing-dateformatter)
     */
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

    /**
     * [Customizing Attachments](https://getstream.io/chat/docs/sdk/android/v5/ui/general-customization/chatui/#customizing-attachments)
     */
    private class CustomizingAttachments {

        private void customizeMessageList() {
            AttachmentFactoryManager attachmentFactoryManager = new AttachmentFactoryManager(
                    // Set your custom attachment factories here
            );

            ChatUI.setAttachmentFactoryManager(attachmentFactoryManager);
        }

        private void customizeMessageComposerOrInput() {
            AttachmentPreviewFactoryManager attachmentPreviewFactoryManager = new AttachmentPreviewFactoryManager(
                    // Set your custom attachment factories here
            );

            ChatUI.setAttachmentPreviewFactoryManager(attachmentPreviewFactoryManager);
        }

        private void customizeQuotedMessageContent() {
            QuotedAttachmentFactoryManager quotedAttachmentFactoryManager = new QuotedAttachmentFactoryManager(
                    // Set your custom attachment factories here
            );

            ChatUI.setQuotedAttachmentFactoryManager(quotedAttachmentFactoryManager);
        }

    }
}
