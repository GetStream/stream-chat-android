package com.getstream.sdk.chat;

import com.getstream.sdk.chat.utils.MediaStringUtil;
import com.getstream.sdk.chat.utils.StringUtility;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import io.getstream.chat.android.client.models.Message;
import io.getstream.chat.android.client.models.User;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class GetDeletedOrMentionedTextTest {

    @Test
    void getMentionedMarkDownTextTest() {
        String text = "@Steep moon @Broken waterfall hi, there?";
        Message message = new Message();
        message.setText(text);
        List<User>mentionedUsers = new ArrayList<>();

        User user1 = new User();
        user1.setId("steep-moon-9");
        user1.getExtraData().put("name", "Steep moon");

        User user2 = new User();
        user2.setId("broken-waterfall-5");
        user2.getExtraData().put("name", "Broken waterfall");

        mentionedUsers.add(user1);
        mentionedUsers.add(user2);
        message.getMentionedUsers().addAll(mentionedUsers);

        String expectedMessage = "**@Steep moon** **@Broken waterfall** hi, there?";
        assertEquals(expectedMessage, StringUtility.getDeletedOrMentionedText(message));
    }

    @Test
    void getMentionedMarkDownTextWithoutPrefixWhitespaceTest() {
        String text = "HI@Steep moonThere";
        Message message = new Message();
        message.setText(text);
        List<User>mentionedUsers = new ArrayList<>();

        User user = new User();
        user.setId("steep-moon-9");
        user.getExtraData().put("name", "Steep moon");
        mentionedUsers.add(user);
        message.getMentionedUsers().addAll(mentionedUsers);

        String expectedMessage = "HI **@Steep moon**There";
        assertEquals(expectedMessage, StringUtility.getDeletedOrMentionedText(message));
    }

    @Test
    void newLineTest() {
        String text = "\n\n\n .a. \n\n\n";
        Message message = new Message();
        message.setText(text);
        assertEquals(" .a. ", StringUtility.getDeletedOrMentionedText(message));
    }

    @Test
    void convertVideoLengthTest() {
        long videoLength = 216844;
        assertEquals("60:14:04", MediaStringUtil.convertVideoLength(videoLength));
    }

    @Test
    void convertFileSizeTest() {
        long fileSize = 999;
        assertEquals("999 B", MediaStringUtil.convertFileSizeByteCount(fileSize));
        fileSize = 110592;
        assertEquals("110.59 KB", MediaStringUtil.convertFileSizeByteCount(fileSize));
        fileSize = 452984832;
        assertEquals("452.98 MB", MediaStringUtil.convertFileSizeByteCount(fileSize));
        fileSize = 900000;
        assertEquals("900 KB", MediaStringUtil.convertFileSizeByteCount(fileSize));
        fileSize = 0;
        assertEquals("0 B", MediaStringUtil.convertFileSizeByteCount(fileSize));
        fileSize = -100;
        assertEquals("0 B", MediaStringUtil.convertFileSizeByteCount(fileSize));
    }

    @Test
    void convertMentionTextTest() {
        String text;
        String userName = "Adrian";

        text = "@";
        assertEquals("@Adrian", StringUtility.convertMentionedText(text, userName));
        text = "@A";
        assertEquals("@Adrian", StringUtility.convertMentionedText(text, userName));
        text = "This@";
        assertEquals("This@Adrian", StringUtility.convertMentionedText(text, userName));
        text = "This a @A";
        assertEquals("This a @Adrian", StringUtility.convertMentionedText(text, userName));
        text = "@@@This a @";
        assertEquals("@@@This a @Adrian", StringUtility.convertMentionedText(text, userName));
        text = "@@@This a @@@@";
        assertEquals("@@@This a @@@@Adrian", StringUtility.convertMentionedText(text, userName));
        text = "@@@Adrian a @@This is @A";
        assertEquals("@@@Adrian a @@This is @Adrian", StringUtility.convertMentionedText(text, userName));
    }
}
