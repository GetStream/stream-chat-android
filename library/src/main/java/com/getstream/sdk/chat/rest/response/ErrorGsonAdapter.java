package com.getstream.sdk.chat.rest.response;

import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;

public class ErrorGsonAdapter extends TypeAdapter<ErrorResponse> {
    @Override
    public void write(JsonWriter out, ErrorResponse value) throws IOException {
        throw new IOException("not supported");
    }

    @Override
    public ErrorResponse read(JsonReader in) throws IOException {
        TypeAdapter adapter = new Gson().getAdapter(ErrorResponse.class);
        ErrorResponse value = (ErrorResponse) adapter.read(in);
        return value;
    }
}
