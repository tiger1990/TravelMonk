[//]: # ()
[//]: # (When working with compose, we should think of scalability, maintainability, and avoiding pain later)

[//]: # (Think in State → UI, not UI → State)

[//]: # (Follow Unidirectional Data Flow &#40;UDF&#41;)

[//]: # (Keep Composables Stateless by default, Push state up, keep UI dumb)

[//]: # (Use the right Effect API &#40;very important&#41;Use the right Effect API &#40;very important&#41;)

[//]: # (Use remember for expensive work)

[//]: # (Use derivedStateOf for computed state)

[//]: # (Avoid passing unstable objects)

[//]: # (State Hoisting &#40;Golden Rule&#41; : Move state up to the lowest common parent)

[//]: # (Don’t Put business logic in Composables)

[//]: # (Make UI Previewable & Testable)

[//]: # (Design for Stability : Compose relies on stable inputs)

[//]: # (Use:)

[//]: # (Immutable data classes)

[//]: # (@Stable or @Immutable when needed)

[//]: # ()
[//]: # (Handle Lifecycle properly:)

[//]: # (Use collectAsStateWithLifecycle&#40;&#41;)

[//]: # (Avoid leaking observers)

[//]: # (Tie work to composition)

[//]: # ()
[//]: # (Theming & Design System:)

[//]: # (Don’t hardcode colors/typography)
