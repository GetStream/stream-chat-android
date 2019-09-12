package com.getstream.sdk.chat.enums;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QuerySort {
    private static int DESC = -1;
    private static int ASC = 1;
    private List<Map<String, Object>> mSort;

    public List<Map<String, Object>> getData() {
        return mSort;
    }

    public QuerySort clone() {
        QuerySort _this = new QuerySort();
        if (mSort == null) {
            mSort = new ArrayList<>();
        }
        _this.mSort = new ArrayList<>(mSort);
        return _this;
    }

    private QuerySort add(String fieldName, Number direction) {
        Map<String, Object> v = new HashMap<>();
        v.put("field", fieldName);
        v.put("direction", direction);
        QuerySort _this = clone();
        _this.mSort.add(v);
        return _this;
    }

    public QuerySort asc(String fieldName) {
        return add(fieldName, ASC);
    }

    public QuerySort desc(String fieldName) {
        return add(fieldName, DESC);
    }

}



