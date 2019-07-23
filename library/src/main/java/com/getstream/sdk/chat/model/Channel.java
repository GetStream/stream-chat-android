package com.getstream.sdk.chat.model;

import com.getstream.sdk.chat.utils.StringUtility;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

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
    private String imageURL;

    @SerializedName("example")
    @Expose
    private int example;

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

    public String getImageURL() {
        return imageURL;
    }

    public int getExample() {
        return example;
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

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public void setExample(int example) {
        this.example = example;
    }

    public Channel() {

    }

    public Map<String, Object> getExtraData() {
        return extraData;
    }

    /**
     * Constructor
     *
     * @param type        Channel type
     * @param id        Channel id
     * @param name      Channel name
     * @param image     Channel image
     * @param extraData Custom channel fields
     */
    public Channel(String type, String id, String name, String image, Map<String, Object> extraData) {
        this.type = type;
        this.id = id;
        this.name = name;
        this.imageURL = image;
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