# Design System Rules — TravelMonk

These rules ensure visual consistency and support for advanced features like the Glass UI and Dark Mode.

## 1. Zero-Literal Rule
- **Colors**: Never use `Color(0xFF...)` or `Color.White/Black` in UI code. Always use `TravelMonkTheme.colors.*`.
- **Typography**: Never use `fontSize = 16.sp` or `fontWeight = FontWeight.Bold` directly. Always use `TravelMonkTheme.typography.*`.
- **Spacing**: Use `TravelMonkTheme.spacing.*` for padding and margins.
- **Dimensions**: Use `TravelMonkTheme.dimensions.*` for fixed sizes like icons or card heights.

## 2. Semantic Token Usage
- Prefer semantic tokens (e.g., `bottomBarBackground`) over raw palette colors (e.g., `primary`) when available.
- This allows for precise tuning of specific UI components without affecting the entire theme.

## 3. Glass UI Principles
- Translucent backgrounds must use the specific `bottomBarBackground` or similar semantic tokens that include alpha values.
- Ensure that `containerColor = Color.Transparent` is used in parent layouts (like `TopBar` or `TabRow`) when they are nested inside a translucent or custom-colored container.
