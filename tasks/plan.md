# Implementation Plan: GymFuel Android

Status: Active — authenticated-profile and complete-history upgrade approved by owner request
Specification: `docs/product-spec.md`
Design system: `.superdesign/design-system.md`
Superdesign draft: `13a39aac-ea98-4696-85d2-718126267cca`

## Overview

GymFuel will be delivered as small, testable Android slices. The first useful release path works entirely offline: calculate nutrition, create a personal food, log a weighed portion, and see today's remaining macros. Authentication and Supabase synchronization are added only after that path is proven locally. This keeps the riskiest infrastructure work from hiding product or arithmetic defects.

## Definition of Done

A task is done only when its acceptance criteria pass, focused automated tests pass, the relevant build or lint check passes, accessibility behavior has been considered, no secret or personal data appears in the diff, and documentation reflects any changed decision. A milestone additionally requires the full verification set available at that stage.

## Architecture Decisions

### Native Android

Use Kotlin and Jetpack Compose because Android is the only target, native accessibility and lifecycle behavior matter, and this is also a professional Android engineering project. Cross-platform abstraction would add cost without current value.

### Single module, explicit boundaries

Begin with one `app` Gradle module organized by domain, data boundary, and feature. This preserves fast iteration while keeping DTOs, Room entities, domain models, and UI state separate. Add Gradle modules only after a measurable build-time or ownership need appears.

### Local-first data flow

Room is the data source observed by screens. Repositories write local state and an outbox transactionally; WorkManager synchronizes with Supabase. Screens never wait for Supabase to accept a food log.

### Immutable history

Food entries snapshot all nutrient values used at log time. Current food definitions are convenient references, not the source for recalculating history.

### Unidirectional UI state

Compose screens render immutable state and send typed user intents. ViewModels coordinate use cases and expose lifecycle-aware flows. Side effects such as navigation, undo messages, and focus requests are explicit.

### Purpose-built design system

Material 3 supplies accessible Android primitives, semantics, sheets, navigation, typography mechanics, and dynamic layout behavior. GymFuel owns a small layer of tokens and nutrition-specific components. No third-party component kit, chart library, gradient system, or animation framework is planned for version 1.

### Dependency policy

During scaffolding, retrieve the current official documentation and pin mutually compatible stable versions in the Gradle version catalog. Likely dependencies are AndroidX Compose/Material 3, Lifecycle/ViewModel, Navigation, Room, WorkManager, Kotlin coroutines/serialization, a small dependency-injection solution only if background worker wiring justifies it, and the maintained Supabase Kotlin client with its required HTTP engine. Dynamic versions are forbidden.

## Dependency Graph

```text
Reproducible Gradle project
    |
    +-- Pure domain values and validation
    |       |
    |       +-- Target calculator
    |       +-- Portion scaling and daily aggregation
    |               |
    |               +-- Today UI state
    |
    +-- GymFuel design tokens and app shell
    |       |
    |       +-- Accessible Today components
    |
    +-- Room database
            |
            +-- Foods repository
            +-- Food entries repository + immutable snapshots
                    |
                    +-- Offline create-food/log-food/Today slice
                    +-- Planned forecast and fast-repeat slice
                    |
                    +-- Supabase Auth + PostgreSQL/RLS migrations
                            |
                            +-- Durable outbox synchronization
                            +-- Clean-install recovery
                                    |
                                    +-- Weekly history
                                    +-- Hardening and release
```

## Phases and Checkpoints

### Phase 1: Reproducible foundation

1. Initialize Git safety files and scaffold a minimal Android application.
2. Pin documented stable dependencies and verification commands.
3. Implement the GymFuel theme, type scale, spacing, and accessible app shell from the approved design.

Checkpoint: a clean checkout builds a debug APK, runs its empty unit suite, and renders an accessible shell without secrets.

### Phase 2: Tested nutrition domain

4. Implement validated nutrient and quantity domain values test-first.
5. Implement target calculation and transparent calculation details test-first.
6. Implement portion scaling, consumed totals, planned forecast, remaining values, and tolerance rules test-first.

