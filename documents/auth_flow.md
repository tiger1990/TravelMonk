A clean authentication flow for a fresh app launch should minimize friction while still making all auth methods discoverable.

Recommended Screen Pattern
1. Splash Screen
   App logo
   Short loading state
   Check:
   existing session
   valid passkey
   cached login state

If authenticated:
→ Go directly to Home

Else:
→ Auth screen

2. Unified Authentication Screen (Best UX)

Instead of separate screens initially, show one combined entry screen.

Example Layout
Header
Welcome back
Sign in or create an account
Primary Options
Passkey (Top Priority)
[ Continue with Passkey ]

Reason:

fastest
passwordless
biometric-friendly
modern UX

If device supports passkeys:

show prominently
maybe auto-trigger suggestion
Divider
OR
Input Section

Single field:

Email or Phone Number

Smart detection:

contains @ → email flow
numeric → phone flow

Then:

[ Send OTP ]

This avoids separate “email login” and “phone login” screens.

3. OTP Verification Screen

After submit:

Enter verification code

Features:

auto-read OTP
resend timer
edit email/phone
fallback methods
4. Optional Account Setup

Only for first-time users:

name
avatar
preferences

Keep skippable.

Recommended UX Hierarchy
Passkey
↓
Email/Phone OTP
↓
Support / recovery
Why This Pattern Works
Less Decision Fatigue

Users don’t choose between:

email login
phone login

They simply enter identity.

Faster Returning User Experience

Passkeys become:

one tap
Face ID / Touch ID
no OTP waiting
Scales Well

Later you can add:

Google
Apple
GitHub
enterprise SSO

without redesigning flow.

Suggested Navigation Flow
Launch App
↓
Session exists?
├─ Yes → Home
└─ No
↓
Auth Screen
├─ Passkey
└─ Email/Phone OTP
↓
Verify OTP
↓
New user?
├─ Yes → Setup
└─ No → Home
Mobile UX Recommendations
iOS
use native passkey sheet
support autofill OTP
Android
Credential Manager API
SMS Retriever API
Avoid
Separate buttons like:
Login with Email
Login with Phone

This adds unnecessary branching.

Ideal Modern Auth Stack
Passkeys → primary
OTP → fallback
Social login → optional
Passwords → avoid if possible


┌──────────────────────────────────┐
│                                  │
│              LOGO                │
│                                  │
│        Welcome back 👋           │
│  Sign in or create an account    │
│                                  │
│  ┌────────────────────────────┐  │
│  │ 🔐 Continue with Passkey   │  │
│  └────────────────────────────┘  │
│                                  │
│              ─ OR ─              │
│                                  │
│  Email or Phone Number           │
│  ┌────────────────────────────┐  │
│  │ example@email.com          │  │
│  └────────────────────────────┘  │
│                                  │
│  ┌────────────────────────────┐  │
│  │         Send OTP           │  │
│  └────────────────────────────┘  │
│                                  │
│                                  │
│  By continuing, you agree to     │
│  Terms & Privacy Policy          │
│                                  │
└──────────────────────────────────┘


┌──────────────────────────────────┐
│                                  │
│          Verify OTP              │
│                                  │
│  Enter the 6-digit code sent to  │
│  +91 98XXXXXX21                  │
│                                  │
│     ┌──┐ ┌──┐ ┌──┐               │
│     │1 │ │2 │ │3 │               │
│     └──┘ └──┘ └──┘               │
│     ┌──┐ ┌──┐ ┌──┐               │
│     │4 │ │5 │ │6 │               │
│     └──┘ └──┘ └──┘               │
│                                  │
│        Resend OTP in 24s         │
│                                  │
│      Change phone/email          │
│                                  │
└──────────────────────────────────┘


First-Time User Setup
┌──────────────────────────────────┐
│                                  │
│        Complete Profile          │
│                                  │
│  Name                            │
│  ┌────────────────────────────┐  │
│  │ John Doe                   │  │
│  └────────────────────────────┘  │
│                                  │
│  Username                        │
│  ┌────────────────────────────┐  │
│  │ @johndoe                   │  │
│  └────────────────────────────┘  │
│                                  │
│  ┌────────────────────────────┐  │
│  │         Continue           │  │
│  └────────────────────────────┘  │
│                                  │
│              Skip                │
│                                  │
└──────────────────────────────────┘

Recommended UI Notes
Make passkey button visually primary
Auto-focus OTP field
Auto-submit when OTP completed
Support paste OTP
Keep auth on a single screen when possible
Avoid deep auth navigation stacks

Verification

1. Run on emulator → tap "Create Passkey" → completes without exception, no error shown, transitions to main app (onboarding complete).
2. Run on emulator → tap "Sign In with Passkey" → same result.
3. Real try/catch blocks for CreateCredentialException remain untouched — no regression on future real-device path.
4. When backend is integrated: set isMockMode = false in PasskeyRepositoryImpl to re-enable real CredentialManager flow.