package com.getstream.sdk.chat.users;

import com.getstream.sdk.chat.interfaces.UserEntity;
import com.getstream.sdk.chat.rest.User;

public interface UsersRepository {

    User getCurrent();

    User getUser(String userId);
}
