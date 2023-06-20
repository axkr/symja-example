# Overview

This is an example application that provides the minimal configuration to make the [Symja computer algebra language](https://github.com/axkr/symja_android_library) work on Android.

# Building

```shell
chmod +x gradlew
./gradlew assembleDebug
```

# Screenshots

<img src="docs/img_1.png" width="200" height="auto"> <img src="docs/img_2.png" width="200" height="auto">

# Gradle Configuration

The repositories which are used by Gradle are defined here:
- https://github.com/axkr/symja-example/blob/main/settings.gradle

The Symja snapshots, which are used by this example app, are published with a Github Maven action, which could be monitored here:
- https://github.com/axkr/symja_android_library/actions
- https://oss.sonatype.org/content/repositories/snapshots/org/matheclipse/matheclipse-core/3.0.0-SNAPSHOT/
