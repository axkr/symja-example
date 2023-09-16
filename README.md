# Overview

This is an Android application for [Symja computer algebra language](https://github.com/axkr/symja_android_library)

# Features

## 1. Console

<img src="docs/img.png" width="200" height="auto">
<img src="docs/img_4.png" width="200" height="auto">
<img src="docs/img_1.png" width="200" height="auto">

## 2. Symja Pods

<img src="docs/img_2.png" width="200" height="auto">
<img src="docs/img_3.png" width="200" height="auto">
<img src="docs/img_9.png" width="200" height="auto">

## 3. Documentation


<img src="docs/img_6.png" width="200" height="auto">
<img src="docs/img_5.png" width="200" height="auto">
<img src="docs/img_7.png" width="200" height="auto">
<img src="docs/img_8.png" width="200" height="auto">

# Gradle Configuration

The repositories which are used by Gradle are defined here:
- https://github.com/axkr/symja-example/blob/main/settings.gradle

The Symja snapshots, which are used by this example app, are published with a Github Maven action, which could be monitored here:
- https://github.com/axkr/symja_android_library/actions
- https://oss.sonatype.org/content/repositories/snapshots/org/matheclipse/matheclipse-core/3.0.0-SNAPSHOT/

# Building

```shell
chmod +x gradlew
./gradlew assembleDebug
```
