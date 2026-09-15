# GymFuel

GymFuel is a private, offline-first Android nutrition tracker for controlled muscle gain. It is being built as a native Kotlin/Jetpack Compose application with Room for local persistence and Supabase for authenticated recovery and synchronization.

The approved product behavior is in [`docs/product-spec.md`](docs/product-spec.md), and the delivery sequence is in [`tasks/plan.md`](tasks/plan.md).

## Local requirements

- Android Studio 2026.1.2 or a compatible stable release
- Android SDK Platform 37 and Build Tools 36.0.0
- JDK 17 or newer; this workspace has been verified with Android Studio's bundled JDK 21
- An Android device with USB debugging enabled for device tests

The machine's default Java may be older than Gradle supports. From PowerShell, scope the bundled JDK to the current shell before invoking Gradle:

```powershell
$env:JAVA_HOME = 'C:\Program Files\Android\Android Studio\jbr'
.\gradlew.bat testDebugUnitTest lintDebug assembleDebug
```

`local.properties` contains the machine-specific Android SDK path and is intentionally excluded from Git.

## Verified dependency baseline

- Android Gradle Plugin 9.4.0
- Gradle 9.6.0, the version in AGP 9.4's compatibility table
- Kotlin/Compose compiler plugin 2.3.21, the version in the current Compose setup guide
- Compose BOM 2026.09.00, the latest stable release in Google's Maven repository when the project was scaffolded

AGP 9 enables built-in Kotlin, so the project intentionally does not apply the obsolete `org.jetbrains.kotlin.android` plugin.

## Authoritative references

- [AGP 9.4 compatibility](https://developer.android.com/build/releases/agp-9-4-0-release-notes#compatibility)
- [Migrate to built-in Kotlin](https://developer.android.com/build/migrate-to-built-in-kotlin)
- [Compose compiler and BOM setup](https://developer.android.com/develop/ui/compose/setup-compose-dependencies-and-compiler)
- [Compose BOM mapping](https://developer.android.com/develop/ui/compose/bom/bom-mapping)

## Current state

The foundation builds a small installable shell. Nutrition domain logic, Room persistence, Supabase configuration, and personal data are not yet present.
