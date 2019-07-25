package com.getstream.sdk.chat.model;

import com.getstream.sdk.chat.utils.StringUtility;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.HashMap;
import java.util.Map;

/**
 * A channel
 */
public class Channel {
    @SerializedName("id")
    @Expose
    private String id;

    @SerializedName("cid")
    @Expose
    private String cid;

    @SerializedName("type")
    @Expose
    private String type;

    @SerializedName("last_message_at")
    @Expose
    private String lastMessageDate;

    @SerializedName("created_by")
    @Expose
    private User createdByUser;

    @SerializedName("frozen")
    @Expose
    private boolean frozen;

    @SerializedName("config")
    @Expose
    private Config config;

    @SerializedName("name")
    @Expose
    private String name;

    @SerializedName("image")
    @Expose
    private String image;

    private Map<String, Object> extraData;


    public String getId() {
        return id;
    }

    public String getCid() {
        return cid;
    }

    public String getType() {
        return type;
    }

    public String getLastMessageDate() {
        return lastMessageDate;
    }

    public User getCreatedByUser() {
        return createdByUser;
    }

    public boolean isFrozen() {
        return frozen;
    }

    public Config getConfig() {
        return config;
    }

    public String getName() {
        return name;
    }

    public String getImage() {
        return image;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setCid(String cid) {
        this.cid = cid;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setLastMessageDate(String lastMessageDate) {
        this.lastMessageDate = lastMessageDate;
    }

    public void setCreatedByUser(User createdByUser) {
        this.createdByUser = createdByUser;
    }

    public void setFrozen(boolean frozen) {
        this.frozen = frozen;
    }

    public void setConfig(Config config) {
        this.config = config;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public Channel() {

    }

    public Map<String, Object> getExtraData() {
        return extraData;
    }

    /**
     * Constructor
     *
     * @param type      Channel type
     * @param id        Channel id
     * @param extraData Custom channel fields
     */
    public Channel(String type, String id, HashMap<String, Object> extraData) {
        this.type = type;
        this.id = id;

        // since name and image are very common fields, we are going to promote them as
        Object image = this.extraData.remove("image");
        if (image != null) {
            this.image = image.toString();
        }

        Object name = this.extraData.remove("name");
        if (name != null) {
            this.name = name.toString();
        }

        this.extraData = extraData;
    }

    /**
     * Constructor
     *
     * @param type      Channel type
     * @param id        Channel id
     * @param extraData Custom channel fields
     */
    public Channel(String type, String id, Map<String, Object> extraData) {
        this.type = type;
        this.id = id;
        this.extraData = extraData;
    }

    public String getInitials() {
        String name = this.name;
        if (name == null) {
            this.name = "";
            return "";
        }
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