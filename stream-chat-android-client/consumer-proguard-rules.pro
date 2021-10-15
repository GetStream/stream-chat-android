## Stream Chat Android Client Proguard Rules

# Classes that are using with QuerySort can't be minified, because QuerySort uses reflection. If the
# name of the fields of the classes being used by QuerySort, change, the sort won't work as expected.
-keep class io.getstream.chat.android.client.models.** { *; }
-keep class io.getstream.chat.android.client.api2.model.** { *; }

# ExtraDataDto can't be minified because we check for extraData using reflection in
# io.getstream.chat.android.client.parser2.adapters.CustomObjectDtoAdapter. If the name of extraData
# is changed, we will have problem with serialization.
-keep class * extends io.getstream.chat.android.client.api2.model.dto.ExtraDataDto { *; }
