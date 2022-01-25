package com.getstream.sdk.chat.disposable

public fun Disposable.addToDisposableList(disposableList: DisposableList) {
    disposableList.add(this)
}
