package io.getstream.chat.docs.java.client.docusaurus;

import android.content.Context;

import androidx.annotation.NonNull;

import io.getstream.chat.android.client.ChatClient;
import io.getstream.chat.android.client.debugger.ChatClientDebugger;
import io.getstream.chat.android.client.debugger.SendMessageDebugger;
import io.getstream.chat.android.client.models.Message;
import io.getstream.chat.android.client.utils.Result;

/**
 * @see <a href="https://getstream.io/chat/docs/sdk/android/basics/debugging/">Debugging</a>
 */
public class Debugging {

    public void addClientDebugger(@NonNull Context context) {
        final ChatClient chatClient = new ChatClient.Builder("apiKey", context)
                .clientDebugger(new ChatClientDebugger() {
                    @NonNull
                    @Override
                    public SendMessageDebugger debugSendMessage(
                            @NonNull String channelType,
                            @NonNull String channelId,
                            @NonNull Message message,
                            boolean isRetrying
                    ) {
                        return Debugging.this.debugSendMessage(
                                channelType,
                                channelId,
                                message,
                                isRetrying
                        );
                    }
                })
                .build();
    }

    /**
     * @see <a href="https://getstream.io/chat/docs/sdk/android/basics/debugging/#debug-message-sending">Debug Message Sending</a>
     */
    private SendMessageDebugger debugSendMessage(
            @NonNull String channelType,
            @NonNull String channelId,
            @NonNull Message message,
            boolean isRetrying
    ) {
        return new SendMessageDebugger() {

            @Override
            public void onStart(@NonNull Message message) {
                // handle onStart
            }

            @Override
            public void onInterceptionStart(@NonNull Message message) {
                // handle onInterceptionStart
            }

            @Override
            public void onInterceptionUpdate(@NonNull Message message) {
                // handle onInterceptionUpdate
            }

            @Override
            public void onInterceptionStop(@NonNull Result<Message> result) {
                // handle onInterceptionStop
            }


            @Override
            public void onSendStart(@NonNull Message message) {
                // handle onSendStart
            }

            @Override
            public void onSendStop(@NonNull Result<Message> result) {
                // handle onSendStop
            }


            @Override
            public void onStop(@NonNull Result<Message> result) {
                // handle onStop
            }

        };
    }

}
