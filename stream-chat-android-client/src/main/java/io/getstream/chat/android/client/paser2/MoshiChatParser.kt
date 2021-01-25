package io.getstream.chat.android.client.paser2

import io.getstream.chat.android.client.errors.ChatNetworkError
import io.getstream.chat.android.client.parser.ChatParser
import io.getstream.chat.android.client.utils.Result
import okhttp3.Response
import retrofit2.Retrofit

internal class MoshiChatParser : ChatParser {

    override fun toJson(any: Any): String {
        TODO("Not yet implemented")
    }

    override fun <T : Any> fromJson(raw: String, clazz: Class<T>): T {
        TODO("Not yet implemented")
    }

    override fun <T : Any> fromJsonOrError(raw: String, clazz: Class<T>): Result<T> {
        TODO("Not yet implemented")
    }

    override fun toError(okHttpResponse: Response): ChatNetworkError {
        TODO("Not yet implemented")
    }

    override fun configRetrofit(builder: Retrofit.Builder): Retrofit.Builder {
        TODO("Not yet implemented")
    }
}
