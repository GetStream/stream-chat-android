---
id: uiGlobalCustomizationsNavigator
title: Navigator
sidebar_position: 2
---

It is possible to add a custom navigator to `ChatUI` by adding a new `ChatNavigator`.

A user can instantiate a `ChatNavigator` and provide its custom implementation of `ChatNavigationHandler`:

```kotlin
public interface ChatNavigationHandler {
    /**
     * Attempt to navigate to the given [destination].
     *
     * @return true if navigation was successfully handled.
     */
    public fun navigate(destination: ChatDestination): Boolean
}
```

Following there's a simple example of a customization of destination:

```Kotlin
val navigationHandler : ChatNavigationHandler = ChatNavigationHandler { destination ->
    //Some custom action here!
    true
}

ChatUI.navigator = ChatNavigator(navigationHandler)
```
