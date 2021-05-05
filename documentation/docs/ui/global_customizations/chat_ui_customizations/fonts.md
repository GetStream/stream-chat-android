---
id: uiGlobalCustomizationsFonts
title: Fonts
sidebar_position: 4
---

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
