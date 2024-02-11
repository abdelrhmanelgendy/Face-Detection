package net.gamal.faceapprecon.utilities.viewModel

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Object that will subscribes to a MviView's [ViewAction]s,
 * process it and emit a [ViewState] back.
 *
 * @param Action Top class of the [ViewAction] that the [MviViewModel] will be subscribing to.
 * @param S Top class of the [ViewState] the [MviViewModel] will be emitting.
 * @param Event Top class of the [ViewEvent] that the [MviViewModel] will be emitting.
 */
interface MviViewModel<Action : ViewAction, Event : ViewEvent, S : ViewState> {

    val viewState: MutableStateFlow<S>

    val singleEvent: Flow<Event>

    /**
     * Must be called in [kotlinx.coroutines.Dispatchers.Main.immediate],
     * otherwise it will throw an exception.
     *
     * If you want to process an intent from other [kotlinx.coroutines.CoroutineDispatcher],
     * use `withContext(Dispatchers.Main.immediate) { processIntent(intent) }`.
     */
    fun processIntent(action: Action)
}