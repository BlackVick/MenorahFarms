# This is a configuration file for ProGuard.
-dontusemixedcaseclassnames
-dontskipnonpubliclibraryclasses
-verbose
-dontpreverify


# Enable Optimization. # Optimization is turned off by default.
-optimizations   code/simplification/arithmetic,!code/simplification/cast,!field
-optimizationpasses 5
-allowaccessmodification


#Disable Optimization
#-dontoptimize
#-dontpreverify

# Remove Log command from code
-assumenosideeffects class android.util.Log{
 public static *** d(...);
 public static *** i(...);
 public static *** v(...);
}

#javascript
-keepclassmembers class fqcn.of.javascript.interface.for.webview {
   public *;
}

# -------------------------------------------------

#model package
-keep class com.blackviking.menorahfarms.Models.** {*;}

#notification package
-keep class com.blackviking.menorahfarms.Notification.** {*;}

#interface
-keep class com.blackviking.menorahfarms.Interface.** {*;}

#common
-keep class com.blackviking.menorahfarms.Common.** {*;}

#viewholders
-keep class com.blackviking.menorahfarms.ViewHolders.** {*;}


#class extensions
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.backup.BackupAgentHelper
-keep public class * extends android.app.backup.BackupAgent
-keep public class * extends android.preference.Preference
-keep public class * extends androidx.recyclerview.widget.RecyclerView

-keep public class * extends android.support.v4.app.Fragment
-keep public class * extends android.support.v4.app.DialogFragment
-keep public class * extends com.actionbarsherlock.app.SherlockListFragment
-keep public class * extends com.actionbarsherlock.app.SherlockFragment
-keep public class * extends android.app.Fragment
-keep public class com.android.vending.licensing.ILicensingService
-keep public class * extends java.lang.Exception



# -------------------------------------------------
-keepattributes *Annotation*, Signature, Exception, EnclosingMethod, InnerClasses
-keepattributes JavascriptInterface

# Keep source file name and line number
-keepattributes SourceFile,LineNumberTable

-keep class okhttp3.** {*;}
-keep interface okhttp3.** {*;}
-dontwarn okhttp3.**

-keep class android.support.v7.** { *; }
-keep interface android.support.v7.** { *; }
-dontwarn android.support.v7.**



# Dontwarn-----------------------------------------
-dontwarn javax.**
-dontwarn java.lang.management.**
-dontwarn org.apache.log4j.**
-dontwarn org.apache.commons.logging.**
-dontwarn android.support.**
-dontwarn com.google.ads.**
-dontwarn org.slf4j.**
-dontwarn org.json.**


-keep public class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
    public void set*(...);
}


# -------------------------------------------------
-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}

-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet, int);
}
-keepclasseswithmembernames class * {
    native <methods>;
}


-keepclassmembers class * extends android.content.Context {
   public void *(android.view.View);
   public void *(android.view.MenuItem);
}

-keepclassmembers class * implements android.os.Parcelable {
    static ** CREATOR;
}

-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    !static !transient <fields>;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

-keepclassmembers class * extends android.app.Activity {
   public void *(android.view.View);
}

-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

-keepclassmembers class **.R$* {
    public static <fields>;
}

-keepclassmembers class * {
    @android.webkit.JavascriptInterface <methods>;
}


# Support Library
-keep class android.support.** {*;}
-keep interface android.support.** {*;}


# Needed when building against Marshmallow SDK.
  -dontwarn android.app.Notification


# Retrofit and GSON
-keep class com.squareup.okhttp3.** { *; }
-keep interface com.squareup.okhttp3.** { *; }
-dontwarn com.squareup.okhttp3.**

-keep class retrofit2.** { *; }
-dontwarn retrofit2.**

-keep class com.google.gson.** { *; }
-dontwarn com.google.gson.**

-keep class sun.misc.Unsafe.** { *; }
-dontwarn sun.misc.Unsafe.**

-keep public class com.google.gson.** {*;}
-keep class * implements com.google.gson.** {*;}
-keep class com.google.gson.stream.** { *; }
-dontwarn com.google.gson.**

-keep class * implements com.blackviking.menorahfarms.Interface.ItemClickListener
#-keep class * implements com.google.gson.JsonSerializer
#-keep class * implements com.google.gson.JsonDeserializer

