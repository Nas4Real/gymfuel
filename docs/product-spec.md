# Product Specification: GymFuel

Status: Draft for owner review
Product: Private Android nutrition tracker
Primary user: The repository owner

## Assumptions

1. The first release targets Android only and is built natively with Kotlin and Jetpack Compose.
2. Every private account signs in with email credentials. One account is active on a phone at a time, and local/cloud records are partitioned by authenticated user id.
3. The app must remain usable without a network connection; the UI reads and writes local data first.
4. Supabase is the authenticated cloud copy used for recovery and synchronization, not the UI's direct source of truth.
5. GitHub stores source code and documentation only. Personal nutrition records never enter the Git repository.
6. Foods are measured primarily in grams with nutrient values defined per 100 g.
7. The first goal is controlled muscle gain. Suggested targets remain editable because calculated nutrition targets are estimates.
8. Version 1 is nutrition-only. Workout tracking and nutrition-to-strength correlation are explicitly deferred.

## Objective

Build a reliable personal Android app that answers two questions quickly:

1. What calories, protein, carbohydrates, and fat have I consumed today, and what remains to reach my targets?
2. If I also eat everything currently planned, where will my daily totals land?

The app must minimize repeated data entry, preserve trustworthy historical calculations, and recover the user's data after replacing or losing a phone.

### Core user stories

- As the user, I can calculate an initial muscle-gain target from my body profile and activity level, inspect the formula, and adjust the result before saving it.
- As the user, I can create and edit foods such as chicken, meat, rice, and oats with nutrients per 100 g.
- As the user, I can attach a food image so the library and picker are faster to scan.
- As the user, I can distinguish preparation states such as raw and cooked so portions are not compared against the wrong nutrient values.
- As the user, I can add a weighed food portion directly as consumed or add it as planned for later.
- As the user, I can mark a planned entry as consumed, change its actual quantity, or skip it.
- As the user, I see consumed progress as the primary display and planned totals as a secondary forecast.
- As the user, I can reuse recent and favorite foods without recreating their values.
- As the user, I can inspect daily and weekly adherence history.
- As the user, I can log while offline and see whether my local changes are pending, synchronized, or failed.
- As the user, I can sign into a replacement phone and restore synchronized records.
- As the user, I start at an English email/password sign-in or sign-up screen and remain signed in across app restarts.
- As a new user, I complete body-profile onboarding before entering the tracker; the profile and calculated targets belong to my account.
- As the user, I can export my food history as CSV for the last 7 days, current month, last 30 days, or all time.
- As the user, I receive immediate confirmation with Undo after logging food and can long-press an entry to remove it safely.

### Non-goals for version 1

- Workout, exercise, set, repetition, or strength tracking
- Adaptive calorie changes based on weight trends
- Multiple household members or social features
- Public food catalog, barcode scanning, label photography, or AI food recognition
- Medical advice, diagnosis, or prescriptive nutrition claims
- Subscriptions, payments, advertisements, or an administrative dashboard
- Real-time simultaneous editing across multiple devices

## Product Behavior

### Target calculation

Onboarding collects the minimum information needed for a transparent estimate:

- date of birth or age
- sex used by the selected metabolic formula
- height in centimeters
- body weight in kilograms
- activity level
- muscle-gain rate or surplus preference

The initial implementation uses the Mifflin-St Jeor basal metabolic rate formula, an explicit activity multiplier, and a conservative surplus. Default macros are derived from body weight (initially 1.8 g protein/kg and 0.8 g fat/kg), with remaining calories assigned to carbohydrates. Constants must be named, documented, unit-tested, and visible in the UI explanation. The user previews and may edit every final target before saving it.

Recalculating targets creates a new effective-dated target. It never rewrites the targets against which older days were evaluated.

### Authentication and onboarding

The app opens into an English email/password authentication screen when no persisted Supabase session exists. It supports sign in and sign up, explains email-confirmation requirements without exposing provider errors, and never logs credentials or tokens. A restored authenticated session may open the local tracker offline.

