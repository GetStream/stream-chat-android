package io.getstream.chat.android.offline.event.handler.internal.model


internal class ParameterizedLazy<T, R>(private val initializer: suspend (T) -> R) : suspend (T) -> R {
    @Volatile
    private var _value: R? = null

    override suspend fun invoke(param: T): R {
        if (_value == null) {
            _value = initializer(param)
        }
        return _value!!
    }

    fun isInitialized(): Boolean = _value != null

}

internal fun <T, R> parameterizedLazy(initializer: suspend (T) -> R): ParameterizedLazy<T, R> = ParameterizedLazy(
    initializer
)