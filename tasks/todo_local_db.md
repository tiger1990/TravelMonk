# TravelMonk — Task Backlog

> See `tasks/pending_review_completion.md` for the full prioritized gap tracker.
> Status legend: `[ ]` = todo · `[x]` = done

---

## Pending

### [ ] Wire `data/local/` — Room Integration for all feature modules

Each feature module has a `data/local/*LocalDataSource.kt` placeholder with a commented-out Room DAO skeleton.

**Scope:**
- `FlightLocalDataSource.kt` → `FlightDao` (cache search results by from/to)
- `StayLocalDataSource.kt` → `StayDao` (cache stays by location)
- `ExperienceLocalDataSource.kt` → `ExperienceDao` (cache by category)
- `BookingLocalDataSource.kt` → `BookingDao` (persist bookings, support cancel)
- `ServiceLocalDataSource.kt` → `ServiceDao` (cache services list)
- `HomeLocalDataSource.kt` → `HomeBannerDao` (cache home banners)

**Prerequisites:**
- `core:database` needs Room dependency + `AppDatabase` class
- Each `*RepositoryImpl.kt` must coordinate `api/` (network) + `local/` (cache)

---