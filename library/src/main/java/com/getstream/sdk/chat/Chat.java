package com.getstream.sdk.chat;

import android.content.Context;

import com.getstream.sdk.chat.enums.OnlineStatus;
import com.getstream.sdk.chat.navigation.ChatNavigationHandler;
import com.getstream.sdk.chat.navigation.ChatNavigator;
import com.getstream.sdk.chat.style.ChatFonts;
import com.getstream.sdk.chat.style.ChatFontsImpl;
import com.getstream.sdk.chat.style.ChatStyle;
import com.getstream.sdk.chat.utils.strings.ChatStrings;
import com.getstream.sdk.chat.utils.strings.ChatStringsImpl;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import io.getstream.chat.android.client.ChatClient;
import io.getstream.chat.android.client.logger.ChatLogLevel;
import io.getstream.chat.android.client.logger.ChatLoggerHandler;
import io.getstream.chat.android.client.models.User;
import io.getstream.chat.android.client.notifications.options.ChatNotificationConfig;

public interface Chat {

    static Chat getInstance() {
        return ChatImpl.instance;
    }

    ChatClient getClient();

    ChatNavigator getNavigator();

    ChatStrings getStrings();

    UrlSigner urlSigner();

    ChatFonts getFonts();

    LiveData<OnlineStatus> getOnlineStatus();

    LiveData<Number> getUnreadMessages();

    LiveData<Number> getUnreadChannels();

    LiveData<User> getCurrentUser();

    String getVersion();

    class Builder {

        private final Context context;
        private ChatClient.Builder clientBuilder;
        private ChatNavigationHandler navigationHandler;
        private ChatStyle style;
        private UrlSigner urlSigner;

        public Builder(String apiKey, Context context) {
            this.context = context;
            clientBuilder = new ChatClient.Builder(apiKey, context);
        }

        public Builder logLevel(@NonNull ChatLogLevel level) {
            clientBuilder.logLevel(level);
            return this;
        }

        public Builder loggerHandler(@NonNull ChatLoggerHandler handler) {
            clientBuilder.loggerHandler(handler);
            return this;
        }

        public Builder apiTimeout(long timeout) {
            clientBuilder.baseTimeout(timeout);
            return this;
        }

        public Builder cdnTimeout(long timeout) {
            clientBuilder.cdnTimeout(timeout);
            return this;
        }

        public Builder apiEndpoint(@NonNull String url) {
            clientBuilder.baseUrl(url);
            return this;
        }

        public Builder cdnEndpoint(@NonNull String url) {
            clientBuilder.cdnUrl(url);
            return this;
        }

        public Builder notifications(@NonNull ChatNotificationConfig config) {
            clientBuilder.notifications(config);
            return this;
        }

        public Builder navigationHandler(@NonNull ChatNavigationHandler navigationHandler) {
            this.navigationHandler = navigationHandler;
            return this;
        }

        public Builder urlSigner(UrlSigner urlSigner) {
            this.urlSigner = urlSigner;
            return this;
        }

        public Builder style(@NonNull ChatStyle style) {
            this.style = style;
            return this;
        }

        public Chat build() {

            if (style == null) style = new ChatStyle.Builder().build();
            if (urlSigner == null) urlSigner = new UrlSigner.DefaultUrlSigner();

            ChatClient client = clientBuilder.build();
            ChatImpl chat = new ChatImpl(
                    client,
                    new ChatFontsImpl(style, context),
                    new ChatStringsImpl(context),
                    navigationHandler,
                    urlSigner
            );

            chat.init();

            ChatImpl.instance = chat;

            return chat;
        }
    }


}
