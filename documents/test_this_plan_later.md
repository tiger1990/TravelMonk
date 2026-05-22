As an Senior Principle Android Architect and In Parallel Get every UI Evaluated with Senior Principle Designer.
Once every features completes, get review from Senior Expert Android Architect and Iterate with fixes and recommendation.
At the end APP should be completely well architected, build production ready and designed with customer success.

# TravelMonk — Production Ready Android Travel Super App
## Architect-Level Implementation Prompt

Build a fully production-ready Android application named **TravelMonk** using modern Android architecture, scalable modularization, clean architecture principles, enterprise-grade security, production observability, offline-first design, and best-in-class UX.

The application is a travel super app combining:
- Travel stays booking
- Experiences booking
- Sports ecommerce/shop
- User profile & identity
- Recommendations & personalization
- Passkey + OTP authentication

The implementation must be architected like a real-world large-scale production application comparable to:
- Airbnb
- Booking.com
- Agoda
- MakeMyTrip
- TripAdvisor
- Amazon (shop module)
- Klook / Headout (experience module)

The implementation should prioritize:
- scalability
- maintainability
- testability
- performance
- security
- modularity
- offline resiliency
- smooth animations
- accessibility
- enterprise engineering standards

---

# 1. Tech Stack

## Language
- Kotlin (100%)

## UI
- Jetpack Compose only
- Material 3
- Adaptive layouts
- Dynamic color support
- Dark mode support
- Edge-to-edge UI

## Architecture
- Clean Architecture
- MVI / UDF architecture
- Modular multi-module architecture
- Feature-first architecture
- Repository pattern
- UseCases/Interactors
- Dependency inversion

## Navigation
- Navigation3 typed navigation
- Type-safe routes
- Nested navigation graphs

## Dependency Injection
- Hilt + Dagger

## Async
- Kotlin Coroutines
- Flow / StateFlow / SharedFlow

## Local Storage
- Room
- Proto DataStore
- EncryptedSharedPreferences
- Tink encryption

## Networking
- Retrofit
- OkHttp
- Kotlin Serialization or Moshi
- StrictMode compatible implementation

## Image Loading
- Coil 3

## Authentication
- OTP Authentication
- Email login
- Phone login
- Passkey / CredentialManager integration
- JWT token handling
- Refresh token handling

## Security
- Android Keystore
- SSL pinning
- Tink encryption
- Root detection
- Emulator detection
- Screenshot prevention for sensitive screens

## Analytics & Observability
- Firebase Analytics
- Crashlytics
- Performance Monitoring
- Timber logging
- Structured logs

## CI/CD
- GitHub Actions
- Fastlane
- Automated testing
- Lint checks
- Detekt
- Ktlint

---

# 2. Modularization Structure

Use strict modularization.

```text
app/

core/
  core-ui/
  core-designsystem/
  core-network/
  core-database/
  core-model/
  core-common/
  core-navigation/
  core-security/
  core-testing/
  core-analytics/
  core-domain/

feature/
  feature-auth/
  feature-home/
  feature-stays/
  feature-experiences/
  feature-shop/
  feature-cart/
  feature-checkout/
  feature-orders/
  feature-search/
  feature-profile/
  feature-notifications/
  feature-bookings/
  feature-passkey/

sync/
benchmark/
```

---

# 3. Architecture Rules

## Follow strict layering

```text
UI Layer
↓
Presentation Layer (ViewModel)
↓
Domain Layer (UseCases)
↓
Repository Layer
↓
Data Sources
↓
Remote / Local
```

Never:
- call repositories from Composables
- place business logic in UI
- expose DTOs outside data layer

---

# 4. Authentication Flow

## Login Methods
- Email + OTP
- Phone + OTP
- Passkey login
- Passkey registration

## Requirements
- CredentialManager integration
- WebAuthn compatible JSON contracts
- Token refresh flow
- Silent login restoration
- Session persistence
- Logout from all devices
- Device binding support

## Screens
- Splash
- Onboarding
- Phone entry
- Email entry
- OTP verification
- Passkey setup
- Passkey login
- Session expired

## Security Requirements
- OTP expiration timer
- Retry limits
- Abuse protection
- Rate limiting support
- Encrypted token storage
- Secure logout
- Biometric re-authentication

---

# 5. Home Screen Requirements

## Bottom Navigation Tabs
- Home
- Stays
- Experiences
- Shop
- Profile

## Top App Bar
Left:
- Profile avatar

Center:
- App name/logo

Right:
- Search icon
- Notification icon

---

# 6. Home Screen Content

## Hero Carousel
Animated auto-scrolling carousel:
- Stays
- Experiences
- Sports shopping products

Requirements:
- parallax effect
- shimmer loading
- snap behavior
- indicators
- immersive visuals

Use:
- HorizontalPager
- animated scaling
- blur overlays
- gradient scrims

---

## Home Sections

### Trending Experiences
- Popular nearby activities
- Dynamic ranking
- Personalized recommendations

### Trending Stays
- Recommended stays
- Rating based sorting

### Trending Purchases
- Most bought sports products

### Recently Viewed
- Continue browsing section

### Nearby Recommendations
- GPS-aware suggestions

### Seasonal Recommendations
- Smart personalized banners

### Deals & Offers
- Discount campaigns
- Promo cards

---

# 7. Search System

Global unified search.

Search categories:
- Stays
- Experiences
- Shop products

Features:
- Recent searches
- Search suggestions
- Debounced search
- Voice search
- Search history
- Trending searches

