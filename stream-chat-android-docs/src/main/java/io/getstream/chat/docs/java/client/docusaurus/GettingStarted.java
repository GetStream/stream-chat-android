package io.getstream.chat.docs.java.client.docusaurus;

import android.app.Application;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import io.getstream.chat.android.client.ChatClient;
import io.getstream.chat.android.models.User;

/**
 * @see <a href="https://getstream.io/chat/docs/sdk/android/client/overview/">Getting Started</a>
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

    public void addingAPlugin(String apiKey, Context context) {
        new ChatClient.Builder(apiKey, context)
                .withPlugins(
                        //Add the desired plugin factories here
                )
                .build();
    }

    public void connectingAUser() {
        User user = new User.Builder()
                .withId("bender")
                .withName("Bender")
                .withImage("https://bit.ly/321RmWb")
                .build();

        // Connect the user only if they aren't already connected
        if (ChatClient.instance().getCurrentUser() == null) {

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
}
