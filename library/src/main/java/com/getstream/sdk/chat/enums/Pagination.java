package com.getstream.sdk.chat.enums;

import java.util.Objects;

public class Pagination {

    public final String messageId;
    public final int limit;
    public final Direction direction;

    private Pagination(Direction direction, String messageId, int limit) {
        this.messageId = messageId;
        this.limit = limit;
        this.direction = direction;
    }

    public static Pagination firstPage(int limit) {
        return new Pagination(Direction.FIRST_PAGE, null, limit);
    }

    public static Pagination nextPage(Direction direction, String messageId, int limit) {
        Objects.requireNonNull(direction);
        Objects.requireNonNull(messageId);
        return new Pagination(direction, messageId, limit);
    }

    public enum Direction {

        FIRST_PAGE(""),

        GREATER_THAN("id_gt"),
        GREATER_THAN_OR_EQUAL("id_gte"),
        LESS_THAN("id_lt"),
        LESS_THAN_OR_EQUAL("id_lte");

        private String value;

        Direction(final String value) {
            this.value = value;
        }

        public String get() {
            return value;
        }

        @Override
        public String toString() {
            return this.get();
        }
    }
}


