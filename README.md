<p align="right">
  English | <a href="./README.ja.md">日本語</a>
</p>

# Bunan Todo

**Bunan** comes from the Japanese word *無難*. For this app, the name represents deliberate simplicity: everything you need for everyday task management, without unnecessary features or complexity.

Bunan Todo focuses on doing the essentials well while remaining simple and straightforward.

An Android todo app that helps you organize your day around planned dates

## Download

Download the latest signed APK from [GitHub Releases](https://github.com/twelnina/android-todo-app/releases/latest).

1. Download `app-release.apk` from the latest release.
2. Open the APK on your Android device and follow the on-screen instructions.
3. If prompted, allow your browser or file manager to install unknown apps.

Bunan Todo requires Android 10 (API 29) or later.

## About Bunan Todo

Bunan Todo is a task management app that keeps today's todos and overdue unfinished todos within
easy reach. Organize todos by planned date and tag, then switch between Home, List, and Calendar
views to focus on what you need.

All data is stored locally on your device, so the app works offline.

This project also serves as a practical reference for developers learning modern Android
development. It demonstrates how Jetpack Compose, ViewModel, StateFlow, Room, and automated testing
can work together in a small, approachable app.

## Features

- **Todo management** — Create, edit, delete, and complete todos with a title, description, planned
  date, and tag.
- **Today at a glance** — View today's todos separately from overdue unfinished todos, and
  reschedule overdue items directly from the Home screen.
- **Search and filters** — Search titles and descriptions, select multiple tags, and filter todos by
  planned date.
- **Date-based list** — Browse todos grouped by date and quickly switch between All, Today,
  Tomorrow, This week, Past unfinished, and Unscheduled views.
- **Calendar view** — See the number of todos on each day of the month and view the todos scheduled
  for a selected date.
- **Five tags** — Organize todos as Study, Work, Health, Hobby, or Shopping.
- **Undo delete** — Restore a todo from the snackbar immediately after deleting it.
- **English and Japanese** — Follow the device language or a per-app language preference.
- **Adaptive themes** — Support light and dark themes, plus dynamic color on Android 12 and later.

## Tech stack

| Category         | Technologies                                                            |
|------------------|-------------------------------------------------------------------------|
| Language         | Kotlin                                                                  |
| UI               | Jetpack Compose, Material 3                                             |
| Navigation       | Navigation 3, Kotlin Serialization                                      |
| State management | ViewModel, StateFlow, Kotlin Coroutines                                 |
| Database         | Room, KSP                                                               |
| Calendar         | Kizitonwose Calendar for Compose                                        |
| Testing          | JUnit, AndroidX Test, Espresso, Compose UI Test, Gradle Managed Devices |
| Build            | Gradle Kotlin DSL, Version Catalog                                      |
| CI/CD            | GitHub Actions                                                          |

The app follows a layered structure in which the Compose UI communicates with ViewModels,
repositories, and Room. Room stores todos in SQLite, while Flow streams data changes back to the UI.

```text
Compose UI → ViewModel → Repository → Room → SQLite
```

## Requirements

- Android 10 (API 29) or later
- JDK 17
- Android SDK 37
- Android Studio

## Build

Clone the repository and build the debug APK from the project root.

```bash
git clone https://github.com/twelnina/bunan-todo-app.git
cd bunan-todo-app
./gradlew assembleDebug
```

The APK is generated at `app/build/outputs/apk/debug/app-debug.apk`. To install it directly on a
connected device or running emulator, use:

```bash
./gradlew installDebug
```

## Test

Run the local unit tests, Android Lint, and instrumented tests on a Gradle Managed Device with:

```bash
./gradlew testDebugUnitTest
./gradlew lintDebug
./gradlew pixel10Api36DebugAndroidTest
```

For pull requests and pushes to `main`, GitHub Actions automatically runs the tests and Lint, then
builds the debug APK.

## Release APK

Pushing a tag that starts with `v`, or manually running the Release APK workflow from GitHub
Actions, builds a signed release APK. Download the generated APK from the `bunan-todo-release-apk`
artifact in the workflow run. APKs published for users are available from
[GitHub Releases](https://github.com/twelnina/android-todo-app/releases).

## License

This project is licensed under the [MIT License](./LICENSE).
