package com.getstream.sdk.chat.disposable

public interface Disposable {

    public val isDisposed: Boolean

    public fun dispose()
}
