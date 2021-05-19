---
title: index
sidebar_position: 1
---
/[stream-chat-android-ui-components](../../index.md)/[io.getstream.chat.android.ui.common](../index.md)/[Debouncer](index.md)  
  
  
  
# Debouncer  
class [Debouncer](index.md)(**debounceMs**: [Long](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-long/index.html))Utility class for debouncing high frequency events.[submit](submit.md)ting a new piece of work to run within the debounce window will cancel the previously submitted pending work.  
  
## Constructors  
  
| | |
|---|---|
| <a name="io.getstream.chat.android.ui.common/Debouncer/Debouncer/#kotlin.Long/PointingToDeclaration/"></a>[Debouncer](Debouncer.md)| <a name="io.getstream.chat.android.ui.common/Debouncer/Debouncer/#kotlin.Long/PointingToDeclaration/"></a>fun [Debouncer](Debouncer.md)(debounceMs: [Long](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-long/index.html))|
  
  
## Functions  
  
|  Name |  Summary | 
|---|---|
| <a name="io.getstream.chat.android.ui.common/Debouncer/shutdown/#/PointingToDeclaration/"></a>[shutdown](shutdown.md)| <a name="io.getstream.chat.android.ui.common/Debouncer/shutdown/#/PointingToDeclaration/"></a>fun [shutdown](shutdown.md)()Cleans up any pending work.|
| <a name="io.getstream.chat.android.ui.common/Debouncer/submit/#kotlin.Function0[kotlin.Unit]/PointingToDeclaration/"></a>[submit](submit.md)| <a name="io.getstream.chat.android.ui.common/Debouncer/submit/#kotlin.Function0[kotlin.Unit]/PointingToDeclaration/"></a>fun [submit](submit.md)(work: () -&gt; [Unit](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html))|
| <a name="io.getstream.chat.android.ui.common/Debouncer/submitSuspendable/#kotlin.coroutines.SuspendFunction0[kotlin.Unit]/PointingToDeclaration/"></a>[submitSuspendable](submitSuspendable.md)| <a name="io.getstream.chat.android.ui.common/Debouncer/submitSuspendable/#kotlin.coroutines.SuspendFunction0[kotlin.Unit]/PointingToDeclaration/"></a>fun [submitSuspendable](submitSuspendable.md)(work: suspend () -&gt; [Unit](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-unit/index.html))|

