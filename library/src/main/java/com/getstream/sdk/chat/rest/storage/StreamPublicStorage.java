package com.getstream.sdk.chat.rest.storage;

import androidx.annotation.Nullable;

import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.LazyHeaders;
import com.getstream.sdk.chat.interfaces.CachedTokenProvider;
import com.getstream.sdk.chat.model.Channel;
import com.getstream.sdk.chat.rest.controller.APIService;
import com.getstream.sdk.chat.rest.controller.RetrofitClient;
import com.getstream.sdk.chat.rest.core.ApiClientOptions;
import com.getstream.sdk.chat.rest.core.Client;
import com.getstream.sdk.chat.rest.interfaces.CompletableCallback;
import com.getstream.sdk.chat.rest.interfaces.UploadFileCallback;
import com.getstream.sdk.chat.rest.response.CompletableResponse;
import com.getstream.sdk.chat.rest.response.ErrorResponse;
import com.getstream.sdk.chat.rest.response.UploadFileResponse;
import com.getstream.sdk.chat.utils.ProgressRequestBody;

import org.jetbrains.annotations.NotNull;

import java.io.File;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * The default CDN backed storage implementation for chat.
 * Note that all files uploaded here are public by default
 * <p>
 * There are a few common scenarios with file uploads
 * - everything is public (livestream)
 * - visibility is restricted to which channels you are allowed to read
 * <p>
 * The storage should also expose the
 * - upload progress
 * - ability to delete a file
 * <p>
 * For the channel visibility the typical workflow is this:
 * - the image is uploaded to a path that contains the channel cid
 * - when you read the image you pass a user token
 * - a proxy service uses the token to authorize a user
 * - the proxy service checks if that user has permission to read that channel
 * - token generation is sometimes done differently than for other API endpoints
 * <p>
 * Alternatively some apps like Slack simply proxy static files behind their auth.
 * So if you logout images are immediately not available.
 * (The disadvantage of this approach is that it mostly breaks your CDN usage)
 * (You could work around it with something like Lamdba on the edge)
 * <p>
 * Some CDNs will need to add an authorization token/header to the images they are requesting
 * One challenge with this is that we use various different flows for loading files
 * <p>
 * - You can open a file when clicking on the link
 * - Images are loaded via Glide
 * - Video also opens a file when clicking on the link
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


    @Override
    public void deleteFile(@NotNull Channel channel, @NotNull String url, @NotNull CompletableCallback callback) {
        mCDNService.deleteFile(channel.getType(), channel.getId(), client.getApiKey(), client.getClientID(), url)
                .enqueue(new Callback<CompletableResponse>() {
                    @Override
                    public void onResponse(Call<CompletableResponse> call, Response<CompletableResponse> response) {
                        callback.onSuccess(response.body());
                    }

                    @Override
                    public void onFailure(Call<CompletableResponse> call, Throwable t) {
                        if (t instanceof ErrorResponse) {
                            callback.onError(t.getMessage(), ((ErrorResponse) t).getCode());
                        } else {
                            callback.onError(t.getLocalizedMessage(), -1);
                        }
                    }
                });
    }

    @Override
    public void deleteImage(@NotNull Channel channel, @NotNull String url, @NotNull CompletableCallback callback) {
        mCDNService.deleteImage(channel.getType(), channel.getId(), client.getApiKey(), client.getClientID(), url)
                .enqueue(new Callback<CompletableResponse>() {
                    @Override
                    public void onResponse(Call<CompletableResponse> call, Response<CompletableResponse> response) {
                        callback.onSuccess(response.body());
                    }

                    @Override
                    public void onFailure(Call<CompletableResponse> call, Throwable t) {
                        if (t instanceof ErrorResponse) {
                            callback.onError(t.getMessage(), ((ErrorResponse) t).getCode());
                        } else {
                            callback.onError(t.getLocalizedMessage(), -1);
                        }
                    }
                });
    }

    public String signFileUrl(String url) {
        return url;
    }

    @Nullable
    public GlideUrl signGlideUrl(@Nullable String url) {
        if (url == null) return null;
        return new GlideUrl(url, new LazyHeaders.Builder()
                .addHeader("X-requested-by", "stream")
                .build());
    }
}
