package io.getstream.chat.android.client.parser

/**
 * Some requests passing payload as url query parameter.
 * This annotations tells gson to convert it to json, rather that call [toString]
 * See [UrlQueryPayloadFactory]
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.VALUE_PARAMETER)
internal annotation class UrlQueryPayload
