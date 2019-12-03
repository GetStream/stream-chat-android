package com.getstream.sdk.chat;

import com.getstream.sdk.chat.rest.Message;
import com.getstream.sdk.chat.rest.User;
import com.getstream.sdk.chat.utils.StringUtility;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class GetDeletedOrMentionedTextTest {

    @Test
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
        assertEquals("60:14:04", StringUtility.convertVideoLength(videoLength));
    }

    @Test
    void convertFileSizeTest() {
        long fileSize = 999;
        assertEquals("999 B", StringUtility.convertFileSizeByteCount(fileSize));
        fileSize = 110592;
        assertEquals("110.59 KB", StringUtility.convertFileSizeByteCount(fileSize));
        fileSize = 452984832;
        assertEquals("452.98 MB", StringUtility.convertFileSizeByteCount(fileSize));
        fileSize = 900000;
        assertEquals("900 KB", StringUtility.convertFileSizeByteCount(fileSize));
        fileSize = 0;
        assertEquals("0 B", StringUtility.convertFileSizeByteCount(fileSize));
        fileSize = -100;
        assertEquals("0 B", StringUtility.convertFileSizeByteCount(fileSize));
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

    @Test
    void containMarkdownTextText() {
        String text = "TestMarkDown";
        assertFalse(StringUtility.containsMarkdown(text));
        text = "*TestMarkDown";
        assertFalse(StringUtility.containsMarkdown(text));
        text = "**TestMarkDown";
        assertFalse(StringUtility.containsMarkdown(text));
        text = "#TestMarkDown";
        assertFalse(StringUtility.containsMarkdown(text));
        text = "##TestMarkDown";
        assertFalse(StringUtility.containsMarkdown(text));
        // Emphasis
        text = "*TestMarkDown*";
        assertTrue(StringUtility.containsMarkdown(text));
        text = "_TestMarkDown_";
        assertTrue(StringUtility.containsMarkdown(text));
        // Strong emphasis
        assertTrue(StringUtility.containsMarkdown(text));
        text = "**TestMarkDown**";
        assertTrue(StringUtility.containsMarkdown(text));
        text = "__TestMarkDown__";
        assertTrue(StringUtility.containsMarkdown(text));
        // Strike-through
        text = "~~TestMarkDown~~";
        assertTrue(StringUtility.containsMarkdown(text));
        // Headers
        text = "# TestMarkDown";
        assertTrue(StringUtility.containsMarkdown(text));
        text = "## TestMarkDown";
        assertTrue(StringUtility.containsMarkdown(text));
        text = "### TestMarkDown";
        assertTrue(StringUtility.containsMarkdown(text));
        // Links
        text = "[GetStream](https://getstream.io/)";
        assertTrue(StringUtility.containsMarkdown(text));
        // Images
        text = "![Minion](https://octodex.github.com/images/minion.png)";
        assertTrue(StringUtility.containsMarkdown(text));
        // Thematic break
        text = "___  ";
        assertTrue(StringUtility.containsMarkdown(text));
        text = "---  ";
        assertTrue(StringUtility.containsMarkdown(text));
        text = "***  ";
        assertTrue(StringUtility.containsMarkdown(text));
        // Code
        text = "`code`";
        assertTrue(StringUtility.containsMarkdown(text));
        text = "// Some comments\n" +
                "line 1 of code\n" +
                "line 2 of code\n" +
                "line 3 of code";
        assertTrue(StringUtility.containsMarkdown(text));

        text = "```\n" +
                "Sample text here...\n" +
                "```";
        assertTrue(StringUtility.containsMarkdown(text));
        text = "``` js\n" +
                "var foo = function (bar) {\n" +
                "  return bar++;\n" +
                "};\n" +
                "\n" +
                "console.log(foo(5));\n" +
                "```";
        assertTrue(StringUtility.containsMarkdown(text));
        // Blockquotes

    }
}
