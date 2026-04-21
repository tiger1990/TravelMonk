# Design System Documentation: The Digital Curator

## 1. Overview & Creative North Star
The North Star for this design system is **"The Digital Curator."**

This system transcends the utility of a standard travel app, positioning itself as a high-end editorial authority. It treats travel destinations not as data points, but as curated exhibits in a digital gallery. The aesthetic breaks from the rigid, "templated" look of modern SaaS by utilizing intentional asymmetry, oversized editorial type, and a philosophy of "negative space as luxury."

We lean heavily into high-contrast transitions—moving from deep, immersive emerald voids to crisp, airy white canvases. This system is designed to showcase high-fidelity photography through layered glassmorphism and sophisticated tonal depth, ensuring the interface feels like a physical, premium publication.

---

## 2. Color Strategy
The palette is rooted in the tension between the organic and the architectural. We use **Deep Emerald (#0B3027)** to anchor the experience in prestige, while our neutral scale provides the "breathing room" required for an editorial feel.

### The "No-Line" Rule
To maintain a bespoke, high-end appearance, **1px solid borders are strictly prohibited for sectioning.** Boundaries must be defined through:
*   **Background Shifts:** Distinguish a section by moving from `surface` (#fcf8ff) to `surface_container_low` (#f6f2ff).
*   **Tonal Transitions:** Use high-contrast blocks of `primary_container` (#0b3027) against `surface` to create hard, intentional edges without the "wireframe" feel of lines.

### Surface Hierarchy & Nesting
Treat the UI as a physical stack of premium materials. Use the `surface-container` tiers to define importance:
1.  **Base Layer:** `surface` (#fcf8ff)
2.  **Sectioning Layer:** `surface_container_low` (#f6f2ff)
3.  **Interactive Elements/Cards:** `surface_container_lowest` (#ffffff) to provide a "natural lift."

### The Glass & Gradient Rule
For elements floating over immersive photography (e.g., navigation bars, image captions), use **Glassmorphism**. Combine semi-transparent surface colors (60-80% opacity) with a `32px` backdrop blur.
*   **Signature Texture:** Apply a subtle linear gradient from `primary` (#001a13) to `primary_container` (#0b3027) on main CTAs to give them a "machined" satin finish rather than a flat, digital look.

---

## 3. Typography
Our typography is the primary driver of the editorial voice.

*   **Epilogue (Headlines):** A bold, expressive sans-serif. Use `display-lg` and `display-md` with tight letter-spacing (-2%) to create a "masthead" feel. This is the voice of the curator.
*   **Manrope (Body/Functional):** A clean, geometric sans-serif that balances the weight of Epilogue. It provides high legibility for itineraries and logistical details.

**Hierarchy as Identity:**
- **Display Scale:** For immersive storytelling and hero sections.
- **Title Scale:** Used for "Curated Collections" or "Exhibits."
- **Label Scale:** Always in `on_surface_variant` (#414845) or `primary` to denote metadata (e.g., "7 DAYS / 6 NIGHTS").

---

## 4. Elevation & Depth
In this system, depth is a result of light and material, not artificial drop shadows.

*   **The Layering Principle:** Avoid elevation shadows where possible. Instead, "nest" containers. A `surface_container_highest` card sitting on a `surface` background creates sufficient contrast and a sense of physical layering.
*   **Ambient Shadows:** If an element must float (like a FAB or a modal), use a "Whisper Shadow": `color: on_surface`, `opacity: 6%`, `blur: 40px`, `y-offset: 12px`. This mimics the soft, diffused light of an art gallery.
*   **The Ghost Border Fallback:** If accessibility requires a stroke, use the `outline_variant` token at **15% opacity**. This creates a "suggestion" of a container without breaking the editorial flow.
*   **Glassmorphism:** Use `surface_container_lowest` at 70% opacity with a heavy backdrop blur for floating navigation. This integrates the UI into the background imagery.

---

## 5. Components

### Buttons
*   **Primary:** Pill-shaped (`full` roundedness). Background: `primary_container` (#0B3027). Text: `on_primary`. Use subtle 16px horizontal padding to allow the bold Epilogue type to breathe.
*   **Secondary:** `surface_container_highest` background with `on_surface` text. No border.
*   **Tertiary:** Pure text in `primary` with a 2px underline that only appears on hover.

### Cards & Lists
*   **The "No-Divider" Rule:** Lists must not use horizontal lines. Separate items using `16px` of vertical white space or a subtle background shift to `surface_container_low` on every second item.
*   **Editorial Cards:** Use high-contrast ratios. Image as the background with a glassmorphic "caption block" anchored to the bottom-left using `xl` (1.5rem) rounded corners on the top-right only.

### Input Fields
*   **Styling:** Forgo the four-sided box. Use a "Filled Tonal" style with `surface_container_high` and a bottom-only accent of `primary` (2px) during the active state.
*   **Error States:** Use `error` (#ba1a1a) for text and a subtle `error_container` wash for the background.

### Immersive Components
*   **The Progress Curator:** A horizontal progress bar for multi-step bookings using `primary_fixed_dim` for the track and `primary` for the progress, with no rounded ends on the progress bar—maintain sharp, architectural edges.
*   **Glass Filters:** Filter chips should be semi-transparent `surface_variant` with a heavy blur, making them feel like physical lenses over the content.

---

## 6. Do's and Don'ts

### Do
*   **Do** use asymmetrical layouts (e.g., a large image on the left, offset headline on the right).
*   **Do** prioritize "negative space." If in doubt, add more padding.
*   **Do** use the `primary_container` for high-impact, full-screen color blocks to reset the user's palette between sections.

### Don't
*   **Don't** use 1px borders to separate content. Use whitespace or color.
*   **Don't** use standard "Material Design Blue" or generic greys. Stick to the sophisticated Emerald and lavender-tinted surfaces provided.
*   **Don't** crowd the "Display" type. Epilogue needs room to be the hero of the page.
*   **Don't** use harsh drop shadows. If it doesn't look like light hitting paper, it’s too heavy.