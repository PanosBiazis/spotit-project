/**
 * SPOTit Anti-Theft Application
 *
 * MotionDetector.kt - Accelerometer-Based Motion Detection
 *
 * This file implements the motion detection functionality using the device's
 * accelerometer sensor. It listens for motion events and triggers a callback
 * when movement exceeds a configurable threshold.
 *
 * Key Features:
 * - Uses LINEAR_ACCELERATION sensor when available (excludes gravity)
 * - Falls back to standard ACCELEROMETER sensor on older devices
 * - Configurable sensitivity threshold via lambda function
 * - Cooldown period to prevent rapid re-triggering
 * - Thread-safe callback invocation
 *
 * @author Panos Biazis
 * @date 10/4/2026
 * @version 2.0
 *
 * Copyright (c) 2026 Panos Biazis. All rights reserved.
 */

// Package declaration - defines the utility layer package
// The util package contains helper classes and utilities
package com.spotit.util

// =============================================================================
// ANDROID FRAMEWORK IMPORTS
// =============================================================================

// Context import for accessing system services
import android.content.Context // Android context

// Sensor-related imports for accelerometer access
import android.hardware.Sensor // Represents a sensor
import android.hardware.SensorEvent // Sensor data event
import android.hardware.SensorEventListener // Callback interface for sensor events
import android.hardware.SensorManager // System sensor service

// Logging utility for debugging
import android.util.Log // Android logging system

// =============================================================================
// KOTLIN MATH IMPORTS
// =============================================================================

import kotlin.math.abs // Absolute value function
import kotlin.math.sqrt // Square root for magnitude calculation

/**
 * MotionDetector - Accelerometer-Based Motion Detection
 *
 * This class wraps the Android Sensor framework to provide simple motion
 * detection functionality. It registers for accelerometer events and
 * invokes a callback when motion above a threshold is detected.
 *
 * Sensor Selection Strategy:
 * - Primary: TYPE_LINEAR_ACCELERATION (API 9+)
 *   - Reports acceleration excluding gravity
 *   - Values are in m/s², representing actual movement
 *   - Ideal for motion detection as gravity is already filtered
 *   - Typical values: 0.1-1.5 m/s² for normal movement
 *
 * - Fallback: TYPE_ACCELEROMETER (all devices)
 *   - Reports raw acceleration including gravity (~9.8 m/s²)
 *   - Requires manual gravity subtraction for accurate detection
 *   - Used only when LINEAR_ACCELERATION is unavailable
 *
 * Detection Algorithm:
 * 1. Read x, y, z acceleration values from sensor
 * 2. Calculate magnitude: sqrt(x² + y² + z²)
 * 3. For ACCELEROMETER: subtract gravity (9.8 m/s²)
 * 4. Compare against threshold value
 * 5. If exceeded AND cooldown passed: trigger callback
 *
 * Threshold Configuration:
 * The threshold is provided as a lambda function () -> Float rather than
 * a fixed value. This allows:
 * - Dynamic sensitivity adjustment without recreating detector
 * - Settings changes take effect immediately
 * - Threshold can vary based on conditions
 *
 * Cooldown Mechanism:
 * A cooldown period prevents multiple callbacks for the same motion event.
 * Without cooldown, continuous motion would trigger hundreds of alerts.
 * The cooldown is implemented as a minimum time between callbacks.
 *
 * Thread Safety:
 * - Sensor events are delivered on a dedicated sensor thread
 * - Callback is invoked on the sensor thread (not main/UI thread)
 * - All state updates are on the sensor thread, ensuring thread safety
 * - No synchronization needed as single-threaded event delivery
 *
 * Memory Management:
 * - Must call stop() to unregister listener when done
 * - Failing to stop will leak the sensor listener
 * - Service handles cleanup in stopSelfSafely()
 *
 * @property context The Android context for accessing SensorManager
 * @property threshold Lambda function returning the current sensitivity threshold
 * @property onMotion Callback invoked when motion is detected
 *
 * @see SpotItForegroundService - Creates and manages MotionDetector instance
 */