Checkpoint: all nutrition behavior runs as fast JVM tests without Android, Room, or network dependencies.

### Phase 3: First useful offline slice

7. Create the Room database foundation and migration test harness.
8. Add local food creation/editing with raw/cooked state and favorites.
9. Add local entry creation with immutable nutrient snapshots.
10. Deliver the Today screen: log consumed food and see remaining calories/macros.

Checkpoint: on an emulator in airplane mode, the user creates chicken, logs a weighed portion, kills/restarts the app, and sees correct persisted totals.

### Phase 4: Planning and daily speed

11. Add planned entries, forecast markers, mark-consumed, skip, edit, and undo.
12. Add recent/favorite food selection and repeat-entry shortcuts.
13. Refine the bottom-sheet logging flow, keyboard behavior, TalkBack semantics, large-font layouts, empty/error states, and screenshot references.

Checkpoint: common repeat logging takes no more than three intentional taps plus quantity entry; consumed and planned state remains unambiguous without color.

### Phase 5: Private cloud safety

14. Add externalized Supabase configuration and private sign-in.
15. Add reviewed PostgreSQL migrations, constraints, indexes, triggers, and RLS policies with two-user isolation tests.
16. Add the durable local outbox and idempotent push/pull synchronization worker.
17. Add visible sync health, retry behavior, tombstones, and clean-install recovery.

Checkpoint: offline writes synchronize exactly once after reconnecting, a second test user cannot access them, and a clean emulator restores the authenticated user's data.

### Phase 6: History and release quality

18. Add daily history and the seven-day adherence view without a chart dependency unless Compose primitives prove insufficient.
19. Verify migration, process-death, timezone, locale, decimal-entry, and accessibility edge cases.
20. Add CI, architecture/security notes, setup documentation, and an internal debug release checklist.

Checkpoint: every specification success criterion passes and the APK can be installed on the owner's Android phone.

## Verification Strategy

After each task:

- Run its focused test target through RTK where supported.
- Run Android lint for touched UI/data code.
- Inspect the diff for scope creep, secrets, generated build output, and personal data.
- Keep a task unchecked if manual verification is still required.

At each checkpoint:

```powershell
.\gradlew.bat testDebugUnitTest
.\gradlew.bat lintDebug
.\gradlew.bat assembleDebug
.\gradlew.bat connectedDebugAndroidTest
supabase db reset
```

Commands that do not apply yet are introduced only when their infrastructure exists. CI later runs the deterministic subset that does not require private credentials.

## UI Implementation Contract

- Target 320 dp through large Android phones in portrait first; do not encode a single device size.
- Keep the Today screen's main information visible without decorative header space.
- Use consumed-only primary tracks with a secondary dashed/planned endpoint.
- Use numeric text with progress semantics so TalkBack conveys exact values.
- Keep the primary logging action in the lower reachable region, but do not obscure list content or system insets.
- Use a modal bottom sheet for food search, quantity, live preview, and consumed/planned choice.
- Use edge-to-edge layout with safe insets according to the current official Android guidance.
- Support dark theme, font scaling, hardware keyboard navigation where applicable, and reduced-motion preferences.
- Use real domain content in previews and tests: chicken breast, cooked rice, oats, and a realistic day.
- Treat loading, empty, offline, pending, failed, validation, and undo as designed states, not afterthoughts.

## Risks and Mitigations

