package io.getstream.chat.android.client.api

/**
 * Marks an API as requiring authentication.
 *
 * May be overridden by [ChatClientConfig.isAnonymous] at runtime.
 *
 * For differences between authenticated and anonymous behaviour, see
 * [HeadersInterceptor] and [TokenAuthInterceptor].
 */
@Retention(AnnotationRetention.RUNTIME)
@Target(AnnotationTarget.CLASS)
internal annotation class AuthenticatedApi
