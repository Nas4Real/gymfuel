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

The Android application now includes the complete personal-use nutrition slice:

- English, dark Poppins Compose UI for authentication, onboarding, Home, food library, and settings
- horizontally scrollable Home dates from the first of the current month through today, backed by indefinite history retention
- compact three-destination navigation pill with a separate circular log action on its right
- owner-scoped Room foods, profiles, immutable meal snapshots, effective-dated targets, tombstones, and a durable sync outbox
- weighed food logging with live calorie/protein/carbohydrate/fat calculation and consumed/planned states
- Mifflin-St Jeor muscle-gain target calculation
- mandatory Supabase email/password authentication, first-login body-profile onboarding, and RLS-protected complete-history synchronization
- success feedback with Undo, long-press entry removal, sync health, profile editing, and selectable-range CSV export
- bundled food examples and WebP images, with seed templates also stored in Supabase

Copy the keys from `.env.example` into untracked `local.properties` before building cloud-enabled variants:

```properties
SUPABASE_URL=https://your-project-ref.supabase.co
SUPABASE_PUBLISHABLE_KEY=sb_publishable_your_key
```

The Android client must use only a publishable key. Never place a Supabase secret or `service_role` key in this repository or the APK.

Supabase Auth must allow the native confirmation callback `com.gymfuel.app://auth-callback` in **Authentication → URL Configuration**. The app requests that URL during sign-up, handles it through an Android browsable intent filter, and exchanges the returned PKCE code for a persisted session.
