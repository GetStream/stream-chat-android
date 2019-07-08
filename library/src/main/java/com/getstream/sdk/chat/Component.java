package com.getstream.sdk.chat;

import java.util.HashMap;
import java.util.Map;

public class Component {

    public static class Channel {

        private static boolean invitation = true;
        private static boolean showReadIndicator = true;
        private static Map<String, Object> filterOptions;
        private static String filterKey = "members";
        private static Map<String, Object> sortOptions;

        public static boolean isShowReadIndicator() {
            return showReadIndicator;
        }

        public static void setShowReadIndicator(boolean showReadIndicator_) {
            showReadIndicator = showReadIndicator_;
        }

        public static boolean isInvitation() {
            return invitation;
        }

        public static void setInvitation(boolean invitation) {
            Channel.invitation = invitation;
        }

        public static Map<String, Object> getFilterOptions() {
            return filterOptions;
        }

        public static void setFilterOptions(Map<String, Object> filterOptions) {
            Channel.filterOptions = filterOptions;
        }

        public static String getFilterKey() {
            return filterKey;
        }

        public static void setFilterKey(String filterKey) {
            Channel.filterKey = filterKey;
        }

        public static Map<String, Object> getSortOptions() {
            return sortOptions;
        }

        public static void setSortOptions(Map<String, Object> sortOptions) {
            Channel.sortOptions = sortOptions;
        }
    }

    public static class Chat {
        private static boolean groupChat = true;

    }

    public static class Message {

    }

    public static class User {

        private static Map<String, Object> filterOptions;
        private static String filterKey = "id";

        private static Map<String, Object> sortOptions;

        public static Map<String, Object> getFilterOptions() {
            return filterOptions;
        }

        public static void setFilterOptions(Map<String, Object> filterOptions) {
            User.filterOptions = filterOptions;
        }

        public static String getFilterKey() {
            return filterKey;
        }

        public static void setFilterKey(String filterKey) {
            User.filterKey = filterKey;
        }

        public static Map<String, Object> getSortOptions() {
            return sortOptions;
        }

        public static void setSortOptions(Map<String, Object> sortOptions) {
            User.sortOptions = sortOptions;
        }
    }
}
