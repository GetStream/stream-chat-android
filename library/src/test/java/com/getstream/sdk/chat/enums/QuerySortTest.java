package com.getstream.sdk.chat.enums;

import com.google.gson.Gson;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class    QuerySortTest {

    @org.junit.jupiter.api.Test
    void decrTest() {
        QuerySort t = new QuerySort().desc("last_message_at");
        String json = new Gson().toJson(
                t.getData()
        );
        assertEquals("[{\"field\":\"last_message_at\",\"direction\":-1}]", json);
    }

    @org.junit.jupiter.api.Test
    void ascTest() {
        QuerySort t = new QuerySort().asc("last_message_at");
        String json = new Gson().toJson(
                t.getData()
        );
        assertEquals("[{\"field\":\"last_message_at\",\"direction\":1}]", json);
    }

    @org.junit.jupiter.api.Test
    void ascDescTest() {
        QuerySort t = new QuerySort().asc("last_message_at").desc("qty");
        String json = new Gson().toJson(
                t.getData()
        );
        assertEquals("[{\"field\":\"last_message_at\",\"direction\":1},{\"field\":\"qty\",\"direction\":-1}]", json);
    }

    @org.junit.jupiter.api.Test
    void descAscTest() {
        QuerySort t = new QuerySort().desc("qty").asc("last_message_at");
        String json = new Gson().toJson(
                t.getData()
        );
        assertEquals("[{\"field\":\"qty\",\"direction\":-1},{\"field\":\"last_message_at\",\"direction\":1}]", json);
    }
}
