package com.getstream.sdk.chat;

import com.getstream.sdk.chat.model.FilterOption;
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
        private String query;
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

        public void setFilterOptions(List<FilterOption> filterOptions) {
            this.filterOptions = filterOptions;
        }

        public String getQuery() {
            return query;
        }

        public void setQuery(String query) {
            this.query = query;
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
        private String query;
        private Map<String, Object> sortOptions;

        public List<FilterOption> getFilterOptions() {
            return filterOptions;
        }

        public void setFilterOptions(List<FilterOption> filterOptions) {
            this.filterOptions = filterOptions;
        }

        public String getQuery() {
            return query;
        }

        public void setQuery(String query) {
            this.query = query;
        }

        public Map<String, Object> getSortOptions() {
            return sortOptions;
        }

        public void setSortOptions(Map<String, Object> sortOptions) {
            this.sortOptions = sortOptions;
        }
    }
}