class MotionDetector(
    context: Context,
    private val threshold: () -> Float,
    private val onMotion: () -> Unit
) : SensorEventListener {

    // =========================================================================
    // COMPANION OBJECT - Constants and Static Members
    // =========================================================================

    /**
     * Companion object containing static constants.
     *
     * These constants define the behavior of the motion detector
     * and provide logging utilities.
     */
    companion object {
        /**
         * Logging tag for this class.
         *
         * Used with Log.d(), Log.e(), etc. to filter logcat output.
         * Format: "MotionDetector" for easy identification in logs.
         */
        private const val TAG = "MotionDetector"

        /**
         * Cooldown period in milliseconds between motion detections.
         *
         * This prevents multiple callbacks for the same motion event.
         * After a motion is detected, subsequent motion events are
         * ignored until this cooldown period has elapsed.
         *
         * Value: 1500ms (1.5 seconds)
         * - Reduced from 2500ms for faster response
         * - Balance between responsiveness and false positives
         * - Too short: Many alerts for single motion
         * - Too long: May miss significant movement
         *
         * When to adjust:
         * - Increase if getting too many alerts
         * - Decrease if missing motion events
         */
        private const val COOLDOWN_MS = 1500L // Reduced from 2500ms for faster response
    }

    // =========================================================================
    // SENSOR MANAGER AND SENSOR - Hardware Access
    // =========================================================================

    /**
     * SensorManager instance for accessing device sensors.
     *
     * Obtained from the system service SENSOR_SERVICE. The SensorManager
     * provides access to all device sensors and handles sensor registration.
     *
     * This is created once in the constructor and reused for all operations.
     */
    private val manager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    /**
     * The sensor used for motion detection.
     *
     * Selection priority:
     * 1. TYPE_LINEAR_ACCELERATION: Preferred, excludes gravity
     * 2. TYPE_ACCELEROMETER: Fallback, includes gravity
     *
     * May be null on devices without an accelerometer (extremely rare).
     * All modern Android devices have accelerometers.
     *
     * The sensor object contains metadata about the hardware:
     * - name: Sensor name (e.g., "LSM6DSO Accelerometer")
     * - type: Sensor type constant
     * - resolution: Minimum detectable change
     * - maxRange: Maximum measurable value
     */
    private val sensor = manager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION)
        ?: manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)

    // =========================================================================
    // STATE TRACKING - Timestamps for Cooldown
    // =========================================================================

    /**
     * Timestamp of the last motion detection callback.
     *
     * Used to implement the cooldown mechanism. When motion is detected,
     * this is updated to the current time. Subsequent detections are
     * ignored until COOLDOWN_MS has elapsed.
     *
     * Initial value: 0 (no previous detection)
     * Updated: On each motion detection callback
     * Compared: With current time on each sensor event
     */
    private var lastTrigger = 0L

    /**
     * Timestamp of the last log output for sensor values.
     *
     * To avoid flooding logcat with sensor readings, we log values
     * periodically (every ~500ms) rather than on every event.
     * This timestamp tracks when the last log was made.
     *
     * Initial value: 0 (no previous log)
     * Updated: On each log output
     * Compared: With current time to throttle logs
     */
    private var lastLogTime = 0L

    // =========================================================================
    // PUBLIC API - Start and Stop Methods
    // =========================================================================

    /**
     * Starts listening for motion events.
     *
     * This method registers this class as a listener for the accelerometer
     * sensor. After calling this method, sensor events will be delivered
     * to onSensorChanged().
     *
     * Registration Details:
     * - Sensor: Linear acceleration (preferred) or standard accelerometer
     * - Rate: SENSOR_DELAY_UI (suitable for user interface updates)
     * - No handler specified: Events delivered on sensor thread
     *
     * Sensor Delay Options:
     * - SENSOR_DELAY_FASTEST (0μs): Maximum speed, high battery usage
     * - SENSOR_DELAY_GAME (20ms): Suitable for games
     * - SENSOR_DELAY_UI (16ms): Balanced, good for UI updates (chosen)
     * - SENSOR_DELAY_NORMAL (200ms): Low power, suitable for monitoring
     *
     * Why SENSOR_DELAY_UI:
     * - Fast enough to detect motion quickly
     * - Not so fast as to drain battery excessively
     * - Good balance for anti-theft use case
     * - Cooldown prevents too-frequent alerts anyway
     *
     * Thread Considerations:
     * - Events are delivered on a dedicated sensor thread
     * - Not the main/UI thread (safe for blocking operations)
     * - Callback will be invoked on this sensor thread
     *
     * Logging:
     * - Logs sensor name and type for debugging
     * - Logs registration success/failure
     * - Use Logcat filter: "MotionDetector"
     */
    fun start() {
        // Log the sensor being used for debugging
        Log.d(TAG, "start() called, sensor: ${sensor?.name}, type=${sensor?.type}")

        // Register for sensor events if sensor is available
        sensor?.let {
            // Register this class as the listener for sensor events
            // Parameters:
            // 1. this: SensorEventListener implementation (this class)
            // 2. it: The sensor to listen to
            // 3. SensorManager.SENSOR_DELAY_UI: Sampling rate
            val registered = manager.registerListener(this, it, SensorManager.SENSOR_DELAY_UI)

            // Log registration result
            Log.d(TAG, "Sensor listener registered: $registered")
        } ?: Log.e(TAG, "No sensor available!")
        // This should never happen on modern devices
        // All Android devices are required to have an accelerometer
    }

    /**
     * Stops listening for motion events.
     *
     * This method unregisters this class from the SensorManager, stopping
     * all sensor event delivery. This MUST be called when motion detection
     * is no longer needed to prevent battery drain and memory leaks.
     *
     * When to call:
     * - When stopping the monitoring service
     * - When the app is being destroyed
     * - When pausing detection temporarily
     *
     * Memory Leak Prevention:
     * If this method is not called, the SensorManager holds a reference
     * to this listener, preventing garbage collection and wasting battery
     * on unnecessary sensor polling.
     *
     * Safe to call multiple times:
     * The SensorManager handles duplicate unregister calls gracefully.
     */
    fun stop() {
        Log.d(TAG, "stop() called")

        // Unregister this listener from all sensors
        // This stops event delivery immediately
        manager.unregisterListener(this)
    }

    // =========================================================================
    // SENSOR EVENT CALLBACK - Core Detection Logic
    // =========================================================================

    /**
     * Called when sensor values change.
     *
     * This is the core detection method, invoked by the SensorManager
     * whenever new accelerometer data is available. The frequency depends
     * on the sensor delay specified in start() (SENSOR_DELAY_UI = ~60Hz).
     *
     * Event Processing:
     * 1. Extract x, y, z acceleration values from event
     * 2. Calculate magnitude of acceleration vector
     * 3. For ACCELEROMETER: Subtract gravity to get actual movement
     * 4. Compare against threshold
     * 5. If exceeded and cooldown passed: Trigger callback
     *
     * SensorEvent Structure:
     * - event.values[0]: X-axis acceleration
     * - event.values[1]: Y-axis acceleration
     * - event.values[2]: Z-axis acceleration
     * - event.timestamp: Sensor timestamp in nanoseconds
     * - event.accuracy: Sensor accuracy (low/medium/high)
     * - event.sensor: Reference to the sensor
     *
     * Coordinate System:
     * - X: Horizontal (positive toward right edge)
     * - Y: Vertical (positive toward top edge)
     * - Z: Depth (positive toward user)
     *
     * Units:
     * - LINEAR_ACCELERATION: m/s² (excludes gravity)
     * - ACCELEROMETER: m/s² (includes gravity)
     *
     * Thread: This method runs on the sensor thread, not the main thread.
     *
     * @param event The sensor event containing acceleration values
     */
    override fun onSensorChanged(event: SensorEvent) {
        // ================================================================
        // EXTRACT ACCELERATION VALUES
        // ================================================================

        // Get the three acceleration components from the event
        // These represent the device's acceleration along each axis
        val x = event.values[0] // X-axis: Left/right acceleration
        val y = event.values[1] // Y-axis: Up/down acceleration
        val z = event.values[2] // Z-axis: Forward/backward acceleration

        // ================================================================
        // CALCULATE MAGNITUDE
        // ================================================================

        // Calculate the magnitude of the acceleration vector
        // This gives us a single value representing total acceleration
        // Formula: |a| = sqrt(x² + y² + z²)
        //
        // For LINEAR_ACCELERATION: This is the actual movement magnitude
        // For ACCELEROMETER: This includes gravity (~9.8 m/s²)
        val magnitude = sqrt((x * x + y * y + z * z).toDouble()).toFloat()

        // ================================================================
        // CALCULATE DELTA (MOTION AMOUNT)
        // ================================================================

        // The detection calculation differs based on sensor type:
        //
        // LINEAR_ACCELERATION (preferred):
        // - Already excludes gravity
        // - magnitude directly represents movement
        // - Typical values: 0.1-1.5 m/s² for normal movement
        //
        // ACCELEROMETER (fallback):
        // - Includes gravity (~9.8 m/s²)
        // - Must subtract gravity to get actual movement
        // - Uses absolute difference to handle orientation
        val delta = if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            // For accelerometer: Compare against Earth's gravity
            // GRAVITY_EARTH is approximately 9.80665 m/s²
            // The absolute value handles all device orientations
            abs(magnitude - SensorManager.GRAVITY_EARTH)
        } else {
            // For linear acceleration: Use magnitude directly
            // Gravity is already filtered out by the sensor
            magnitude
        }

        // ================================================================
        // GET CURRENT THRESHOLD
        // ================================================================

        // Invoke the threshold lambda to get current sensitivity
        // This is a lambda to allow dynamic adjustment without recreating detector
        // The sensitivity can be changed in settings and takes effect immediately
        val currentThreshold = threshold()

        // ================================================================
        // THROTTLED LOGGING
        // ================================================================

        // Get current time for cooldown and logging checks
        val now = System.currentTimeMillis()

        // Log sensor values periodically for debugging
        // We throttle logging to avoid flooding logcat
        // Only log every ~500ms to balance visibility and noise
        if (now - lastLogTime > 500) {
            Log.d(
                TAG,
                "Sensor: x=$x, y=$y, z=$z, magnitude=$magnitude, delta=$delta, threshold=$currentThreshold"
            )
            lastLogTime = now
        }

        // ================================================================
        // MOTION DETECTION
        // ================================================================

        // Check if motion should trigger an alert:
        // 1. delta > currentThreshold: Motion exceeds sensitivity setting
        // 2. now - lastTrigger > COOLDOWN_MS: Cooldown period has elapsed
        //
        // The cooldown prevents multiple callbacks for continuous motion
        // Without it, picking up the phone would trigger dozens of alerts
        if (delta > currentThreshold && now - lastTrigger > COOLDOWN_MS) {
            // Log the detection for debugging
            Log.d(TAG, "Motion detected! delta=$delta > threshold=$currentThreshold")

            // Update the last trigger time to implement cooldown
            lastTrigger = now

            // Invoke the callback to notify the service
            // This runs on the sensor thread, not the main thread
            onMotion()
        }
        // If conditions not met: No motion detected, continue listening
    }

    // =========================================================================
    // ACCURACY CHANGE CALLBACK - Unused but Required
    // =========================================================================

    /**
     * Called when sensor accuracy changes.
     *
     * This method is required by the SensorEventListener interface but
     * is not used in this implementation. Accuracy changes are logged
     * but do not affect motion detection behavior.
     *
     * Accuracy Values:
     * - SensorManager.SENSOR_STATUS_NO_CONTACT (-1): No contact
     * - SensorManager.SENSOR_STATUS_UNRELIABLE (0): Unreliable
     * - SensorManager.SENSOR_STATUS_ACCURACY_LOW (1): Low accuracy
     * - SensorManager.SENSOR_STATUS_ACCURACY_MEDIUM (2): Medium accuracy
     * - SensorManager.SENSOR_STATUS_ACCURACY_HIGH (3): High accuracy
     *
     * For motion detection, accuracy changes are typically not critical.
     * The sensor continues to provide useful data even at lower accuracy.
     *
     * @param sensor The sensor whose accuracy changed
     * @param accuracy The new accuracy value
     */
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // No action needed for accuracy changes
        // The sensor continues to function regardless of accuracy
        // We could log this for debugging if needed:
        // Log.d(TAG, "Sensor ${sensor?.name} accuracy changed to $accuracy")
    }
}

/**
 * Additional Architecture Notes:
 *
 * Sensor Selection Rationale:
 * - LINEAR_ACCELERATION is preferred because it automatically filters
 *   gravity, making detection more straightforward
 * - The fallback to ACCELEROMETER ensures compatibility with all devices
 * - Most modern devices support LINEAR_ACCELERATION
 *
 * Alternative Detection Algorithms:
 * 1. High-pass filter: More sophisticated gravity filtering
 * 2. Orientation-independent detection: Same threshold in any orientation
 * 3. Machine learning: Could reduce false positives (heavyweight)
 * 4. Multi-sample averaging: Smooths noise, slower detection
 *
 * Current Algorithm Pros:
 * - Simple and efficient
 * - Works in any device orientation
 * - Adjustable sensitivity
 * - Good balance of responsiveness and false positives
 *
 * Current Algorithm Cons:
 * - May miss slow, careful movement
 * - Threshold requires tuning per device
 * - No distinction between device types (phone vs tablet)
 *
 * Future Improvements:
 * 1. Adaptive threshold based on initial calibration
 * 2. Multiple sensitivity profiles (home, car, travel)
 * 3. Orientation-aware detection
 * 4. Pattern recognition for common movements (pocket, bag)
 */
