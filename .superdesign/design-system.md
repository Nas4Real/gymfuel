# GymFuel Mobile Design System

## Product context

GymFuel is a private Android nutrition tracker for a gym user pursuing controlled muscle gain. Its primary job is to show consumed calories and macros, show where planned food would take the day, and make weighed food logging fast enough to sustain every day. It is a utility, not a social fitness product or a medical product.

Primary screens: private sign-in, target onboarding, Today, food picker/editor, history, and settings/sync health. Today is the product center and should feel useful within one second of opening.

## Visual direction

Use a mobile adaptation of technical minimalism: precise, calm, physical, and quietly performance-oriented. The interface should evoke a well-organized training notebook and a nutrition label, not a gaming dashboard.

- Flat two-dimensional surfaces with crisp 1 dp dividers
- Warm neutral backgrounds rather than clinical pure white
- Deep forest green as the only dominant brand color
- Coral and gold used sparingly for forecast or attention states
- No gradients, glassmorphism, glowing charts, or heavy shadows
- No generic bento-card wall; layout follows the hierarchy of today's nutrition
- Corners use a deliberate 4/8/12 dp hierarchy, never universal pill shapes
- Dense enough for repeated daily use but never cramped or below accessible touch sizes

## Color tokens

Light theme:

- `background`: Paper `#F7F7F2`
- `surface`: Clean Paper `#FFFFFF`
- `surfaceVariant`: Moss Wash `#E9EEE8`
- `primary`: Forest `#173B2B`
- `onPrimary`: Paper `#FFFFFF`
- `textPrimary`: Ink `#17211B`
- `textSecondary`: Field Gray `#5E6962`
- `outline`: Grid `#CBD3CD`
- `protein`: Forest `#257A52`
- `carbohydrate`: Gold `#C88A16`
- `fat`: Coral `#D66B55`
- `calories`: Ink `#26352D`
- `success`: Green `#2F7D4C`
- `warning`: Amber `#A96B00`
- `error`: Brick `#B3261E`

Dark theme:

- `background`: Deep Ink `#101713`
- `surface`: Forest Black `#18211C`
- `surfaceVariant`: Raised Moss `#233129`
- `primary`: Mint `#9ED7B3`
- `onPrimary`: Deep Forest `#092416`
- `textPrimary`: Paper `#F1F5F1`
- `textSecondary`: Sage Gray `#AAB7AE`
- `outline`: Dark Grid `#3B4A40`
- Protein, carbohydrate, fat, warning, and error colors are adjusted by Material tonal roles to retain WCAG AA contrast.

Never communicate macro type or sync state by color alone. Pair color with labels, values, icons, line styles, or status text.

## Typography

- Primary family: Space Grotesk, bundled with the application for predictable offline rendering
- Technical/numeric family: JetBrains Mono for macro values, quantities, dates, and sync metadata only
- Display: 32/38 sp, weight 650, tight but readable
- Screen title: 24/30 sp, weight 650
- Section heading: 16/22 sp, weight 600
- Body: 15/21 sp, weight 400
- Supporting: 13/18 sp, weight 400
- Label: 11/16 sp, weight 600, modest tracking; uppercase only for very short metadata

Respect Android font scaling. Important values may reflow but never clip, overlap, or disappear.

## Spacing, shape, and elevation

- Base spacing unit: 4 dp
- Main horizontal page padding: 20 dp
- Common gaps: 8, 12, 16, 24, 32 dp
- Minimum touch target: 48 x 48 dp
- Small controls: 4 dp radius
- Inputs and list containers: 8 dp radius
- Primary action and modal sheets: 12 dp radius
- Pills are reserved for status/filter chips that are semantically pill-like
- Prefer borders and tonal surface changes over shadow
- Elevation is limited to navigation, modal bottom sheets, and actively dragged items

## Today screen hierarchy

Target viewport: a common Android phone around 360 x 800 dp, adaptable down to 320 dp width.

1. Compact top app bar: date and a small explicit sync state; no oversized logo header.
2. Hero total: calories consumed, daily target, and amount remaining. Use readable text plus a precise horizontal progress treatment.
3. Macro rail: protein, carbohydrates, and fat in aligned rows. Each row shows consumed / target, remaining text, a solid consumed line, and a lighter or dashed planned endpoint.
4. Forecast callout: a single compact sentence such as “With planned food: 2,640 kcal · 12 g protein remaining.” Do not duplicate every metric in another large card.
5. Entries: chronological or meal-grouped list with status text. Consumed entries are solid; planned entries use a lighter surface and explicit `PLANNED` label.
6. Persistent primary action: “Log food” in the reachable lower region. A secondary action in the same flow allows “Plan for later.”
7. Bottom navigation: Today, Foods, History, Settings. Labels remain visible; selected state is not color-only.

Avoid circular progress rings for all four nutrients. They use space poorly and make exact comparison harder. Horizontal tracks and aligned numbers support faster scanning.

## Food logging flow

Use a modal bottom sheet so context remains visible:

1. Search input with recent and favorite foods shown immediately
2. Select a food; show name plus raw/cooked preparation state prominently
3. Numeric quantity field in grams with a large keypad-friendly target
4. Live nutrient preview using an aligned, compact row
5. Primary “Log consumed” and secondary “Plan for later” actions

The common recent-food path should require no more than three intentional taps plus quantity entry. Provide clear validation before submit, preserve typed quantity through transient UI changes, and allow immediate Undo after logging.

## Components

- `MacroProgressRow`: label, consumed/target numeric pair, remaining state, solid consumed track, dashed planned marker
- `SyncStatus`: icon, short text (`Synced`, `3 pending`, `Sync failed`), and accessible description
- `FoodEntryRow`: time/status, food snapshot name and preparation, quantity, macro summary, overflow actions
- `NutrientInput`: visible label, decimal keyboard, unit suffix, helper/error text
- `TargetSummary`: formula explanation and editable result without claiming medical certainty
- `EmptyDayState`: useful instruction and direct Log food action, not an illustration-only dead end
- `UndoMessage`: accessible transient confirmation with enough time to act

## States

Every screen defines loading, empty, content, validation, offline, pending-sync, sync-failed, and destructive-confirmation behavior. Skeletons are used only when restoring/hydrating remote data; normal offline launches render Room data immediately without a blocking spinner.

## Motion

- Functional and restrained, 120–220 ms
- Standard Material emphasized easing for modal/container transitions
- Macro progress animates only when a value changes, not continuously
- Logging confirmation may use a brief number/track transition and haptic feedback
- Honor Android reduced-motion settings and never make motion necessary to understand state

## Accessibility

- Meet WCAG 2.1 AA contrast as a minimum and Android accessibility guidance
- All actions have TalkBack labels and roles
- Progress semantics announce nutrient name, consumed amount, target, remaining amount, and planned forecast
- Focus moves predictably when sheets open, validation fails, or confirmation appears
- Use real visible field labels; placeholders never replace labels
- Do not rely only on red/green, solid/dashed, or icon-only status
- Support font scaling, screen widths from 320 dp, portrait rotation, and larger touch/accessibility settings

## Design constraints

Use ONLY the fonts, colors, spacing, and component styles defined in this design system. Do not introduce purple, blue neon, gradients, glass effects, decorative illustrations, stock photography, random fonts, or unlisted visual styles. The initial design is a production Android app screen, not a marketing page or a desktop dashboard.
