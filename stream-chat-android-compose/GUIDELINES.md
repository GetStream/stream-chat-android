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


--------------------


## Write Stable Classes

To optimize runtime performance, Jetpack Compose relies on being able to infer if a state that is being read has
changed.

Fundamentally, there are 3 stability types:

- **Unstable** - These hold data that is mutable and do not notify Composition upon mutating. Compose is unable to
  validate that these have not changed.
- **Stable** - These hold data that is mutable, but notify Composition upon mutating. This renders them stable since
  Composition is always informed of any changes to state.
- **Immutable** - As the name suggests, these hold data that is immutable. Since the data never changes, Compose can
  treat this as stable data.

### What are the real world implications of this?

If Compose is able to guarantee stability, it can grant certain performance benefits to a Composable, chiefly it can
mark it as skippable.

Let's create pairs of classes and Composables and generate a Compose Compiler report for them. For now, we only
care about the implications and not the mechanics, so we'll just analyze the results.

#### Let's create a stable pair:

First a stable class:

```kotlin
data class InherentlyStableClass(val text: String)
```

Then a Composable that uses said class as state:

```kotlin
@Composable
fun StableComposable(
    stableClass: InherentlyStableClass
) {
    Text(
        text = stableClass.text
    )
}
```

Now, let's generate compiler reports for our code and analyze the results.

The Class:

```
stable class InherentlyStableClass {
  stable val text: String
  <runtime stability> = Stable
}
```

The Composable

```
restartable skippable scheme("[androidx.compose.ui.UiComposable]") fun StableComposable(
  stable stableClass: InherentlyStableClass
)
```

What does this tell us?

First off, we see that because our class has all of its (in this case a single) parameters marked as stable, its runtime
stability is deemed to be stable as well.

This has implications for our Composable, which is now marked as:

- Restartable: Meaning that this composable can serve as a restarting scope. This means that whenever this Composable
  needs to recompose, it will not trigger the recomposition of its parent scope.
- Skippable: Since the only parameter our Composable uses as state is stable, Compose is able to infer when it has or
  has not changed. This makes Compose Runtime able to **skip** recomposition of this Composable when its parent scope
  recomposes and all the parameters it uses as state remain the same.

#### Let's create an ustable pair:

An unstable class:

```kotlin
data class InherentlyUnstableClass(var text: String)
```

And a Composable using it as state:

```kotlin
@Composable
fun UnstableComposable(
    unstableClass: InherentlyUnstableClass
) {
    Text(
        text = unstableClass.text
    )
}
```

Now we generate a Compose Compiler report again.

The Class:

```
unstable class InherentlyUnstableClass {
    stable
    var text: String
    <runtime stability> = Unstable
}
```

The Composable:

```
restartable scheme("[androidx.compose.ui.UiComposable]") fun UnstableComposable(
  unstable unstableClass: InherentlyUnstableClass
)
```

Our state classes and Composables perform the same job, but not equally well. Even though 90% of what makes them is
identical, because we are using an unstable class we have lost the ability to skip this composable when necessary.

For smaller Composables that do not do much of anything other than calling other Composables, this may not be such a
worrisome situation, however, for larger and more complex Composables, this can present a significant performance hit.

**Note**: Composables that do not return `Unit` will be neither skippable nor restartable. It is understandable that
these are not restartable as they are value producers and should force their parent to recompose upon change.

### Rules for Writing classes

We have inferred that we desire stability. Mostly this means we aim for immutability as gaining stability through
notifying composition requires a lot of work, such as was done by the creation of the `MutableState<T>` class.

1) #### Do not use `var`s as properties inside state holding classes

As these are mutable, but do not notify composition, they will make the composables which use them unstable.

Do:

```kotlin
data class InherentlyStableClass(val text: String)
```

Don't:

```kotlin
data class InherentlyUnstableClass(var text: String)
```

2) #### Private properties still affect stability

As of the time of writing, it is uncertain if this is a design choice or a bug, but let's slightly modify our stable
class from above.

```kotlin
data class InherentlyStableClass(
    val publicStableProperty: String,
    private var privateUnstableProperty: String
)
```

The compiler report will mark this class as unstable:

```
unstable class InherentlyStableClass {
  stable val publicStableProperty: String
  stable var privateUnstableProperty: String
  <runtime stability> = Unstable
}
```

Looking at the results, it's fairly obvious that the compiler struggles here. It marks both individual properties as
stable, even though one is not, but marks the whole class as unstable.

3) #### Do not use classes that belong to an external module

Sadly, Compose can only infer stability for classes, interfaces and objects that originate from a module compiled by the
Compose Compiler. This means that any externally originated class will be marked as unstable, regardless of its true
stability.

Do:

// TODO generate example

Don't:

// TODO generate example, we should create a UI model here

4) #### Do not expect immutability from collections

Things such as `List`, `Set` and `Map` might seem immutable at first, but they are not and the Compiler will mark them
as unstable.

Currently, there are two alternatives, the more straightforward one includes using
Kotlin's [immutable collections](https://github.com/Kotlin/kotlinx.collections.immutable).
However, these are still pre-release and might not be viable.

The other solution, which is a technical hack and not officially advised but used by the community is to wrap your lists
and mark the wrapper class as `@Immutable`.

```kotlin
@Immutable
data class WrappedList(
    val list: List<String> = listOf()
)
```

Here the compiler still marks the individual property as unstable, but marks the whole wrapper class as stable.

```
stable class WrappedList {
  unstable val list: List<String>
}
```

