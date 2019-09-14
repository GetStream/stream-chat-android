package com.getstream.sdk.chat.enums;

import com.google.gson.annotations.JsonAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * FilterObject holds the filtering data
 * <p>
 * If you need to serialize this to JSON make sure to either use gson or serialize the result of
 * getData()
 **/
@JsonAdapter(FilterObjectAdapter.class)
public class FilterObject {

    private HashMap<String, Object> data;

    public FilterObject() {
        this.data = new HashMap<>();
    }

    public FilterObject(HashMap<String, Object> data) {
        this.data = data;
    }


    public FilterObject(String key, Object v) {
        this.data = new HashMap<>();
        this.data.put(key, v);
    }

    public HashMap<String, Object> getData() {
        HashMap<String, Object> data = new HashMap<>();
        for (Map.Entry<String, Object> set : this.data.entrySet()) {
            if (set.getValue() instanceof FilterObject) {
                data.put(set.getKey(), ((FilterObject) set.getValue()).getData());
                continue;
            }
            if (set.getValue() instanceof FilterObject[]) {
                ArrayList<HashMap<String, Object>> listOfMaps = new ArrayList<>();
                FilterObject[] values = (FilterObject[]) set.getValue();
                for (FilterObject subVal : values) {
                    listOfMaps.add(subVal.getData());
                }
                data.put(set.getKey(), listOfMaps);
                continue;
            }
            data.put(set.getKey(), set.getValue());
        }
        return data;
    }

    public FilterObject put(String key, Object v) {
        HashMap<String, Object> clone = new HashMap<>(this.data);
        clone.put(key, v);
        return new FilterObject(clone);
    }

}
