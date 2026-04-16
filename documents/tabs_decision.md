
Bottom Bar with Nested Navigation: Each tab maintains its own isolated back stack, 
providing a deeply nested experience ideal for many common app flows.
Bottom Bar with Separate Navigation: Detail screens exist on a separate, 
global back stack, allowing them to overlay the entire tabbed interface and providing 
a different back navigation behavior.

Tab Preservations via Decorators
Navigation 3.0 uses Decorators to solve the problem of "losing place" when switching tabs. 
Even if you swap out the UI, the state of the non-active tabs must be cached.

rememberSavedStateNavEntryDecorator: This is critical for your use case. 
When you switch from Tab A (at A2) to Tab B, this decorator saves the "User Interface State" 
(scroll positions, text field inputs) of A2. When you return to Tab A, it restores exactly where the user left off.

rememberViewModelStoreNavEntryDecorator: Use this to ensure each screen (A1, A2, etc.) 
keeps its own ViewModel instance alive as long as it remains in that tab's backstack.

Handling Secondary Activities (A2 -> New Activity)
For your requirement of launching a new Activity from the 2nd level (like A2):
Decoupled Launching: Since Navigation 3.0 treats the backstack as a simple list, 
your "Navigator" or "Coordinator" should handle the intent logic separately from the Compose state.
State Coordination: When the new Activity is launched, the "Parent" Activity's state 
(including the tabs' back-stacks) is automatically saved by the OS. When the user returns, 
the rememberNavBackStack in the original Activity will restore the user to A2 automatically.

// Exit Through Home Flow
// Rule 1: Pop nested screens if they exist
if (stack.size > 1) {
stack.removeLastOrNull()
}
// Rule 2: If at root of secondary tab, jump to Primary Tab
if (currentTab != BottomBarItem.Home) {
currentTab = BottomBarItem.Home
}
// Rule 3: At root of Primary Tab - return false to let system exit app