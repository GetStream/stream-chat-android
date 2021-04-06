package io.getstream.chat.android.client.parser2

import io.getstream.chat.android.client.logger.ChatLogger
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Converter
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.lang.reflect.Type

internal fun MoshiConverterFactory.withErrorLogging(): Converter.Factory {
    val originalFactory = this
    val logger = ChatLogger.get("NEW_SERIALIZATION_ERROR")

    return object : Converter.Factory() {
        override fun responseBodyConverter(
            type: Type,
            annotations: Array<out Annotation>,
            retrofit: Retrofit,
        ): Converter<ResponseBody, *> {
            val originalConverter: Converter<ResponseBody, *> =
                originalFactory.responseBodyConverter(type, annotations, retrofit)!!
            return Converter { value ->
                try {
                    originalConverter.convert(value)
                } catch (e: Throwable) {
                    logger.logE(e)
                    throw e
                }
            }
        }

        override fun requestBodyConverter(
            type: Type,
            parameterAnnotations: Array<out Annotation>,
            methodAnnotations: Array<out Annotation>,
            retrofit: Retrofit,
        ): Converter<*, RequestBody>? {
            return originalFactory.requestBodyConverter(type, parameterAnnotations, methodAnnotations, retrofit)
        }

        override fun stringConverter(
            type: Type,
            annotations: Array<out Annotation>,
            retrofit: Retrofit,
        ): Converter<*, String>? {
            return originalFactory.stringConverter(type, annotations, retrofit)
        }
    }
}
