package com.getstream.sdk.chat.disposable

/**
 * List of disposables.
 */
public class DisposableList {

    private val disposables: MutableList<Disposable> = mutableListOf()

    /**
     * Add a new disposable to the list
     */
    public fun add(disposable: Disposable) {
        disposables.add(disposable)
    }

    /**
     * Disposes all disposables and clears the list
     */
    public fun clear() {
        disposables.forEach { disposable ->
            disposable.dispose()
        }

        disposables.clear()
    }
}
