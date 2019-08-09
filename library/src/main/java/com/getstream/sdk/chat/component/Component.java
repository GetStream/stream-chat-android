package com.getstream.sdk.chat.component;

import com.getstream.sdk.chat.R;
import com.getstream.sdk.chat.adapter.ChannelListItemViewHolder;
import com.getstream.sdk.chat.adapter.MessageListItemViewHolder;
import com.getstream.sdk.chat.enums.FilterObject;
import com.getstream.sdk.chat.enums.ReadIndicator;

import java.util.Map;

/**
 * A Utility class to customize Channels preview and Filter & Order Channels and Users
 */
public class Component{

    public Channel channel = new Channel();
    public User user = new User();
    public Message message = new Message();

    /**
     * A class to customize Channels preview and Filtering & Ordering Channels
     */
    public class Channel{
        private boolean invitation = true;
        private boolean showReadIndicator = true;

        public FilterObject getFilter() {
            return _filter;
        }

        private FilterObject _filter;
        private Map<String, Object> sortOptions;

        public boolean isInvitation() {
            return invitation;
        }

        public void setInvitation(boolean invitation) {
            this.invitation = invitation;
        }

        /**
         * Setter Method for Channels filter option
         * @param filter FilterQuery for query operator
         */
        public void filter(FilterObject filter) {
            this._filter = filter;
        }

        public Map<String, Object> getSortOptions() {
            return sortOptions;
        }
        /**
         * Setter Method for Channels sort option
         * @param sortOptions QuerySort Syntax for channels
         */
        public void setSortOptions(Map<String, Object> sortOptions) {
            this.sortOptions = sortOptions;
        }


        // Custom Channel Preview
        private int channelItemLayoutId = R.layout.list_item_channel; // Default
        private String channelItemViewHolderName = ChannelListItemViewHolder.class.getName(); // Default

        /**
         * Getter Method for Custom Channel Preview Layout
         * @return  layout Id of Custom channel preview
         */
        public int getChannelItemLayoutId() {
            return channelItemLayoutId;
        }

        /**
         * Setter Method for Custom Channel Preview Layout
         * @param channelItemLayoutId layout Id of Custom channel preview
         */
        public void setChannelItemLayoutId(int channelItemLayoutId) {
            this.channelItemLayoutId = channelItemLayoutId;
        }

        /**
         * Getter Method for Custom Channel Preview Item ViewHolder
         * @return  class name of custom channel preview ItemViewHolder
         */
        public String getChannelItemViewHolderName() {
            return channelItemViewHolderName;
        }

        /**
         * Setter Method for Custom Channel Preview Item ViewHolder
         * @param channelItemViewHolderName class name of custom channel preview ItemViewHolder
         */
        public void setChannelItemViewHolderName(String channelItemViewHolderName) {
            this.channelItemViewHolderName = channelItemViewHolderName;
        }

        // Show/Hide Search bar
        private boolean showSearchBar = false;

        /**
         * Getter Method for show/hide of Search Bar
         * @return  true if Searchbar is showing
         */
        public boolean isShowSearchBar() {
            return showSearchBar;
        }

        /**
         * Setter Method for show/hide of Search Bar
         * @param showSearchBar true : show, false : hide
         */
        public void setShowSearchBar(boolean showSearchBar) {
            this.showSearchBar = showSearchBar;
        }

        private ReadIndicator readIndicator = ReadIndicator.LAST_READ_USER;
        /**
         * Getter Method for show/hide of read indicator
         * @return  true if readindicator is showing
         */
        public boolean isShowReadIndicator() {
            return showReadIndicator;
        }
        /**
         * Setter Method for show/hide of read indicator
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

    }

    /**
     * A class to customize Filtering & Ordering Users
     */
    public class User {

        private FilterObject _filter;
        private Map<String, Object> sortOptions;

        public FilterObject getFilter() {
            return _filter;
        }

        /**
         * Setter Method for Users filter option
         * @param filter FilterQuery for query operator
         */
        public void filter(FilterObject filter) {
            this._filter = filter;
        }


        public Map<String, Object> getSortOptions() {
            return sortOptions;
        }

        /**
         * Setter Method for Users sort option
         * @param sortOptions QuerySort Syntax for channels
         */
        public void setSortOptions(Map<String, Object> sortOptions) {
            this.sortOptions = sortOptions;
        }
    }

    public class AvatarView{
        private float radius;
        private float cornerRadius;

        public float getRadius() {
            return radius;
        }

        public void setRadius(float radius) {
            this.radius = radius;
        }

        public float getCornerRadius() {
            return cornerRadius;
        }

        public void setCornerRadius(float cornerRadius) {
            this.cornerRadius = cornerRadius;
        }
    }

    public class Message{

        private int messageItemLayoutId = R.layout.list_item_message; // Default
        private String messageItemViewHolderName = MessageListItemViewHolder.class.getName(); // Default

        /**
         * Getter Method for Custom Message Item View Layout
         * @return  layout Id of custom message item view layout
         */
        public int getMessageItemLayoutId() {
            return messageItemLayoutId;
        }

        /**
         * Setter Method for Custom Message Item View Layout
         * @param messageItemLayoutId layout Id of custom message item view layout
         */
        public void setMessageItemLayoutId(int messageItemLayoutId) {
            this.messageItemLayoutId = messageItemLayoutId;
        }

        /**
         * Getter Method for Custom Message Item ViewHolder
         * @return  class name of custom message ItemViewHolder
         */
        public String getMessageItemViewHolderName() {
            return messageItemViewHolderName;
        }

        /**
         * Setter Method for Custom Message Item ViewHolder
         * @param messageItemViewHolderName class name of custom message ItemViewHolder
         */
        public void setMessageItemViewHolderName(String messageItemViewHolderName) {
            this.messageItemViewHolderName = messageItemViewHolderName;
        }
    }

}