After authentication, an account without a complete local or synchronized profile must finish onboarding before using the main navigation. The profile stores age, formula sex, height, weight, activity multiplier, surplus preference, unit system, and timezone. Saving onboarding creates both the account profile and an effective-dated nutrition target in one local transaction and queues both for synchronization. Settings edits the same profile and creates a new target snapshot when calculation inputs change.

On the first account sign-in after upgrading an existing installation, unowned legacy records are assigned to that authenticated account locally before synchronization. Records owned by one account must never be displayed or uploaded under another account.

### Food library

Each food contains:

- client-generated UUID
- user ownership
- name and optional brand
- preparation state, for example raw, cooked, drained, or custom
- calories, protein, carbohydrates, and fat per 100 g
- favorite status and optional notes/source
- optional private image stored in the user's `food-images` storage folder
- creation, update, deletion, and synchronization metadata

Food names need not be globally unique, but the interface warns when a very similar name and preparation state already exists. Nutrients and quantities must be non-negative and bounded to plausible storage limits. The UI should warn about unusual values without pretending that every food must fit a universal nutritional rule. The food library is also the source for logging: the user selects an existing food, enters the quantity eaten in grams, previews the calculated nutrition, and confirms the entry. Manual food creation remains a separate action.

### Daily entries and historical integrity

A daily entry contains the selected food, quantity in grams, local date, optional meal label, and status (`planned`, `consumed`, or `skipped`). A consumed entry records the consumption time.

Every entry also stores an immutable snapshot of the food name, preparation state, calories, protein, carbohydrates, and fat used for that calculation. Editing or deleting a food later must not alter historical totals.

Primary progress indicators use consumed entries only. A visually lighter forecast marker shows consumed plus planned totals. Skipped entries contribute to neither.

### Home and day history

The current Home layout replaces the separate History screen. It provides:

- an English, horizontally scrollable selector from the first day of the current month through today, initially positioned at today; tapping a date updates its food log, hydration, totals, and effective target
- a prominent calorie card, three macro progress cards, hydration, and recently logged food snapshots
- logging for the selected date, including saved food, quick food, and water
- Settings always edits the current target independently of the date being viewed

Weekly averages and streaks are deferred. The separate History destination is removed; stored entries remain intact.

Food and water history is retained indefinitely in Room and Supabase. Date selectors and export ranges are views over the complete history, never retention rules or synchronization filters. A successful food log displays an English snackbar naming the item with an Undo action. Long-pressing a food card reveals a destructive confirmation; confirming creates a synchronized tombstone and immediately recalculates the selected day's totals. Undo restores the entry and synchronizes the restoration.

### Settings and export

Settings displays the authenticated email and a sync-health state that distinguishes synchronized, pending, failed, and offline work. It provides manual retry, sign out, and profile editing. History export uses Android's document-creation flow so the user chooses the destination; the app writes an English UTF-8 CSV containing dates, food names, quantities, statuses, calories, protein, carbohydrates, and fat for the selected range. Exports are generated locally from Room and never committed or uploaded as files.

### Visual system and navigation

