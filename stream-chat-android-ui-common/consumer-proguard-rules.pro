## Stream Chat Android UI Common Proguard Rules

# The client finds this class by name and builds it with its Context constructor.
-keep class io.getstream.chat.android.ui.common.notifications.StreamCoilUserIconBuilder { public <init>(android.content.Context); }
