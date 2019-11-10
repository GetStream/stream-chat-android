package com.getstream.sdk.chat;

import com.getstream.sdk.chat.rest.Message;
import com.getstream.sdk.chat.rest.User;
import com.getstream.sdk.chat.utils.Constant;
import com.getstream.sdk.chat.utils.StringUtility;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class GetDeletedOrMentionedTextTest {

    @org.junit.jupiter.api.Test
    void getMentionedMarkDownTextTest() {
        String text = "@Steep moon @Broken waterfall hi, there?";
        Message message = new Message();
        message.setText(text);
        List<User>mentionedUsers = new ArrayList<>();

        User user1 = new User("steep-moon-9");
        user1.setName("Steep moon");

        User user2 = new User("broken-waterfall-5");
        user2.setName("Broken waterfall");

        mentionedUsers.add(user1);
        mentionedUsers.add(user2);
        message.setMentionedUsers(mentionedUsers);

        String expectedMessage = "**@Steep moon** **@Broken waterfall** hi, there?";
        assertEquals(expectedMessage, StringUtility.getDeletedOrMentionedText(message));
    }

    @org.junit.jupiter.api.Test
    void newLineTest() {
        String text = "\n\n\n .a. \n\n\n";
        Message message = new Message();
        message.setText(text);
        assertEquals(" .a. ", StringUtility.getDeletedOrMentionedText(message));
    }

    @org.junit.jupiter.api.Test
    void convertVideoLengthTest() {
        long videoLength = 216844;
        assertEquals("60:14:04", StringUtility.convertVideoLength(videoLength));
    }
}