---

# 8. Filtering System

## Stay Filters
- Price
- Ratings
- Amenities
- Distance
- Availability
- Guest count

## Experience Filters
- Ratings
- Category
- Duration
- Difficulty
- Location
- Language

## Shop Filters
- Brand
- Category
- Price
- Ratings
- Availability

Use:
- Bottom sheets
- Chips
- Dynamic filters

---

# 9. Stay Booking Module

## Features
- Stay details page
- Image gallery
- Availability calendar
- Room selection
- Guest selection
- Pricing breakdown
- Reviews
- Ratings
- Wishlist
- Map integration
- Booking flow

## Booking Requirements
- Partial payment support
- Cancellation policies
- Coupons
- Taxes & fees
- Booking confirmation

---

# 10. Experience Module

## Features
- Experience listing
- Experience detail screen
- Schedule selection
- Ticket booking
- Group booking
- Host details
- Reviews
- Maps integration
- Safety instructions

---

# 11. Shop Module (Ecommerce)

Complete ecommerce implementation.

## Features
- Product listing
- Product detail page
- Product variants
- Add to cart
- Wishlist
- Checkout
- Coupons
- Shipping addresses
- Payment methods
- Order tracking
- Return/refund flow
- Inventory handling

## Cart Features
- Quantity updates
- Price recalculation
- Promo codes
- Saved for later
- Cart persistence

## Checkout Features
- Address management
- Payment integration abstraction
- Tax calculations
- Delivery estimation

---

# 12. Profile Module

## Features
- User details
- Edit profile
- Saved stays
- Saved experiences
- Wishlist
- Order history
- Booking history
- Payment methods
- Passkey management
- Notification preferences
- Language settings
- Privacy settings
- Support center

---

# 13. Notification System

Support:
- Push notifications
- In-app notifications
- Deep linking

Types:
- Booking updates
- Order updates
- Promotions
- Recommendations
- Security alerts

---

# 14. Offline First Strategy

Requirements:
- Local caching
- Sync queues
- Retry handling
- Optimistic updates
- Stale data indicators

Use:
- Room
- RemoteMediator
- Paging3

---

# 15. Performance Requirements

## App Startup
- Baseline Profiles
- Macrobenchmark
- Lazy initialization
- StrictMode compliance

## Rendering
- Avoid recomposition issues
- Stable models
- Immutable state

## Images
- Progressive loading
- Smart caching
- Blurhash placeholders

---

# 16. Security Requirements

## Mandatory
- SSL pinning
- JWT refresh rotation
- Root detection
- Emulator detection
- Secure storage
- Tink encryption
- Passkey support
- Certificate transparency

## Sensitive Screens
Prevent screenshots on:
- OTP screens
- Payment screens
- Passkey screens

---

# 17. Accessibility

Support:
- TalkBack
- Dynamic font scaling
- Screen readers
- Color contrast compliance
- Keyboard navigation

---

# 18. Animations

Use premium animations throughout app.

## Required
- Shared element transitions
- Motion layout patterns
- Animated visibility
- Skeleton loading
- Hero transitions
- Pull-to-refresh
- Micro interactions

---

# 19. Design System

Build centralized design system.

## Include
- Typography system
- Color system
- Shapes
- Elevation
- Spacing tokens
- Icons
- Buttons
- Cards
- Loading states

---

# 20. API Design Requirements

## Use
- DTO mapping
- Error wrappers
- NetworkResult abstraction
- Retry policies
- Pagination support

## Error Handling
- Offline handling
- Timeout handling
- Rate limiting
- Empty states
- Retry states

---

# 21. Testing Requirements

## Unit Testing
- ViewModels
- UseCases
- Repositories

## UI Testing
- Compose tests
- Screenshot tests

## Integration Testing
- Fake backend
- MockWebServer

## Performance Testing
- Startup benchmarks
- Scroll benchmarks

Target:
- 80%+ meaningful coverage

---

# 22. State Management

Use:
- immutable UI state
- sealed intents
- sealed effects

Example:

```kotlin
sealed interface HomeIntent
sealed interface HomeEffect

data class HomeUiState(...)
```

---

# 23. Pagination

Use Paging3 for:
- stays
- experiences
- products
- notifications
- reviews

---

# 24. Maps & Location

Features:
- Nearby stays
- Nearby experiences
- Interactive maps
- Current location
- Directions

Use:
- Google Maps SDK abstraction

---

# 25. Payment Architecture

Abstract payment providers.

Support:
- Razorpay
- Stripe
- Google Pay

Architecture:
- PaymentOrchestrator
- PaymentRepository
- Provider adapters

---

# 26. Production Readiness Checklist

## Include
- feature flags
- remote config
- A/B testing support
- analytics events
- crash reporting
- logging
- deep links
- app links
- Play Integrity API
- secure backups
- localization
- RTL support

---

# 27. Recommended UI Experience

Design aesthetic:
- Airbnb + Booking.com + Nike + Headout hybrid
- immersive visuals
- premium travel feel
- cinematic imagery
- minimal clutter
- smooth transitions

---

# 28. Expected Deliverables

Generate:
- complete architecture
- module setup
- package structure
- navigation graphs
- API contracts
- repositories
- ViewModels
- UI states
- composables
- fake APIs
- DTOs
- Room entities
- mappers
- test strategy
- CI/CD setup
- production security checklist
- analytics plan
- monitoring plan
- coding conventions
- design system

Everything should be implementation-ready and production-grade.