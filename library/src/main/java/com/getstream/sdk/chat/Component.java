package com.getstream.sdk.chat;

import com.getstream.sdk.chat.model.FilterOption;
import com.getstream.sdk.chat.model.enums.FilterQuery;
import com.getstream.sdk.chat.model.enums.ReadIndicator;

import java.util.List;
import java.util.Map;

public class Component{
    public Channel channel = new Channel();
    public User user = new User();

    public class Channel{

        private boolean invitation = true;
        private boolean showReadIndicator = true;
        private List<FilterOption> filterOptions;
        private FilterQuery query;
        private Map<String, Object> sortOptions;
        private ReadIndicator readIndicator = ReadIndicator.UNREAD_COUNT;
        public boolean isShowReadIndicator() {
            return showReadIndicator;
        }

        public void setShowReadIndicator(boolean showReadIndicator) {
            this.showReadIndicator = showReadIndicator;
        }

        public void setReadIndicatorType(ReadIndicator readIndicator) {
            this.readIndicator = readIndicator;
        }

        public ReadIndicator getReadIndicator() {
            return readIndicator;
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

        public void setSortOptions(Map<String, Object> sortOptions) {
            this.sortOptions = sortOptions;
        }
    }

    public class User {

        private List<FilterOption> filterOptions;
        private FilterQuery query;
        private Map<String, Object> sortOptions;

        public List<FilterOption> getFilterOptions() {
            return filterOptions;
        }

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

        public void setSortOptions(Map<String, Object> sortOptions) {
            this.sortOptions = sortOptions;
        }
    }
}
