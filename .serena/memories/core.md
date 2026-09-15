# GymFuel core

- Private, single-user Android nutrition tracker for controlled muscle gain.
- Product spec: `docs/product-spec.md`; implementation plan: `tasks/plan.md`; checklist: `tasks/todo.md`.
- UI reads/writes Room first; Supabase is authenticated cloud recovery/sync, never a screen data source.
- Food entries snapshot nutrient/name/preparation values; later food edits must not rewrite history.
- Primary progress is consumed-only; planned food is a separate forecast.
- Version 1 excludes workouts, AI/barcodes, public catalog, and simultaneous multi-device editing.
- Stack/version pins: `mem:tech_stack`.
- Code/architecture conventions: `mem:conventions`.
- Windows commands: `mem:suggested_commands`.
- Required verification before completion: `mem:task_completion`.