package net.gamal.faceapprecon.utilities.viewModel

import androidx.annotation.CallSuper
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

abstract class BaseViewModel<A : ViewAction, E : ViewEvent, S : ViewState>(initialState: S) :
    MviViewModel<A, E, S>, ViewModel() {

    protected val mutableState: MutableStateFlow<S> = MutableStateFlow(initialState)

    override val viewState: MutableStateFlow<S>
        get() = mutableState

    val oldViewState: S
        get() = mutableState.value

    private val eventChannel = Channel<E>(Channel.UNLIMITED)
    final override val singleEvent: Flow<E> = eventChannel.receiveAsFlow()

    private val _actionMutableFlow = MutableSharedFlow<A>(extraBufferCapacity = Int.MAX_VALUE)

    final override fun processIntent(action: A) {
        check(_actionMutableFlow.tryEmit(action)) { "Failed to emit action: $action" }
    }

    // Send event and access action flow.

    /**
     * Must be called in [kotlinx.coroutines.Dispatchers.Main],
     * otherwise it will throw an exception.
     *
     * If you want to send an event from other [kotlinx.coroutines.CoroutineDispatcher],
     * use `withContext(Dispatchers.Main.immediate) { sendEvent(event) }`.
     */
    protected fun sendEvent(event: E) {
        println("Current event: ${event}")
        eventChannel.trySend(event)
    }


    fun setState(newState: S) {
        mutableState.value = newState
        println("Current state: ${viewState.value}")
    }

    abstract fun clearState()

    private val actionSharedFlow: SharedFlow<A> get() = _actionMutableFlow

    internal fun <L : SharedFlow<A>> observe(sharedFlow: L, body: (A) -> Unit) {
        viewModelScope.launch {
            sharedFlow.collect {
                body(it)
            }
        }
    }

    abstract fun onActionTrigger(action: ViewAction?)

    init {
        viewModelScope.launch {
            actionSharedFlow.collect {
                onActionTrigger(it)
            }
        }
    }

    @CallSuper
    override fun onCleared() {
        super.onCleared()
        eventChannel.close()
    }
}