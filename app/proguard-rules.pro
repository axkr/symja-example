# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*,!code/allocation/variable
-dontwarn java.awt.**
-dontwarn java.beans.**
-dontwarn java.lang.management.**
-dontwarn javax.lang.model.**
-dontwarn javax.imageio.**
-dontwarn org.osgi.framework.**
-dontwarn org.owasp.encoder.**
-dontwarn org.apache.lucene.**

-dontwarn org.matheclipse.io.builtin.IntegerName
-dontwarn org.matheclipse.io.builtin.RemoveDiacritics
-dontwarn org.matheclipse.io.builtin.Transliterate