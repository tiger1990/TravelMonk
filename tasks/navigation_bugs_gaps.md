# Navigation Bugs & Architectural Gaps

## Bug: Flight results / search screen persists after booking

### Problem
After searching for a flight and completing a booking, returning to the Transport tab
shows the stale `FlightResultsScreen` (or the embedded search content). The source tab's
back stack is never cleared when the user transitions to a booking confirmation in the
Bookings tab.

Additionally, `navigateToMyBookings()` incorrectly pushes `BookingNavKey.Root` onto the
Bookings back stack instead of popping back to it, creating a stack of
`[Root, Confirmation, Root]`.

### Root Causes
1. `FlightNavKey.Results` remains in the Transport back stack after booking — no mechanism
   to clear a tab's stack when transitioning to a terminal cross-tab action.
2. `navigateToMyBookings()` calls `bus.navigate(BookingNavKey.Root)` which adds Root again
   instead of popping the Confirmation.
3. `GlobalNavigator` uses `MutableSharedFlow(extraBufferCapacity = 1, DROP_LATEST)` —
   calling `bus.back()` + `bus.navigate()` in sequence silently drops the second command,
   making two-command workarounds unreliable.

---

## Fix Plan — NavOptions Approach

### Why NavOptions (not `navigateAndClear`)
`navigateAndClear` bakes business policy into a navigation primitive and forces 4 navigators
to duplicate the same rule. The industry-standard pattern (Navigation Component, React Navigation,
Flutter Navigator 2.0) uses **NavOptions**: callers declare *what* they want, the navigation
layer executes *how*. Extensible, DRY, and self-documenting.

---

## Implementation Checklist

### Step 1 — Add `NavOptions` to `core/navigation`
- ✅ Created `core/navigation/src/main/java/com/travelmonk/core/navigation/NavOptions.kt`

### Step 2 — Update `NavigationBus` interface
- ✅ `core/navigation/src/.../NavigationBus.kt` — added `navigate(key, options)` with default

### Step 3 — Update `NavCommand` + `GlobalNavigator`
- ✅ `app/src/main/java/com/travelmonk/navigation/GlobalNavigator.kt`
  - `NavCommand.Navigate` now carries `options: NavOptions`
  - `navigate(key, options)` implemented

### Step 4 — Update `NavigationState`
- ✅ `app/src/main/java/com/travelmonk/ui/navigation/NavigationState.kt`
  - `navigateTo(key, options)` clears current tab stack when `popCurrentTabToRoot = true`
  - `rememberNavigationState` `LaunchedEffect` passes `options` through

### Step 5 — Update all booking navigators in `app/di/NavigationModule`
- ✅ `FlightNavigator.navigateToBookingConfirmation` → `NavOptions(popCurrentTabToRoot = true)`
- ✅ `ExperienceNavigator.navigateToBookingConfirmation` → same
- ✅ `StayNavigator.navigateToBookingConfirmation` → same
- ✅ `ServiceNavigator.navigateToBookingConfirmation` → same

### Step 6 — Fix `navigateToMyBookings`
- ✅ `feature/bookings/src/.../di/NavigatorModule.kt`
  - Now: `bus.navigate(BookingNavKey.Root, NavOptions(popCurrentTabToRoot = true))`

---

## Files Modified

| File | Change | Status |
|------|--------|--------|
| `core/navigation/src/.../NavOptions.kt` | **NEW** — NavOptions data class | ✅ Done |
| `core/navigation/src/.../NavigationBus.kt` | Added `navigate(key, options)` overload | ✅ Done |
| `app/src/.../navigation/GlobalNavigator.kt` | NavCommand.Navigate carries options | ✅ Done |
| `app/src/.../ui/navigation/NavigationState.kt` | `navigateTo` handles `popCurrentTabToRoot` | ✅ Done |
| `app/src/.../di/NavigationModule.kt` | 4 navigators pass NavOptions for booking | ✅ Done |
| `feature/bookings/src/.../di/NavigatorModule.kt` | `navigateToMyBookings` uses NavOptions | ✅ Done |

---

## Deferred

- **BookingConfirmation as a full-screen modal** — pending decision. If adopted, eliminates
  the root cause entirely: no back-stack manipulation needed, the tab bar is hidden during
  booking, and the user returns to exactly where they came from on dismiss.