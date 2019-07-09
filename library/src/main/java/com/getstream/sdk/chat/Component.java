package com.getstream.sdk.chat;

import java.util.Map;

public class Component{
    public Channel channel = new Channel();
    public User user = new User();

    public class Channel{

        private boolean invitation = true;
        private boolean showReadIndicator = true;
        private Map<String, Object> filterOptions;
        private String filterKey = "members";
        private Map<String, Object> sortOptions;
       

        public boolean isShowReadIndicator() {
            return showReadIndicator;
        }

        public void setShowReadIndicator(boolean showReadIndicator_) {
            showReadIndicator = showReadIndicator_;
        }

        public boolean isInvitation() {
            return invitation;
        }

        public void setInvitation(boolean invitation) {
            this.invitation = invitation;
        }

        public Map<String, Object> getFilterOptions() {
            return filterOptions;
        }

        public void setFilterOptions(Map<String, Object> filterOptions) {
            this.filterOptions = filterOptions;
        }

        public String getFilterKey() {
            return filterKey;
        }

        public void setFilterKey(String filterKey) {
            this.filterKey = filterKey;
        }

        public Map<String, Object> getSortOptions() {
            return sortOptions;
        }

        public void setSortOptions(Map<String, Object> sortOptions) {
            this.sortOptions = sortOptions;
        }
    }

    public class User {

        private Map<String, Object> filterOptions;
        private String filterKey = "id";

        private Map<String, Object> sortOptions;

        public Map<String, Object> getFilterOptions() {
            return filterOptions;
        }

        public void setFilterOptions(Map<String, Object> filterOptions) {
            this.filterOptions = filterOptions;
        }

        public String getFilterKey() {
            return filterKey;
        }

        public void setFilterKey(String filterKey) {
            this.filterKey = filterKey;
        }

        public Map<String, Object> getSortOptions() {
            return sortOptions;
        }

        public void setSortOptions(Map<String, Object> sortOptions) {
            this.sortOptions = sortOptions;
        }
    }
}
