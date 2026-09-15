# Task completion

- Run focused tests for changed behavior, then `rtk test .\gradlew.bat testDebugUnitTest lintDebug assembleDebug` with Android Studio JDK 21 scoped as `JAVA_HOME`.
- Run `connectedDebugAndroidTest` for changed Room/Compose/device behavior when an authorized device or emulator is available.
- Inspect `rtk git diff --check`, `rtk git status`, and staged diff before commit.
- Scan staged changes for credentials/tokens, personal nutrition data, local DBs, APKs, keystores, and machine-local config.
- Do not mark a checklist item complete while its manual verification remains outstanding.
- Keep docs/spec/tasks aligned when behavior, architecture, or scope changes.
- Commit each green vertical increment atomically with conventional commit messages.