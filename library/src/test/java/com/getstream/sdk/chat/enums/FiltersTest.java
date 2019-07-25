package com.getstream.sdk.chat.enums;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Filter;

import static com.getstream.sdk.chat.enums.Filters.and;
import static com.getstream.sdk.chat.enums.Filters.eq;
import static com.getstream.sdk.chat.enums.Filters.greaterThan;
import static com.getstream.sdk.chat.enums.Filters.greaterThanEquals;
import static com.getstream.sdk.chat.enums.Filters.in;
import static com.getstream.sdk.chat.enums.Filters.lessThan;
import static com.getstream.sdk.chat.enums.Filters.lessThanEquals;
import static com.getstream.sdk.chat.enums.Filters.ne;
import static com.getstream.sdk.chat.enums.Filters.nin;
import static com.getstream.sdk.chat.enums.Filters.nor;
import static com.getstream.sdk.chat.enums.Filters.or;
import static org.junit.jupiter.api.Assertions.*;

class FiltersTest {

    @org.junit.jupiter.api.Test
    void andTest() {
        FilterObject filter = and(eq("name", "max"), eq("name", "tommaso"));
        String json = new Gson().toJson(
                filter.getData()
        );
        assertEquals("{\"$and\":[{\"name\":\"max\"},{\"name\":\"tommaso\"}]}", json);
    }

    @org.junit.jupiter.api.Test
    void orTest() {
        String json = new Gson().toJson(
                or(eq("name", "max"), eq("name", "tommaso"))
        );
        assertEquals("{\"$or\":[{\"name\":\"max\"},{\"name\":\"tommaso\"}]}", json);
    }

    @org.junit.jupiter.api.Test
    void norTest() {
        String json = new Gson().toJson(
                nor(eq("name", "max"), eq("name", "tommaso"))
        );
        assertEquals("{\"$nor\":[{\"name\":\"max\"},{\"name\":\"tommaso\"}]}", json);
    }

    @org.junit.jupiter.api.Test
    void neTest() {
        String json = new Gson().toJson(
                ne("qty", 20)
        );
        assertEquals("{\"qty\":{\"$ne\":20}}", json);
    }

    @org.junit.jupiter.api.Test
    void gtTest() {
        String json = new Gson().toJson(
                greaterThan("qty", 20)
        );
        assertEquals("{\"qty\":{\"$gt\":20}}", json);
    }

    @org.junit.jupiter.api.Test
    void gteTest() {
        String json = new Gson().toJson(
                greaterThanEquals("qty", 20)
        );
        assertEquals("{\"qty\":{\"$gte\":20}}", json);
    }

    @org.junit.jupiter.api.Test
    void ltTest() {
        String json = new Gson().toJson(
                lessThan("qty", 20)
        );
        assertEquals("{\"qty\":{\"$lt\":20}}", json);
    }

    @org.junit.jupiter.api.Test
    void lteTest() {
        String json = new Gson().toJson(
                lessThanEquals("qty", 20)
        );
        assertEquals("{\"qty\":{\"$lte\":20}}", json);
    }

    @org.junit.jupiter.api.Test
    void eqTest() {
        String json = new Gson().toJson(
                eq("name", "max")
        );
        assertEquals("{\"name\":\"max\"}", json);
    }

    @org.junit.jupiter.api.Test
    void greaterThanEqualsTest(){
        String json = new Gson().toJson(
                greaterThanEquals("cost", 123)
        );
        assertEquals("{\"cost\":{\"$gte\":123}}", json);
    }

    @org.junit.jupiter.api.Test
    void inTest(){
        String json = new Gson().toJson(
                in("id", 123, 456)
        );
        assertEquals("{\"id\":{\"$in\":[123,456]}}", json);
    }

    @org.junit.jupiter.api.Test
    void inListTest(){
        List<Number> l = new ArrayList<>();
        l.add(123);
        l.add(456);
        String json = new Gson().toJson(
                in("id", l)
        );
        assertEquals("{\"id\":{\"$in\":[123,456]}}", json);
    }

    @org.junit.jupiter.api.Test
    void inListStringTest(){
        List<String> l = new ArrayList<>();
        l.add("123");
        l.add("456");
        String json = new Gson().toJson(
                in("id", l)
        );
        assertEquals("{\"id\":{\"$in\":[\"123\",\"456\"]}}", json);
    }

    @org.junit.jupiter.api.Test
    void inStrTest(){
        String json = new Gson().toJson(
                in("id", "123", "456")
        );
        assertEquals("{\"id\":{\"$in\":[\"123\",\"456\"]}}", json);
    }

    @org.junit.jupiter.api.Test
    void ninTest(){
        String json = new Gson().toJson(
                nin("id", 123, 456)
        );
        assertEquals("{\"id\":{\"$nin\":[123,456]}}", json);
    }

    @org.junit.jupiter.api.Test
    void ninStrTest(){
        String json = new Gson().toJson(
                nin("id", "123", "456")
        );
        assertEquals("{\"id\":{\"$nin\":[\"123\",\"456\"]}}", json);
    }

    @org.junit.jupiter.api.Test
    void complexTest() {
        String json = new Gson().toJson(
                or(eq("name", "max"), and(eq("country", "italy"), greaterThanEquals("age", 33)))
        );
        assertEquals("{\"$or\":[{\"name\":\"max\"},{\"$and\":[{\"country\":\"italy\"},{\"age\":{\"$gte\":33}}]}]}", json);
    }
}
