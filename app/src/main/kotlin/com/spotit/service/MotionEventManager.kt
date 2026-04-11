/**
 * SPOTit Anti-Theft Application
 *
 * MotionEventManager.kt - Service-to-ViewModel Communication Bridge
 *
 * This file implements a singleton object that serves as a communication bridge
 * between the foreground service (SpotItForegroundService) and the ViewModel
 * (SpotItViewModel). When motion is detected by the service, it emits an event
 * through this manager, which the ViewModel collects to update the UI.
 *
 * Architecture Pattern:
 * - Uses Kotlin SharedFlow for event broadcasting
 * - Singleton pattern ensures single instance across app
 * - Decouples service from ViewModel (no direct reference needed)
 * - Follows reactive programming principles
 *
 * Communication Flow:
 * ```
 * MotionDetector -> SpotItForegroundService -> MotionEventManager -> SpotItViewModel -> UI
 * ```
 *
 * @author Panos Biazis
 * @date 10/4/2026
 * @version 2.0
 *
 * Copyright (c) 2026 Panos Biazis. All rights reserved.
 */

// Package declaration - defines the service layer package
// The service layer contains background services and their utilities
package com.spotit.service

// =============================================================================
// ANDROID FRAMEWORK IMPORTS
// =============================================================================

import android.util.Log // Android logging system for debugging

// =============================================================================
// KOTLIN COROUTINES IMPORTS
// =============================================================================

import kotlinx.coroutines.flow.MutableSharedFlow // Hot flow that can emit values
import kotlinx.coroutines.flow.SharedFlow // Read-only flow interface
import kotlinx.coroutines.flow.asSharedFlow // Converts MutableSharedFlow to SharedFlow

/**
 * MotionEventManager - Singleton Event Bus for Motion Detection
 *
 * This object implements the Singleton pattern to provide a centralized
 * communication channel between the foreground service and the ViewModel.
 * It uses Kotlin's SharedFlow to broadcast motion detection events.
 *
 * Why Singleton + SharedFlow:
 * - Singleton ensures single event bus instance across app lifetime
 * - SharedFlow allows multiple collectors (though we only have one here)
 * - Decouples components: Service doesn't need ViewModel reference
 * - Thread-safe: SharedFlow handles concurrent access
 * - Lifecycle-aware: Events can be collected with proper lifecycle handling
 *
 * Design Pattern: Observer/Pub-Sub
 * - Publisher: SpotItForegroundService (calls emitMotionEvent)
 * - Subscriber: SpotItViewModel (collects motionEvent flow)
 * - Event Bus: This MotionEventManager object
 *
 * Alternative Approaches Considered:
 * 1. LiveData: Not suitable because it's lifecycle-aware and would drop
 *    events when app is backgrounded (we need events even then)
 * 2. BroadcastReceiver: More overhead, requires Manifest registration,
 *    less type-safe
 * 3. Callback interface: Creates tight coupling, harder to test
 * 4. EventBus library: Additional dependency, SharedFlow is built-in
 *
 * SharedFlow Configuration:
 * - replay = 1: New collectors receive the most recent event
 *   This ensures no events are missed if collection starts after emission
 * - extraBufferCapacity = 1: Additional buffer for emission
 *   This prevents emission blocking when collector is slow
 *
 * Thread Safety:
 * - All methods are thread-safe
 * - SharedFlow uses internal synchronization
 * - tryEmit is non-suspending and thread-safe
 *
 * Usage Flow:
 * 1. Service detects motion via MotionDetector
 * 2. Service calls MotionEventManager.emitMotionEvent()
 * 3. SharedFlow emits Unit signal
 * 4. ViewModel's collection in init block receives the event
 * 5. ViewModel calls movAlert() to update UI
 *
 * @see SpotItForegroundService.onMotionDetected - Publisher
 * @see SpotItViewModel.init - Subscriber
 */
object MotionEventManager {

    // =========================================================================
    // CONSTANTS - Logging and Debugging
    // =========================================================================

    /**
     * Logging tag for this class.
     *
     * Used with Log.d(), Log.e(), etc. to filter logcat output.
     * Format: "MotionEventManager" for easy identification in logs.
     */
    private const val TAG = "MotionEventManager"

    // =========================================================================
    // SHARED FLOW - Event Broadcasting Channel
    // =========================================================================

    /**
     * Mutable backing flow for emitting motion events.
     *
     * MutableSharedFlow is used instead of StateFlow because:
     * - We don't need a specific state, just a signal (Unit)
     * - Events are consumed, not stored indefinitely
     * - replay = 1 ensures late subscribers get latest event
     *
     * Configuration Parameters:
     *
     * @param replay = 1
     *   Number of values to replay to new collectors.
     *   - 0: New collectors don't receive any past events (may miss events)
     *   - 1: New collectors receive the most recent event (recommended)
     *   - Higher: More history, but increased memory usage
     *
     *   Why replay = 1:
     *   - If ViewModel is recreated, it gets the last event
     *   - If collection starts slightly after emission, event isn't lost
     *   - Prevents race conditions between service and ViewModel startup
     *
     * @param extraBufferCapacity = 1
     *   Additional buffer capacity beyond replay.
     *   - 0: Only replay buffer available (may suspend/block emitter)
     *   - 1: One additional slot for new emissions (recommended)
     *   - Higher: More buffer, but increased memory usage
     *
     *   Why extraBufferCapacity = 1:
     *   - Prevents blocking when emitter is faster than collector
     *   - Allows non-suspending emission via tryEmit()
     *   - Provides slack for temporary slow collection
     *
     * Buffer Behavior:
     * Total buffer = replay + extraBufferCapacity = 2
     * - Slot 1: Replayed to new collectors
     * - Slot 2: Available for new emissions
     *
     * Emission Strategies:
     * - emit(): Suspending, waits if buffer full (we don't use this)
     * - tryEmit(): Non-suspending, returns false if buffer full (we use this)
     */
    private val _motionEvent = MutableSharedFlow<Unit>(
        replay = 1,           // New collectors get the most recent event
        extraBufferCapacity = 1 // Additional buffer for non-blocking emission
    )

