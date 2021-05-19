---
title: SupportedReactions
---
/[stream-chat-android-ui-components](../../index.md)/[io.getstream.chat.android.ui](../index.md)/[SupportedReactions](index.md)/[SupportedReactions](SupportedReactions.md)  
  
  
  
# SupportedReactions  
fun [SupportedReactions](SupportedReactions.md)(context: [Context](https://developer.android.com/reference/kotlin/android/content/Context.html), reactions: [Map](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-map/index.html)&lt;[String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), [SupportedReactions.ReactionDrawable](ReactionDrawable/index.md)&gt; = mapOf(
        LOVE to loveDrawable(context),
        THUMBS_UP to thumbsUpDrawable(context),
        THUMBS_DOWN to thumbsDownDrawable(context),
        LOL to lolDrawable(context),
        WUT to wutDrawable(context),
    ))
