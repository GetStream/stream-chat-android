# Compose SDK Guidelines

## Table of Contents

* [Motivation](#motivation)
* [Project Structure](#project-structure)
* [Writing documentation](#writing-documentation)
  * [KDoc Comments](#kdoc-comments)
* [Naming Conventions](#naming-conventions)
  * [Naming Components](#naming-components)
  * [Naming Listeners](#naming-listeners)
  * [Naming State](#naming-state)
  * [Naming Resources](#naming-resources)
* [Customizing Components](#customizing-components)
  * [Customization Options](#customization-options)
  * [Slot APIs](#slot-apis)
* [Drawable colors](#drawable-colors)
* [Component Previews](#component-previews)  

## Motivation

We follow official [Guidelines For Compose](https://github.com/androidx/androidx/blob/androidx-main/compose/docs/compose-api-guidelines.md) when developing our Compose SDK. On top of that, to make the code consistent and easy to maintain, we developed a set of coding conventions described in this document.

Contributions to this project are very much welcome! Even if some of the requirements below are not met, don't hesitate to submit your code changes. 💙

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
* **state**: Contains classes encapsulating state for Composable UI components.
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

## Writing documentation

### KDoc Comments

For code comments, we use [KDoc](https://kotlinlang.org/docs/kotlin-doc.html). We believe that **all of the code** should be covered with KDoc. This includes classes, methods, fields, parameters and return values.

When writing code comments, keep the following in mind:

- Use full sentences: start with capital letters, end with punctuation.
   ```diff
   - the default message container for all messages
   + The default message container for all messages.
   ```
- Do NOT put dashes between the KDoc tag and its contents, as these get interpreted as bullet points (KDoc supports markdown).
   ```diff
   - * @param modifier - Modifier for styling.
   + * @param modifier Modifier for styling.
   - * @return - The display name for the given channel.
   + * @return The display name for the given channel.
   ```
- Do NOT end comments with `* */`. Just use `*/`.
   ```diff
   - * */
   + */
   ```
- Use square brackets to refer to other declarations (classes, functions, properties, etc) in the code where applicable.
  ```
  Checks if the [Channel] is distinct.
  ```

## Naming Conventions

### Naming Components

When choosing a name for your component, try to avoid the naming conventions from Android view-based system.

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

### Customization Options

Customization options should be exposed where possible:

- Modifiers for styling
- Action handlers
- Styling options
- [Slot APIs](https://developer.android.com/jetpack/compose/layouts/basics#slot-based-layouts)

#### Example:

```kotlin
@Composable
fun UserAvatar(
    // State
    user: User,
    // Modifier and Styling
    modifier: Modifier = Modifier,
    shape: Shape = ChatTheme.shapes.avatar,
    // Action Handlers
    onClick: (() -> Unit)? = { },
    // Slot APIs
    onlineIndicator: @Composable BoxScope.() -> Unit = { ... }
)
```

### Slot APIs

We heavily rely on [Slot APIs](https://developer.android.com/jetpack/compose/layouts/basics#slot-based-layouts) when building our components. When designing a complex component, it is hard to expose every possible customization parameter. In that case, consider exposing customization slots instead.

Rules when implementing Slot APIs:
- **Consistent naming**: When exposing content slots consider using these common names: `leadingContent`, `centerContent`, `trailingContent`, `footerContent`, `headerContent`, `itemContent`, `content`.
- **Default implementation**: The default implementation of a content slot should be internal, located in the same source file and should be named according to this pattern: `Default*LeadingContent`, `Default*CenterContent`, etc.  
- **Inner padding**: A component containing slots should be as simple as possible. That's why we try to handle paddings inside slots (for example, instead of adding a margin between slots).

#### Example:

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

@Composable
internal fun RowScope.DefaultMyComposableLeadingContent(myComposableState: MyComposableState) {
    // The padding is handled inside the default implementation
    val modifier = Modifier.padding(start = 16.dp)
    // ...
}
```

## Drawable colors

Whenever possible, prefer to create drawable resources that have a `#000000` color, and then tint them at the use site (instead of creating drawable resources that are specific colors, e.g. red or grey).

#### Example:

```kotlin
Icon(
    painter = painterResource(id = R.drawable.stream_compose_ic_share),
    contentDescription = null,
    tint = ChatTheme.colors.textHighEmphasis
)
```

## Component Previews

We provide component [previews](https://developer.android.com/jetpack/compose/tooling#preview) for [stateless](https://getstream.io/chat/docs/sdk/android/compose/component-architecture/#stateless-components) components.

When creating component previews, keep the following in mind:

- Place previews in the same source file as the Composable.
- Set a display name for the preview.
- Store sample data for the preview in the `previewdata` package.

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