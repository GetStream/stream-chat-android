package com.getstream.sdk.chat.rest;

import com.getstream.sdk.chat.enums.FilterObject;
import com.getstream.sdk.chat.enums.QueryOptions;
import com.getstream.sdk.chat.enums.QuerySort;
import com.getstream.sdk.chat.rest.controller.ClientRetrofit;
import com.getstream.sdk.chat.rest.response.GetChannelsResponse;
import com.getstream.sdk.chat.utils.Global;

import org.json.JSONException;
import org.json.JSONObject;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Client {
    private ClientRetrofit retrofitClient;
    protected Retrofit RetrofitServiceFactory;

    public String getApiKey() {
        return ApiKey;
    }

    private String ApiKey;
    protected String UserToken;
    protected String BaseURL;
    protected User UserData;
    protected String ConnectionID;

    public Client(String ApiKey){
        this.ApiKey = ApiKey;
        this.retrofitClient = this.getRetrofitServiceFactory().create(ClientRetrofit.class);
    }

    public void setUser(User user, String token){}

    public void disconnect(){}

    public void setAnonymousUser(){}

    public void setGuestUser(){}

    public void on(){}

    public void off(){}

    public void sendFile(){}

    public void queryUsers(){}

    public Call<GetChannelsResponse> queryChannels(FilterObject filterConditions, QuerySort sort, QueryOptions options) throws JSONException {
        JSONObject payload = new JSONObject();
        payload.put("filter_conditions", filterConditions);
        payload.put("sort", sort);
        payload.put("user_details", UserData);
        payload.put("options", options);
        return this.retrofitClient.queryChannels(ApiKey, UserData.getId(), ConnectionID, payload);
    }

    public void addDevice(){}

    public void getDevices(){}

    public void removeDevice(){}

    public Channel channel(){
        return new Channel();
    }

    public void muteUser(){}

    public void unmuteUser(){}

    public void flagMessage(){}

    public void unflagMessage(){}

    public void markAllRead(){}

    public void updateMessage(){}

    public void deleteMessage(){}

    public void getMessage(){}

    public Retrofit getRetrofitServiceFactory() {
        if (RetrofitServiceFactory != null) {
            return RetrofitServiceFactory;
        }

        HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(interceptor)
                .addInterceptor(loggingInterceptor)
                .addInterceptor(chain -> {
                    Request request = chain.request()
                            .newBuilder()
                            .addHeader("Authorization", Global.streamChat.getUserToken())
                            .addHeader("Content-Type", "application/json")
                            .addHeader("stream-auth-type", "jwt")
                            .addHeader("Accept-Encoding", "application/gzip")
                            .build();
                    Response response = chain.proceed(request);
                    return response;
                })
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(this.BaseURL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        return retrofit;
    }
}
