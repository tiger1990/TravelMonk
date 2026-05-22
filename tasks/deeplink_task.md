# TravelMonk — Architecture Gap Tracker

Sourced from the full architectural audit. Work through these in priority order.
Status legend: `[ ]` = todo

---

## MEDIUM

### GAP-14 — No Deep Link Handling
- **Status:** `[ ]`
- **File(s):** `app/src/main/java/com/travelmonk/MainActivity.kt`
- **Problem:** `MainActivity.onCreate` does not process `intent`. No deep link routing, no notification tap handling, no external URL dispatch. For a travel app, booking confirmations, push notifications ("Your flight is tomorrow"), and marketing links all require deep links.
- **Fix:**
  - Add `intent-filter` entries in `AndroidManifest.xml` for app scheme / HTTPS deep links.
  - Handle `intent` in `MainActivity.onCreate` and `onNewIntent`.
  - Route the parsed path/params to the `NavigationBus` as the appropriate `TravelNavKey`.
  - Consider defining a `DeepLinkHandler` in `core/navigation` to keep `MainActivity` thin.

---

## Progress Summary

| Gap | Severity | Status | Area |
|-----|----------|--------|------|
| GAP-14 | MEDIUM | `[ ]` | Missing Feature — Deep Links |

> Completed gaps (GAP-01–13, 15, 16) removed. See `tasks/pending_review_completion.md` for full history.
