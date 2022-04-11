# Compose SDK Guidelines

## Table of Contents

* [Motivation](#motivation)
* [Project Structure](#project-structure)
* [KDoc Comments](#kdoc-comments)
* [Component Previews](#component-previews)
* [Naming Conventions](#naming-conventions)
  * [Naming Components](#naming-components)
  * [Naming Listeners](#naming-listeners)
  * [Naming State](#naming-state)
  * [Naming Resources](#naming-resources)
* [Customizing Components](#customizing-components)
  * [Slot APIs](#slot-apis)

## Motivation

We follow the official [Guidelines For Compose](https://github.com/androidx/androidx/blob/androidx-main/compose/docs/compose-api-guidelines.md) when developing our Compose SDK. On top of that, we developed a set of rules described in this document.

Contributions to this project are very much welcome! Even if some of the requirements above are not met, don't hesitate to submit your code changes. 💙

## Project Structure

Since the SDK is quite small, we decided to package the code by layer. The main packages are listed below:

```
stream-chat-android-compose
├── ...
├── viewmodel
├── state
├── ui
│   └── components
├── previewdata
```

* **viewmodel**: Contains ViewModels for [bound](https://getstream.io/chat/docs/sdk/android/compose/component-architecture/#bound-components) UI components.
* **state**: Contains classes encapsulating state for Composables in `ui` package.
* **ui**: Contains [screen](https://getstream.io/chat/docs/sdk/android/compose/component-architecture/#screen-components), [bound](https://getstream.io/chat/docs/sdk/android/compose/component-architecture/#bound-components) and [stateless](https://getstream.io/chat/docs/sdk/android/compose/component-architecture/#stateless-components) UI components.
* **ui/components**: Contains reusable independent components that can be used as building blocks when creating fully custom UI.
* **previewdata**: Contains sample data for component previews.

For simplicity, we try to have only one public Composable UI component per file. Also, within each layer we try to package the code by feature. The following is an example of how to package a feature:

```
stream-chat-android-compose
├── ...
├── viewmodel
│      └── messages          // ViewModels and factories for the "messages" feature
├── state
│      └── messages          // Models and entities for the "messages" feature
├── ui
│      └── messages          // Higher-level components for the "messages" feature 
│      └── components
│             └── messages   // Utility components for the "messages" feature 
```

## KDoc Comments

We believe that all the code should be covered with KDoc. This includes classes, methods, fields, parameters and return values.

## Component Previews

We provide component [previews](https://developer.android.com/jetpack/compose/tooling#preview) for [stateless](https://getstream.io/chat/docs/sdk/android/compose/component-architecture/#stateless-components) components.

When creating component previews, keep the following in mind:

- A preview should be located in the same source file as the Composable.
- Sample data should be located in the `previewdata` package.
- A preview should have a display name.

#### Example:

```kotlin
@Composable
fun MyComposable() {
    // ...
}

@Preview(name = "MyComposable Preview")
@Composable
fun MyComposablePreview() {
    ChatTheme {
        MyComposable()
    }
}
```

## Naming Conventions

### Naming Components

When choosing a name for your component, try not to use the naming conventions from Android View based system.

#### Do

```kotlin
@Composable
fun LoadingIndicator() {
    // ...
}
```

#### Don't

```kotlin
@Composable
fun LoadingView() {
    // ...
}
```

### Naming Listeners

We prefer using present tense for action handlers.

#### Do

```kotlin
@Composable
fun MyComposable(onChannelClick: () -> Unit) {
    // ...
}
```

#### Don't

```kotlin
@Composable
fun MyComposable(onChannelClicked: () -> Unit) {
    // ...
}
```

### Naming State

When choosing a name for a class encapsulating state for a Composable component, simply append the `State` suffix to the Composable name.

```kotlin
data class MessageItemState(
    // ...  
)

@Composable
fun MessageItem(messageItemState: MessageItemState) {
    // ...
}
```

### Naming Resources

Resource names must start with `stream_compose_` prefix.

## Customizing Components 

Customization options should be exposed where possible. 

- Theming via `ChatTheme` (ChatTheme.colors, ChatTheme.typography, etc.)
- Exposing Modifier
- Action handlers  
- Slot APIs

The order of parameters in a Composable (state, handlers, styling, Slot APIs)

### Slot APIs

We heavily rely on [Slot APIs](https://developer.android.com/jetpack/compose/layouts/basics#slot-based-layouts) when building our components. When designing a complex component, it is hard to expose every possible customization parameter. In that case, consider exposing customization slots.

Rules when implementing Slot APIs:
- Consistent naming: `leadingContent`, `centerContent`, `trailingContent`, `footerContent`, `headerContent`, `itemContent`, `content`, etc.
- The default implementations of Slot APIs should be located in the same source file and name according to the template: `Default*LeadingContent`, `Default*CenterContent`
- Padding inside Slots: we should tend to make components containing Slot APIs as simple as possible without inner padding, without margins between slots.

#### Example

```kotlin
@Composable
fun MyComposable(
    myComposableState: MyComposableState,
    // ... Modifier, Styling and Actions
    leadingContent: @Composable RowScope.(MyComposableState) -> Unit = {
        DefaultMyComposableLeadingContent(it)
    },
    headerContent: @Composable ColumnScope.(MyComposableState) -> Unit = {
        DefaultMyComposableHeaderContent(it)
    },
    centerContent: @Composable ColumnScope.(MyComposableState) -> Unit = {
        DefaultMyComposableCenterContent(it)
    },
    footerContent: @Composable ColumnScope.(MyComposableState) -> Unit = {
        DefaultMyComposableFooterContent(it)
    },
    trailingContent: @Composable RowScope.(MyComposableState) -> Unit = {
        DefaultMMyComposableTrailingContent(it)
    },
) {
    Row {
        leadingContent(messageItem)

        Column {
            headerContent(messageItem)

            centerContent(messageItem)

            footerContent(messageItem)
        }

        trailingContent(messageItem) 
    }
}
```
