package com.getstream.sdk.chat.users;

import com.getstream.sdk.chat.rest.User;

import java.util.HashMap;
import java.util.Map;

public class UsersCacheImpl implements UsersCache {

    private User currentUser;
    private Map<String, User> users = new HashMap<>();

    @Override
    public void setCurrentUser(User user) {
        if (user == null) return;
        this.currentUser = user;
        users.put(user.getId(), user);
    }

    @Override
    public User getCurrent() {
        return currentUser;
    }

    @Override
    public String getCurrentId() {
        if (currentUser == null) return null;
        else return currentUser.getId();
    }

    @Override
    public User getUser(String userId) {
        return users.get(userId);
    }

    @Override
    public boolean isCurrentUser(String userId) {
        if (currentUser == null || userId == null) return false;
        return userId.equals(currentUser.getId());
    }
}
