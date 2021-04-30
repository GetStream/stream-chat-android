---
id: uiChannelListBindingViewModels
title: Binding View Models
sidebar_position: 2
---
The Android SDK comes together with view models for view components which are responsible for providing all necessary data. You need to do two things to connect a particular view with its view model:
```kotlin
// Step 1: Create ViewModels
val channelListHeaderViewModel: ChannelListHeaderViewModel by viewModels()
val channelListFactory: ChannelListViewModelFactory = ChannelListViewModelFactory(
    filter = Filters.and(
        Filters.eq(“type”, “messaging”),
        Filters.`in`(“members”, listOf(ChatDomain.instance().currentUser.id)),
    ),
    sort = QuerySort.desc(Channel::lastUpdated),
    limit = 30
)
val channelListViewModel: ChannelListViewModel by viewModels { channelListFactory }
// Step 2: Bind views with view models
channelListHeaderViewModel.bindView(channelListHeaderView, viewLifecycleOwner)
channelListViewModel.bindView(channelListView, viewLifecycleOwner)
```
This is how default channel list should look like:

| Light Mode | Dark Mode |
| --- | --- |
|![light mode](/img/channel_list_view_light.png)|![dark mode](/img/channel_list_view_dark.png)|
From that point, `ChannelListHeaderView` will be able to display the current user avatar as well as online status, while `ChannelListView` will display different channels view states, as well as the channel’s pagination, which will be handled automatically.
`ChannelListViewModelFactory` allows customizing _filter_ and _sort_ options. You can find more about possible options [here](https://getstream.io/chat/docs/android/query_channels/?language=java#common-filters-by-use-case).
