# GymFuel Task Checklist

Specification: `docs/product-spec.md`
Plan: `tasks/plan.md`

## Milestone 1: Foundation

### Task 1 — Repository and Android scaffold

- [x] Initialize Git on `main` and add Android/Supabase secret and build-output ignore rules.
- [x] Scaffold the minimal Kotlin/Compose Android application with Gradle wrapper and version catalog.
- [x] Record the installed JDK/Android SDK assumptions and make a clean debug build reproducible.
- Acceptance: `testDebugUnitTest`, `lintDebug`, and `assembleDebug` exist and pass on the scaffold.
- Verify: clean build plus staged-diff secret scan.
- Likely files: `.gitignore`, Gradle root files, version catalog, generated wrapper, minimal `app` files.
- Dependency: none.
- Scope: M/L because scaffold files are generated; behavioral code remains minimal.

### Task 2 — Theme and accessible application shell

- [x] Translate `.superdesign/design-system.md` into Compose color, type, shape, and spacing tokens.
- [x] Implement edge-to-edge app shell and labeled bottom navigation placeholders.
- [x] Add light/dark previews and semantic UI tests for navigation labels.
- Acceptance: theme matches the approved visual direction and remains usable at large font scale.
- Verify: Compose preview review, focused UI test, lint, debug build.
- Likely files: theme tokens, typography, app shell, shell UI test.
- Dependency: Task 1.
- Scope: M, 4–5 files.

## Milestone 2: Nutrition domain

### Task 3 — Nutrient and quantity values

- [ ] Write failing tests for non-negative nutrients, positive logged quantities, unit scaling, and rounding.
- [ ] Implement pure Kotlin domain values with unit-bearing names and deterministic display rounding.
- Acceptance: invalid values cannot enter the domain and valid per-100-g values scale correctly.
- Verify: focused JVM tests and full unit suite.
- Likely files: domain value file, focused test file.
- Dependency: Task 1.
- Scope: S, 2 files.

### Task 4 — Muscle-gain target calculator

- [ ] Write table-driven failing tests for Mifflin-St Jeor, activity multipliers, surplus, macro derivation, and manual override validation.
- [ ] Implement formula constants and an explanation model independent of Android.
- Acceptance: documented fixtures produce deterministic targets and expose every calculation step.
- Verify: focused JVM tests.
- Likely files: calculator models, calculator implementation, test fixtures/tests.
- Dependency: Task 3.
- Scope: M, 3 files.

### Task 5 — Daily aggregation and forecast

- [ ] Write failing tests for consumed, planned, skipped, remaining, over-target, adherence tolerance, and empty-day cases.
- [ ] Implement aggregation and Today summary models without Android dependencies.
- Acceptance: consumed totals and planned forecast never contaminate one another.
- Verify: focused JVM tests.
- Likely files: entry/summary domain model, aggregator, tests.
- Dependency: Task 3.
- Scope: M, 3 files.

## Milestone 3: First useful offline slice

### Task 6 — Room database foundation

- [ ] Define database configuration, converters, sync metadata, and migration-test infrastructure.
- [ ] Prove a fresh database opens and a placeholder migration path is testable.
- Acceptance: Room is injectable/testable and destructive fallback is disabled for user data.
- Verify: Room instrumentation test and debug build.
- Likely files: database, converters, database test, DI/provider wiring.
- Dependency: Task 1.
- Scope: M, 4 files.

### Task 7 — Local foods

- [ ] Write DAO/repository tests for create, edit, favorite, soft delete, recent query, and similar-name lookup.
- [ ] Implement food entity, DAO, mappings, and local-first repository behavior.
- Acceptance: raw/cooked state and per-100-g nutrients persist through process restart.
- Verify: DAO instrumentation tests plus repository JVM tests.
- Likely files: food entity/model mapping, DAO, repository, tests.
- Dependencies: Tasks 3 and 6.
- Scope: M, 4–5 files.

### Task 8 — Local food entries and snapshots

- [ ] Write tests proving entries snapshot nutrition/name/preparation and survive later food edit/delete.
- [ ] Implement entry entity, DAO transaction, mappings, and repository flow by local date.
- Acceptance: entry creation and immutable history are atomic and persistent.
- Verify: Room transaction/instrumentation tests.
- Likely files: entry entity, DAO, repository/mapping, tests.
- Dependencies: Tasks 5–7.
- Scope: M, 4–5 files.

