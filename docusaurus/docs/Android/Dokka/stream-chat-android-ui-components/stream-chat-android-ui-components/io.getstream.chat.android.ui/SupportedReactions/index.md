---
title: index
sidebar_position: 1
---
//[stream-chat-android-ui-components](../../../index.md)/[io.getstream.chat.android.ui](../index.md)/[SupportedReactions](index.md)



# SupportedReactions  
 [androidJvm] class [SupportedReactions](index.md)(**context**: [Context](https://developer.android.com/reference/kotlin/android/content/Context.html), **reactions**: [Map](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-map/index.html)&lt;[String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), [SupportedReactions.ReactionDrawable](ReactionDrawable/index.md)&gt;)

Class allowing to define set of supported reactions. You can customize reactions by providing your own implementation of this class to [ChatUI.supportedReactions](../ChatUI/supportedReactions.md).

   


## Constructors  
  
| | |
|---|---|
| <a name="io.getstream.chat.android.ui/SupportedReactions/SupportedReactions/#android.content.Context#kotlin.collections.Map[kotlin.String,io.getstream.chat.android.ui.SupportedReactions.ReactionDrawable]/PointingToDeclaration/"></a>[SupportedReactions](SupportedReactions.md)| <a name="io.getstream.chat.android.ui/SupportedReactions/SupportedReactions/#android.content.Context#kotlin.collections.Map[kotlin.String,io.getstream.chat.android.ui.SupportedReactions.ReactionDrawable]/PointingToDeclaration/"></a> [androidJvm] fun [SupportedReactions](SupportedReactions.md)(context: [Context](https://developer.android.com/reference/kotlin/android/content/Context.html), reactions: [Map](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-map/index.html)&lt;[String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), [SupportedReactions.ReactionDrawable](ReactionDrawable/index.md)&gt; = mapOf(<br/>        LOVE to loveDrawable(context),<br/>        THUMBS_UP to thumbsUpDrawable(context),<br/>        THUMBS_DOWN to thumbsDownDrawable(context),<br/>        LOL to lolDrawable(context),<br/>        WUT to wutDrawable(context),<br/>    ))   <br/>|


## Types  
  
|  Name |  Summary | 
|---|---|
| <a name="io.getstream.chat.android.ui/SupportedReactions.DefaultReactionTypes///PointingToDeclaration/"></a>[DefaultReactionTypes](DefaultReactionTypes/index.md)| <a name="io.getstream.chat.android.ui/SupportedReactions.DefaultReactionTypes///PointingToDeclaration/"></a>[androidJvm]  <br/>Content  <br/>object [DefaultReactionTypes](DefaultReactionTypes/index.md)  <br/>More info  <br/>Default reaction types  <br/><br/><br/>|
| <a name="io.getstream.chat.android.ui/SupportedReactions.ReactionDrawable///PointingToDeclaration/"></a>[ReactionDrawable](ReactionDrawable/index.md)| <a name="io.getstream.chat.android.ui/SupportedReactions.ReactionDrawable///PointingToDeclaration/"></a>[androidJvm]  <br/>Content  <br/>class [ReactionDrawable](ReactionDrawable/index.md)(**inactiveDrawable**: [Drawable](https://developer.android.com/reference/kotlin/android/graphics/drawable/Drawable.html), **activeDrawable**: [Drawable](https://developer.android.com/reference/kotlin/android/graphics/drawable/Drawable.html))  <br/><br/><br/>|


## Properties  
  
|  Name |  Summary | 
|---|---|
| <a name="io.getstream.chat.android.ui/SupportedReactions/reactions/#/PointingToDeclaration/"></a>[reactions](reactions.md)| <a name="io.getstream.chat.android.ui/SupportedReactions/reactions/#/PointingToDeclaration/"></a> [androidJvm] val [reactions](reactions.md): [Map](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-map/index.html)&lt;[String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), [SupportedReactions.ReactionDrawable](ReactionDrawable/index.md)&gt;: Map&lt;String, ReactionDrawable&gt; instance.   <br/>|
| <a name="io.getstream.chat.android.ui/SupportedReactions/types/#/PointingToDeclaration/"></a>[types](types.md)| <a name="io.getstream.chat.android.ui/SupportedReactions/types/#/PointingToDeclaration/"></a> [androidJvm] val [types](types.md): [List](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.collections/-list/index.html)&lt;[String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)&gt;   <br/>|


## Functions  
  
|  Name |  Summary | 
|---|---|
| <a name="io.getstream.chat.android.ui/SupportedReactions/getReactionDrawable/#kotlin.String/PointingToDeclaration/"></a>[getReactionDrawable](getReactionDrawable.md)| <a name="io.getstream.chat.android.ui/SupportedReactions/getReactionDrawable/#kotlin.String/PointingToDeclaration/"></a>[androidJvm]  <br/>Content  <br/>fun [getReactionDrawable](getReactionDrawable.md)(type: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)): [SupportedReactions.ReactionDrawable](ReactionDrawable/index.md)?  <br/><br/><br/>|

