# ══════════════════════════════════════════════════════════════════════════════
# TravelMonk — Staging Specific Rules
# ══════════════════════════════════════════════════════════════════════════════

# Disables renaming of classes, methods, and fields.
# Why: Allows for readable stack traces in staging crash logs (Firebase, Logcat).
# Impact: R8 still performs shrinking and optimization (if enabled), but keeps
# names intact. This is ideal for QA testing "production-like" performance
# without the difficulty of de-obfuscating reports.
-dontobfuscate
