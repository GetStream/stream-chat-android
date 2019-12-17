package com.getstream.sdk.chat.rest;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.getstream.sdk.chat.interfaces.UserEntity;
import com.getstream.sdk.chat.model.Device;
import com.getstream.sdk.chat.model.Mute;
import com.getstream.sdk.chat.rest.adapter.UserGsonAdapter;
import com.getstream.sdk.chat.storage.converter.DateConverter;
import com.getstream.sdk.chat.storage.converter.ExtraDataConverter;
import com.google.gson.annotations.JsonAdapter;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * A user
 */

@JsonAdapter(UserGsonAdapter.class)
@Entity(tableName = "stream_user")
public class User implements UserEntity {

    @PrimaryKey
    @NonNull
    private String id;

    private String name;

    private String image;

    private String role;

    @TypeConverters(DateConverter.class)
    private Date createdAt;

    @TypeConverters(DateConverter.class)
    private Date updatedAt;

    @TypeConverters(DateConverter.class)
    private Date lastActive;

    @Ignore
    private Boolean online;

    private Boolean invisible;

    @Ignore
    private Boolean banned;

    @Ignore
    private List<Mute> mutes;

    @Ignore
    private List<Device> devices;

    private Integer totalUnreadCount;

    private Integer unreadChannels;

    @TypeConverters(ExtraDataConverter.class)
    private HashMap<String, Object> extraData;

    /**
     * Constructor
     *
     */
    @Ignore
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
    public User(String id, Map<String, Object> extraData) {
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

    public void setUpdatedAt(Date updatedAt) {
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
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        // we compare based on the CID
        User otherUser = (User) obj;
        return Objects.equals(id, otherUser.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
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

    public Boolean getInvisible() {
        return invisible;
    }

    public void setInvisible(Boolean invisible) {
        this.invisible = invisible;
    }


    /**
     * Returns true if the other user is muted
     */
    public boolean hasMuted(User user) {
        if (mutes == null || mutes.size() == 0)
            return false;
        for (Mute mute : getMutes()) {
            if (mute.getTarget().getId().equals(user.getId()))
                return true;
        }
        return false;
    }

    public List<Mute> getMutes() {
        return mutes;
    }

    public void setMutes(List<Mute> mutes) {
        this.mutes = mutes;
    }

    public List<Device> getDevices() {
        return devices;
    }

    public void setDevices(List<Device> devices) {
        this.devices = devices;
    }

    public Boolean getBanned() {
        return banned;
    }

    public void setBanned(Boolean banned) {
        this.banned = banned;
    }

    @Override
    public String toString() {
        return "User{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
