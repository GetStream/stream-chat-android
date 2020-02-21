package com.getstream.sdk.chat.storage;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import io.getstream.chat.android.client.models.User;

import java.util.List;

@Dao
public interface UsersDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertUser(User user);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertUsers(List<User> users);

    @Query("SELECT * FROM User " +
            "WHERE User.id IN (:ids)")
    List<User> getUsers(final List<String> ids);

}