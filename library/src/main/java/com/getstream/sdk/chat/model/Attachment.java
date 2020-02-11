package com.getstream.sdk.chat.model;

import androidx.room.TypeConverters;

import com.getstream.sdk.chat.R;
import com.getstream.sdk.chat.rest.adapter.AttachmentGsonAdapter;
import com.getstream.sdk.chat.storage.converter.ExtraDataConverter;
import com.google.gson.annotations.Expose;
import com.google.gson.annotations.JsonAdapter;
import com.google.gson.annotations.SerializedName;

import java.text.DecimalFormat;
import java.util.HashMap;

/**
 * An attachment
 */
@JsonAdapter(AttachmentGsonAdapter.class)
public class Attachment {
    public Config config = new Config(); // Local file Attach Config
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

    // Additional Params
    @TypeConverters(ExtraDataConverter.class)
    private HashMap<String, Object> extraData;

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

    public HashMap<String, Object> getExtraData() {
        return extraData;
    }

    public void setExtraData(HashMap<String, Object> extraData) {
        this.extraData = new HashMap<>(extraData);
        this.extraData.remove("id");
    }

    public int getIcon() {
        int fileTyineRes = 0;
        if (this.getMime_type() == null) {
            fileTyineRes = R.drawable.stream_ic_file;
            return fileTyineRes;
        }

        switch (this.getMime_type()) {
            case ModelType.attach_mime_pdf:
                fileTyineRes = R.drawable.stream_ic_file_pdf;
                break;
            case ModelType.attach_mime_csv:
                fileTyineRes = R.drawable.stream_ic_file_csv;
                break;
            case ModelType.attach_mime_tar:
                fileTyineRes = R.drawable.stream_ic_file_tar;
                break;
            case ModelType.attach_mime_zip:
                fileTyineRes = R.drawable.stream_ic_file_zip;
                break;
            case ModelType.attach_mime_doc:
            case ModelType.attach_mime_docx:
            case ModelType.attach_mime_txt:
                fileTyineRes = R.drawable.stream_ic_file_doc;
                break;
            case ModelType.attach_mime_xlsx:
                fileTyineRes = R.drawable.stream_ic_file_xls;
                break;
            case ModelType.attach_mime_ppt:
                fileTyineRes = R.drawable.stream_ic_file_ppt;
                break;
            case ModelType.attach_mime_mov:
            case ModelType.attach_mime_mp4:
                fileTyineRes = R.drawable.stream_ic_file_mov;
                break;
            case ModelType.attach_mime_m4a:
            case ModelType.attach_mime_mp3:
                fileTyineRes = R.drawable.stream_ic_file_mp3;
                break;
            default:
                if (this.getMime_type().contains("audio")) {
                    fileTyineRes = R.drawable.stream_ic_file_mp3;
                } else if (this.getMime_type().contains("video")) {
                    fileTyineRes = R.drawable.stream_ic_file_mov;
                }
                break;
        }
        return fileTyineRes;
    }

    public int getFile_size() {
        return file_size;
    }

    public void setFile_size(int file_size) {
        this.file_size = file_size;
    }

    public String getFileSizeHumanized() {
        int size = getFile_size();
        if (size <= 0) return "0";
        final String[] units = new String[]{"B", "kB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return new DecimalFormat("#,##0.#").format(size / Math.pow(1024, digitGroups)) + " " + units[digitGroups];
    }

    public class Config {
        private String filePath;
        private boolean isSelected = false;
        private int videoLengh = 0;
        private boolean isUploaded = false;
        private int progress = 0;

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

        public int getProgress() {
            return progress;
        }

        public void setProgress(int progress) {
            this.progress = progress;
        }

    }

}
