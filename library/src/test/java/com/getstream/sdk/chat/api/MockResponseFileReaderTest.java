package com.getstream.sdk.chat.api;

import com.getstream.sdk.chat.api.utils.MockResponseFileReader;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;

/*
 * Created by Anton Bevza on 2019-10-21.
 */
public class MockResponseFileReaderTest {
    @Test
    void readTestFileSuccess() throws IOException {
        MockResponseFileReader reader = new MockResponseFileReader("test.json");
        assertEquals(reader.getContent(), "success");
    }
}
