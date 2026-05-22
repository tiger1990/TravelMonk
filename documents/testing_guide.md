MockK: Simulating Dependencies
MockK is a Kotlin-first library used to create"mocks" of your repositories or services 
so you can test your ViewModel in isolation.

Mocking: Create a fake instance of a class using mockk<MyRepository>().
Stubbing: Use every { ... } returns ... to define what a method should return when called. For suspend functions, use coEvery { ... }.
Verification: Use verify { ... } or coVerify { ... } to ensure a specific method was actually called during the test.

Turbine: Testing Kotlin Flows
Turbine is a specialized library for testing Kotlin Flows. 
It allows you to "collect" a flow and assert its values item-by-item in a sequential, readable way.

test { } block: The entry point for testing any Flow. It handles collection and cleanup automatically.
awaitItem(): Pauses the test until the Flow emits a new value, which you can then assert against.
awaitComplete(): Asserts that the Flow has finished successfully.
cancelAndIgnoreRemainingEvents(): Safely ends the test for "hot" flows (like StateFlow or SharedFlow) that never naturally complete.

Example:
@Test
fun `when loading booking, emits loading then success`() = runTest {
// 1. GIVEN: Mock the repository using MockK
val repo = mockk<BookingRepository>()
coEvery { repo.getBooking() } returns Booking(id = 1)
val viewModel = BookingViewModel(repo)

    // 2. WHEN & EXPECT: Use Turbine to check emissions
    viewModel.state.test {
        viewModel.loadBooking()

        assertEquals(BookingState.Loading, awaitItem()) // First emission
        assertEquals(BookingState.Success(Booking(1)), awaitItem()) // Second emission
        
        cancelAndIgnoreRemainingEvents()
    }
}

## Passkey Testing TODO

Our current mock coverage is correct and complete for real-device passkey flow testing.

The only missing scenario is unit/UI testing without a real device or Credential Manager integration.  
In that case:

- Use **Mock Registration Result** to stub `CredentialManager.createCredential()`
- Use **Mock Authentication Result** to stub `CredentialManager.getCredential()`

This is only required for:
- unit tests
- ViewModel tests
- Compose UI tests
- fake CredentialManager implementations

It is **not required for runtime flow testing** because Android Credential Manager generates these responses automatically.

---

# Runtime Flow

## Registration Flow

```text
App → beginRegistration()
Backend → Registration Challenge JSON
CredentialManager.createCredential()
Android → Registration Result JSON
App → completeRegistration()
Backend → AuthToken
```

## Authentication Flow

```text
App → beginAuthentication()
Backend → Authentication Challenge JSON
CredentialManager.getCredential()
Android → Authentication Result JSON
App → completeAuthentication()
Backend → AuthToken
```

---

# 1. Mock Registration Challenge

Server → App

```json
{
  "rp": {
    "name": "TravelMonk",
    "id": "travelmonk.com"
  },
  "user": {
    "id": "dXNlcl8xMjM0NQ",
    "name": "john@example.com",
    "displayName": "John Doe"
  },
  "challenge": "Q1hBTExFTkdFX1JFR0lTVEVS",
  "pubKeyCredParams": [
    {
      "type": "public-key",
      "alg": -7
    },
    {
      "type": "public-key",
      "alg": -257
    }
  ],
  "timeout": 60000,
  "attestation": "none",
  "authenticatorSelection": {
    "residentKey": "preferred",
    "userVerification": "preferred"
  }
}
```

---

# 2. Mock Registration Result

App → Server

Used only for:
- unit tests
- fake CredentialManager implementations
- ViewModel testing

```json
{
  "id": "ZHVtbXlfY3JlZGVudGlhbF9pZA",
  "rawId": "ZHVtbXlfY3JlZGVudGlhbF9pZA",
  "type": "public-key",
  "response": {
    "clientDataJSON": "eyJ0eXBlIjoid2ViYXV0aG4uY3JlYXRlIiwiY2hhbGxlbmdlIjoiUTFoQlRFeEZUa2RGWDFKRlIwbFRSVlkiLCJvcmlnaW4iOiJodHRwczovL3RyYXZlbG1vbmsuY29tIn0",
    "attestationObject": "o2NmbXRkbm9uZWhhdXRoRGF0YVg..."
  },
  "clientExtensionResults": {}
}
```

---

# 3. Mock Authentication Challenge

Server → App

```json
{
  "challenge": "QVVUSEVOVElDQVRJT05fQ0hBTExFTkdF",
  "timeout": 60000,
  "rpId": "travelmonk.com",
  "allowCredentials": [
    {
      "id": "ZHVtbXlfY3JlZGVudGlhbF9pZA",
      "type": "public-key"
    }
  ],
  "userVerification": "preferred"
}
```

---

# 4. Mock Authentication Result

App → Server

Used only for:
- unit tests
- fake CredentialManager implementations
- ViewModel testing

```json
{
  "id": "ZHVtbXlfY3JlZGVudGlhbF9pZA",
  "rawId": "ZHVtbXlfY3JlZGVudGlhbF9pZA",
  "type": "public-key",
  "response": {
    "clientDataJSON": "eyJ0eXBlIjoid2ViYXV0aG4uZ2V0IiwiY2hhbGxlbmdlIjoiUVZWVVNFVk9WRWxEUVZSSlQwNWZRMGhCVExGRlRrZEYiLCJvcmlnaW4iOiJodHRwczovL3RyYXZlbG1vbmsuY29tIn0",
    "authenticatorData": "SZYN5YgOjGh0NBcPZHZgW4...",
    "signature": "MEUCIQDkfakeSignature123456",
    "userHandle": "dXNlcl8xMjM0NQ"
  },
  "clientExtensionResults": {}
}
```

---

# Android Credential Manager Mock Responses

## CreatePublicKeyCredentialResponse

```json
{
  "id": "test-passkey-id",
  "rawId": "dGVzdC1wYXNza2V5LWlk",
  "type": "public-key",
  "response": {
    "clientDataJSON": "eyJjaGFsbGVuZ2UiOiJ0ZXN0In0",
    "attestationObject": "dGVzdGF0dGVzdGF0aW9u"
  }
}
```

---

## GetPublicKeyCredentialResponse

```json
{
  "id": "test-passkey-id",
  "rawId": "dGVzdC1wYXNza2V5LWlk",
  "type": "public-key",
  "response": {
    "clientDataJSON": "eyJjaGFsbGVuZ2UiOiJ0ZXN0In0",
    "authenticatorData": "dGVzdGF1dGhkYXRh",
    "signature": "dGVzdHNpZ25hdHVyZQ",
    "userHandle": "dGVzdHVzZXI"
  }
}
```

---

# Notes

- These mocks are structurally valid WebAuthn payloads
- They are suitable for:
    - UI testing
    - app integration testing
    - fake backend flows
    - local development
- They are NOT cryptographically valid
- They should NOT be used for production backend verification