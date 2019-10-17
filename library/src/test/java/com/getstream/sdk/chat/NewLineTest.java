package com.getstream.sdk.chat;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class NewLineTest {
    @org.junit.jupiter.api.Test
    void newlineSimpleTest() {
        String string = "\n\n\n .a. \n\n\n";
        String result = string.replaceAll("^[\r\n]+|[\r\n]+$", "");

        assertEquals(" .a. ", result);
    }
}