-keepclasseswithmembers class * {@retrofit2.http.* <methods>;}
-keepclasseswithmembers interface * { @retrofit2.* <methods>;}
-dontwarn com.google.appengine.**
-dontwarn java.nio.file.**
-dontwarn org.codehaus.**
-dontwarn org.codehaus.mojo.**
-dontwarn retrofit2.Platform$Java8
-dontnote retrofit2.Platform
-dontnote retrofit2.Platform$IOS$MainThreadExecutor


## Butterknife 8.0
-keep public class * implements butterknife.Unbinder { public <init>(**, android.view.View); }
-keep class butterknife.*
-keepclasseswithmembernames class * { @butterknife.* <methods>; }
-keepclasseswithmembernames class * { @butterknife.* <fields>; }


# EventBus
-keepclassmembers class * {
    @org.greenrobot.eventbus.Subscribe <methods>;
}
-keep enum org.greenrobot.eventbus.ThreadMode { *; }
# Only required if you use AsyncExecutor
-keepclassmembers class * extends org.greenrobot.eventbus.util.ThrowableFailureEvent {
    <init>(java.lang.Throwable);
}


# OkHttp and Picasso
-keep class com.squareup.okhttp.** { *; }
-keep interface com.squareup.okhttp.** { *; }
-dontwarn com.squareup.okhttp.**


# Otto
-keepclassmembers class ** {
    @com.squareup.otto.Subscribe public *;
    @com.squareup.otto.Produce public *;
}

# Slidingmenu
-keep class com.jeremyfeinstein.** { *; }

# Pulltorefresh
-keep class uk.co.senab.actionbarpulltorefresh.library.** { *; }

# OrmLite
-keep class com.j256.**
-keepclassmembers class com.j256.** { *; }
-keep enum com.j256.**
-keepclassmembers enum com.j256.** { *; }
-keep interface com.j256.**
-keepclassmembers interface com.j256.** { *; }
-dontwarn com.j256.**

# Keep line number
-keepattributes SourceFile,LineNumberTable


# Google Play Admob
    -keep public class com.google.android.gms.ads.** {
        public *;
    }

    -keep public class com.google.ads.** {
         public *;
    }

# Dagger 2
 -dontwarn com.google.errorprone.annotations.**

# Rxjava
 -keep class rx.schedulers.Schedulers {
     public static <methods>;
 }
 -keep class rx.schedulers.ImmediateScheduler {
     public <methods>;
 }
 -keep class rx.schedulers.TestScheduler {
     public <methods>;
 }
 -keep class rx.schedulers.Schedulers {
     public static ** test();
 }
 -keepclassmembers class rx.internal.util.unsafe.*ArrayQueue*Field* {
     long producerIndex;
     long consumerIndex;
 }
 -keepclassmembers class rx.internal.util.unsafe.BaseLinkedQueueProducerNodeRef {
     long producerNode;
     long consumerNode;
 }

 # Realm
 -keepnames public class * extends io.realm.RealmObject
 -keep @io.realm.annotations.RealmModule class *
 -keep class io.realm.** { *; }
 -dontwarn io.realm.**

# Lru Cache
-keep class com.squareup.picasso.LruCache { *; }
-dontwarn com.squareup.picasso.LruCache.**

# Firebase
-keep class com.firebase.** { *; }
-dontwarn com.firebase.**

-keepnames class com.shaded.fasterxml.** { *; }
-dontwarn org.shaded.apache.**

-keep class org.apache.** { *; }
-dontwarn org.apache.**

-keepnames class org.ietf.jgss.** { *; }
-dontwarn org.ietf.jgss.**

-keepnames class com.fasterxml.jackson.** { *; }
-dontwarn com.fasterxml.**

-keepnames class javax.servlet.** { *; }

-dontwarn org.w3c.dom.**
-dontwarn org.joda.time.**
-dontnote com.firebase.client.core.GaePlatform

#---------------------------------------------------------------------------------------
# My Personal
#-keep public class shishirtstudio.com.proguardtest.MyPack.** {
#  private protected public *;
#}

# CirlceImageView- No need pro guard rules..
# --------------------------------------------------
# Here include the POJO's that have you have created for mapping JSON response to POJO in Retrofit/Application classes that will be serialized/deserialized over Gson
-keep class shishirtstudio.com.proguardtest.data.network.apiResponse.** { *; }