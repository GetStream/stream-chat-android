package com.getstream.sdk.chat.rest.adapter;

import com.getstream.sdk.chat.StreamChat;
import com.getstream.sdk.chat.model.Attachment;
import com.getstream.sdk.chat.model.ModelType;
import com.getstream.sdk.chat.rest.codecs.GsonConverter;
import com.google.gson.Gson;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class AttachmentGsonAdapter extends TypeAdapter<Attachment> {

    private static final String TAG = AttachmentGsonAdapter.class.getSimpleName();

    @Override
    public void write(JsonWriter writer, Attachment attachment) throws IOException {

        HashMap<String, Object> data = new HashMap<>();

        if (attachment.getExtraData() != null && !attachment.getExtraData().isEmpty())
            for (Map.Entry<String, Object> set : attachment.getExtraData().entrySet())
                data.put(set.getKey(), set.getValue());

        if (attachment.getTitle() != null)
            data.put("title", attachment.getTitle());

        if (attachment.getAuthor() != null)
            data.put("author_name", attachment.getAuthor());

        if (attachment.getText() != null)
            data.put("text", attachment.getText());

        if (attachment.getType() != null)
            data.put("type", attachment.getType());

        if (attachment.getImage() != null)
            data.put("image", attachment.getImage());

        if (attachment.getUrl() != null)
            data.put("url", attachment.getUrl());

        if (attachment.getName() != null)
            data.put("name", attachment.getName());

        if (attachment.getTitleLink() != null)
            data.put("title_link", attachment.getTitleLink());

        if (attachment.getThumbURL() != null)
            data.put("thumb_url", attachment.getThumbURL());

        if (attachment.getFallback() != null)
            data.put("fallback", attachment.getFallback());

        if (attachment.getImageURL() != null)
            data.put("image_url", attachment.getImageURL());

        if (attachment.getAssetURL() != null)
            data.put("asset_url", attachment.getAssetURL());

        if (attachment.getOgURL() != null)
            data.put("og_scrape_url", attachment.getOgURL());

        if (attachment.getMime_type() != null)
            data.put("mime_type", attachment.getMime_type());

        data.put("file_size", attachment.getFile_size());

        TypeAdapter adapter = GsonConverter.Gson().getAdapter(HashMap.class);
        adapter.write(writer, data);
    }

    @Override
    public Attachment read(JsonReader reader) throws IOException {

        Gson gson = GsonConverter.Gson();

        TypeAdapter adapter = gson.getAdapter(HashMap.class);
        HashMap<String, Object> value = (HashMap) adapter.read(reader);

        if (value == null) {
            return null;
        }

        Attachment attachment = new Attachment();
        HashMap<String, Object> extraData = new HashMap<>();

        for (HashMap.Entry<String, Object> set : value.entrySet()) {
            String json = gson.toJson(set.getValue());
            // Set Reserved Data
            switch (set.getKey()) {
                case "title":
                    attachment.setTitle((String) set.getValue());
                    continue;
                case "author_name":
                    attachment.setAuthor((String) set.getValue());
                    continue;
                case "text":
                    attachment.setText((String) set.getValue());
                    continue;
                case "type":
                    attachment.setType((String) set.getValue());
                    continue;
                case "image":
                    attachment.setImage((String) set.getValue());
                    continue;
                case "url":
                    attachment.setUrl((String) set.getValue());
                    continue;
                case "name":
                    attachment.setName((String) set.getValue());
                    continue;
                case "title_link":
                    attachment.setTitleLink((String) set.getValue());
                    continue;
                case "thumb_url":
                    attachment.setThumbURL((String) set.getValue());
                    continue;
                case "fallback":
                    attachment.setFallback((String) set.getValue());
                    continue;
                case "image_url":
                    attachment.setImageURL((String) set.getValue());
                    continue;
                case "asset_url":
                    attachment.setAssetURL((String) set.getValue());
                    continue;
                case "og_scrape_url":
                    attachment.setOgURL((String) set.getValue());
                    continue;
                case "mime_type":
                    attachment.setMime_type((String) set.getValue());
                    continue;
                case "file_size":
                    try {
                        double fileSize = (Double) set.getValue();
                        attachment.setFile_size((int)fileSize);
                    }catch (Exception e){
                        StreamChat.getLogger().logT(this, e);
                    }
                    continue;
            }
            // Set Extra Data
            extraData.put(set.getKey(), set.getValue());
        }

        if (attachment.getType() == null)
            attachment.setType(ModelType.attach_unknown);

        attachment.setExtraData(extraData);
        return attachment;
    }
}
