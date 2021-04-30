---
id: uiChannelListChangingChannelListViewComponents
title: Changing Channel List View Components
sidebar_position: 2
---
Let's make an example and create a custom layout in which `ChannelListHeaderView` won't contain `ActionButton` and `ChannelListView` will have a custom loading view with a shimmer effect:

| Light Mode | Dark Mode |
| --- | --- |
|![light](https://user-images.githubusercontent.com/17440581/108346797-10004c00-71e0-11eb-813b-c807067eb146.png)|![dark](https://user-images.githubusercontent.com/17440581/108346793-0f67b580-71e0-11eb-97f1-1adb99d92443.png)|

Assuming that we have the setup similar to previous steps, we have to do the following steps:
1. Add Shimmer dependency
```groovy
implementation "com.facebook.shimmer:shimmer:0.5.0"
```
2. Add `shape_shimmer.xml` into _drawable_ folder:
```xml
<?xml version="1.0" encoding="utf-8"?>
<shape xmlns:android="http://schemas.android.com/apk/res/android"
    android:shape="rectangle"
    >
    <solid android:color="#CCCCCC" />
    <corners android:radius="20dp" />
</shape>
```
3. Add a single row layout - `item_loading_view.xml` into _layout_ folder:
```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.constraintlayout.widget.ConstraintLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="64dp"
    >

    <View
        android:id="@+id/avatarPlaceholder"
        android:layout_width="40dp"
        android:layout_height="40dp"
        android:layout_marginStart="8dp"
        android:background="@drawable/shape_shimmer"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toTopOf="parent"
        />

    <View
        android:id="@+id/titlePlaceholder"
        android:layout_width="80dp"
        android:layout_height="16dp"
        android:layout_gravity="center"
        android:layout_marginStart="8dp"
        android:layout_marginEnd="8dp"
        android:background="@drawable/shape_shimmer"
        app:layout_constraintStart_toEndOf="@id/avatarPlaceholder"
        app:layout_constraintTop_toTopOf="@id/avatarPlaceholder"
        />

    <View
        android:id="@+id/subtitlePlaceholder"
        android:layout_width="0dp"
        android:layout_height="16dp"
        android:layout_gravity="center"
        android:layout_marginStart="8dp"
        android:layout_marginTop="8dp"
        android:background="@drawable/shape_shimmer"
        app:layout_constraintBottom_toBottomOf="@id/avatarPlaceholder"
        app:layout_constraintEnd_toStartOf="@+id/datePlaceholder"
        app:layout_constraintStart_toEndOf="@id/avatarPlaceholder"
        />

    <View
        android:id="@+id/datePlaceholder"
        android:layout_width="40dp"
        android:layout_height="16dp"
        android:layout_gravity="center"
        android:layout_marginStart="16dp"
        android:layout_marginEnd="8dp"
        android:background="@drawable/shape_shimmer"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintStart_toEndOf="@id/subtitlePlaceholder"
        app:layout_constraintTop_toTopOf="@id/subtitlePlaceholder"
        />

    <View
        android:id="@+id/separator"
        android:layout_width="0dp"
        android:layout_height="1dp"
        android:layout_gravity="center"
        android:background="@drawable/shape_shimmer"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        />

</androidx.constraintlayout.widget.ConstraintLayout>
```
4. Create final loading view with shimmer effect. Let's call it `channel_list_loading_view`:
```xml
<?xml version="1.0" encoding="utf-8"?>
<com.facebook.shimmer.ShimmerFrameLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:id="@+id/loadingViewContainer"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:layout_marginTop="64dp"
    app:shimmer_auto_start="true"
    app:shimmer_base_color="#CCCCCC"
    app:shimmer_colored="true"
    app:shimmer_highlight_color="#FFFFFF"
    >

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:orientation="vertical"
        >

        <include layout="@layout/item_loading_view" />

        <include layout="@layout/item_loading_view" />

        <include layout="@layout/item_loading_view" />

        <include layout="@layout/item_loading_view" />

        <include layout="@layout/item_loading_view" />

        <include layout="@layout/item_loading_view" />

    </LinearLayout>

</com.facebook.shimmer.ShimmerFrameLayout>
```
5. Modify `ChannelListHeaderView`'s attributes:
```xml
    <io.getstream.chat.android.ui.channel.list.header.ChannelListHeaderView
        android:id="@+id/channelListHeaderView"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toTopOf="parent"
        app:streamUiShowActionButton="false"
        />
```
6. Change `ChannelListView`'s loading view:
```kotlin
// Inflate loading view
val loadingView = LayoutInflater.from(context).inflate(R.layout.channel_list_loading_view, channelListView)
// Set loading view
channelListView.setLoadingView(loadingView, FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT))
```
