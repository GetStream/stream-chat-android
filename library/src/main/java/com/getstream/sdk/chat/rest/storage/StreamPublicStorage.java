package com.getstream.sdk.chat.rest.storage;

import com.getstream.sdk.chat.interfaces.CachedTokenProvider;
import com.getstream.sdk.chat.model.Channel;
import com.getstream.sdk.chat.rest.controller.APIService;
import com.getstream.sdk.chat.rest.controller.RetrofitClient;
import com.getstream.sdk.chat.rest.core.ApiClientOptions;
import com.getstream.sdk.chat.rest.core.Client;
import com.getstream.sdk.chat.rest.interfaces.SendFileCallback;
import com.getstream.sdk.chat.rest.response.ErrorResponse;
import com.getstream.sdk.chat.rest.response.FileSendResponse;

import java.io.File;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * The default CDN backed storage implementation for chat.
 * Note that all files uploaded here are public by default
 *
 * There are a few common scenarios with file uploads
 * - everything is public (livestream)
 * - visibility is restricted to which channels you are allowed to read
 *
 * For the channel visibility the typical workflow is this:
 * - the image is uploaded to a path that contains the channel cid
 * - when you read the image you pass a user token
 * - a proxy service uses the token to authorize a user
 * - the proxy service checks if that user has permission to read that channel
 * - token generation is sometimes done differently than for other API endpoints
 *
 */
public class StreamPublicStorage {
    private Client client;
    private APIService mCDNService;


    public StreamPublicStorage(Client client, CachedTokenProvider tokenProvider, ApiClientOptions options) {
        this.client = client;

        mCDNService = RetrofitClient.getAuthorizedCDNClient(tokenProvider, options).create(APIService.class);
    }

    public void sendFile(Channel channel, File file, String mimeType, SendFileCallback callback) {
        RequestBody fileReqBody = RequestBody.create(MediaType.parse(mimeType), file);
        MultipartBody.Part part = MultipartBody.Part.createFormData("file", file.getName(), fileReqBody);

        Callback<FileSendResponse> callbackWrapper = new Callback<FileSendResponse>() {
            @Override
            public void onResponse(Call<FileSendResponse> call, Response<FileSendResponse> response) {
                callback.onSuccess(response.body());
            }

            @Override
            public void onFailure(Call call, Throwable t) {
                if (t instanceof ErrorResponse) {
                    callback.onError(t.getMessage(), ((ErrorResponse) t).getCode());
                } else {
                    String errorMsg = t.getLocalizedMessage();
                    if (t.getLocalizedMessage().toLowerCase().equals("timeout"))
                        errorMsg = "The file is too large to upload!";

                    callback.onError(errorMsg, -1);
                }
            }
        };

        // TODO detect if it's an image or not
        mCDNService.sendFile(channel.getType(), channel.getId(), part, client.getApiKey(), client.getUserId(), client.getClientID()).enqueue(callbackWrapper);
    }
}