    /**
     * Read-only SharedFlow exposed to collectors.
     *
     * This exposes the motion event flow as a read-only SharedFlow,
     * preventing external code from emitting events. Only this
     * MotionEventManager can emit through _motionEvent.
     *
     * The asSharedFlow() extension:
     * - Wraps MutableSharedFlow in a read-only interface
     * - Blocks access to emit/tryEmit methods
     * - Provides same collection semantics
     *
     * Collectors receive Unit (empty signal) because:
     * - We only need to signal that motion occurred
     * - No additional data is needed (location is in ViewModel)
     * - Simpler than creating a custom event class
     * - Unit is the most lightweight type possible
     *
     * Collection Example (in ViewModel):
     * ```kotlin
     * viewModelScope.launch {
     *     MotionEventManager.motionEvent.collect {
     *         // Handle motion event
     *         movAlert()
     *     }
     * }
     * ```
     *
     * @return SharedFlow<Unit> A read-only flow that emits Unit
     *         whenever motion is detected.
     */
    val motionEvent: SharedFlow<Unit> = _motionEvent.asSharedFlow()

    // =========================================================================
    // PUBLIC API - Event Emission Method
    // =========================================================================

    /**
     * Emits a motion event to all collectors.
     *
     * This method is called by SpotItForegroundService when motion is
     * detected by the MotionDetector. It broadcasts a Unit signal to
     * all active collectors (the ViewModel).
     *
     * Implementation Details:
     * - Uses tryEmit() instead of emit() for non-blocking behavior
     * - Returns Boolean indicating success/failure
     * - Logs emission for debugging purposes
     *
     * Why tryEmit instead of emit:
     * - emit() is suspending and could block the service
     * - tryEmit() is non-suspending and safe to call from any thread
     * - Returns false only if buffer is full (unlikely with our config)
     * - Service should not be blocked by event emission
     *
     * Return Value:
     * - true: Event was successfully emitted (normal case)
     * - false: Buffer was full, event was dropped (should not happen)
     *
     * False return handling:
     * With replay=1 and extraBufferCapacity=1, we have 2 buffer slots.
     * This should be sufficient for our single collector. A false
     * return would indicate a serious issue:
     * - Collector is extremely slow
     * - Multiple collectors added unexpectedly
     * - System under heavy load
     *
     * In practice, if this returns false, the event is lost. However,
     * this is acceptable because:
     * - Another motion event will likely occur soon
     * - The alarm is already triggered by this point
     * - Missing one event doesn't compromise security
     *
     * Thread Safety:
     * - Can be called from any thread
     * - Internally synchronized by SharedFlow
     * - Safe to call from service's background thread
     *
     * Debugging:
     * - Logs subscriber count for visibility
     * - Logs emission result for troubleshooting
     * - Use Logcat filter: "MotionEventManager" to see logs
     *
     * @return Boolean true if the event was emitted successfully,
     *         false if the buffer was full and event was dropped.
     *
     * @see SpotItForegroundService.onMotionDetected - Calls this method
     * @see SpotItViewModel.init - Collects events from motionEvent
     */
    fun emitMotionEvent(): Boolean {
        // Log the emission attempt with subscriber count
        // subscriptionCount.value gives the current number of active collectors
        // This helps debug whether the ViewModel is collecting
        Log.d(TAG, "emitMotionEvent called, subscribers: ${_motionEvent.subscriptionCount.value}")

        // Attempt to emit the event
        // tryEmit is non-suspending and returns immediately
        // Unit.INSTANCE (Kotlin Unit) is the signal value
        val result = _motionEvent.tryEmit(Unit)

        // Log the result for debugging
        // true: Event is now in buffer, will be delivered to collectors
        // false: Buffer was full, event was dropped
        Log.d(TAG, "emitMotionEvent result: $result")

        // Return the result to caller
        // Caller can decide whether to retry or log error
        return result
    }
}

/**
 * Additional Architecture Notes:
 *
 * Singleton Pattern Implementation:
 * - Kotlin 'object' declaration creates a singleton
 * - Single instance initialized on first access
 * - Thread-safe initialization (JVM guarantees)
 * - No need for manual synchronization
 *
 * Memory Management:
 * - SharedFlow holds no strong references to collectors
 * - Collectors are tracked weakly by subscriptionCount
 * - No memory leaks from abandoned collectors
 * - GC can collect stopped coroutines
 *
 * Lifecycle Considerations:
 * - SharedFlow is "hot" - always active regardless of collectors
 * - Events are emitted even with no collectors (buffered)
 * - New collectors receive replay buffer on start
 * - Stopped collectors automatically unsubscribe
 *
 * Testing:
 * - Can be mocked for unit tests
 * - Can create test instance by using reflection
 * - Events can be emitted in tests to verify ViewModel behavior
 *
 * Future Improvements:
 * 1. Add event type instead of Unit for extensibility:
 *    sealed class MotionEvent { object Detected : MotionEvent() }
 * 2. Add timestamp to events for debugging
 * 3. Add emission statistics for monitoring
 * 4. Consider using a Channel for single-consumer scenarios
 */
