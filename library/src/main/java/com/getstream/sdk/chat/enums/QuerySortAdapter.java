package com.getstream.sdk.chat.enums;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.util.ArrayList;

public class QuerySortAdapter extends TypeAdapter<QuerySort> {
    @Override
    public void write(JsonWriter out, QuerySort value) throws IOException {
        TypeAdapter adapter = new Gson().getAdapter(ArrayList.class);
        adapter.write(out, value.getData());
    }

    @Override
    public QuerySort read(JsonReader in) throws IOException {
        throw new IOException("not supported");
    }
}

