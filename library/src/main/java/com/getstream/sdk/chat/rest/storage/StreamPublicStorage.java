package com.getstream.sdk.chat.rest.storage;

import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.getstream.sdk.chat.interfaces.CachedTokenProvider;
import com.getstream.sdk.chat.model.Channel;
import com.getstream.sdk.chat.rest.controller.APIService;
import com.getstream.sdk.chat.rest.controller.RetrofitClient;
import com.getstream.sdk.chat.rest.core.ApiClientOptions;
import com.getstream.sdk.chat.rest.core.Client;
import com.getstream.sdk.chat.rest.interfaces.UploadFileCallback;
import com.getstream.sdk.chat.rest.response.ErrorResponse;
import com.getstream.sdk.chat.rest.response.UploadFileResponse;
import com.getstream.sdk.chat.utils.ProgressRequestBody;

import java.io.File;

import okhttp3.MultipartBody;
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
 * The storage should also expose the
 * - upload progress
 * - ability to delete a file
 *
 * For the channel visibility the typical workflow is this:
 * - the image is uploaded to a path that contains the channel cid
 * - when you read the image you pass a user token
 * - a proxy service uses the token to authorize a user
 * - the proxy service checks if that user has permission to read that channel
 * - token generation is sometimes done differently than for other API endpoints
 *
 * Alternatively some apps like Slack simply proxy static files behind their auth.
 * So if you logout images are immediately not available.
 * (The disadvantage of this approach is that it mostly breaks your CDN usage)
 * (You could work around it with something like Lamdba on the edge)
 *
 * Some CDNs will need to add an authorization token/header to the images they are requesting
 * One challenge with this is that we use various different flows for loading files
 *
 * - You can open a file when clicking on the link
 * - Images are loaded via Glide
 * - Video also opens a file when clicking on the link
 *
 */
public class StreamPublicStorage extends BaseStorage {


    public StreamPublicStorage(Client client, CachedTokenProvider tokenProvider, ApiClientOptions options) {
        super(client);
        mCDNService = RetrofitClient.getAuthorizedCDNClient(tokenProvider, options).create(APIService.class);
    }

    public void sendFile(Channel channel, File file, String mimeType, UploadFileCallback callback) {
        ProgressRequestBody fileReqBody = new ProgressRequestBody(file, mimeType, callback);
        MultipartBody.Part part = MultipartBody.Part.createFormData("file", file.getName(), fileReqBody);

        Callback<UploadFileResponse> callbackWrapper = new Callback<UploadFileResponse>() {
            @Override
            public void onResponse(Call<UploadFileResponse> call, Response<UploadFileResponse> response) {
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

        if (mimeType.contains("image")) {
            mCDNService.sendImage(channel.getType(), channel.getId(), part, client.getApiKey(), client.getUserId(), client.getClientID()).enqueue(callbackWrapper);
        } else {
            mCDNService.sendFile(channel.getType(), channel.getId(), part, client.getApiKey(), client.getUserId(), client.getClientID()).enqueue(callbackWrapper);
        }
    }



    public String signFileUrl(String url) {
        return url;
    }


    public GlideUrl signGlideUrl(String url) {
        return new GlideUrl(url, new LazyHeaders.Builder()
                .addHeader("X-requested-by", "stream")
                .build());
    }
}
