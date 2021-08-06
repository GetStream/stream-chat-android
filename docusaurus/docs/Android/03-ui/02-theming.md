# Theming

Many aspects of the UI components can be changed and customized. For example, it's possible to change:

- Font family
- Text color
- Background of views
- Item views in lists
- Enable and disable features
- Text style (italic, bold, normal)
- Drawable of icons
- Stroke widths
- Divider color

:::note
It is not possible to change the tint of icons. Use colored drawables instead.
:::

These customizations can be applied in multiple ways. From simplest to most complex, these are:

- Adding attributes to the View in the XML layout where it's created.
- Using the `TransformStyle` object to apply transformations to all style objects of a given type.
- Using themes to style all Views globally.

:::caution
Be careful when using multiple theming approaches. Themes are applied first, then XML attributes, then style transformations. Values applied later will override previously set values.
:::

### XML Attributes

The simplest way to customize Views is by setting attributes on them in the XML layout. For example, here are some custom values you can set on `MessageListView`.

```xml
<io.getstream.chat.android.ui.message.list.MessageListView
    android:id="@+id/messageListView"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    app:streamUiMessageBackgroundColorMine="#70AF74"
    app:streamUiMessageBackgroundColorTheirs="@android:color/white"
    app:streamUiMessageTextColorMine="@android:color/white"
    app:streamUiMessageTextColorTheirs="@android:color/black"
    />
```

This will have the following result:

![Custom messages](../assets/custom_messages.png)

You can find the full list of available attributes for each view linked on their individual component pages, or in the `attrs_view_name` files [here](https://github.com/GetStream/stream-chat-android/tree/main/stream-chat-android-ui-components/src/main/res/values).

### Style Transformations

Styles can be configured programmatically by overriding the corresponding `StyleTransformer` from the `TransformStyle` object. These transformations will be applied to all UI Components of the given type.

:::caution
You have to set up any custom `StyleTransformer` instances *before* the View instances are initialized, otherwise they won't take effect.
:::

Here's an example using `TransformStyle` to change multiple appearance characteristics of all `MessageListView` instances:

```kotlin
TransformStyle.messageListItemStyleTransformer = StyleTransformer { defaultViewStyle ->
    defaultViewStyle.copy(
        messageBackgroundColorMine = Color.parseColor("#70AF74"),
        messageBackgroundColorTheirs = Color.WHITE,
        textStyleMine = defaultViewStyle.textStyleMine.copy(color = Color.WHITE),
        textStyleTheirs = defaultViewStyle.textStyleTheirs.copy(color = Color.BLACK),
    )
}
```

This will have the following result:

![Custom messages](../assets/custom_messages.png)

This is the same as the XML customization shown above, but applied to all `MessageListView` instances in the app, and configured programmatically.

### Themes

You can also use Android themes to set attributes for the UI Components. To do this, set a `streamUiTheme` attribute within your app's theme, in the `themes.xml` file:

```xml
<style name="AppTheme" parent="Theme.MaterialComponents.DayNight.NoActionBar">
    <!-- Other items... -->
    <item name="streamUiTheme">@style/StreamTheme</item>
</style>
```

The theme you pass in as the `streamUiTheme` can then define a style for each type of UI Component where you can set attribute values.

For example, you can create styles for avatars in the message list, or for the message list header:

```xml
<style name="StreamTheme" parent="@style/StreamUiTheme">
    <item name="streamUiMessageListItemAvatarStyle">@style/MessageListAvatarTheme</item>
    <item name="streamUiMessageListHeaderStyle">@style/MessageListHeaderStyle</item>
</style>
```

:::note
The list of available styles you can define here is available here in our [attrs file](https://github.com/GetStream/stream-chat-android/blob/main/stream-chat-android-ui-components/src/main/res/values/attrs.xml).
:::

These styles can then contain attributes for the views. For example, you can make the avatars display in a given size:

```xml
<style name="MessageListAvatarTheme" parent="StreamUi.MessageList.Item.Avatar">
    <item name="android:layout_width">48dp</item>
    <item name="android:layout_height">48dp</item>
</style>
```

Or you can hide the avatar that's shown by default on the message list header:

```xml
<style name="MessageListHeaderStyle" parent="StreamUi.MessageListHeader">
    <item name="streamUiMessageListHeaderShowUserAvatar">true</item>
</style>
```

:::note
Your custom themes can use the default styles can use the default styles for these Views as their parent. See the [sample app's themes](https://github.com/GetStream/stream-chat-android/blob/main/stream-chat-android-ui-components-sample/src/main/res/values/themes.xml) for examples of this.
:::

#### Themes for Activities
SDK contains following activities: `AttachmentMediaActivity`, `AttachmentActivity` and `AttachmentGalleryActivity`. You can customize them by overriding the activity with custom `theme` in your manisfest: 

**AndroidManifest.xml**

```
<activity
    android:name="io.getstream.chat.android.ui.gallery.AttachmentMediaActivity"
    android:theme="@style/MediaActivityStyle"
    tools:replace="android:theme"
    />
```

**themes.xml**

```
<style name="MediaActivityStyle" parent="StreamUi.MediaActivity">
    <item name="streamUiMediaActivityIconStyle">@style/MediaActivityIconStyle</item>
</style>

<style name="MediaActivityIconStyle" parent="StreamUi.MediaActivity.Icon">
    <item name="android:layout_width">150dp</item>
    <item name="android:layout_height">150dp</item>
    <item name="android:src">@drawable/stream_ui_ic_audio</item>
</style>
```

### Choose Light/Dark Theme
If is possible to choose between LIGHT, DARK and SYSTEM (default) ui mode. LIGHT forces the colors to be in light mode, DARK to dark colors and SYSTEM uses the colors accondingly to what which theme is selected in the system. You can chose the theme using ChatUI:

```
ChatUI.uiMode = UiMode.DARK
```