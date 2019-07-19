package com.getstream.sdk.chat.component;

public class FilterOption {

    private String key;   // Filtering field name
    private Object value; // Filtering query syntax

    /**
     * Constructor
     * @param key Filtering field name
     * @param value Filtering query syntax
     * */
    public FilterOption(String key, Object value){
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }
}
