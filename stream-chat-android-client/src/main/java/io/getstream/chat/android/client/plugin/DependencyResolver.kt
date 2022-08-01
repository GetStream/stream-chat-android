package io.getstream.chat.android.client.plugin

import io.getstream.chat.android.core.internal.InternalStreamChatApi
import kotlin.reflect.KClass

@InternalStreamChatApi
public interface DependencyResolver {

    @InternalStreamChatApi
    public fun <T : Any> resolveDependency(klass: KClass<T>): T?

}