```markdown
# TravelMonk Development Patterns

> Auto-generated skill from repository analysis

## Overview
This skill teaches the core development patterns and conventions used in the TravelMonk Kotlin codebase. It covers file organization, code style, commit practices, and testing approaches to ensure consistency and maintainability across the project. While no specific framework is detected, the repository follows clear conventions for naming, imports, exports, and commit messages.

## Coding Conventions

### File Naming
- Use **PascalCase** for all file names.
  - **Example:** `UserProfile.kt`, `BookingManager.kt`

### Import Style
- Use **relative imports** within the codebase.
  - **Example:**
    ```kotlin
    import ../models/Booking
    ```

### Export Style
- Use **named exports** to expose classes or functions.
  - **Example:**
    ```kotlin
    // In BookingManager.kt
    class BookingManager { ... }
    export { BookingManager }
    ```

### Commit Messages
- Follow **conventional commit** patterns.
- Use prefixes such as `refactor` and `fix`.
- Keep commit messages concise (average 65 characters).
  - **Example:**
    ```
    refactor: update BookingManager to use new date format
    fix: resolve null pointer in UserProfile initialization
    ```

## Workflows

_No automated workflows detected in this repository._

## Testing Patterns

- **Test File Naming:** Test files use the `*.test.ts` pattern, indicating TypeScript-based tests.
  - **Example:** `BookingManager.test.ts`
- **Testing Framework:** Not explicitly detected. Ensure that tests are colocated with the code they cover and follow the naming convention above.

## Commands

| Command         | Purpose                                      |
|-----------------|----------------------------------------------|
| /commit-guide   | Show commit message conventions and examples |
| /naming-guide   | Show file naming and export/import patterns  |
| /test-guide     | Show how to write and name test files        |

```