### Task 9 — Create-food vertical UI slice

- [ ] Implement food form state, validation, raw/cooked selection, nutrient inputs, and save behavior.
- [ ] Cover empty, validation, saving, and success states with semantics.
- Acceptance: a user can create and find chicken entirely offline.
- Verify: ViewModel unit tests, Compose UI test, manual emulator check.
- Likely files: UI state/ViewModel, screen, reusable nutrient input, tests.
- Dependency: Task 7.
- Scope: M, 4–5 files.

### Task 10 — Consumed logging and Today UI slice

- [ ] Implement recent/search food picker, quantity entry, live nutrient preview, and consumed submission.
- [ ] Render calorie hero, three macro rows, entries, remaining values, empty state, and Undo.
- Acceptance: logging a weighed portion updates the Today screen immediately and persists offline.
- Verify: aggregation/ViewModel tests, Compose flow test, airplane-mode process-restart check.
- Likely files: logging ViewModel/state, Today screen/components, bottom sheet, tests.
- Dependencies: Tasks 2, 5, 8, and 9.
- Scope: split into subcommits if any file exceeds 200 lines or more than 5 files change.

## Milestone 4: Planning and speed

### Task 11 — Planned lifecycle and forecast

- [ ] Test create planned, edit quantity, consume exactly once, skip, and undo transitions.
- [ ] Add dashed forecast markers and compact forecast copy without duplicating the dashboard.
- Acceptance: state is understandable without color and consumed totals remain accurate.
- Verify: transition unit tests, Compose semantics test, manual design comparison.
- Likely files: transition use case, Today UI state, macro component, tests.
- Dependency: Task 10.
- Scope: M, 4 files.

### Task 12 — Repeat logging shortcuts

- [ ] Add recent/favorite ordering and repeat with remembered quantity requiring explicit confirmation.
- [ ] Measure the common path against the three-tap-plus-quantity target.
- Acceptance: common food reuse is fast without accidental duplicate logging.
- Verify: repository ordering tests, UI flow test, manual tap count.
- Likely files: query/use case, picker state, picker UI, tests.
- Dependencies: Tasks 7 and 10.
- Scope: M, 4 files.

### Task 13 — UI quality checkpoint

- [ ] Verify 320 dp width, common phone sizes, dark theme, 200% font scale, TalkBack, switch access/hardware keyboard, and reduced motion.
- [ ] Add explicit offline, pending, failed, validation, empty, and undo states.
- [ ] Compare implementation with approved Superdesign direction and capture reviewed screenshots.
- Acceptance: no clipped critical content, inaccessible control, color-only meaning, or blocked common flow.
- Verify: Compose tests, Accessibility Scanner/TalkBack manual pass, lint.
- Dependencies: Tasks 10–12.
- Scope: split findings into focused fixes; do not perform unrelated redesign.

## Milestone 5: Supabase and synchronization

### Task 14 — Private authentication

- [ ] Externalize URL/publishable key with a redacted example and fail safely when missing.
- [ ] Implement sign-in, session restore, sign-out, and auth error state using verified current Supabase APIs.
- Acceptance: no service-role key or credential reaches source control/logging.
- Verify: auth state tests, clean-install manual sign-in, secret scan.
- Likely files: build config, auth data source/repository, auth ViewModel/screen, tests.
- Dependency: Tasks 1–2.
- Scope: M; separate config and UI commits if needed.

### Task 15 — PostgreSQL schema and RLS

- [ ] Write migrations for profiles, versioned targets, foods, and food entries with constraints/indexes/revisions/tombstones.
- [ ] Enable RLS and test select/insert/update/delete isolation with two users.
- Acceptance: migrations reproduce from zero and cross-user operations fail.
- Verify: `supabase db reset` plus SQL policy tests.
- Likely files: schema migration, RLS migration, SQL tests, schema notes.
- Dependency: approved local entity contracts from Tasks 7–8.
- Scope: M, 3–4 files.

### Task 16 — Transactional outbox

