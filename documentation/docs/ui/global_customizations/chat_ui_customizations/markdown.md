---
id: uiGlobalCustomizationsMarkdown
title: Markdown
sidebar_position: 5
---

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

![mardown messages](/img/markdown_support.png)
