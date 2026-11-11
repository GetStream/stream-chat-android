## Stream Chat Android UI Common Proguard Rules

# The client finds these classes by name and builds them through kotlin-reflect, which needs the class kept for its
# Kotlin metadata, and its constructors.
-keep class io.getstream.chat.android.ui.common.notifications.StreamCoilUserIconBuilder { <init>(...); }
-keep class io.getstream.android.push.permissions.snackbar.SnackbarNotificationPermissionHandler { <init>(...); }
