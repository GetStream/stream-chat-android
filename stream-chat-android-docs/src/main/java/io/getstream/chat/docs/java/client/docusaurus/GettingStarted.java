package io.getstream.chat.docs.java.client.docusaurus;

import android.app.Application;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import io.getstream.chat.android.client.ChatClient;
import io.getstream.chat.android.client.models.User;
import io.getstream.chat.android.offline.plugin.factory.StreamOfflinePluginFactory;

/**
 * @see <a href="https://getstream.io/chat/docs/sdk/android/basics/getting-started/#getting-started">Getting Started</a>
 */
public class GettingStarted {

    public void creatingAChatClient() {
        class App extends Application {
            @Override
            public void onCreate() {
                super.onCreate();
                ChatClient chatClient = new ChatClient.Builder("apiKey", getApplicationContext()).build();
            }
        }

        class MainActivity extends AppCompatActivity {
            @Override
            protected void onCreate(@Nullable Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
                ChatClient chatClient = ChatClient.instance();  // Returns the singleton instance
            }
        }
    }

    public void addingTheOfflinePlugin(String apiKey, Context context) {
        StreamOfflinePluginFactory offlinePluginFactory = new StreamOfflinePluginFactory(context);
        new ChatClient.Builder("apiKey", context).withPlugins(offlinePluginFactory).build();
    }

    public void connectingAUser() {
        User user = new User();
        user.setId("bender");
        user.setName("Bender");
        user.setImage("https://bit.ly/321RmWb");

        ChatClient.instance().connectUser(user, "userToken")  // Replace with a real token
                .enqueue((result) -> {
                    if (result.isSuccess()) {
                        // Handle success
                    } else {
                        // Handle error
                    }
                });
    }
}
