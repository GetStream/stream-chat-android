package com.getstream.sdk.chat.component;

import com.getstream.sdk.chat.enums.FilterQuery;
import com.getstream.sdk.chat.enums.ReadIndicator;

import java.util.List;
import java.util.Map;

/**
 * A Utility class to customize Channels preview and Filter & Order Channels and Users
 */
public class Component{

    public Channel channel = new Channel();
    public User user = new User();

    /**
     * A class to customize Channels preview and Filtering & Ordering Channels
     */
    public class Channel{
        private boolean invitation = true;
        private boolean showReadIndicator = true;
        private List<FilterOption> filterOptions;
        private FilterQuery query;
        private Map<String, Object> sortOptions;
        private ReadIndicator readIndicator = ReadIndicator.UNREAD_COUNT;
        /**
         * Getter Method for show/hide of read indicator
         * @return  True if readindicator is showing
         */
        public boolean isShowReadIndicator() {
            return showReadIndicator;
        }
        /**
         * Setter Method for Show/Hide last message read indicator
         * @param showReadIndicator true : show, false : hide
         */
        public void setShowReadIndicator(boolean showReadIndicator) {
            this.showReadIndicator = showReadIndicator;
        }

        /**
         * Getter Method for type of read indicator
         * @return  last message readIndicator type
         */
        public ReadIndicator getReadIndicator() {
            return readIndicator;
        }
        /**
         * Setter Method for type of read indicator
         * @param readIndicator ReadIndicator for ReadIndicator type
         */
        public void setReadIndicatorType(ReadIndicator readIndicator) {
            this.readIndicator = readIndicator;
        }

        public boolean isInvitation() {
            return invitation;
        }

        public void setInvitation(boolean invitation) {
            this.invitation = invitation;
        }

        public List<FilterOption> getFilterOptions() {
            return filterOptions;
        }
        /**
         * Setter Method for Channels filter option
         * @param query FilterQuery for query operator
         * @param filterOptions Query syntax
         */
        public void setFilterOptions(FilterQuery query, List<FilterOption> filterOptions) {
            this.query = query;
            this.filterOptions = filterOptions;
        }


        public FilterQuery getQuery() {
            return query;
        }


        public Map<String, Object> getSortOptions() {
            return sortOptions;
        }
        /**
         * Setter Method for Channels sort option
         * @param sortOptions Sort Syntax for channels
         */
        public void setSortOptions(Map<String, Object> sortOptions) {
            this.sortOptions = sortOptions;
        }
    }

    /**
     * A class to customize Filtering & Ordering Users
     */
    public class User {

        private List<FilterOption> filterOptions;
        private FilterQuery query;
        private Map<String, Object> sortOptions;

        public List<FilterOption> getFilterOptions() {
            return filterOptions;
        }

        /**
         * Setter Method for Users filter option
         * @param query FilterQuery for query operator
         * @param filterOptions Query syntax
         */
        public void setFilterOptions(FilterQuery query, List<FilterOption> filterOptions) {
            this.filterOptions = filterOptions;
            this.query = query;
        }

        public FilterQuery getQuery() {
            return query;
        }

        public Map<String, Object> getSortOptions() {
            return sortOptions;
        }

        /**
         * Setter Method for Users sort option
         * @param sortOptions Sort Syntax for channels
         */
        public void setSortOptions(Map<String, Object> sortOptions) {
            this.sortOptions = sortOptions;
        }
    }
}
