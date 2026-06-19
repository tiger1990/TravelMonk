# Migration: Nav Arguments via @AssistedInject (Navigation 3)

## Goal
Align arg-bearing ViewModels with **both** the article
("SavedStateHandle in the Navigation 3 Era") **and** our own committed rule
`.claude/rules/navigation3-viewmodel-args.md`, which mandates `@AssistedInject`
for nav-key arguments.

Today every arg-bearing screen delivers its nav arg with
`LaunchedEffect(arg) { viewModel.onIntent(Load(arg)) }`. This works and survives
process death, but:
- It contradicts our own rule (rule says `@AssistedInject`; code uses none).
- The arg is a side-effect, not a construction invariant → it can be null/empty
  for the first composition frame.
- `LaunchedEffect` re-fires on every config change → **re-fetch on every rotation**.

After this migration the nav arg is a non-null constructor parameter, the load
kicks off once in `init {}`, and rotation no longer re-fetches.

## Scope (4 ViewModels + their screens + 4 nav entries)
- [ ] `OtpViewModel` / `OtpScreen` — arg `phone` (entry in `app/ui/OnboardingFlow.kt`)
- [ ] `ExperienceDetailsViewModel` / `ExperienceDetailsScreen` — arg `experienceId`
      (entry in `feature/experiences/di/NavigationModule.kt`)
- [ ] `StayDetailsViewModel` / `StayDetailsScreen` — arg `stayId`
      (entry in `feature/stays/di/NavigationModule.kt`)
- [ ] `StayResultsViewModel` / `StayResultsScreen` — arg `location`
      (entry in `feature/stays/di/NavigationModule.kt`)

Out of scope: `BookingConfirmationScreen` (no ViewModel — pure typed params, already ideal),
all no-arg VMs (Home, Transport, Services, Welcome, PhoneEntry, PasskeyPrompt, Flight, Booking, Experience list, Stay search).

## Pre-flight (no code changes)
- [ ] Confirm `hiltExt = 1.3.0` exposes the `hiltViewModel<VM, Factory> { factory -> ... }`
      overload (available since 1.2.0 — already on classpath via
      `hilt-lifecycle-viewmodel-compose`). No dependency bump expected.
- [ ] Confirm no other VM reads a nav arg from `SavedStateHandle["..."]` (audit found none).

## Migration pattern (apply per ViewModel)

### ViewModel
```kotlin
@HiltViewModel(assistedFactory = FooViewModel.Factory::class)
class FooViewModel @AssistedInject constructor(
    @Assisted private val fooArg: String,
    private val someUseCase: SomeUseCase,
) : BaseViewModel<...>(InitialState(/* seed arg into state if displayed */)) {

    @AssistedFactory
    interface Factory { fun create(fooArg: String): FooViewModel }

    init { /* kick off the load once, using fooArg */ }
}
```

### Screen
```kotlin
@Composable
fun FooScreen(
    fooArg: String,
    navigator: FooNavigator,
    viewModel: FooViewModel =
        hiltViewModel<FooViewModel, FooViewModel.Factory> { it.create(fooArg) },
) { ... }   // remove the LaunchedEffect(fooArg) { onIntent(Load) } block
```

Nav entry wiring (`entry<...> { key -> FooScreen(fooArg = key.x, ...) }`) is **unchanged** —
screens already receive the arg from the typed key.

## Per-ViewModel notes

### 1. OtpViewModel (`feature/onboarding`)
- [ ] Add `@Assisted phone: String` + `Factory`. Seed `OtpState(phone = phone, ...)`.
- [ ] Delete `OtpIntent.SetPhone` and its handler branch; remove
      `LaunchedEffect(phone) { onIntent(SetPhone) }` in `OtpScreen`.
- [ ] **Keep** `SavedStateHandle` + `KEY_RESEND_COOLDOWN` and the `init{}` cooldown
      resume — that is transient UI state, the article's approved use. Both the
      `@Assisted` arg and `SavedStateHandle` coexist in the constructor.
