package com.getstream.sdk.chat;

import com.getstream.sdk.chat.utils.StringUtility;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class EmptyTextMessageTest {

    @Test
    void validTextMessage(){
        String text = "Hi, Tommaso!";
        assertFalse(StringUtility.isEmptyTextMessage(text));
    }

    @Test
    void emptyTextMessage(){
        String text = "";
        assertTrue(StringUtility.isEmptyTextMessage(text));
    }

    @Test
    void nullTextMessage(){
        String text = null;
        assertTrue(StringUtility.isEmptyTextMessage(text));
    }

    @Test
    void allSpaceTextMessage(){
        String text = "  ";
        assertTrue(StringUtility.isEmptyTextMessage(text));
    }

    @Test
    void allNewLineTextMessage(){
        String text = "\n \n  \n\n  ";
        assertTrue(StringUtility.isEmptyTextMessage(text));
    }
}
