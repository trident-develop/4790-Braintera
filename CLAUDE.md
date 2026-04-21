# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

Braintera — single-module Android app, Kotlin + Jetpack Compose (Material 3). Package / applicationId: `com.com2us.wannabe.android.google.global.nor`. minSdk 24, compileSdk/targetSdk 36, Java 11. Dependency versions are centralized in `gradle/libs.versions.toml` (version catalog); reference libraries/plugins via `libs.*` in `build.gradle.kts` rather than hard-coding coordinates.

The project is essentially a fresh scaffold: two empty `ComponentActivity` subclasses (`LoadingActivity` is the launcher per `AndroidManifest.xml`, `MainActivity` is unexported) with empty `setContent {}` blocks, plus the default Compose `ui/theme/` (Color, Theme, Type). Expect to be building out real UI and navigation from near-zero.

## Common commands

Run from repo root using the wrapper:

- Build debug APK: `./gradlew :app:assembleDebug`
- Install on connected device/emulator: `./gradlew :app:installDebug`
- Unit tests (JVM): `./gradlew :app:testDebugUnitTest`
- Single unit test: `./gradlew :app:testDebugUnitTest --tests "com.com2us.wannabe.android.google.global.nor.ExampleUnitTest.addition_isCorrect"`
- Instrumented tests (needs device/emulator): `./gradlew :app:connectedDebugAndroidTest`
- Lint: `./gradlew :app:lintDebug` (report at `app/build/reports/lint-results-debug.html`)
- Clean: `./gradlew clean`

## Notes

- `local.properties` contains the local `sdk.dir` — do not commit or rely on its contents in code.
- `LoadingActivity` is the LAUNCHER activity (not `MainActivity`); keep this in mind when wiring navigation or changing entry points.
