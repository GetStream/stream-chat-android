package com.getstream.sdk.chat.disposable

public class CoilDisposable(private val disposable: coil.request.Disposable) : Disposable {

    override val isDisposed: Boolean
        get() = disposable.isDisposed

    override fun dispose() {
        disposable.dispose()
    }
}