- Poppins typography on a near-black blue-tinted foundation (`#05070A`), elevated dark card surfaces, and readable light text.
- Light blue `#4D8DFF` is the primary dark-theme action, selection, and hydration color. Protein, carbohydrate, and fat retain distinct accessible accents: green `#43C679`, gold `#F0B429`, and red `#FF625F`.
- Navigation follows the owner's latest screenshot: Home, Foods, and Settings inside one compact pill, with a separate circular `+` on its right. Both are siblings in one row; no floating overlay, center cutout, or overlapping tab hit areas.
- The destination pill divides available width equally. The action has a fixed touch target; the dock reserves its own content space and Android navigation insets.
- Reference: [Material navigation guidance](https://m3.material.io/components/navigation-bar/overview). The owner's side-action arrangement is a custom Compose layout using native accessible controls.

### Offline-first synchronization

The UI observes Room/SQLite only. User actions update Room in one local transaction and add a durable mutation to an outbox. A background worker pushes pending mutations to Supabase when connectivity is available and then pulls remote changes.

Synchronization rules:

- IDs are generated on the device so offline records never need temporary IDs.
- Mutations are idempotent and safe to retry.
- Deletes use tombstones until all relevant synchronization work is complete.
- A local record exposes `synced`, `pending`, or `failed` state.
- A retryable failure keeps data locally and never blocks further logging.
- Server timestamps and row revisions determine ordering; phone clock time is not trusted for conflict resolution.
- Version 1 assumes one active phone. If conflicting edits do occur, the latest accepted revision wins and the event is logged for diagnosis.
- Initial login on a replacement phone hydrates Room from Supabase before normal background synchronization continues.
- Push and pull cover all owned foods, profiles, targets, food entries, and water entries regardless of age; no last-week or last-month cutoff is permitted.

## Data and Security Model

### Cloud tables

The initial Supabase schema is expected to contain:

- `profiles`
- `nutrition_targets`
- `foods`
- `food_entries`
- `water_entries`

Supabase Storage contains a private `food-images` bucket. Object paths begin with the authenticated user's id and Storage policies enforce that ownership for read, upload, update, and delete operations.

The local database additionally contains synchronization metadata and an `outbox` table. Local table shapes may include UI-oriented fields, but domain models must not depend directly on Room or Supabase DTOs.

### Security invariants

- Supabase Auth owns credentials; the app never stores a plaintext password.
- Every cloud row has a non-null `user_id` referencing `auth.users`.
- Row-Level Security is enabled on every user-data table.
- Every select, insert, update, and delete policy enforces `auth.uid() = user_id`.
- The Supabase service-role key is never included in the Android app, repository, CI logs, or documentation.
- Runtime configuration is supplied outside version control; a redacted example documents required values.
- Database constraints validate ownership, required fields, non-negative nutrient values, and valid statuses in addition to client validation.
- Logs and crash reports must not include credentials, access tokens, or complete nutrition records.
- A local row and outbox mutation are scoped to the active authenticated user before they can be displayed or synchronized.
- SQL migrations are committed and reviewed; production schema changes are never performed only through a dashboard.

## Architecture and Technology

### Stack

- Kotlin with strict null safety
- Native Android, minimum SDK 26
- Jetpack Compose and Material 3
- Android Architecture Components (`ViewModel`, lifecycle-aware state collection)
- Kotlin coroutines and `Flow`
- Room over SQLite for local persistence
- WorkManager for durable background synchronization
- Supabase Auth and Postgres through the maintained Supabase Kotlin client
- Kotlin serialization for boundary DTOs
- Gradle Kotlin DSL with a version catalog and dependency locking where supported

Exact compatible stable versions will be verified against official Android and Supabase documentation during scaffolding, then pinned in `gradle/libs.versions.toml`. No dynamic dependency versions are allowed.

### Architectural boundaries

The project begins as one Android application module with explicit package boundaries rather than premature Gradle multi-module complexity:

```text
app/src/main/java/.../
  core/model/          Pure domain values and calculations
  core/ui/             Reusable visual primitives and theme
  data/local/          Room entities, DAOs, database, migrations
  data/remote/         Supabase DTOs and remote data source
  data/sync/           Outbox processor and WorkManager integration
  data/repository/     Offline-first repository implementations
  feature/auth/        Private sign-in
  feature/onboarding/  Body profile and target calculator
  feature/today/       Consumed progress, forecast, daily entries
  feature/foods/       Food library and editor
  feature/history/     Daily and weekly adherence
  feature/settings/    Targets, sync health, and sign-out
app/src/test/          Fast JVM unit tests
app/src/androidTest/   Room, Compose, and device integration tests
supabase/migrations/   Reviewed PostgreSQL schema and RLS migrations
docs/                  Product and architecture documentation
tasks/                 Approved implementation plan and task checklist
```

Rules:

- Compose screens render immutable UI state and emit user intents.
- ViewModels coordinate use cases; they do not access Room or Supabase clients directly.
- Repositories expose domain models and `Flow`; database/network DTOs remain inside data packages.
- Nutrient arithmetic is pure domain code and does not depend on Android.
- Room is the only data source directly observed by the UI.
- Supabase interaction occurs through synchronization boundaries, not from screens.
- New Gradle modules are introduced only when build time, ownership, or boundary enforcement provides measurable value.

## Code Style

- Kotlin official formatting, four-space indentation, and trailing commas where the formatter permits.
- Types and composables use `PascalCase`; functions and values use `camelCase`; constants use `UPPER_SNAKE_CASE`.
- Names include units where ambiguity is possible (`quantityGrams`, `proteinGramsPer100g`).
- Domain values reject invalid state at construction boundaries.
- Avoid boolean parameters when a named enum communicates intent.
- Public interfaces have concise KDoc when behavior or units are not obvious.

Representative domain style:

```kotlin
data class NutritionPer100g(
    val calories: BigDecimal,
    val proteinGrams: BigDecimal,
    val carbohydrateGrams: BigDecimal,
    val fatGrams: BigDecimal,
) {
    init {
        require(calories.signum() >= 0) { "Calories cannot be negative" }
        require(proteinGrams.signum() >= 0) { "Protein cannot be negative" }
        require(carbohydrateGrams.signum() >= 0) { "Carbohydrates cannot be negative" }
        require(fatGrams.signum() >= 0) { "Fat cannot be negative" }
    }

    fun forQuantity(quantityGrams: BigDecimal): NutritionTotals =
        NutritionTotals.from(nutrition = this, quantityGrams = quantityGrams)
}
```

## Commands

From PowerShell at the repository root:

```powershell
# Fast domain and ViewModel tests
.\gradlew.bat testDebugUnitTest

# Static Android checks
.\gradlew.bat lintDebug

# Build an installable debug APK
.\gradlew.bat assembleDebug

# Device/emulator integration and Compose tests
.\gradlew.bat connectedDebugAndroidTest

# Recreate the local Supabase database from committed migrations
supabase db reset

# Inspect the complete verification result
.\gradlew.bat testDebugUnitTest lintDebug assembleDebug
```

RTK wraps supported commands when run by Codex, for example `rtk test .\gradlew.bat testDebugUnitTest`.

## Testing Strategy

Development follows red-green-refactor for domain behavior and synchronization rules.

- **JVM unit tests:** target calculation, unit scaling, daily aggregation, forecast logic, adherence tolerance, validation, conflict decisions, and ViewModel state reducers.
- **Repository tests:** fake local and remote data sources verify local-first writes, durable outbox creation, retry, idempotency, and error states.
- **Room instrumentation tests:** constraints, transactions, queries, and migrations on Android SQLite.
- **Supabase database tests:** migration reproducibility, constraints, and RLS isolation between two test users.
- **Compose tests:** critical semantics and flows—add consumed food, add planned food, mark consumed, and understand sync status.
- **Manual device checks:** airplane-mode logging, reconnection sync, process death, sign-out/sign-in, and restore onto a clean emulator.

Coverage percentage is not the target. All nutrition calculations, ownership boundaries, historical snapshot behavior, and synchronization state transitions require direct tests. Pure domain and repository code should normally reach at least 90% branch coverage; generated code and simple Compose layout are excluded from that expectation.

## Accessibility and UX Quality

- The Today screen must make consumed values visually dominant and forecasts distinguishable without relying on color alone.
- Touch targets are at least 48 dp and all actionable icons have semantic labels.
- Text supports Android font scaling without clipping critical nutrient values.
- Progress includes readable text, not only rings or bars.
- Loading, empty, offline, pending-sync, failed-sync, and validation states are explicitly designed.
- All product copy and formatted app dates/times use English regardless of the phone's system language; numeric keyboards continue respecting safe decimal input.
- Common logging actions should be reachable in at most three intentional taps after selecting a recent or favorite food.
- Destructive actions offer a recoverable undo where practical.

## Boundaries

### Always

- Update this specification before changing agreed product behavior.
- Write a failing behavior test before implementing logic.
- Keep UI state local-first and preserve historical nutrient snapshots.
- Validate data at UI/domain, Room, and PostgreSQL boundaries as appropriate.
- Run focused tests after every increment and the full verification set before a milestone is complete.
- Review staged changes for credentials and personal data before committing.
- Commit migrations, lockfiles, and material architecture decisions.

### Ask first

- Add paid services or dependencies with restrictive licensing.
- Change the target calculation formula or default macro coefficients after they are approved.
- Expand scope into workout tracking, AI recognition, public food data, or multiple active devices.
- Introduce a second app module, backend function, analytics provider, or crash-reporting service.
- Delete synchronized user records irreversibly or perform a destructive cloud migration.

### Never

- Commit `.env`, `local.properties`, passwords, tokens, service-role keys, keystores, or personal nutrition exports.
- Bypass Row-Level Security from the mobile client.
- Let remote availability block local food logging.
- Recalculate historical entries from the current food record.
- Silently discard an unsynchronized mutation.
- Present calculated targets as medical advice or guaranteed muscle-gain outcomes.
- Remove or weaken a failing test merely to make verification pass.

## Success Criteria

Version 1 is complete when all of the following are demonstrated:

1. A new installation can sign into the private account and complete nutrition-target onboarding.
2. Given the same documented inputs, target calculations produce deterministic tested outputs and can be manually adjusted before saving.
3. The user can create a raw or cooked food, enter nutrients per 100 g, add an image, favorite it, and find it again.
4. The user can select an existing food with its image, enter a weighed portion, preview automatically calculated nutrition, and immediately update consumed totals; entering it as planned updates only the forecast.
5. A planned portion can be marked consumed, edited, skipped, or undone without creating duplicate intake.
6. Editing a food does not alter any earlier daily totals.
7. Logging works in airplane mode, clearly indicates pending changes, and synchronizes them exactly once after reconnection.
8. A clean installation can authenticate and restore synchronized foods, targets, and entries from Supabase.
9. Weekly history reports correct totals, averages, target adherence, and streak behavior from tested fixtures.
10. RLS tests prove that one authenticated test user cannot read or mutate another user's rows.
11. Unit tests, Android lint, debug build, database migration tests, and critical Compose tests pass.
12. No secret or personal data is present in the Git history or build logs.
13. Launch without a session shows English sign-in/sign-up; a new account cannot enter the tracker until profile onboarding is saved.
14. The date selector scrolls from the first of the current month through today, while synchronization and all-time export prove older records remain retained.
15. Food logging confirms success with Undo, and long-press removal updates totals immediately and synchronizes a tombstone.
16. Settings edits the account profile, reports sync health, and exports valid CSV for each supported range.

## Delivery Milestones

1. **Foundation:** reproducible Android build, domain model, calculation tests, local database shell, and CI-ready commands.
2. **First useful slice:** create a food, add a consumed portion, and see today's remaining macros entirely offline.
3. **Planning and speed:** planned forecast, favorites, recent foods, and fast repeat logging.
4. **Cloud safety:** private authentication, RLS schema, outbox synchronization, recovery, and visible sync health.
5. **History and polish:** weekly adherence, accessibility, error states, migration tests, and release checklist.

Each milestone must leave the app buildable and its completed behavior verifiable. Cloud integration deliberately follows a useful offline slice so the core product can be tested before synchronization complexity is introduced.

## Open Questions

No question currently blocks planning. Non-critical decisions—working app name, visual identity, optional meal labels, exact surplus preset labels, and final stable dependency versions—may be resolved during their relevant task without changing the product boundary.
