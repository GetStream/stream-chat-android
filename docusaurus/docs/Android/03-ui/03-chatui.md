# ChatUI

The `ChatUI` object supports UI component customization by default. It's initialized with default implementations - no initialization is required.

You can access `ChatUI` to customize the global behaviour of UI elements.

 * `ChatUI.fonts`: allows you to overwrite fonts
 * `ChatUI.markdown` interface to to customize the markdown parsing behaviour, useful if you want to use more markdown modules
 * `ChatUI.urlSigner` url signing logic, enables you to add authorization tokens for images, video etc
 * `ChatUI.avatarBitmapFactory` allows to generate custom bitmap for avatarView
 * `ChatUI.mimeTypeIconProvider` allows to define own icons for different mime types
 * `ChatUI.supportedReactions` allows to define own set of supported message reaction
 * `ChatUI.style` allows to override global style of UI components, like the TextStyle.

## Navigator

To display some screens available in the SDK, it is necessary to navigate to them.
The SDK does that using the ChatNavigator and it navigates to:
- `AttachmentGaleryActivity`: To display the gallery of pictures
- After a click in a link, to the destination of the link (the user leaves the chat).

It is possible to add a custom navigator to `ChatUI` by adding a new `ChatNavigator`.

A user can instantiate a `ChatNavigator` and provide its custom implementation of `ChatNavigationHandler`:

```kotlin
public interface ChatNavigationHandler {
    /**
     * Attempt to navigate to the given [destination].
     *
     * @return true if navigation was successfully handled.
     */
    public fun navigate(destination: ChatDestination): Boolean
}
```

Following there's a simple example of a customization of destination:

```Kotlin
val navigationHandler : ChatNavigationHandler = ChatNavigationHandler { destination ->
    //Some custom action here!
    true
}

ChatUI.navigator = ChatNavigator(navigationHandler)
```


## URLSigner

`ChatUi` allows to use of a custom url signer by implementing the following interface:

```kotlin
public interface UrlSigner {
    public fun signFileUrl(url: String): String
    public fun signImageUrl(url: String): String
}
```

This is the way to add a new `UrlSigner`:

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

## Fonts

It is possible to customize the fonts of the SDK. To change the fonts, just implement
the `ChatFont` interface:

```kotlin
public interface ChatFonts {
    public fun setFont(textStyle: TextStyle, textView: TextView)
    public fun setFont(textStyle: TextStyle, textView: TextView, defaultTypeface: Typeface = Typeface.DEFAULT)
    public fun getFont(textStyle: TextStyle): Typeface?
}
```

And add it to `ChatUi`:

```kotlin
val fonts: ChatFonts = object : ChatFonts {
    override fun setFont(textStyle: TextStyle, textView: TextView) {
        textStyle.apply(textView)
    }

    override fun setFont(textStyle: TextStyle, textView: TextView, defaultTypeface: Typeface) {
        textStyle.apply(textView)
    }

    override fun getFont(textStyle: TextStyle): Typeface? = textStyle.font
}

ChatUI.fonts = fonts
```

## Markdown

The Android SDK already has Markdown support by default. You can modify it by implementing a custom `ChatMarkdown` interface:

```Java
public interface ChatMarkdown {
    void setText(@NonNull TextView textView, @NonNull String text);
}
```

And add it to `ChatUI`:

```
val markdown = ChatMarkdown { textView, text ->
    //parse markdown the the new text and apply it.
    textView.text = applyMarkdown(text)
}

ChatUI.markdown = markdown
```

Then the SDK will parse markdown automatically:

![mardown messages](../assets/markdown_support.png)

## Avatar Factory

It is possible to customize `AvatarBitmapFactory` so the avatars will
be generated accordingly to the new configuration. It is possible to configure
the user bitmap, user default bitmap, channel bitmap, channel default bitmap, also choose
between blocking and non blocking options and configure the keys for easy bitmap to be used
in the cache system.

To change the default behaviour of this factory, a user needs to extend `AvatarBitmapFactory`,
which is an open class, and set the desired behaviour. This example makes the avatar for offline users blurred:

```kotlin
ChatUI.avatarBitmapFactory = object : AvatarBitmapFactory(context) {
    override suspend fun createUserBitmap(user: User, style: AvatarStyle, avatarSize: Int): Bitmap? {
        val imageResult = context.imageLoader.execute(
            ImageRequest.Builder(context)
                .data(user.image)
                .apply {
                    if (!user.online) {
                        transformations(BlurTransformation(context))
                    }
                }
                .build()
        )

        return (imageResult.drawable as? BitmapDrawable)?.bitmap
    }
}
```
Result:

![Blurred images for offline users](../assets/blurred_images.png)

