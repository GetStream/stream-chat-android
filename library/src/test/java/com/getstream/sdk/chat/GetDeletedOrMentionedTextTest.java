package com.getstream.sdk.chat;

import com.getstream.sdk.chat.rest.Message;
import com.getstream.sdk.chat.rest.User;
import com.getstream.sdk.chat.utils.Constant;
import com.getstream.sdk.chat.utils.StringUtility;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class GetDeletedOrMentionedTextTest {

    @org.junit.jupiter.api.Test
    void getDeletedMarkDownTextTest() {
        Message message = new Message();
        message.setDeletedAt(new Date());
        message.setText("Test Message");
        String expectedMessage = "_" + Constant.MESSAGE_DELETED + "_";
        assertEquals(expectedMessage, StringUtility.getDeletedOrMentionedText(message));
    }

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

    @org.junit.jupiter.api.Test
    void convertFileSizeTest() {
        long fileSize = 999;
        assertEquals("999 B", StringUtility.convertFileSizeByteCount(fileSize));
        fileSize = 110592;
        assertEquals("110.59 KB", StringUtility.convertFileSizeByteCount(fileSize));
        fileSize = 452984832;
        assertEquals("452.98 MB", StringUtility.convertFileSizeByteCount(fileSize));
        fileSize = 900000;
        assertEquals("900 KB", StringUtility.convertFileSizeByteCount(fileSize));
    }
}