- [ ] Update `OtpScreen` signature to use the factory overload.
- [ ] Wire `app/ui/OnboardingFlow.kt` entry (signature unchanged: `phone = key.phone`).

### 2. ExperienceDetailsViewModel (`feature/experiences`)
- [ ] Add `@Assisted experienceId: String` + `Factory`.
- [ ] **Preserve the `MutableSharedFlow` retry mechanism.** Replace the
      `LaunchedEffect`-driven first emit with a single `init { _experienceIdSignal.tryEmit(experienceId) }`.
      A retry-after-error still re-emits the same id (add/keep a `Retry` intent that
      calls `tryEmit(experienceId)`), preserving the documented G3 behavior.
- [ ] Remove `LaunchedEffect(experienceId) { onIntent(LoadDetails) }` from the screen.
- [ ] Keep `LoadDetails(id)` intent only if still needed for retry; otherwise drop the
      param and emit the stored `experienceId`.

### 3. StayDetailsViewModel (`feature/stays`)
- [ ] Same as Experience: `@Assisted stayId`, seed `init { _stayIdSignal.tryEmit(stayId) }`,
      preserve SharedFlow retry, remove the screen `LaunchedEffect`.

### 4. StayResultsViewModel (`feature/stays`)
- [ ] Add `@Assisted location: String` + `Factory`. Seed `StayResultsState(location = location)`
      so the top bar shows the location on frame one.
- [ ] Move the `LoadStays` launch into `init {}` using `location`.
- [ ] Keep `LoadStays` intent only if a manual refresh path needs it; else remove.
- [ ] Remove the screen `LaunchedEffect(location)`.

## Tests
- [x] Each migrated VM can now be constructed directly in tests
      (`FooViewModel(fooArg = "x", fakeUseCase)`) — no fake `SavedStateHandle` map.
      Turbine test per VM asserting initial-arg-driven load.
- [x] OtpViewModel test: assert cooldown still resumes from a seeded `SavedStateHandle`
      (process-death path) independently of the assisted `phone`.
- [x] Added `testImplementation(core:testing, junit4, turbine, coroutines-test, mockk)` to
      `:feature:stays` and `:feature:experiences` (they had **no** test deps).

## Verification
- [x] `./gradlew :feature:onboarding:compileDebugKotlin :feature:experiences:compileDebugKotlin :feature:stays:compileDebugKotlin :app:compileDevDebugKotlin`
      (`:app` is flavored — use `compileDevDebugKotlin`, not `compileDebugKotlin`)
- [x] `./gradlew assembleDevDebug` — **BUILD SUCCESSFUL** (plan said `assembleDebug`; `:app` is flavored).
- [x] Unit tests green: `:feature:onboarding`, `:feature:stays`, `:feature:experiences`
      `testDebugUnitTest` — **23 tests, 0 failures**.
- [~] Manual: rotate each detail/results screen → no re-fetch. Now **structurally guaranteed**
      (load runs once in `init{}`, no `LaunchedEffect`). On-device pass still recommended.
- [~] Manual: process death on OTP → `phone` from key, cooldown resumes. Covered by unit tests
      (`assisted phone arg seeds initial state`, `saved cooldown is initialised…`). On-device pass recommended.

## Docs
- [x] Code matches `.claude/rules/navigation3-viewmodel-args.md` — all 4 screens use the
      `hiltViewModel<VM, VM.Factory> { factory.create(arg) }` overload; no rule edit needed.
- [x] No nav-arg `SavedStateHandle` reads remain. Every surviving `SavedStateHandle` use is
      transient/process-death state (search forms, selected tab/category, OTP cooldown) — the
      rule-blessed use. Rule and code do not contradict.

---

## Review

### Summary
Migrated all 4 arg-bearing ViewModels off the `LaunchedEffect(arg) { onIntent(Load) }` pattern
onto Hilt `@AssistedInject`, so each nav arg is a non-null constructor invariant and the load
runs once in `init{}` (no re-fetch on rotation). Added the missing unit-test infrastructure and
full coverage, fixed two latent bugs surfaced by the tests, and verified compile + assemble + tests.

