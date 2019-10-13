package com.getstream.sdk.chat.rest.codecs;

import com.getstream.sdk.chat.enums.FilterObject;
import com.getstream.sdk.chat.enums.FilterObjectAdapter;
import com.getstream.sdk.chat.enums.QuerySort;
import com.getstream.sdk.chat.enums.QuerySortAdapter;
import com.getstream.sdk.chat.model.Attachment;
import com.getstream.sdk.chat.model.Channel;
import com.getstream.sdk.chat.model.Reaction;
import com.getstream.sdk.chat.rest.Message;
import com.getstream.sdk.chat.rest.User;
import com.getstream.sdk.chat.rest.adapter.AttachmentGsonAdapter;
import com.getstream.sdk.chat.rest.adapter.ChannelGsonAdapter;
import com.getstream.sdk.chat.rest.adapter.MessageGsonAdapter;
import com.getstream.sdk.chat.rest.adapter.ReactionGsonAdapter;
import com.getstream.sdk.chat.rest.adapter.UserGsonAdapter;
import com.getstream.sdk.chat.rest.response.ChannelState;
import com.getstream.sdk.chat.rest.response.ChannelStateGsonAdapter;
import com.getstream.sdk.chat.rest.response.ErrorGsonAdapter;
import com.getstream.sdk.chat.rest.response.ErrorResponse;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class GsonConverter {
    private static Gson gson;

    public static Gson Gson() {
        if (gson == null) {
            GsonBuilder gsonBuilder = new GsonBuilder().excludeFieldsWithoutExposeAnnotation();
            gsonBuilder.setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
            gsonBuilder.registerTypeAdapter(FilterObject.class, new FilterObjectAdapter());
            gsonBuilder.registerTypeAdapter(QuerySort.class, new QuerySortAdapter());
            gsonBuilder.registerTypeAdapter(ChannelState.class, new ChannelStateGsonAdapter());
            gsonBuilder.registerTypeAdapter(Channel.class, new ChannelGsonAdapter());
            gsonBuilder.registerTypeAdapter(User.class, new UserGsonAdapter());
            gsonBuilder.registerTypeAdapter(Message.class, new MessageGsonAdapter());
            gsonBuilder.registerTypeAdapter(Attachment.class, new AttachmentGsonAdapter());
            gsonBuilder.registerTypeAdapter(ErrorResponse.class, new ErrorGsonAdapter());
            gsonBuilder.registerTypeAdapter(Reaction.class, new ReactionGsonAdapter());
            gson = gsonBuilder.create();
        }
        return gson;
    }
}
