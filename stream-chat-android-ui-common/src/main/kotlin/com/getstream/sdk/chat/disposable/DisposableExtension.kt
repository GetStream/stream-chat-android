package com.getstream.sdk.chat.disposable

/**
 * Helper function to add a new disposable to disposableList
 *
 * @param disposableList [DisposableList]
 */
public fun Disposable.addToDisposableList(disposableList: DisposableList) {
    disposableList.add(this)
}
