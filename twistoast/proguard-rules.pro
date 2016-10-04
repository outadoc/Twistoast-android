## Background workers
-keep public class fr.outadev.twistoast.background.PebbleWatchReceiver
-keep public class fr.outadev.twistoast.background.NextStopAlarmReceiver
-keep public class fr.outadev.twistoast.background.TrafficAlertAlarmReceiver
-keep public class fr.outadev.twistoast.background.BootReceiver

-keep public enum fr.outadev.twistoast.Database$SortBy

-printmapping build/outputs/mapping/release/mapping.txt

## Android support libs
-dontwarn android.support.v7.**
-dontwarn android.support.v4.**
-keep class android.support.v7.** { *; }
-keep interface android.support.v7.** { *; }

-dontwarn android.support.design.**
-keep class android.support.design.** { *; }
-keep interface android.support.design.** { *; }

-dontwarn android.support.customtabs.**
-keep class android.support.customtabs.** { *; }
-keep interface android.support.customtabs.** { *; }

## 3rd party libs
-dontwarn com.google.common.**
-dontwarn okio.**

-dontwarn org.joda.convert.**
-dontwarn org.joda.time.**
-keep class org.joda.time.** { *; }
-keep interface org.joda.time.** { *; }

-keep class javax.xml.stream.** { *; }
-keep interface javax.xml.stream.** { *; }
-dontwarn javax.xml.stream.**
