# Conventions

- One `app` Gradle module until a measurable boundary/build need justifies more.
- Packages are organized by `core/model`, `core/ui`, `data/{local,remote,sync,repository}`, and `feature/*`.
- Compose renders immutable UI state and emits typed intents; ViewModels do not access Room/Supabase clients directly.
- Domain arithmetic is pure Kotlin and Android-free. Names carry units (`quantityGrams`, `proteinGramsPer100g`).
- Room is the observable source of truth; local mutation + outbox enqueue must be transactional.
- Validate at domain, local database, and PostgreSQL boundaries. Every Supabase user table requires RLS ownership policies.
- Use Kotlin official formatting, 4-space indentation, trailing commas, PascalCase types/composables, camelCase functions/values, UPPER_SNAKE_CASE constants.
- UI system: `.superdesign/design-system.md`; flat warm-paper/forest palette, precise dividers, no gradients/heavy shadows, 48dp touch targets, never color-only meaning.
- Use Material 3/Compose primitives before adding UI/chart/animation libraries.
- Tests follow red-green-refactor and assert state/outcomes rather than internal call sequences.