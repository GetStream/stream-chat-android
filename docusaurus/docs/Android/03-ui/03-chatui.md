# Configuration

The SDK provides an API for general configuration of the UI Component library's behavior and appearance, which is exposed via the `ChatUI` object.

`ChatUI` allows you to override default implementations of commonly used parts of SDK like:

* Available message reactions
* MIME type icons for attachments
* Default font used across the UI components
* Avatar bitmap loading logic
* Attachments urls

The full list of `ChatUI` properties you can override include:

* `supportedReactions`: The set of supported message reactions.
* `mimeTypeIconProvider`: The icons used for different mime types.
* `fonts`: The default font for TextViews displayed by UI Components.
* `avatarBitmapFactory`: The factory responsible for creating user and channel avatar bitmaps displayed by `AvatarView`. Can be used to modify the default bitmaps that are loaded, or to add custom avatar loading logic.
* `imageHeadersProvider`: Allows adding extra headers to image loading requests.
* `markdown`: Interface to customize the markdown parsing behaviour, useful if you want to provide your custom markdown parsing logic or use more markdown modules.
* `style`: Allows overriding the global, default style of UI components, like the TextStyle.
* `navigator`: Allows intercepting and modifying default navigation between SDKs components (e.g. redirection from `MessageListView` to `AttachmentGalleryActivity`).

:::note
`ChatUI` is initialized out-of-the-box with default implementations - no initialization is required on app startup.
:::

### Custom Reactions

As shown below, by default the SDK provides 5 built-in reactions.

![Default reactions](../assets/chatui_default_reactions.png)

You can override the default set of reactions. In order to define a custom set of reactions for your app, you need to pass the the following data to the `ChatUI.supportedReactions` property:

```kotlin 
val loveDrawable = ContextCompat.getDrawable(this, R.drawable.ic_reaction_love)!!
val loveDrawableSelected = ContextCompat.getDrawable(this, R.drawable.ic_reaction_love)!!.apply { setTint(Color.RED) }
val supportedReactionsData = mapOf(
    "love" to SupportedReactions.ReactionDrawable(loveDrawable, loveDrawableSelected)
)
ChatUI.supportedReactions = SupportedReactions(this, supportedReactionsData)
```

As a result, there will only be a _love_ reaction available in the chat, and when selected, the reaction icon will have a red tint.

| Normal state - available reactions | Active state - reaction selected |
| --- | --- |
|![Light_mode](../assets/chat_ui_custom_reaction.png)|![Dark_mode](../assets/chat_ui_custom_reaction_active.png)|

### Custom MIME Type Icons

When possible, the SDK displays thumbnails for image files. For other files, mime type icons are displayed in `MessageListView` and in the file browser UI.

<!-- TODO add screenshot for this -->

By default, the SDK provides built-in MIME type icons for the most popular file types and displays a generic file icon for others.

To customize these icons, you need to override `ChatUI.mimeTypeIconProvider` in the following way:

```kotlin
ChatUI.mimeTypeIconProvider = MimeTypeIconProvider { mimeType ->
    when {
        // Generic icon for missing MIME type
        mimeType == null -> R.drawable.stream_ui_ic_file
        // Special icon for XLS files
        mimeType == "application/vnd.ms-excel" -> R.drawable.ic_file_xls
        // Generic icon for audio files
        mimeType.contains("audio") -> R.drawable.ic_file_mp3
        // Generic icon for video files
        mimeType.contains("video") -> R.drawable.ic_file_mov
        // Generic icon for other files
        else -> R.drawable.stream_ui_ic_file
    }
}
```

### Customizing Avatar Bitmap Loading

Overriding the `AvatarBitmapFactory` allows you to add custom loading logic for the channel and user avatars displayed in `AvatarView`. 

The factory has a pair of methods for both user and channel avatars, such as `createUserBitmap` and `createDefaultUserBitmap`. The first should load the avatar from the network, this is attempted first when an avatar is requested. If the first method fails, the `Default` variant is called as a fallback. This should have an implementation that never fails, and always returns a valid bitmap.

:::note
The methods mentioned above are suspending functions. If you don't use coroutines, you'll find variants with `Blocking` in their name that you can use instead.
:::

If you add custom loading logic for your avatars, you can also override the `userBitmapKey` and `channelBitmapKey` methods to get correct caching behavior. The cache keys returned from these methods should include a representation of the pieces of data you use to load the avatars.

Here's an example of a custom factory that extends `AvatarBitmapFactory`, which makes the avatar for offline users blurred:

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

:::note
This example uses the [Coil](https://github.com/coil-kt/coil) library for loading the image URL and applying transformations to it.
:::

This results in avatars rendered like this:

![Blurred images for offline users](../assets/blurred_images.png)

### Adding Extra Headers to Image Requests

If you're [using your own CDN](https://getstream.io/chat/docs/android/file_uploads/?language=kotlin#using-your-own-cdn), you might also need to add extra headers to image loading requests. You can do this by creating your own implementation of the `ImageHeadersProvider` interface and then setting it on `ChatUI`:

```kotlin
ChatUI.imageHeadersProvider = object : ImageHeadersProvider {
    override fun getImageRequestHeaders(): Map<String, String> {
        return mapOf("token" to "12345")
    }
}
```

### Changing the Default Font

You can customize the default fonts used by all of the UI components. To change the fonts, implement the `ChatFont` interface and set the new implementation on `ChatUI`:

```kotlin
ChatUI.fonts = object : ChatFonts {
    override fun setFont(textStyle: TextStyle, textView: TextView) {
        textStyle.apply(textView)
    }

    override fun setFont(textStyle: TextStyle, textView: TextView, defaultTypeface: Typeface) {
        textStyle.apply(textView)
    }

    override fun getFont(textStyle: TextStyle): Typeface? = textStyle.font
}
```

### Markdown

The SDK provides Markdown support out-of-the-box. You can modify this by implementing the `ChatMarkdown` interface and setting your own custom instance on `ChatUI`:

```kotlin
ChatUI.markdown = ChatMarkdown { textView: TextView, text: String ->
    // Parse markdown from text and apply formatting here
    textView.text = applyMarkdown(text)
}
```

Then the SDK will parse Markdown automatically:

![Markdown messages](../assets/markdown_support.png)

### Navigator

The SDK performs navigation in certain cases:

- Navigating to `AttachmentGaleryActivity` to display the gallery of pictures in the chat.
- Opening the browser after clicking a link in the chat.

This is done performed by `ChatNavigator`, which you can provide a custom `ChatNavigationHandler` implementation to override its behavior:

```kotlin
val navigationHandler = ChatNavigationHandler { destination: ChatDestination ->
    // Perform some custom action here
    true
}

ChatUI.navigator = ChatNavigator(navigationHandler)
```
