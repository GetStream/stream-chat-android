package io.getstream.chat.android.offline.experimental.interceptor

import io.getstream.chat.android.client.experimental.interceptor.Interceptor
import io.getstream.chat.android.client.experimental.interceptor.SendMessageInterceptor
import io.getstream.chat.android.core.ExperimentalStreamChatApi
import io.getstream.chat.android.core.internal.InternalStreamChatApi

/**
 * Implementation of [Interceptor] that brings support for intercepting API requests. This class work as a delegator of calls for one
 * of its dependencies, so avoid to add logic here.
 *
 * @param sendMessageInterceptor [SendMessageInterceptor]
 */
@InternalStreamChatApi
@ExperimentalStreamChatApi
internal class DefaultInterceptor(sendMessageInterceptor: SendMessageInterceptor) :
    Interceptor,
    SendMessageInterceptor by sendMessageInterceptor
