package io.getstream.chat.android.core.poc.library.socket

import com.google.gson.annotations.SerializedName
import io.getstream.chat.android.core.poc.library.errors.ChatHttpError
import io.getstream.chat.android.core.poc.library.json.ChatGson
import okhttp3.Response


class ErrorResponse {

    val code: Int = -1
    var message: String = ""
    var duration: String = ""

    @SerializedName("StatusCode")
    var statusCode: Int = -1

    //TODO: move parsing logic out of the model
    companion object {

        private fun parseError(body: String?): ErrorResponse {
            return try {
                ChatGson.instance.fromJson(body, ErrorResponse::class.java)
            } catch (e: Exception) {
                ErrorResponse().apply {
                    message = e.message.toString() + " from body: " + body
                }
            }
        }

        fun parseError(okHttpResponse: Response): ChatHttpError {

            var statusCode = -1

            return try {

                statusCode = okHttpResponse.code

                val body = okHttpResponse.peekBody(Long.MAX_VALUE).string()
                val error = parseError(body)
                ChatHttpError(error.code, statusCode, error.message)
            } catch (t: Throwable) {
                //TODO: check java.lang.IllegalStateException: Cannot read raw response body of a converted body.
                ChatHttpError(okHttpResponse.code, statusCode, t.message.toString(), t)
            }
        }
    }
}
