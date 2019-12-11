package com.getstream.sdk.chat.users;

import com.getstream.sdk.chat.rest.User;

public interface UsersCache {

    void setCurrentUser(User user);

    User getCurrent();

    String getCurrentId();

    User getUser(String userId);

    boolean isCurrentUser(String userId);
}
