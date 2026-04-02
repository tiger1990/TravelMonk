/**
* UseCase:
* Register/unregister listeners
* Subscribe/unsubscribe to streams
* Add/remove observers
* Attach/detach callbacks
* Work with external APIs that need cleanup
*
* DisposableEffect(lifecycleOwner) {
*     val observer = LifecycleEventObserver { _, event ->
*         // handle lifecycle events
*     }
*
*     lifecycleOwner.lifecycle.addObserver(observer)
*
*     onDispose {
*         lifecycleOwner.lifecycle.removeObserver(observer)
*     }
* }
* Whenever navigationState changes(key should represent what the effect depends on),
* the DisposableEffect block will be executed (restart the side effect).
* But before restarting, clean up the old one.
*/
DisposableEffect(navigationState) {
globalNavigator.bind(navigationState)
onDispose { globalNavigator.unbind() }
}