package io.getstream.chat.docs.java.client.docusaurus;

import io.getstream.chat.android.client.ChatClient;
import io.getstream.chat.android.models.User;

/**
 * @see <a href="https://getstream.io/chat/docs/sdk/android/client/guides/channel-list-updates/">Channel List Updates</a>
 */
public class HandlingUserConnection {

    public void connectingAUser() {
        User user = new User.Builder()
                .withId("bender")
                .withName("Bender")
                .withImage("https://bit.ly/321RmWb")
                .build();

        // Check if the user is not already set
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

    public void disconnectTheUser() {
        boolean flushPersistence = false;
        ChatClient.instance().disconnect(flushPersistence).enqueue((result) -> {
            if (result.isSuccess()) {
                // Handle success
            } else {
                // Handle error
            }
        });
    }

    public void switchTheUser() {
        User user1 = new User.Builder()
                .withId("bender")
                .withName("Bender")
                .withImage("https://bit.ly/321RmWb")
                .build();

        // Connect the first user
        ChatClient.instance().connectUser(user1, "userToken") // Replace with a real token
                .enqueue((result) -> {
                    if (result.isSuccess()) {
                        // Handle success
                    } else {
                        // Handle error
                    }
                });

        User user2 = new User.Builder()
                .withId("bender2")
                .withName("Bender2")
                .withImage("https://bit.ly/321RmWb")
                .build();
        ChatClient.instance().switchUser(user2, "userToken") // Replace with a real token
                .enqueue((result) -> {
                    if (result.isSuccess()) {
                        // Handle success
                    } else {
                        // Handle error
                    }
                });
    }
}