### Latent bugs found & fixed (via the new tests)
1. **Detail screens stuck on "Loading" forever (CRITICAL).** `StayDetailsViewModel` and
   `ExperienceDetailsViewModel` seeded the load with `init{ _idSignal.tryEmit(id) }` into a
   `MutableSharedFlow(replay = 0)`. The emit fires *before* the screen subscribes
   (`collectAsStateWithLifecycle` runs after construction, and `stateIn` is `WhileSubscribed`),
   so with `replay = 0` the id was dropped and the load never ran. The old `LaunchedEffect`
   emitted *after* subscription, masking this. **Fix:** `replay = 1` so a late subscriber (and a
   `WhileSubscribed` re-subscribe after backgrounding) receives the seeded id. Preserves the G3
   retry property (SharedFlow re-delivers the same id; no `distinctUntilChanged`) and also resolves
   the old "stuck after 5 s restart" NOTE. Proven red→green by the Turbine tests.
2. **Stale error banner after a successful Retry (`StayResultsViewModel`).** `loadStays()` didn't
   clear `error` on (re)load, so a successful Retry left the previous error in state.
   **Fix:** `copy(isLoading = true, error = null)` at load start.

### Tests added (23 total, all green)
- `OtpViewModelTest` (12) — fixed for the new 4-arg `@AssistedInject` constructor (phone passed
  directly, dropped from `SavedStateHandle`); added `assisted phone arg seeds initial state`.
  Process-death cooldown tests retained and passing.
- `StayResultsViewModelTest` (5, **new file**) — assisted location seeds state, init-load
  success/error (+`ShowError`), `Retry` reloads, `SelectStay` navigates.
- `StayDetailsViewModelTest` (3, **new file**) — init-seeded success/error, `Retry` re-emits same id.
- `ExperienceDetailsViewModelTest` (3, **new file**) — mirror of stay details.

### Files touched
- VMs: `OtpViewModel`, `StayResultsViewModel`, `StayDetailsViewModel`, `ExperienceDetailsViewModel`
- Screens: `OtpScreen`, `StayResultsScreen`, `StayDetailsScreen`, `ExperienceDetailsScreen`
- MVI: `OtpMvi`, `StayResultsMvi`, `StayDetailsMvi`, `ExperienceDetailsMvi` (retired intents commented)
- Build: `:feature:stays` + `:feature:experiences` `build.gradle.kts` (test deps)
- Tests: 3 new test files + `OtpViewModelTest` fix
- Nav entry wiring (`OnboardingFlow`, feature `NavigationModule`s): unchanged signatures

### Verification results
- `:app:compileDevDebugKotlin` ✅ · `assembleDevDebug` ✅ · 23 unit tests ✅ 0 failures

---

## Status (last updated 2026-06-19)

### ✅ Done — migration complete & verified
- All 4 ViewModels on `@AssistedInject` + `@AssistedFactory`; all 4 screens on the factory overload;
  per-screen `LaunchedEffect(arg)` blocks removed; retired intents commented (not deleted).
- Two latent bugs found via tests and fixed (detail screens stuck loading; stale Retry error) — see Review.
- Test infra added to `:feature:stays` and `:feature:experiences`; **23 unit tests, 0 failures**.
- `:app:compileDevDebugKotlin`, `assembleDevDebug` both **BUILD SUCCESSFUL**.
- Docs reconciled — code conforms to `.claude/rules/navigation3-viewmodel-args.md`; no rule edit needed.

### Correction to the plan (applied)
- `:app` is flavored (`dev`/`staging`/`production`) → use `compileDevDebugKotlin` / `assembleDevDebug`,
  not the non-flavored task names in the original plan. Verification section updated.

### ⬜ Optional / out of scope
- On-device manual pass (rotation no-refetch, OTP process-death) — behavior is structurally
  guaranteed and unit-covered; on-device confirmation recommended but not blocking.
