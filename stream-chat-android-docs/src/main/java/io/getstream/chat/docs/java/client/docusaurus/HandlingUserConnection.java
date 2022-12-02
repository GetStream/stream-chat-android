package io.getstream.chat.docs.java.client.docusaurus;

import io.getstream.chat.android.client.ChatClient;
import io.getstream.chat.android.models.User;

/**
 * @see <a href="https://getstream.io/chat/docs/sdk/android/client/guides/channel-list-updates/">Channel List Updates</a>
 */
public class HandlingUserConnection {

    public void connectingAUser() {
        User user = new User();
        user.setId("bender");
        user.setName("Bender");
        user.setImage("https://bit.ly/321RmWb");

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
        User user1 = new User();
        user1.setId("bender");
        user1.setName("Bender");
        user1.setImage("https://bit.ly/321RmWb");

        // Connect the first user
        ChatClient.instance().connectUser(user1, "userToken") // Replace with a real token
                .enqueue((result) -> {
                    if (result.isSuccess()) {
                        // Handle success
                    } else {
                        // Handle error
                    }
                });

        User user2 = new User();
        user2.setId("bender2");
        user2.setName("Bender2");
        user2.setImage("https://bit.ly/321RmWb");

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
