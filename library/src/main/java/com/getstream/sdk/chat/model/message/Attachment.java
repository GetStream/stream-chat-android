package com.getstream.sdk.chat.model.message;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Attachment {
    @SerializedName("title")
    @Expose
    private String title;

    @SerializedName("author_name")
    @Expose
    private String author;

    @SerializedName("text")
    @Expose
    private String text;

    @SerializedName("type")
    @Expose
    private String type;

    @SerializedName("image")
    @Expose
    private String image;

    @SerializedName("url")
    @Expose
    private String url;

    @SerializedName("name")
    @Expose
    private String name;

    @SerializedName("title_link")
    @Expose
    private String titleLink;

    @SerializedName("thumb_url")
    @Expose
    private String thumbURL;

    @SerializedName("fallback")
    @Expose
    private String fallback;

    @SerializedName("image_url")
    @Expose
    private String imageURL;

    @SerializedName("asset_url")
    @Expose
    private String assetURL;

    @SerializedName("og_scrape_url")
    @Expose
    private String ogURL;

    @SerializedName("mime_type")
    @Expose
    private String mime_type;

    @SerializedName("file_size")
    @Expose
    private int file_size;

    public Config config = new Config(); // Local file Attach Config

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTitleLink() {
        return titleLink;
    }

    public void setTitleLink(String titleLink) {
        this.titleLink = titleLink;
    }

    public String getThumbURL() {
        return thumbURL;
    }

    public void setThumbURL(String thumbURL) {
        this.thumbURL = thumbURL;
    }

    public String getFallback() {
        return fallback;
    }

    public void setFallback(String fallback) {
        this.fallback = fallback;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public String getAssetURL() {
        return assetURL;
    }

    public void setAssetURL(String assetURL) {
        this.assetURL = assetURL;
    }

    public String getOgURL() {
        return ogURL;
    }

    public void setOgURL(String ogURL) {
        this.ogURL = ogURL;
    }

    public String getMime_type() {
        return mime_type;
    }

    public void setMime_type(String mime_type) {
        this.mime_type = mime_type;
    }

    public void setFile_size(int file_size) {
        this.file_size = file_size;
    }

    public int getFile_size() {
        return file_size;
    }

    public class Config {
        private String filePath;
        private boolean isSelected = false;
        private int videoLengh = 0;
        private boolean isUploaded = false;

        public String getFilePath() {
            return filePath;
        }

        public void setFilePath(String filePath) {
            this.filePath = filePath;
        }

        public boolean isSelected() {
            return isSelected;
        }

        public void setSelected(boolean selected) {
            isSelected = selected;
        }

        public int getVideoLengh() {
            return videoLengh;
        }

        public void setVideoLengh(int videoLengh) {
            this.videoLengh = videoLengh;
        }

        public boolean isUploaded() {
            return isUploaded;
        }

        public void setUploaded(boolean uploaded) {
            isUploaded = uploaded;
        }
    }

}
