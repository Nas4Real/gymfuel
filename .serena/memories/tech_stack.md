# Tech stack

- Native Android; Kotlin with AGP built-in Kotlin; Jetpack Compose + Material 3.
- AGP 9.4.0; Gradle wrapper 9.6.0; Compose compiler/Kotlin 2.3.21; Compose BOM 2026.09.00.
- compileSdk/targetSdk 37; minSdk 26; Java bytecode 17; Gradle runs with Android Studio bundled JDK 21.
- AndroidX stable releases only unless a spec change approves preview use.
- Planned persistence/sync: Room/SQLite, WorkManager outbox, Supabase Auth/Postgres with RLS.
- Version pins live only in `gradle/libs.versions.toml`; no dynamic versions.
- Authoritative setup links and local requirements are in `README.md`.