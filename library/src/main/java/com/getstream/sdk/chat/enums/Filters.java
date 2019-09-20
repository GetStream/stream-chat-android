package com.getstream.sdk.chat.enums;

import java.util.List;

/**
 * Filters a set of static methods to build a filter object
 **/
public class Filters {

    static public FilterObject and(FilterObject... filters) {
        return new FilterObject("$and", filters);
    }

    static public FilterObject or(FilterObject... filters) {
        return new FilterObject("$or", filters);
    }

    static public FilterObject nor(FilterObject... filters) {
        return new FilterObject("$nor", filters);
    }

    static public FilterObject eq(String field, Object value) {
        return new FilterObject(field, value);
    }

    static public FilterObject ne(String field, Object value) {
        return new FilterObject(field, new FilterObject("$ne", value));
    }

    static public FilterObject greaterThan(String field, Object value) {
        return new FilterObject(field, new FilterObject("$gt", value));
    }

    static public FilterObject greaterThanEquals(String field, Object value) {
        return new FilterObject(field, new FilterObject("$gte", value));
    }

    static public FilterObject lessThan(String field, Object value) {
        return new FilterObject(field, new FilterObject("$lt", value));
    }

    static public FilterObject lessThanEquals(String field, Object value) {
        return new FilterObject(field, new FilterObject("$lte", value));
    }

    static public FilterObject in(String field, String... values) {
        return new FilterObject(field, new FilterObject("$in", values));
    }

    static public FilterObject in(String field, List values) {
        return new FilterObject(field, new FilterObject("$in", values));
    }

    static public FilterObject in(String field, Number... values) {
        return new FilterObject(field, new FilterObject("$in", values));
    }

    static public FilterObject nin(String field, String... values) {
        return new FilterObject(field, new FilterObject("$nin", values));
    }

    static public FilterObject nin(String field, List values) {
        return new FilterObject(field, new FilterObject("$nin", values));
    }

    static public FilterObject nin(String field, Number... values) {
        return new FilterObject(field, new FilterObject("$nin", values));
    }
}