| Risk | Impact | Mitigation |
|---|---|---|
| Nutrition formulas imply false precision | High | Show inputs/formula, label as estimate, allow edits, version targets |
| Raw/cooked ambiguity corrupts tracking | High | Require preparation state and repeat it during selection/logging |
| Food edits rewrite the past | High | Store immutable nutrient/name snapshots in every entry |
| Offline sync duplicates intake | High | Client UUIDs, transactional outbox, idempotent upsert, direct retry tests |
| Clock skew chooses the wrong record | Medium | Server timestamps/revisions for ordering; device time only for user event time |
| Mobile app key exposes records | High | Authenticated user ownership and RLS on every operation; no service key in app |
| UI becomes a dashboard of cards | Medium | Enforce Today hierarchy and compact purpose-built components from design system |
| Too many libraries increase fragility | Medium | Use AndroidX/Material primitives first and document the reason for every dependency |
| Scope expands into workout coaching | Medium | Keep explicit non-goals and require spec change before expanding |
| Timezone changes split days incorrectly | Medium | Store event instants plus explicit local date/timezone semantics and test travel/DST cases |

## Source Verification Plan

Before framework-specific code is written, capture and follow the relevant official documentation for:

- Android recommended architecture and UI-layer state production
- Compose Material 3, accessibility semantics, adaptive layouts, and edge-to-edge rendering
- Room entities, migrations, transactions, and testing
- WorkManager constraints, retries, unique work, and testing
- Android credential/configuration handling
- Supabase Kotlin initialization and Auth session handling
- PostgreSQL Row-Level Security and Supabase policy guidance

Non-obvious implementation decisions will cite deep official documentation links in the task summary or architecture notes. Community examples may inform experiments but are not authoritative.

## Git Strategy

- Initialize `main` with approved specifications, design system, plan, task list, and safety ignore rules.
- Work in short vertical increments with conventional atomic commits.
- Never commit private config, local databases, APKs, keystores, build directories, IDE-local state, or nutrition exports.
- Create the GitHub remote only after the local repository has a reviewed initial commit and the user supplies or approves the repository identity.

## Approval Gate

Implementation starts only after the owner approves both this plan and `tasks/todo.md`, and approves the Today visual direction or explicitly asks to implement without further visual iteration.

## Active Upgrade: Authenticated Profiles and Complete History

### Dependency graph

```text
Authenticated session
    -> active local owner + legacy-data claim
        -> synchronized account profile
            -> onboarding / profile editing / calculated target snapshot
        -> complete-history push and pull
            -> month-to-date date selector
            -> local CSV export ranges
            -> synchronized entry deletion and undo
```

### Architecture decisions

- The root Compose app is an explicit session state machine: restoring session, signed out, restoring account data, onboarding required, or ready.
- Room remains the UI source of truth. Profile saves, entry deletes/restores, and target creation are transactional local mutations with owner-scoped outbox rows.
- The first authenticated account on an upgraded installation claims legacy rows whose owner is null. Subsequent accounts can only observe and synchronize their own rows plus shared seed templates.
- `profiles` stores current calculator inputs; `nutrition_targets` remains the effective-dated historical snapshot. Editing the profile never rewrites older targets.
- Export reads Room and writes through Android's Create Document contract. Supabase stores structured history indefinitely and never stores generated export files.
- Soft deletion remains the cloud contract. Long-press removal writes a tombstone; Undo clears it and queues another idempotent upsert.

### Verification checkpoints

1. Auth/profile checkpoint: RLS and Room owner-isolation tests pass; sign-up/sign-in/onboarding works on the physical phone.
2. History checkpoint: month selector, complete pull, CSV range generation, delete, and Undo tests pass.
3. Release checkpoint: database advisors, full JVM/lint/build/device suites, fresh install, upgrade install, English-copy audit, secret scan, and phone smoke test pass.

### Risks and mitigations

| Risk | Impact | Mitigation |
|---|---|---|
| Existing local records are uploaded to the wrong account | High | Claim only null-owner legacy rows once; owner-scope every DAO and outbox query |
| New sign-in overwrites an existing cloud profile before restore | High | Pull/restore before deciding onboarding is required |
| Delete/Undo races create duplicate or lost history | High | Stable client UUID, tombstone upsert, one outbox key per entity, state-based tests |
| CSV export leaks data unintentionally | Medium | User-selected document destination, local generation only, no logs or automatic sharing |
| Large history becomes slow | Medium | Indexed owner/date queries, range-scoped export, paginated/ordered cloud reads when data grows |
