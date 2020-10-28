package io.getstream.chat.android.client.api

/**
 * Marks an API as requiring no authentication.
 *
 * For differences between authenticated and anonymous behaviour, see
 * [HeadersInterceptor] and [TokenAuthInterceptor].
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
internal annotation class AnonymousApi
