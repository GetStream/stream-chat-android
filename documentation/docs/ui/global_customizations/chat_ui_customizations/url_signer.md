---
id: uiGlobalCustomizationsUrlSigner
title: UrlSigner
sidebar_position: 3
---

It is possible to use a custom url signer to `ChatUi` by adding a new `UrlSigner` implementing the following interface:

```kotlin
public interface UrlSigner {
    public fun signFileUrl(url: String): String
    public fun signImageUrl(url: String): String
}
```

This is the way to  add a new UrlSigner:

```kotlin
val urlSigner: UrlSigner = object : UrlSigner {
    override fun signFileUrl(url: String): String {
        //Do some change with url here!
        return url + "new added text"
    }

    override fun signImageUrl(url: String): String {
        //Do some change with url here!
        return url + "new added text"
    }
}

ChatUI.urlSigner = urlSigner
```
