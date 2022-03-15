package com.getstream.sdk.chat.disposable

/**
 * Wrapper around the Coil Disposable.
 *
 * @param disposable [coil.request.Disposable]
 */
public class CoilDisposable(private val disposable: coil.request.Disposable) : Disposable {

    override val isDisposed: Boolean
        get() = disposable.isDisposed

    /**
     * Dispose all the source. Use it when the resource is already used or the result is no longer needed.
     */
    override fun dispose() {
        disposable.dispose()
    }
}
