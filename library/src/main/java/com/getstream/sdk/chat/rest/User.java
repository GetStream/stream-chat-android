package com.getstream.sdk.chat.rest;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.getstream.sdk.chat.interfaces.UserEntity;
import com.getstream.sdk.chat.rest.adapter.UserGsonAdapter;
import com.getstream.sdk.chat.storage.converter.DateConverter;
import com.getstream.sdk.chat.storage.converter.ExtraDataConverter;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;

import java.util.Date;
import java.util.HashMap;

/**
 * A user
 */

@JsonAdapter(UserGsonAdapter.class)
@Entity(tableName = "stream_user")
public class User implements UserEntity {
    @PrimaryKey
    @NonNull
    @SerializedName("id")
    @Expose
    private String id;

    @SerializedName("name")
    @Expose
    private String name;

    @SerializedName("image")
    @Expose
    private String image;

    @SerializedName("role")
    @Expose
    private String role;

    @SerializedName("created_at")
    @Expose
    @TypeConverters(DateConverter.class)
    private Date createdAt;

    @SerializedName("updated_at")
    @Expose
    @TypeConverters(DateConverter.class)
    private Date updatedAt;

    @SerializedName("last_active")
    @Expose
    @TypeConverters(DateConverter.class)
    private Date lastActive;

    @SerializedName("online")
    @Expose
    private Boolean online;

    @SerializedName("total_unread_count")
    @Expose
    private Integer totalUnreadCount;


    @SerializedName("unread_channels")
    @Expose
    private Integer unreadChannels;

    @TypeConverters(ExtraDataConverter.class)
    private HashMap<String, Object> extraData;

    /**
     * Constructor
     *
     */
    public User() {

    }
    /**
     * Constructor
     *
     * @param id User id
     */
    public User(String id) {
        this(id, new HashMap<>());
    }

    /**
     * Constructor
     *
     * @param id        User id
     * @param extraData Custom user fields (ie: name, image, anything that json can serialize is ok)
     */
    @Ignore
    public User(String id, HashMap<String, Object> extraData) {
        this.id = id;
        this.online = false;

        if (extraData == null) {
            this.extraData = new HashMap<>();
        } else {
            this.extraData = new HashMap<>(extraData);
        }

        // since name and image are very common fields, we are going to promote them as
        Object image = this.extraData.remove("image");
        if (image != null) {
            this.image = image.toString();
        }

        Object name = this.extraData.remove("name");
        if (name != null) {
            this.name = name.toString();
        }

        this.extraData.remove("id");
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updated_at) {
        this.updatedAt = updatedAt;
    }

    public Date getLastActive() {
        return lastActive;
    }

    public void setLastActive(Date lastActive) {
        this.lastActive = lastActive;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Boolean getOnline() {
        return online;
    }

    public void setOnline(Boolean online) {
        this.online = online;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getTotalUnreadCount() {
        return totalUnreadCount;
    }

    public void setTotalUnreadCount(Integer totalUnreadCount) {
        this.totalUnreadCount = totalUnreadCount;
    }

    public Integer getUnreadChannels() {
        return unreadChannels;
    }

    public void setUnreadChannels(Integer unreadChannels) {
        this.unreadChannels = unreadChannels;
    }

    public User shallowCopy() {
        User copy = new User(id);
        copy.shallowUpdate(this);
        return copy;
    }

    public void shallowUpdate(User user) {
        name = user.name;
        online = user.online;
        image = user.image;
        createdAt = user.createdAt;
        lastActive = user.lastActive;
        updatedAt = user.updatedAt;
        if (user.extraData != null) {
            extraData = new HashMap<>(user.extraData);
        }
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (getClass() != obj.getClass()) {
            return false;
        }
        // we compare based on the CID
        User otherUser = (User) obj;
        return TextUtils.equals(this.getId(), otherUser.getId());
    }

    public HashMap<String, Object> getExtraData() {
        return extraData;
    }

    public void setExtraData(HashMap<String, Object> data) {
        this.extraData = data;
    }

    public String getInitials() {
        if (this.name == null) {
            this.name = "";
        }
        String name = this.name;
        String[] names = name.split(" ");
        String firstName = names[0];
        String lastName = null;
        try {
            lastName = names[1];
        } catch (Exception e) {
        }

        if (!TextUtils.isEmpty(firstName) && TextUtils.isEmpty(lastName))
            return firstName.substring(0, 1).toUpperCase();
        if (TextUtils.isEmpty(firstName) && !TextUtils.isEmpty(lastName))
            return lastName.substring(0, 1).toUpperCase();

        if (!TextUtils.isEmpty(firstName) && !TextUtils.isEmpty(lastName))
            return firstName.substring(0, 1).toUpperCase() + lastName.substring(0, 1).toUpperCase();
        return null;
    }

    // TODO: populate this from API
    public boolean isMe() {
        return false;
    }

    public String getUserId() {
        return id;
    }
}
