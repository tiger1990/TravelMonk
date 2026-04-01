# Project Plan

Build agoda and booking like app, clearly in compose. use latest navigation 3 library, make every composable respecting compose design proper state hoisting to reduce unnecessary recomoposition, proper usage of clean architecture with MVI design pattern. should have flight booking, bus booking, train booking, travel packages booking, local tour guide booking, yoga class booking/reseving, rental apartment booking for travel, site visit booking , helper or maid booking. use bottom tabs and latest clean : for flight booking use design as in this link: https://dribbble.com/shots/26307898-Travel-Booking-Mobile-App-Flight-Booking-UI , Access from this design usecases: https://uxdesign.cc/designing-an-app-for-a-travel-agency-a-ux-case-study-c37dd8fbb32f, check this for ui missing: https://dribbble.com/shots/27181774-Travel-Hotel-Booking-Mobile-App-UI-Design

## Project Brief

# TravelMonk Project Brief

## Features
1.  **Unified Transport Booking**: A comprehensive search and booking system for flights, buses, and trains, allowing users to compare and secure travel options in one interface.
2.  **Accommodations & Rental Stays**: Discovery and reservation platform for diverse lodging options, including luxury hotels, rental apartments, and specialized yoga retreats.
3.  **Curated Tours & Local Experiences**: Booking engine for local tour guides, site visits, and all-inclusive travel packages to enhance the travel experience.
4.  **Centralized Itinerary Management**: A dedicated "My Bookings" hub to view, manage, and track all active reservations and e-tickets in real-time.

## High-Level Technical Stack
*   **Language**: Kotlin
*   **UI Framework**: Jetpack Compose with Material Design 3 (M3)
*   **Architecture**: Clean Architecture with MVI (Model-View-Intent) design pattern
*   **Navigation**: Navigation 3 library for seamless multi-stack and bottom-tab navigation
*   **Networking**: Retrofit & OkHttp with Moshi for type-safe API communication
*   **Code Generation**: KSP (Kotlin Symbol Processing) for Moshi and other annotation-based tasks
*   **Asynchronous Programming**: Kotlin Coroutines & Flow for reactive state management and background tasks
*   **Image Loading**: Coil for optimized and performant image rendering
*   **System Integration**: Full Edge-to-Edge display support and Adaptive App Icon implementation

## Implementation Steps

### Task_1_SetupInfrastructure: Setup the project infrastructure including dependencies, clean architecture layers, and Material 3 theme.
- **Status:** IN_PROGRESS
- **Acceptance Criteria:**
  - Add Navigation 3, Retrofit, Moshi, KSP, and Coil dependencies to libs.versions.toml and build.gradle.kts.
  - Implement a vibrant Material 3 color scheme with light and dark mode support.
  - Configure full Edge-to-Edge display and base Clean Architecture folder structure (data, domain, ui).
  - Project builds successfully.
- **StartTime:** 2026-03-19 21:05:28 IST

### Task_2_NavigationAndMainShell: Implement the Navigation 3 multi-stack structure and the main UI shell with bottom tabs.
- **Status:** PENDING
- **Acceptance Criteria:**
  - Define NavKey and NavigationState for Navigation 3.
  - Implement a bottom navigation bar with icons for Transport, Stays, Experiences, and My Bookings.
  - Create the My Bookings hub to manage itinerary state.
  - Navigation between tabs works seamlessly with state retention.

### Task_3_TransportAndStayBooking: Implement the UI and MVI logic for Flight, Bus, Train, and Hotel/Apartment booking.
- **Status:** PENDING
- **Acceptance Criteria:**
  - Develop the Flight booking UI matching the design from the provided Dribbble link (26307898).
  - Develop the Hotel/Apartment booking UI matching the design from the provided Dribbble link (27181774).
  - Implement Bus and Train search/booking screens using proper state hoisting.
  - All screens follow the MVI pattern and use Clean Architecture layers.

### Task_4_ServicesAndExperiences: Implement booking flows for Travel Packages, Local Guides, Yoga Classes, Site Visits, and Helpers.
- **Status:** PENDING
- **Acceptance Criteria:**
  - Create discovery and reservation screens for travel packages and local tour guides.
  - Implement booking interfaces for Yoga classes, site visits, and helper/maid services.
  - Ensure all services are integrated into the centralized 'My Bookings' hub.
  - App handles diverse booking states (search, details, confirmation) correctly.

### Task_5_FinalizeAndVerify: Finalize app identity and perform comprehensive verification.
- **Status:** PENDING
- **Acceptance Criteria:**
  - Generate an adaptive app icon matching the TravelMonk theme.
  - Verify the 'vibrant and energetic' color scheme across all screens.
  - Ensure all existing tests pass, the build is stable, and the app does not crash.
  - Verify UI fidelity against provided designs and report any critical issues.

