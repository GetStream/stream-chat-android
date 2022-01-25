package com.getstream.sdk.chat.disposable

public class DisposableList {

    private val disposables: MutableList<Disposable> = mutableListOf()

    public fun add(disposable: Disposable) {
        disposables.add(disposable)
    }

    public fun clear() {
        disposables.forEach { disposable ->
            disposable.dispose()
        }

        disposables.clear()
    }
}