- [ ] Write failing tests for atomic local write + enqueue, idempotent retry, permanent/retryable failures, and tombstones.
- [ ] Implement outbox persistence and repository integration.
- Acceptance: app termination cannot leave accepted local data without a durable pending mutation.
- Verify: Room/repository tests including simulated interruption.
- Likely files: outbox entity/DAO, transaction coordinator, repository integration, tests.
- Dependencies: Tasks 6–8 and 15.
- Scope: M, 4–5 files.

### Task 17 — Push/pull worker and sync health

- [ ] Implement authenticated idempotent push, revision-aware pull, unique WorkManager scheduling, retry/backoff, and observability-safe errors.
- [ ] Surface synced/pending/failed status and a manual retry action.
- Acceptance: airplane-mode entries synchronize exactly once after reconnection and failures never remove local data.
- Verify: fake-remote tests, WorkManager integration tests, local Supabase manual scenario.
- Likely files: remote DTO/source, sync engine, worker, status UI, tests; split into two tasks if over 5 files.
- Dependencies: Tasks 14–16.
- Scope: M/L; mandatory split if implementation exceeds task-size limits.

### Task 18 — Clean-install recovery

- [ ] Hydrate an empty Room database after authenticated sign-in while showing explicit restore progress/failure.
- [ ] Test replay safety, pagination, target/food/entry ordering, and interrupted restore.
- Acceptance: a clean emulator reproduces synchronized totals without duplicates.
- Verify: integration tests and clean-emulator manual recovery.
- Dependencies: Task 17.
- Scope: M, 3–5 files.

## Milestone 6: History and release readiness

### Task 19 — Daily and weekly history

- [ ] Add date navigation, daily totals, seven-day averages, adherence counts, and streak rules from the domain layer.
- [ ] Build a readable compact visualization in Compose; justify any chart dependency before adding it.
- Acceptance: tested fixtures match all displayed values and incomplete/future days do not break streaks.
- Verify: domain/ViewModel tests, Compose semantics and screenshot review.
- Dependencies: Tasks 5, 8, and 18.
- Scope: M; split domain and UI if needed.

### Task 20 — Hardening, documentation, and release checkpoint

- [ ] Test Room/Postgres migrations, process death, timezone/locale changes, decimal input, auth expiry, and sync recovery.
- [ ] Add CI for deterministic checks and document environment/setup/Supabase migration workflow.
- [ ] Run code quality, security, accessibility, and dependency reviews; resolve all high-priority findings.
- Acceptance: all 12 product-spec success criteria have recorded evidence and a debug APK installs on the owner's phone.
- Verify: full command suite, clean checkout, clean emulator, physical-device smoke test.
- Dependencies: all prior tasks.
- Scope: findings become individual focused tasks rather than one bulk change.

## Approval

- [x] Owner approved the implementation plan.
- [x] Owner approved the Today visual direction or explicitly requested implementation without visual iteration.

## Milestone 7: Authenticated profiles and complete history

### Task 21 — Owner-scoped local data and profile contract

- [x] Add an owner-scoped local profile, owner-scoped targets/outbox, and a migration that safely claims legacy null-owner data for the first authenticated account.
- [x] Filter every user-data DAO and pending-mutation query by active owner while keeping seed food templates shared.
- Acceptance: signing into another account cannot display or upload the first account's local records.
- Verify: Room migration/repository isolation tests and focused JVM tests.

### Task 22 — Supabase profile migration and RLS verification

- [x] Add validated current profile inputs to `profiles`, preserve effective target snapshots, and retain complete history without date-based deletion.
- [x] Extend two-user RLS tests and run security/performance advisors after the linked migration.
- Acceptance: each account can CRUD only its profile and history; owner/date indexes support complete-history queries.
- Verify: linked migration list, SQL assertions, advisors, and aggregate row checks.

### Task 23 — Auth-first application state

- [x] Add English sign-in/sign-up UI, persisted-session restore, safe errors, restore progress, and sign out.
- [x] Claim legacy local data after the first successful sign-in and pull existing account data before deciding onboarding is required.
- Acceptance: signed-out launch cannot enter the tracker; cached sessions reopen offline when local profile data exists.
- Verify: gateway/reducer tests, Compose auth flow tests, clean-install phone check.

