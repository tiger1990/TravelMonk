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