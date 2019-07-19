package com.getstream.sdk.chat.model;

import com.getstream.sdk.chat.utils.Global;
import com.getstream.sdk.chat.utils.StringUtility;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.Map;
/**
 * A user
 */

public class User {
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
    private String created_at;

    @SerializedName("updated_at")
    @Expose
    private String updated_at;

    @SerializedName("last_active")
    @Expose
    private String last_active;

    @SerializedName("online")
    @Expose
    private Boolean online;

    private Map<String, Object>additionalFields;

    public String getCreated_at() {
        return created_at;
    }

    public void setCreated_at(String created_at) {
        this.created_at = created_at;
    }

    public String getUpdated_at() {
        return updated_at;
    }

    public void setUpdated_at(String updated_at) {
        this.updated_at = updated_at;
    }

    public String getLast_active() {
        return last_active;
    }

    public void setLast_active(String last_active) {
        this.last_active = last_active;
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

    /**
    * Constructor
    * @param id User id
    * @param name User name
    * @param additionalFields Custom user fields
    * */
    public User(String id, String name, Map<String,Object> additionalFields) {
        this.id = id;
        this.name = name;
        this.online = false;
        this.additionalFields = additionalFields;
    }

    public Map<String, Object> getAdditionalFields() {
        return additionalFields;
    }

    public boolean isMe(){
        return id.equals(Global.streamChat.getUser().getId());
    }

    public String getUserInitials() {
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