Currently, neither of the two solutions are ideal.

5) #### `Flow`s are unstable

Even though they might seem stable since they are observable, `Flow`s do not notify composition when they emit new
values. This makes them inherently unstable. Use them only if absolutely necessary.

6) #### Inlined Composables are neither restartable nor skippable

As with all inlined functions, these can present performance benefits. Some common Composables such as `Column`, `Row`
and `Box` are all inlined. As such this is not an admonishment of inlining Composables, just a suggestion that you
should be mindful when inlining composables, or using inlined composables and be aware of how they affect the parent
scope recomposition.

We will cover this in more detail in future sections.

### Hoist state properly

Hoisting state is the act of creating stateless Composables. The formula is simple:

- All necessary state should be passed down from the Composable's caller
- ALl events should be flow upwards to the source of the state

Let's create a simple example.

```kotlin
@Composable
fun CustomButton(text: String, onClick: () -> Unit) {
    Button(onClick = onClick) {
        Text(text = text)
    }
}
```

And host it inside set content:

```kotlin
setContent {
    var count by remember {
        mutableStateOf(0)
    }

    CustomButton(text = "Clicked $count times") {
        count++
    }
}
```

Due to state hoisting, our composable is well-behaved, it follows unidirectional flow and is more testable.

However, this doesn't make us completely safe, as we can easily misuse this pattern in more complex scenarios.

#### Don't read the state too high

Let's presume we have a slightly more complex state holder:

```kotlin
class StateHoldingClass {
    var counter by mutableStateOf(0)
    var whatAreWeCounting by mutableStateOf("Days without having to write XML.")
}
```

And it's being used in the following manner:

```kotlin
@Composable
fun CustomButton(count: Int, onClick: () -> Unit) {
    Button(onClick = onClick) {
        Text(text = count.toString())
    }
}
```

In our hypothetical scenario this state holder is hosted inside a `ViewModel` which is a common practice, and read in
the following manner:

```kotlin
setContent {
    val viewModel = ViewModel()

    Column {
        Text("This is a cool column I have")
        CustomButton(count = viewModel.stateHoldingClass.counter) {
            viewModel.stateHoldingClass.counter++
        }
    }
}
```

At first glance, this might seem perfectly fine, you might think that because the property `StateHoldingClass.counter`
is being used as a `CustomButtom` parameter that means that only `CustomButton` gets recomposed, however this is not the
case.

This counts as a state read inside `Column`, meaning that the whole `Column` now has to get recomposed. But our woes
don't end there. Since `Column` is an inlined function, this means that it will trigger recomposition of its parent
scope as well.

Luckily we have an easy way of avoiding this, the answer is to **lower state reads**.

Let's re-write our Composable in the following manner:

```kotlin
@Composable
fun CustomButton(stateHoldingClass: StateHoldingClass, onClick: () -> Unit) {
    Button(onClick = onClick) {
        Text(text = stateHoldingClass.counter.toString())
    }
}
```

And change the call site to:

```kotlin
setContent {
    val viewModel = ViewModel()

    Column {
        Text("This is a cool column I have")
        CustomButton(stateHoldingClass = viewModel.stateHoldingClass) {
            viewModel.stateHoldingClass.counter++
        }
    }
}
```

Now the state read is happening inside `CustomButton`, this means that we will only recompose the contents of said
Composable. Both `Column` and it's parent scope are spared unnecessary recomposition in this scenario!

### Avoid running expensive calculations unnecessarily.

Let's create the following Composable:

```kotlin
@Composable
fun ConcertPerformers(venueName: String, performers: PersistentList<String>) {
    val sortedPerformers = performers.sortedDescending()

    Column {
        Text(text = "The following performers are performing at $venueName tonight:")

        LazyColumn {
            items(items = sortedPerformers) { performer ->
                PerformerItem(performer = performer)
            }
        }
    }
}
```

This is a simple composable, it displays the name of a venue, along with the performs who are performing at that venue.
It also wants to sort that list so that the readers have an easier time finding if a performer they are interested in is
performing.

However, it has one key flaw. If the venue gets changed, but the list of performers stays the same, the list will have
to be sorted again. This is a potentially very costly operation. Luckily it's fairly easy to run it only when necessary.

```kotlin
@Composable
fun ConcertPerformers(venueName: String, performers: PersistentList<String>) {
    val sortedPerformers = remember(performers) {
        performers.sortedDescending()
    }

    Column {
        Text(text = "The following performers are performing at $venueName tonight:")

        LazyColumn {
            items(items = sortedPerformers) { performer ->
                PerformerItem(performer = performer)
            }
        }
    }
}
```

In this example, you've used `remember` that uses `performers` as a key to calculate the sorted list. It will
re-calculate _only_ when the list of performers changes, sparing unnecessary recomposition.

If you have access to the original `State<T>` instance, you can reap additional benefits by deriving state directly from
it, such as in the following example (please note the changed function signature):

```kotlin
@Composable
fun ConcertPerformers(venueName: String, performers: State<PersistentList<String>>) {
    val sortedPerformers = remember {
        derivedStateOf { performers.value.sortedDescending() }
    }

    Column {
        Text(text = "The following performers are performing at $venueName tonight:")

        LazyColumn {
            items(items = sortedPerformers.value) { performer ->
                PerformerItem(performer = performer)
            }
        }
    }
}
```

Here, Compose does not only skip unnecessary calculations, but is smart enough to skip recomposing the parent scope
since you're only changing a locally read property and not recomposing the whole function with new parameters.