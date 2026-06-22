# Compose does not expose a public API to move screen-reader (accessibility) focus, so the SDK
# resolves it reflectively via AndroidComposeView.getSemanticsOwner() when placing the screen
# reader on the composer as a message view opens. Keep that method so the lookup keeps working in
# R8-minified release builds; without it the call fails safe (focus is simply not moved).
-keepclassmembers class androidx.compose.ui.platform.AndroidComposeView {
    androidx.compose.ui.semantics.SemanticsOwner getSemanticsOwner();
}
