package com.getstream.sdk.chat;

import com.getstream.sdk.chat.utils.StringUtility;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ValidMessageTextTest {

    @Test
    void validTextMessage(){
        String text = "Hi, Tommaso!";
        assertTrue(StringUtility.isValidTextMessage(text));
    }

    @Test
    void emptyTextMessage(){
        String text = "";
        assertFalse(StringUtility.isValidTextMessage(text));
    }

    @Test
    void allSpaceTextMessage(){
        String text = "  ";
        assertFalse(StringUtility.isValidTextMessage(text));
    }

    @Test
    void allNewLineTextMessage(){
        String text = "\n \n  \n\n  ";
        assertFalse(StringUtility.isValidTextMessage(text));
    }
}
