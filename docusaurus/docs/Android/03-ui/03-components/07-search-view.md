# Search View

The `SearchInputView` and `SearchResultListView` components can be used to search and display messages that contain specific text. The search is performed across all channels a user is a member of.

| Light Mode | Dark Mode |
| --- | --- |
|![search view light](../../assets/search_view_hey_light.png)|![search view dark](../../assets/search_view_hey_dark.png)|

## Usage

Here's an example layout using these two Views:

```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.constraintlayout.widget.ConstraintLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent">

    <io.getstream.chat.android.ui.search.SearchInputView
        android:id="@+id/searchInputView"
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        android:layout_margin="8dp"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toTopOf="parent" />

    <io.getstream.chat.android.ui.search.list.SearchResultListView
        android:id="@+id/searchResultListView"
        android:layout_width="0dp"
        android:layout_height="0dp"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toBottomOf="@id/searchInputView" />

</androidx.constraintlayout.widget.ConstraintLayout>
```

We recommend using `SearchViewModel` to get search results from the Stream API and then render them using the `SearchResultListView`.

The basic setup of the ViewModel and connecting it to the View is done the following way:

```kotlin 
// Instantiate the ViewModel 
val searchViewModel: SearchViewModel by viewModels()

// Bind the ViewModel with SearchResultListView
searchViewModel.bindView(searchResultListView, viewLifecycleOwner)
```

Finally, start the search by passing the search query to the ViewModel:

```kotlin 
searchInputView.setSearchStartedListener { query ->
    // Search is triggered
    searchViewModel.setQuery(query)
}
```

:::note
`bindView` sets listeners on the view and the ViewModel. Any additional listeners should be set _after_ calling `bindView`.
:::

## Handling Actions

In addition to the `SearchStartedListener` described above, `SearchInputView` allows you to listen for text changes by using listeners:

```kotlin
searchInputView.setContinuousInputChangedListener { query ->
    // Search query changed 
}
searchInputView.setDebouncedInputChangedListener { query ->
    // Search query changed and has been stable for a short while 
}
```

`SearchResultListView` exposes a listener for handling item clicks:

```kotlin
searchResultView.setSearchResultSelectedListener { message ->
    // Handle search result click
}
```

The full list of listeners available for `SearchInputView` can be found [here](https://getstream.github.io/stream-chat-android/stream-chat-android-ui-components/stream-chat-android-ui-components/io.getstream.chat.android.ui.search/-search-input-view/index.html), and for `SearchResultListView` [here](https://getstream.github.io/stream-chat-android/stream-chat-android-ui-components/stream-chat-android-ui-components/io.getstream.chat.android.ui.search.list/-search-result-list-view/index.html).

## Updating the Search Query Programmatically

`SearchInputView` provides a way to change the search query programmatically:

```kotlin
searchInputView.setQuery("query")
```

You can also easily clear the current input:

```kotlin
searchInputView.clear()
```

:::note
Updating the search query programmatically automatically notifies corresponding listeners.
:::
