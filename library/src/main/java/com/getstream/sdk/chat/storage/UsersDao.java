package com.getstream.sdk.chat.storage;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.getstream.sdk.chat.rest.User;

import java.util.List;

@Dao
public interface UsersDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public void insertUser(User user);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public void insertUsers(List<User> users);

    @Query("SELECT * FROM user " +
            "WHERE user.id IN (:ids)")
    List<User> getUsers(final List<String> ids);

}