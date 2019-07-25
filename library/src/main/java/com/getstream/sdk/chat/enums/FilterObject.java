package com.getstream.sdk.chat.enums;

import com.google.gson.annotations.JsonAdapter;

import java.util.HashMap;

@JsonAdapter(FilterObjectAdapter.class)
public class FilterObject {

    // TODO: deep-clone this
    public HashMap<String, Object> getData() {
        return data;
    }

    private HashMap<String, Object> data;

    public FilterObject(HashMap<String, Object> data) {
        this.data = data;
    }

    public FilterObject(String key, Object v) {
        this.data = new HashMap<>();
        this.data.put(key, v);
    }

    public FilterObject put(String key, Object v) {
        HashMap<String, Object> clone = new HashMap<>(this.data);
        clone.put(key, v);
        return new FilterObject(clone);
    }

}
