package com.getstream.sdk.chat.enums;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.util.HashMap;


/**
 * FilterObjectAdapter makes it easier to serialize this to JSON via gson
 **/
public class FilterObjectAdapter extends TypeAdapter<FilterObject> {
    @Override
    public void write(JsonWriter out, FilterObject value) throws IOException {
        TypeAdapter adapter = new Gson().getAdapter(HashMap.class);
        adapter.write(out, value.getData());
    }

    @Override
    public FilterObject read(JsonReader in) throws IOException {
        TypeAdapter adapter = new Gson().getAdapter(HashMap.class);
        HashMap data = (HashMap<String, Object>) adapter.read(in);
        FilterObject o = new FilterObject(data);
        return o;
    }
}
