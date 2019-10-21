package com.getstream.sdk.chat.api.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/*
 * Created by Anton Bevza on 2019-10-21.
 */
public class MockResponseFileReader {

    private String content;

    public MockResponseFileReader(String path) throws IOException {
        InputStream stream = getClass().getClassLoader().getResourceAsStream(path);
        content = convertStreamToString(stream);
        stream.close();
    }

    private static String convertStreamToString(InputStream is) throws IOException {
        StringBuilder sb = new StringBuilder(2048); // Define a size if you have an idea of it.
        char[] read = new char[128]; // Your buffer size.
        try (InputStreamReader ir = new InputStreamReader(is, StandardCharsets.UTF_8)) {
            for (int i; -1 != (i = ir.read(read)); sb.append(read, 0, i)) ;
        }
        return sb.toString();
    }

    public String getContent() {
        return content;
    }
}
