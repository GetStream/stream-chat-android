package io.getstream.chat.android.core.poc.library.api

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.TypeAdapter
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import io.getstream.chat.android.core.poc.library.CachedTokenProvider
import io.getstream.chat.android.core.poc.library.QueryChannelsRequest
import io.getstream.chat.android.core.poc.library.socket.ErrorResponse
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.lang.reflect.Type
import java.util.concurrent.TimeUnit


object RetrofitClient {

    private val TAG = RetrofitClient::class.java.simpleName

    private val gson = GsonBuilder()
        .registerTypeAdapterFactory(TypeAdapterFactory())
        .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
        .create()

    fun getClient(
        options: ApiClientOptions,
        tokenProvider: CachedTokenProvider,
        anonymousAuth: () -> Boolean
    ): Retrofit? {

        var authInterceptor = TokenAuthInterceptor(tokenProvider, anonymousAuth)

        val logging = HttpLoggingInterceptor()
        logging.setLevel(HttpLoggingInterceptor.Level.BODY)

        val clientBuilder = OkHttpClient.Builder()
            .connectTimeout(options.timeout.toLong(), TimeUnit.MILLISECONDS)
            .writeTimeout(options.timeout.toLong(), TimeUnit.MILLISECONDS)
            .readTimeout(options.timeout.toLong(), TimeUnit.MILLISECONDS)
            .addInterceptor(logging)
            .addInterceptor { chain: Interceptor.Chain ->
                val request: Request = chain.request()
                val response: Response = chain.proceed(request)
                if (!response.isSuccessful) {
                    throw ErrorResponse.parseError(response)
                }
                response
            }
            .addInterceptor { chain: Interceptor.Chain ->
                chain.proceed(
                    prepareRequest(chain, anonymousAuth())
                )
            }
            .followRedirects(false)

        clientBuilder.addInterceptor(authInterceptor)

        return Retrofit.Builder()
            .baseUrl(options.httpURL)
            .client(clientBuilder.build())
            .addConverterFactory(ZConverter(gson))
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    fun getAuthorizedCDNClient(
        tokenProvider: CachedTokenProvider,
        options: ApiClientOptions
    ): Retrofit {
        val authInterceptor = TokenAuthInterceptor(tokenProvider) { false }
        val client = OkHttpClient.Builder()
            .connectTimeout(options.cdntimeout.toLong(), TimeUnit.MILLISECONDS)
            .writeTimeout(options.cdntimeout.toLong(), TimeUnit.MILLISECONDS)
            .readTimeout(options.cdntimeout.toLong(), TimeUnit.MILLISECONDS)
            .addInterceptor { chain: Interceptor.Chain ->
                val request: Request = chain.request()
                val response: Response = chain.proceed(request)
                if (!response.isSuccessful) {
                    throw ErrorResponse.parseError(response)
                }
                response
            }
            .addInterceptor { chain: Interceptor.Chain ->
                val request: Request = chain.request()
                    .newBuilder()
                    .addHeader("Content-Type", "application/json")
                    .addHeader("stream-auth-type", "jwt")
                    .addHeader("Accept-Encoding", "application/gzip")
                    .build()
                chain.proceed(request)
            }
            .addInterceptor(authInterceptor)
            .followRedirects(false)
            .build()
        return Retrofit.Builder()
            .baseUrl(options.cdnHttpURL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create(Gson()))
            .build()
    }

    private fun prepareRequest(
        chain: Interceptor.Chain,
        isAnonymousClient: Boolean
    ): Request {
        val authType = if (isAnonymousClient) "anonymous" else "jwt"
        return chain.request()
            .newBuilder()
            .addHeader("Content-Type", "application/json")
            .addHeader("stream-auth-type", authType)
            .addHeader("Accept-Encoding", "application/gzip")
            .build()
    }

    private fun createGsonConverter(
        type: Type,
        typeAdapter: Any
    ): Converter.Factory {
        val gsonBuilder = GsonBuilder()
        gsonBuilder.registerTypeAdapter(type, typeAdapter)
        return GsonConverterFactory.create(gsonBuilder.create())
    }

    class TypeAdapterFactory : com.google.gson.TypeAdapterFactory {
        override fun <T> create(gson: Gson, type: TypeToken<T>): TypeAdapter<T>? {
            if (type.rawType == QueryChannelsRequest::class) {
                return QueryChannelsAdapter(gson, type) as TypeAdapter<T>
            } else {
                //TODO: replace with chat error
                //throw RuntimeException("undefined type: " + type.rawType)
                return null
            }
        }
    }

    private class ZConverter(val gson: Gson) : Converter.Factory() {
        override fun stringConverter(
            type: Type,
            annotations: Array<Annotation?>,
            retrofit: Retrofit
        ): Converter<*, String>? {
            return if (type === QueryChannelsRequest::class.java) {
                C(gson)
            } else null
        }
    }

    private class C(val gson: Gson) : Converter<QueryChannelsRequest, String> {
        override fun convert(value: QueryChannelsRequest): String {
            return gson.toJson(value)
        }
    }

    private class QueryChannelsAdapter(
        val gson: Gson,
        val type: TypeToken<*>
    ) : TypeAdapter<QueryChannelsRequest>() {

        override fun write(writer: JsonWriter, value: QueryChannelsRequest) {
            gson.toJson(value, type.type, writer)
        }

        override fun read(reader: JsonReader): QueryChannelsRequest {
            return gson.fromJson(reader, type.rawType)
        }
    }

}
