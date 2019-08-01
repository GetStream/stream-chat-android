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
    public MessageItemView messageItemView = new MessageItemView();
    public ChannelPreview channelPreview = new ChannelPreview();
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
        private ReadIndicator readIndicator = ReadIndicator.LAST_READ_USER;
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
         * @param sortOptions Sort Syntax for channels
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

    public class MessageItemView{

        private int messageItemLayoutId = R.layout.list_item_message; // Default
        private String messageItemViewHolderName = MessageListItemViewHolder.class.getName(); // Default

        public int getMessageItemLayoutId() {
            return messageItemLayoutId;
        }

        public void setMessageItemLayoutId(int messageItemLayoutId) {
            this.messageItemLayoutId = messageItemLayoutId;
        }

        public String getMessageItemViewHolderName() {
            return messageItemViewHolderName;
        }

        public void setMessageItemViewHolderName(String messageItemViewHolderName) {
            this.messageItemViewHolderName = messageItemViewHolderName;
        }
    }

    public class ChannelPreview{
        private boolean showSearchBar = false;
        private int channelItemLayoutId = R.layout.list_item_channel; // Default
        private String channelItemViewHolderName = ChannelListItemViewHolder.class.getName(); // Default

        public int getChannelItemLayoutId() {
            return channelItemLayoutId;
        }

        public void setChannelItemLayoutId(int channelItemLayoutId) {
            this.channelItemLayoutId = channelItemLayoutId;
        }

        public String getChannelItemViewHolderName() {
            return channelItemViewHolderName;
        }

        public void setChannelItemViewHolderName(String channelItemViewHolderName) {
            this.channelItemViewHolderName = channelItemViewHolderName;
        }

        public boolean isShowSearchBar() {
            return showSearchBar;
        }

        public void setShowSearchBar(boolean showSearchBar) {
            this.showSearchBar = showSearchBar;
        }
    }
}
