package com.getstream.sdk.chat.rest;

import android.text.TextUtils;

import androidx.annotation.Nullable;

import com.getstream.sdk.chat.utils.StringUtility;
import com.google.gson.annotations.SerializedName;

import java.util.Date;
import java.util.HashMap;

/**
 * A user
 */

public class User {
    @SerializedName("id")
    private String id;

    @SerializedName("name")
    private String name;

    @SerializedName("image")
    private String image;

    @SerializedName("role")
    private String role;

    @SerializedName("created_at")
    private Date createdAt;

    @SerializedName("updated_at")
    private Date updatedAt;

    @SerializedName("last_active")
    private Date lastActive;

    @SerializedName("online")
    private Boolean online;

    private HashMap<String, Object> extraData;

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

    @Override
    public boolean equals(@Nullable Object obj) {
        if (getClass() != obj.getClass()) {
            return false;
        }
        // we compare based on the CID
        User otherUser = (User) obj;
        return TextUtils.equals(this.getId(), otherUser.getId());
    }

    /**
     * Constructor
     * @param id User id
     * */
    public User(String id) {
        this(id, new HashMap<>());
    }

    /**
    * Constructor
    * @param id User id
    * @param extraData Custom user fields (ie: name, image, anything that json can serialize is ok)
    * */
    public User(String id, HashMap<String,Object> extraData) {
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

    public HashMap<String, Object> getExtraData() {
        return extraData;
    }

    // TODO: move to modelview
    public boolean isMe(){
        return false;
//        return id.equals(StreamChat.getInstance().getUserId());
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

        if (!StringUtility.isNullOrEmpty(firstName) && StringUtility.isNullOrEmpty(lastName))
            return firstName.substring(0, 1).toUpperCase();
        if (StringUtility.isNullOrEmpty(firstName) && !StringUtility.isNullOrEmpty(lastName))
            return lastName.substring(0, 1).toUpperCase();

        if (!StringUtility.isNullOrEmpty(firstName) && !StringUtility.isNullOrEmpty(lastName))
            return firstName.substring(0, 1).toUpperCase() + lastName.substring(0, 1).toUpperCase();
        return null;
    }
}
