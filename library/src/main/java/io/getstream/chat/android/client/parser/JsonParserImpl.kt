package io.getstream.chat.android.client.parser

import com.google.gson.ExclusionStrategy
import com.google.gson.FieldAttributes
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import io.getstream.chat.android.client.utils.Result
import io.getstream.chat.android.client.errors.ChatError
import io.getstream.chat.android.client.errors.ChatNetworkError
import io.getstream.chat.android.client.socket.ErrorResponse
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class JsonParserImpl : JsonParser {

    private val gson: Gson by lazy {
        GsonBuilder()
            .registerTypeAdapterFactory(TypeAdapterFactory())
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
            .setPrettyPrinting()
            .addSerializationExclusionStrategy(object : ExclusionStrategy {
                override fun shouldSkipClass(clazz: Class<*>): Boolean {
                    return false
                }

                override fun shouldSkipField(f: FieldAttributes): Boolean {
                    return f.getAnnotation(IgnoreSerialisation::class.java) != null
                }

            })
            .addDeserializationExclusionStrategy(object : ExclusionStrategy {
                override fun shouldSkipClass(clazz: Class<*>): Boolean {
                    return false
                }

                override fun shouldSkipField(f: FieldAttributes): Boolean {
                    return f.getAnnotation(IgnoreDeserialisation::class.java) != null
                }

            })
            .create()
    }

    override fun toJson(any: Any): String {
        return gson.toJson(any)
    }

    override fun <T> fromJson(raw: String, clazz: Class<T>): T {
        return gson.fromJson(raw, clazz)
    }

    override fun <T> fromJsonOrError(raw: String, clazz: Class<T>): Result<T> {
        return try {
            Result(
                fromJson(raw, clazz),
                null
            )
        } catch (t: Throwable) {
            Result(
                null,
                ChatError("fromJsonOrError error parsing of $clazz into $raw", t)
            )
        }
    }

    override fun toError(okHttpResponse: Response): ChatNetworkError {

        var statusCode = -1

        return try {

            statusCode = okHttpResponse.code

            val body = okHttpResponse.peekBody(Long.MAX_VALUE).string()
            val error = toError(body)
            ChatNetworkError(error.message, streamCode = error.code, statusCode = statusCode)
        } catch (t: Throwable) {
            ChatNetworkError(t.message.toString(), t, okHttpResponse.code, statusCode)
        }
    }

    override fun configRetrofit(builder: Retrofit.Builder): Retrofit.Builder {
        return builder
            .addConverterFactory(RequestsBodiesConverter(gson))
            .addConverterFactory(GsonConverterFactory.create(gson))
    }

    private fun toError(body: String?): ErrorResponse {

        if (body == null) return ErrorResponse(message = "Body is null")

        return try {
            fromJson(body, ErrorResponse::class.java)
        } catch (e: Throwable) {
            ErrorResponse().apply {
                message = e.message.toString() + " from body: " + body
            }
        }
    }
}