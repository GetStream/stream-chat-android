---
id: uiChannelListCustomizationsChangingStyle
title: Changing Channel List View Style
sidebar_position: 1
---
Let's make an example and modify _ChannelListViewStyle_ programmatically. We want to change the title text appearance, some default icons, and colors, and disable some options:

| Before | After |
| --- | --- |
|![before](/img/channel_list_view_style_before.png)|![after](/img/channel_list_view_style_after.png)|

In order to achieve such effect we need to provide custom _TransformStyle.channelListStyleTransformer_:
```kotlin
TransformStyle.channelListStyleTransformer = StyleTransformer { defaultStyle ->
    defaultStyle.copy(
        optionsEnabled = false,
        foregroundLayoutColor = Color.LTGRAY,
        indicatorReadIcon = ContextCompat.getDrawable(requireContext(), R.drawable.stream_ui_ic_clock)!!,
        channelTitleText = TextStyle(
            color = Color.WHITE,
            size = resources.getDimensionPixelSize(R.dimen.stream_ui_text_large),
        ),
        lastMessageText = TextStyle(
            size = resources.getDimensionPixelSize(R.dimen.stream_ui_text_small),
        ),
        unreadMessageCounterBackgroundColor = Color.BLUE,
    )
}
```
> **NOTE**: The transformer should be set before the view is rendered to make sure that the new style was applied.
