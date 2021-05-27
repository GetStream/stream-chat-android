---
id: ui-global-customizations-navigator
title: Navigator
sidebar_position: 2
---

To display some screens available in the SDK, it is necessary to navigate to then.
The SDK does that using the ChatNavigator and it navigates to:
- `AttachmentGaleryActivity`: To display the gallery of pictures
- After a click in a link, to the destination of the link (the user leaves the chat).

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