### Task 24 — Account onboarding and profile editing

- [x] Require sex, age, height, weight, activity, and surplus inputs for accounts without a profile.
- [x] Save profile plus calculated effective target transactionally; reuse the same editor from Settings.
- Acceptance: onboarding is required once per account and profile edits recalculate only future/effective targets.
- Verify: calculator/profile tests, Compose validation tests, Supabase profile row check.

### Task 25 — Complete date navigation and English formatting

- [x] Replace the seven-day row with an English horizontal month-to-date selector from day 1 through today, initially scrolled to today.
- Acceptance: every current-month day is reachable and selecting one updates entries, hydration, and target without limiting stored history.
- Verify: pure date-range test, Compose scroll/select test, physical-phone check under a non-English system locale.

### Task 26 — Feedback, delete, and Undo

- [x] Show a success snackbar with Undo after saved/quick food logging.
- [x] Long-press a food card to reveal delete confirmation; soft-delete and restore via owner-scoped outbox mutations.
- Acceptance: totals update immediately, Undo restores the same entry id, and Supabase receives tombstone/restoration state.
- Verify: repository state tests, Compose gesture/snackbar tests, linked row verification.

### Task 27 — Sync status and CSV history export

- [x] Present synchronized/pending/failed status, manual retry, account email, and profile edit in Settings.
- [x] Export last 7 days, current month, last 30 days, or all history as English UTF-8 CSV through Android Create Document.
- Acceptance: export rows/totals match Room and no export is uploaded or committed.
- Verify: CSV formatter unit tests, Compose range-selection test, phone document export.

### Task 28 — Upgrade release gate

- [x] Run database advisors/RLS assertions, full JVM/lint/build/device suites, English string audit, secret/personal-data scan, and clean/upgrade phone smoke tests.
- [x] Install and push the verified APK/source while preserving existing phone data.
- Acceptance: all new specification criteria have recorded evidence and the worktree is clean on `main`.

## Milestone 8: Hosted auth confirmation result

### Task 29 — Tested callback contract

- [x] Add a dependency-free web package and write failing tests for successful PKCE callbacks, expired/invalid errors, direct visits, and unsafe codes.
- [x] Implement the smallest pure parser and native-app URL builder that satisfies the tests.
- Acceptance: only a bounded `code` produces a success state and native continuation URL; external error text is never rendered verbatim.
- Verify: `npm test` from `web/` and focused test output proving the initial RED state was observed.
- Likely files: `web/package.json`, parser, parser tests.
- Dependency: approved `docs/auth-confirmation-web-spec.md`.
- Scope: S, 3 files plus lockfile.

### Task 30 — Accessible Vercel static page

- [x] Implement success, expired/invalid, and unavailable result states using GymFuel’s dark Poppins design system.
- [x] Add a deterministic static build, local server, Vercel headers, deployment guide, and ignored output.
- Acceptance: responsive result page has no inline script, no secret, no analytics, safe headers, and a keyboard-visible primary action.
- Verify: `npm test`, `npm run build`, `npm audit`, local browser screenshots and accessibility/console checks.
- Likely files: page HTML/CSS/controller, build/server scripts, `vercel.json`, `web/README.md`, `.gitignore`.
- Dependency: Task 29.
- Scope: split into parser and UI commits if the combined change exceeds reviewable size.

### Task 31 — Production callback integration

- [x] After Vercel deployment, add the exact production callback URL to Supabase Auth while preserving the native callback.
- [ ] Configure Android sign-up to request the hosted callback, rebuild/install, and verify new-account confirmation on phone and desktop.
- Acceptance: confirmation never ends on a blank tab; success/error states are accurate; the Android app completes PKCE when continued on the originating phone.
- Verify: Supabase config diff, Android unit/lint/build checks, hosted browser check, and physical-phone sign-up smoke test.
- Dependencies: Task 30 and the owner-provided Vercel production URL.
- Scope: M, hosted config plus focused Android configuration/test changes.

## Hosted confirmation approval

- [x] Owner approved the hosted callback architecture and requested repository/Supabase preparation.
- [x] Owner will deploy the prepared `web` root to Vercel and provide the assigned production URL.
