package io.getstream.chat.android.core.poc.library.api

import com.google.gson.Gson
import io.getstream.chat.android.core.poc.library.CachedTokenProvider
import io.getstream.chat.android.core.poc.library.socket.ErrorResponse
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit


object RetrofitClient {

    private val TAG = RetrofitClient::class.java.simpleName

    fun getClient(
        options: ApiClientOptions,
        tokenProvider: CachedTokenProvider,
        anonymousAuth: () -> Boolean
    ): Retrofit? {

        var authInterceptor = TokenAuthInterceptor(tokenProvider, anonymousAuth)

        val clientBuilder = OkHttpClient.Builder()
            .connectTimeout(options.timeout.toLong(), TimeUnit.MILLISECONDS)
            .writeTimeout(options.timeout.toLong(), TimeUnit.MILLISECONDS)
            .readTimeout(options.timeout.toLong(), TimeUnit.MILLISECONDS)
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
            .addConverterFactory(GsonConverterFactory.create(Gson()))
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
                if (!response.isSuccessful()) {
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

}
