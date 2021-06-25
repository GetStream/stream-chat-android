# ChatUI

SDK provides an API for general-level configuration of chat behavior and appearance. It is exposed via the `ChatUI` object.

## Overview

ChatUI allows to override default implementations of commonly used parts of SDK like:
 * Set of available message reactions
 * MIME type icons for attachments 
 * Default font used across the UI components
 * Avatar view bitmap transformation
 * Attachments urls overriding

The full list of `ChatUI` properties you can override include:
 * `ChatUI.supportedReactions` Allows overriding default set of supported Message reaction.
 * `ChatUI.mimeTypeIconProvider` Allows defining own icons for different mime types.
 * `ChatUI.fonts` Allows specifying default font for TextViews displayed by UI components. 
 * `ChatUI.avatarBitmapFactory` Allows intercepting the avatar Bitmap and modifying it before it's displayed by the `AvatarView`.
 * `ChatUI.urlSigner` Allows adding authorization tokens for images, video, etc.
 * `ChatUI.markdown` Interface to to customize the markdown parsing behaviour, useful if you want to provide your custom markdown parsing logic, use more markdown modules, etc.
 * `ChatUI.style` Allows overriding global, default style of UI components, like the TextStyle.
 * `ChatUI.navigator` Allows intercepting and modifying default navigation between SDKs components (e.g. Redirection from `MessageListView` to `AttachmentGalleryActivity`).
 
 :::note
 `ChatUI` is initialized out-of-the-box with default implementations - no initialization is required.
 :::
 
 You will find information on how to use the above features in action in the next section.

## Usage

### Custom Reactions
As shown below, by default the SDK provides 5 built-in reactions. 

![Default reactions](../assets/chatui_default_reactions.png)

It is possible to override the default set of reactions. 
In order to define custom set of reactions for your chat app, you need to pass the the following data to the `ChatUI.supportedReactions` property:
```kotlin 
val loveDrawable = ContextCompat.getDrawable(this, R.drawable.ic_reaction_love)!!
val loveDrawableSelected = ContextCompat.getDrawable(this, R.drawable.ic_reaction_love)!!.apply { setTint(Color.RED) }
val supportedReactionsData = mapOf(
    "love" to SupportedReactions.ReactionDrawable(loveDrawable, loveDrawableSelected)
)
ChatUI.supportedReactions = SupportedReactions(this, supportedReactionsData)
```
As a result, there will only be a _love_ reaction available in the chat, and when selected the reaction icon will have a red tint.

| Normal state - available reactions | Active state - reaction selected |
| --- | --- |
|![Light_mode](../assets/chat_ui_custom_reaction.png)|![Dark_mode](../assets/chat_ui_custom_reaction_active.png)|

### Custom MIME type icons

When possible SDK displays thumbnails for image files. Mime type icons are displayed for other files at `MessageListView` as attachments icons and at files gallery screen. 
By default SDK provides built-in MIME type icons for the most popular file types, and displays a generic file icon for others.

In order to customize MIME icons, you need to override `ChatUI.mimeTypeIconProvider` in the following way:

```kotlin
ChatUI.mimeTypeIconProvider = MimeTypeIconProvider { mimeType ->
    if (mimeType == null) {
        R.drawable.stream_ui_ic_file
    }

    when {
        // special icon for XLS files
        mimeType == "application/vnd.ms-excel" -> R.drawable.ic_file_xls
        // generic icon for audio files
        mimeType.contains("audio") -> R.drawable.ic_file_mp3
        // generic icon for video files
        mimeType.contains("video") -> R.drawable.ic_file_mov
        // generic icon for other files
        else -> R.drawable.stream_ui_ic_file
    }
}
```

### Transforming avatar bitmap with AvatarBitmapFactory

Overriding the `AvatarBitmapFactory` allows the avatars to be generated accordingly to a new configuration. 
It is possible to configure the user bitmap, user default bitmap, channel bitmap, channel default bitmap, also choose
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

        return (imageResult.drawable as?    BitmapDrawable)?.bitmap
    }
}
```
Result:

![Blurred images for offline users](../assets/blurred_images.png)

### Intercepting attachments URLs with UrlSigner 

`ChatUi.urlSigner` is used internally to transform urls of file and image attachments before displaying them. 
Providing custom implementation of `UrlSigner` allows intercepting url, e.g. to add authorization tokens to the urls. 

```kotlin
interface UrlSigner {
    fun signFileUrl(url: String): String
    fun signImageUrl(url: String): String
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

### Setting default Font

It is possible to customize the default fonts used by all of the UI components. To change the fonts, just implement
the `ChatFont` interface:

```kotlin
interface ChatFonts {
    fun setFont(textStyle: TextStyle, textView: TextView)
    fun setFont(textStyle: TextStyle, textView: TextView, defaultTypeface: Typeface = Typeface.DEFAULT)
    fun getFont(textStyle: TextStyle): Typeface?
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

### Markdown

SDK provides a default Markdown support out-of-the-box. You can modify it by implementing a custom `ChatMarkdown` interface:

```Java
public interface ChatMarkdown {
    void setText(@NonNull TextView textView, @NonNull String text);
}
```

And adding it to `ChatUI`:

```
val markdown = ChatMarkdown { textView, text ->
    //parse markdown the the new text and apply it.
    textView.text = applyMarkdown(text)
}

ChatUI.markdown = markdown
```

Then the SDK will parse markdown automatically:

![Markdown messages](../assets/markdown_support.png)

### Navigator

To display some screens available in the SDK, it is necessary to navigate to them.
The SDK does that using the ChatNavigator and it navigates to:
- `AttachmentGaleryActivity`: To display the gallery of pictures
- After clicking a link, to the destination of the link (the user leaves the chat).

It is possible to add a custom navigator to `ChatUI` by adding a new `ChatNavigator`.

You can instantiate the `ChatNavigator` by providing its custom implementation of `ChatNavigationHandler`:

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

```kotlin
val navigationHandler : ChatNavigationHandler = ChatNavigationHandler { destination ->
    //Some custom action here!
    true
}

ChatUI.navigator = ChatNavigator(navigationHandler)
```
