## Stream Chat Android Client Proguard Rules

-keep class io.getstream.chat.android.client.api.* { *; }
-keep class io.getstream.chat.android.client.api.models.* { *; }
-keep class io.getstream.chat.android.client.errors.* { *; }
-keep class io.getstream.chat.android.client.events.* { *; }
-keep class io.getstream.chat.android.client.models.* { *; }
-keep class io.getstream.chat.android.client.socket.* { *; }
-keep class io.getstream.chat.android.client.utils.SyncStatus { *; }

-keepattributes Signature,*Annotation*

-keepattributes EnclosingMethod