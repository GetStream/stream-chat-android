---
title: index
sidebar_position: 1
---
//[stream-chat-android-ui-components](../../../index.md)/[io.getstream.chat.android.ui.message.list.viewmodel.factory](../index.md)/[MessageListViewModelFactory](index.md)



# MessageListViewModelFactory  
 [androidJvm] class [MessageListViewModelFactory](index.md)@[JvmOverloads](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.jvm/-jvm-overloads/index.html)()constructor(**cid**: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), **messageId**: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)?) : [ViewModelProvider.Factory](https://developer.android.com/reference/kotlin/androidx/lifecycle/ViewModelProvider.Factory.html)

A ViewModel factory for MessageListViewModel, MessageListHeaderViewModel and MessageInputViewModel

   


## See also  
  
androidJvm  
  
| | |
|---|---|
| <a name="io.getstream.chat.android.ui.message.list.viewmodel.factory/MessageListViewModelFactory///PointingToDeclaration/"></a>com.getstream.sdk.chat.viewmodel.messages.MessageListViewModel| <a name="io.getstream.chat.android.ui.message.list.viewmodel.factory/MessageListViewModelFactory///PointingToDeclaration/"></a>|
| <a name="io.getstream.chat.android.ui.message.list.viewmodel.factory/MessageListViewModelFactory///PointingToDeclaration/"></a>[io.getstream.chat.android.ui.message.list.header.viewmodel.MessageListHeaderViewModel](../../io.getstream.chat.android.ui.message.list.header.viewmodel/MessageListHeaderViewModel/index.md)| <a name="io.getstream.chat.android.ui.message.list.viewmodel.factory/MessageListViewModelFactory///PointingToDeclaration/"></a>|
| <a name="io.getstream.chat.android.ui.message.list.viewmodel.factory/MessageListViewModelFactory///PointingToDeclaration/"></a>com.getstream.sdk.chat.viewmodel.MessageInputViewModel| <a name="io.getstream.chat.android.ui.message.list.viewmodel.factory/MessageListViewModelFactory///PointingToDeclaration/"></a>|
  


## Parameters  
  
androidJvm  
  
| | |
|---|---|
| <a name="io.getstream.chat.android.ui.message.list.viewmodel.factory/MessageListViewModelFactory///PointingToDeclaration/"></a>cid| <a name="io.getstream.chat.android.ui.message.list.viewmodel.factory/MessageListViewModelFactory///PointingToDeclaration/"></a><br/><br/>: the channel id in the format messaging:123<br/><br/>|
| <a name="io.getstream.chat.android.ui.message.list.viewmodel.factory/MessageListViewModelFactory///PointingToDeclaration/"></a>messageId| <a name="io.getstream.chat.android.ui.message.list.viewmodel.factory/MessageListViewModelFactory///PointingToDeclaration/"></a><br/><br/>: the id of the target message to displayed<br/><br/>|
  


## Constructors  
  
| | |
|---|---|
| <a name="io.getstream.chat.android.ui.message.list.viewmodel.factory/MessageListViewModelFactory/MessageListViewModelFactory/#kotlin.String#kotlin.String?/PointingToDeclaration/"></a>[MessageListViewModelFactory](MessageListViewModelFactory.md)| <a name="io.getstream.chat.android.ui.message.list.viewmodel.factory/MessageListViewModelFactory/MessageListViewModelFactory/#kotlin.String#kotlin.String?/PointingToDeclaration/"></a> [androidJvm] @[JvmOverloads](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin.jvm/-jvm-overloads/index.html)()  <br/>  <br/>fun [MessageListViewModelFactory](MessageListViewModelFactory.md)(cid: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html), messageId: [String](https://kotlinlang.org/api/latest/jvm/stdlib/kotlin/-string/index.html)? = null): the channel id in the format messaging:123   <br/>|


## Functions  
  
|  Name |  Summary | 
|---|---|
| <a name="io.getstream.chat.android.ui.message.list.viewmodel.factory/MessageListViewModelFactory/create/#java.lang.Class[TypeParam(bounds=[androidx.lifecycle.ViewModel])]/PointingToDeclaration/"></a>[create](create.md)| <a name="io.getstream.chat.android.ui.message.list.viewmodel.factory/MessageListViewModelFactory/create/#java.lang.Class[TypeParam(bounds=[androidx.lifecycle.ViewModel])]/PointingToDeclaration/"></a>[androidJvm]  <br/>Content  <br/>open override fun &lt;[T](create.md) : [ViewModel](https://developer.android.com/reference/kotlin/androidx/lifecycle/ViewModel.html)&gt; [create](create.md)(modelClass: [Class](https://developer.android.com/reference/kotlin/java/lang/Class.html)&lt;[T](create.md)&gt;): [T](create.md)  <br/><br/><br/>|

