/**
 * SPOTit Anti-Theft Application
 *
 * SpotItForegroundService.kt - Background Monitoring Service
 *
 * This file implements the foreground service that performs the core anti-theft
 * monitoring functionality. It runs continuously in the background, listening
 * for motion events from the accelerometer sensor and triggering alerts when
 * suspicious movement is detected.
 *
 * Key Responsibilities:
 * - Motion detection via accelerometer sensor
 * - Location tracking for alert messages
 * - SMS alert sending to emergency contact
 * - Alarm sound and vibration playback
 * - Persistent notification for foreground service requirement
 *
 * @author Panos Biazis
 * @date 10/4/2026
 * @version 2.0
 *
 * Copyright (c) 2026 Panos Biazis. All rights reserved.
 */

// Package declaration - defines the service layer package
package com.spotit.service

// =============================================================================
// ANDROID FRAMEWORK IMPORTS
// =============================================================================

// Notification-related imports for foreground service notification
import android.app.NotificationChannel // API 26+ notification channels
import android.app.NotificationManager // System notification service
import android.app.PendingIntent // Intent wrapper for notifications
import android.app.Service // Base class for Android services

// Context and Intent for service operations
import android.content.Context // Android context for system services
import android.content.Intent // Intent for starting/stopping service

// Audio and vibration imports for alarm functionality
import android.media.AudioManager // Audio volume control
import android.media.Ringtone // Alarm sound playback
import android.media.RingtoneManager // System ringtone access

// Android OS imports
import android.os.Build // API version checking
import android.os.IBinder // Service binding interface
import android.os.VibrationEffect // Modern vibration patterns
import android.os.Vibrator // Vibration hardware access
import android.os.VibratorManager // API 31+ vibrator manager

// =============================================================================
// ANDROIDX IMPORTS
// =============================================================================

import androidx.core.app.NotificationCompat // Notification builder
import androidx.core.content.ContextCompat // Context utilities

// =============================================================================
// APPLICATION-SPECIFIC IMPORTS
// =============================================================================

import com.spotit.MainActivity // Main activity for notification intent
import com.spotit.data.AppSettings // Settings data class
import com.spotit.data.SettingsStore // Settings persistence

// Utility imports
import com.spotit.util.LocationHelper // GPS location access
import com.spotit.util.MotionDetector // Accelerometer-based motion detection
import com.spotit.util.SmsHelper // SMS message sending

// =============================================================================
// KOTLIN COROUTINES IMPORTS
// =============================================================================

import kotlinx.coroutines.CoroutineScope // Coroutine scope for structured concurrency
import kotlinx.coroutines.Dispatchers // Coroutine dispatchers (IO, Main, etc.)
import kotlinx.coroutines.Job // Coroutine job for cancellation
import kotlinx.coroutines.SupervisorJob // Job that doesn't cancel children on failure
import kotlinx.coroutines.delay // Non-blocking delay
import kotlinx.coroutines.flow.first // Terminal flow operator
import kotlinx.coroutines.isActive // Coroutine active state check
import kotlinx.coroutines.launch // Launches a new coroutine

/**
 * SpotItForegroundService - The Core Monitoring Service
 *
 * This service implements the primary anti-theft monitoring functionality.
 * It runs as a foreground service to ensure it continues running even when
 * the app is backgrounded or the screen is off.
 *
 * Foreground Service Requirements:
 * - Must show a persistent notification (Android requirement since API 26)
 * - Must declare foregroundServiceType in manifest (Android 14+)
 * - Provides higher priority than background services
 * - Less likely to be killed by the system
 *
 * Service Lifecycle:
 * 1. onCreate(): Initialize motion detector and location helper
 * 2. onStartCommand(): Start monitoring or handle stop request
 * 3. onMotionDetected(): Triggered by MotionDetector callback
 * 4. stopSelfSafely(): Clean shutdown when monitoring stops
 *
 * Monitoring Flow:
 * 1. Service starts via start() static method
 * 2. MotionDetector registers for accelerometer events
 * 3. When motion detected above threshold:
 *    a. Emit event to ViewModel via MotionEventManager
 *    b. Start alarm sound and vibration
 *    c. Send SMS alerts every 5 seconds
 *    d. Update notification to show alarm state
 * 4. Service stops via stop() static method
 *    a. Motion detector unregistered
 *    b. Alarm stopped
 *    c. SMS loop cancelled
 *    d. Foreground notification removed
 *
 * Thread Safety:
 * - Motion detection runs on sensor thread (not main)
 * - Coroutines run on IO dispatcher for background work
 * - UI updates posted to main thread
 *
 * Battery Optimization:
 * - Uses SENSOR_DELAY_UI (not fastest) for balance
 * - Cooldown period prevents rapid re-triggering
 * - Location cached, not constantly requested
 *
 * @see MotionDetector - Detects motion via accelerometer
 * @see MotionEventManager - Broadcasts motion events to ViewModel
 * @see LocationHelper - Provides GPS coordinates for alerts
 * @see SmsHelper - Sends SMS alerts to emergency contact
 */
class SpotItForegroundService : Service() {

    // =========================================================================
    // COROUTINE SCOPE - Background Task Management
    // =========================================================================

    /**
     * Coroutine scope for background operations.
     *
     * Uses SupervisorJob to ensure:
     * - Child coroutine failures don't cancel siblings
     * - SMS job failure doesn't stop alarm playback
     * - Individual coroutines can be cancelled independently
     *
     * Uses Dispatchers.IO because:
     * - Location operations may block
     * - SMS sending is I/O operation
     * - Settings reading from DataStore is I/O
     * - We want to avoid blocking main thread
     *
     * This scope is NOT tied to service lifecycle automatically.
     * Jobs must be cancelled in stopSelfSafely() to prevent leaks.
     */
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // =========================================================================
    // MOTION DETECTION - Accelerometer-based Detection
    // =========================================================================

    /**
     * Motion detector instance for accelerometer-based detection.
     *
     * Initialized in onCreate() with:
     * - Context for accessing sensor service
     * - threshold lambda that returns current sensitivity setting
     * - onMotion callback that triggers alert
     *
     * The threshold is a lambda (not a fixed value) because:
     * - Sensitivity can be changed while monitoring is active
     * - Lambda is evaluated on each detection
     * - No need to recreate detector when settings change
     */
    private lateinit var detector: MotionDetector

    // =========================================================================
    // LOCATION HELPER - GPS Coordinates for Alerts
    // =========================================================================

    /**
     * Location helper for retrieving GPS coordinates.
     *
     * Provides the current device location for inclusion in SMS alerts.
     * Location is retrieved when an alarm is triggered, not continuously,
     * to save battery.
     *
     * Initialized in onCreate() with the service context.
     */
    private lateinit var locationHelper: LocationHelper

    // =========================================================================
    // ALARM STATE - Sound and Vibration Control
    // =========================================================================

    /**
     * Reference to the currently playing alarm ringtone.
     *
     * Kept as a member variable to:
     * - Stop the ringtone when alarm is cancelled
     * - Prevent duplicate ringtones
     * - Control playback (stop, restart)
     *
     * Null when no alarm is playing.
     */
    private var alarmRingtone: Ringtone? = null

    /**
     * Flag indicating whether the alarm is currently active.
     *
     * This flag is used to:
     * - Prevent multiple alarm triggers for single motion
     * - Control the SMS sending loop
     * - Track alarm state for UI updates
     *
     * true: Alarm is playing, SMS loop active
     * false: No alarm, monitoring only
     *
     * Reset to false when:
     * - User stops monitoring via pattern lock
     * - Service is destroyed
     */
    private var isAlarmPlaying = false

    // =========================================================================
    // SMS JOB - Periodic SMS Sending
    // =========================================================================

    /**
     * Job reference for the SMS sending loop.
     *
     * The SMS loop sends alerts every 5 seconds while the alarm is active.
     * This job reference allows:
     * - Cancelling SMS sending when alarm stops
     * - Preventing duplicate SMS loops
     * - Proper cleanup in stopSelfSafely()
     *
     * Null when SMS loop is not running.
     */
    private var smsJob: Job? = null

    // =========================================================================
    // CURRENT SETTINGS - Cached Configuration
    // =========================================================================

    /**
     * Cached current settings for quick access.
     *
     * Settings are loaded when monitoring starts and cached to:
     * - Avoid reading from DataStore on every motion detection
     * - Ensure consistent settings during alert handling
     * - Provide immediate access in onMotionDetected callback
     *
     * May be null if not yet loaded.
     *
     * Contains:
     * - Emergency contact number for SMS
     * - Siren enabled flag
     * - SMS enabled flag
     * - Sensitivity threshold (also accessed via lambda)
     */
    private var currentSettings: AppSettings? = null

    // =========================================================================
    // LIFECYCLE METHODS - Service Creation
    // =========================================================================

    /**
     * Called when the service is first created.
     *
     * This initialization method sets up the core components:
     *
     * 1. LocationHelper: Initializes GPS location access for alerts
     *
     * 2. MotionDetector: Creates the accelerometer listener with:
     *    - threshold: Lambda returning currentSensitivity (dynamic)
     *    - onMotion: Callback that triggers onMotionDetected()
     *
     * 3. Notification Channel: Creates the required channel for
     *    foreground service notifications (Android 8.0+)
     *
     * This method is called once per service instance. If the service
     * is stopped and restarted, this will be called again.
     */
    override fun onCreate() {
        // Call superclass to perform default service initialization
        super.onCreate()

        // Initialize location helper with service context
        // LocationHelper uses FusedLocationProviderClient internally
        locationHelper = LocationHelper(this)

        // Initialize motion detector
        // Parameters:
        // 1. context: Service context for accessing SensorManager
        // 2. threshold: Lambda returning current sensitivity setting
        //    This is a lambda so changes to sensitivity take effect immediately
        // 3. onMotion: Callback invoked when motion above threshold detected
        detector = MotionDetector(
            context = this,
            threshold = { currentSensitivity },
            onMotion = { onMotionDetected() }
        )

        // Create the notification channel (required for Android 8.0+)
        // This must be done before showing any notifications
        createChannel()
    }

    // =========================================================================
    // SERVICE COMMAND HANDLING
    // =========================================================================

    /**
     * Called when the service is started via startService() or startForegroundService().
     *
     * This method handles two actions:
     *
     * 1. ACTION_START (or no action): Begin monitoring
     *    - Load settings from DataStore
     *    - Start the motion detector
     *    - Show foreground notification
     *
     * 2. ACTION_STOP: Stop monitoring and service
     *    - Stop motion detector
     *    - Stop alarm and SMS loop
     *    - Remove foreground notification
     *    - Stop the service
     *
     * @param intent The Intent that started the service, contains the action
     * @param flags Additional data about the start request
     * @param startId Unique identifier for this start request
     * @return Int The return value indicating what to do if service is killed:
     *         START_STICKY: Service should be restarted if killed
     */
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Handle the intent action
        when (intent?.action) {
            // Stop action: User explicitly stopped monitoring
            ACTION_STOP -> stopSelfSafely()

            // Start action (or null): Begin monitoring
            else -> startMonitoring()
        }

        // Return START_STICKY to ensure service is restarted if killed by system
        // This is important for anti-theft - we want monitoring to resume
        // However, we don't want automatic restart after user stops
        return START_STICKY
    }

    // =========================================================================
    // CURRENT SENSITIVITY - Dynamic Threshold Access
    // =========================================================================

    /**
     * Current motion detection sensitivity threshold.
     *
     * This property is accessed by the MotionDetector via the threshold lambda.
     * It's updated when settings are loaded in startMonitoring().
     *
     * Default value: 0.8f (appropriate for linear_acceleration sensor)
     *
     * The value is compared against the magnitude of acceleration:
     * - If magnitude > currentSensitivity, motion is detected
     * - Lower values = more sensitive (detects small movements)
     * - Higher values = less sensitive (requires larger movements)
     */
    private var currentSensitivity: Float = 0.8f

    // =========================================================================
    // MONITORING START METHOD
    // =========================================================================

    /**
     * Starts the anti-theft monitoring process.
     *
     * This method is called when the service receives a start command.
     * It performs the following initialization:
     *
     * 1. Load Settings: Reads current settings from DataStore including:
     *    - Sensitivity threshold
     *    - Emergency contact number
     *    - SMS and siren enable flags
     *
     * 2. Start Foreground: Displays the persistent notification required
     *    by Android for foreground services. The notification shows
     *    "Monitoring active" to inform the user.
     *
     * 3. Start Detector: Registers the motion detector for accelerometer
     *    events. The detector will call onMotionDetected() when motion
     *    above the threshold is detected.
     *
     * This method runs asynchronously:
     * - Settings are loaded in a coroutine (non-blocking)
     * - Motion detector starts immediately (doesn't wait for settings)
     * - Settings are updated when coroutine completes
     */
    private fun startMonitoring() {
        // Launch a coroutine to load settings asynchronously
        // This prevents blocking while reading from DataStore
        scope.launch {
            // Get the settings flow and take the first (current) value
            // SettingsStore.settingsFlow emits current settings then updates
            val settings = SettingsStore(this@SpotItForegroundService).settingsFlow.first()

            // Update the sensitivity threshold for motion detection
            // This will be used in subsequent motion checks
            currentSensitivity = settings.sensitivity

            // Cache all settings for use in alert handling
            // This avoids reading from DataStore during alarm
            currentSettings = settings
        }

        // Start the service as a foreground service
        // This is required on Android 8.0+ to keep the service running
        // Parameters:
        // 1. NOTIFICATION_ID: Unique ID for this notification
        // 2. notification(): Creates the notification with "Monitoring active" text
        startForeground(NOTIFICATION_ID, notification("Monitoring active"))

        // Start listening for motion events
        // The detector registers with the SensorManager
        detector.start()
    }

    // =========================================================================
    // MOTION DETECTION CALLBACK
    // =========================================================================

    /**
     * Called when motion is detected by the MotionDetector.
     *
     * This is the core alert handling method. When the accelerometer detects
     * movement above the sensitivity threshold, this method is invoked.
     *
     * Actions performed:
     *
     * 1. Emit Event: Notifies the ViewModel via MotionEventManager so the
     *    UI can update to show alarm state.
     *
     * 2. Prevent Duplicates: Checks isAlarmPlaying flag to avoid triggering
     *    multiple alarms for the same motion event.
     *
     * 3. Load Settings: Retrieves current settings if not already loaded.
     *
     * 4. Get Location: Retrieves current GPS coordinates for the alert.
     *
     * 5. Start Alarm: Plays alarm sound and starts vibration pattern.
     *
     * 6. Update Notification: Changes notification to show "ALARM" state.
     *
     * 7. Start SMS Loop: Begins sending SMS alerts every 5 seconds.
     *
     * This method is called from the sensor thread (not main thread),
     * so all UI updates are posted via coroutines.
     */
    private fun onMotionDetected() {
        // Log for debugging
        android.util.Log.d("SpotItService", "onMotionDetected called! isAlarmPlaying=$isAlarmPlaying")

        // Emit motion event to ViewModel for UI update
        // This signals the ViewModel to update the UI state
        MotionEventManager.emitMotionEvent()

        // Guard clause: Don't start another alarm if one is already playing
        // This prevents multiple overlapping alarms from continuous motion
        if (!isAlarmPlaying) {
            // Mark alarm as active
            isAlarmPlaying = true

            // Launch a coroutine for alarm handling
            // This runs on IO dispatcher for background work
            scope.launch {
                // Get current settings
                // Use cached settings if available, otherwise load from DataStore
                val settings = currentSettings
                    ?: SettingsStore(this@SpotItForegroundService).settingsFlow.first()

                // Cache settings for future use
                currentSettings = settings

                // Get current location for the alert message
                val location = locationHelper.lastLocationText()

                // Start the alarm sound and vibration
                // Only if siren is enabled in settings
                if (settings.sirenEnabled) {
                    playAlarmContinuous()
                }

                // Update the notification to show alarm state
                val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                nm.notify(NOTIFICATION_ID, notification("ALARM • $location"))

                // Start the SMS sending loop
                // This sends alerts every 5 seconds while alarm is active
                startSmsLoop(settings, location)
            }
        }
    }

    // =========================================================================
    // SMS SENDING LOOP
    // =========================================================================

    /**
     * Starts the periodic SMS sending loop.
     *
     * This method creates a coroutine that sends SMS alerts to the emergency
     * contact every 5 seconds while the alarm is active. This provides
     * continuous alerts until the user disables the alarm.
     *
     * SMS Loop Behavior:
     * 1. Send initial SMS immediately when alarm triggers
     * 2. Wait 5 seconds
     * 3. Get fresh location
     * 4. Send another SMS
     * 5. Repeat steps 2-4 while alarm is active
     *
     * The loop continues until:
     * - User stops monitoring via pattern lock
     * - Service is stopped
     * - Coroutine is cancelled
     *
     * Location Updates:
     * Each SMS includes the current GPS coordinates. The location is
     * refreshed for each SMS to track the device if it's being moved.
     *
     * @param settings Current app settings containing contact number
     * @param initialLocation Initial location string for first SMS
     */
    private fun startSmsLoop(settings: AppSettings, initialLocation: String) {
        // Cancel any existing SMS job to prevent duplicates
        smsJob?.cancel()

        // Log SMS loop start
        android.util.Log.d(
            "SpotItService",
            "Starting SMS loop - smsEnabled: ${settings.smsEnabled}, contact: ${settings.contactNumber}"
        )

        // Launch the SMS sending coroutine
        smsJob = scope.launch {
            var location = initialLocation
            var smsCount = 0

            // Send initial SMS immediately
            // Only if SMS is enabled and contact number is set
            if (settings.smsEnabled && settings.contactNumber.isNotBlank()) {
                smsCount++
                android.util.Log.d("SpotItService", "Sending SMS #$smsCount to ${settings.contactNumber}")

                // Send SMS with alert message and location
                val sent = SmsHelper.send(
                    this@SpotItForegroundService,
                    settings.contactNumber,
                    "SPOTit ALERT: Movement Detected. $location"
                )

                android.util.Log.d("SpotItService", "SMS #$smsCount sent result: $sent")
            }

            // Continue sending every 5 seconds while alarm is active
            // isActive is false when coroutine is cancelled
            while (isActive && isAlarmPlaying) {
                // Wait 5 seconds between SMS
                delay(5000)

                // Check if alarm is still active and SMS is enabled
                if (isAlarmPlaying && settings.smsEnabled && settings.contactNumber.isNotBlank()) {
                    // Get fresh location for updated tracking
                    location = locationHelper.lastLocationText()

                    smsCount++
                    android.util.Log.d("SpotItService", "Sending SMS #$smsCount to ${settings.contactNumber}")

                    // Send SMS with updated location
                    val sent = SmsHelper.send(
                        this@SpotItForegroundService,
                        settings.contactNumber,
                        "SPOTit ALERT: Movement Detected. $location"
                    )

                    android.util.Log.d("SpotItService", "SMS #$smsCount sent result: $sent")
                }
            }

            // Log when SMS loop ends
            android.util.Log.d("SpotItService", "SMS loop ended, total SMS sent: $smsCount")
        }
    }

    // =========================================================================
    // ALARM SOUND PLAYBACK
    // =========================================================================

    /**
     * Starts continuous alarm sound and vibration.
     *
     * This method plays the device's default alarm ringtone at maximum volume
     * and starts a repeating vibration pattern. The alarm continues until
     * stopAlarmSound() is called.
     *
     * Audio Configuration:
     * - Sets alarm stream to maximum volume
     * - Uses default system alarm ringtone
     * - Plays in a loop (ringtone handles looping)
     *
     * Vibration Pattern:
     * - Pattern: [0, 500, 200, 500, 200, 500]
     *   - 0: Start immediately
     *   - 500: Vibrate for 500ms
     *   - 200: Wait 200ms
     *   - 500: Vibrate for 500ms
     *   - 200: Wait 200ms
     *   - 500: Vibrate for 500ms
     * - Repeat index 0: Pattern repeats from beginning
     *
     * API Compatibility:
     * - Android 12+: Uses VibratorManager
     * - Pre-Android 12: Uses deprecated Vibrator directly
     */
    private fun playAlarmContinuous() {
        // Stop any existing ringtone first to prevent overlap
        stopAlarmSound()

        // Get audio manager to set volume
        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager

        // Get maximum volume for alarm stream
        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM)

        // Set alarm volume to maximum
        // This ensures the alarm is heard even in noisy environments
        audioManager.setStreamVolume(AudioManager.STREAM_ALARM, maxVolume, 0)

        // Get the default alarm ringtone URI
        val ringtoneUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)

        // Create ringtone instance
        alarmRingtone = RingtoneManager.getRingtone(this, ringtoneUri)

        // Attempt to set ringtone volume to maximum via reflection
        // Some devices don't have this API publicly available
        try {
            val ringtoneClass = Ringtone::class.java
            val setVolumeMethod = ringtoneClass.getDeclaredMethod(
                "setVolume",
                Float::class.javaPrimitiveType
            )
            setVolumeMethod.isAccessible = true
            setVolumeMethod.invoke(alarmRingtone, 1.0f) // 100% volume
        } catch (e: Exception) {
            // Log warning if volume can't be set
            android.util.Log.w("SpotItService", "Could not set ringtone volume: ${e.message}")
        }

        // Start playing the ringtone
        // runCatching handles potential exceptions gracefully
        runCatching { alarmRingtone?.play() }

        // Define vibration pattern
        // [delay, vibrate, delay, vibrate, ...]
        val pattern = longArrayOf(0, 500, 200, 500, 200, 500)

        // Start vibration based on API level
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Android 12+ (API 31+): Use VibratorManager
            val vm = getSystemService(VIBRATOR_MANAGER_SERVICE) as VibratorManager
            // CreateWaveform: pattern, repeat index (-1 = no repeat, 0 = repeat from start)
            vm.defaultVibrator.vibrate(VibrationEffect.createWaveform(pattern, 0))
        } else {
            // Pre-Android 12: Use deprecated Vibrator
            @Suppress("DEPRECATION")
            val v = getSystemService(VIBRATOR_SERVICE) as Vibrator
            v.vibrate(VibrationEffect.createWaveform(pattern, 0))
        }
    }

    // =========================================================================
    // ALARM STOP METHOD
    // =========================================================================

    /**
     * Stops the alarm sound and vibration.
     *
     * This method is called when:
     * - The alarm needs to be stopped
     * - Before starting a new alarm (to prevent overlap)
     * - When the service is being stopped
     *
     * Actions:
     * - Stops and releases the ringtone
     * - Cancels any ongoing vibration
     */
    private fun stopAlarmSound() {
        // Stop the ringtone if playing
        alarmRingtone?.stop()
        alarmRingtone = null

        // Cancel vibration based on API level
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            // Android 12+: Use VibratorManager
            val vm = getSystemService(VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vm?.defaultVibrator?.cancel()
        } else {
            // Pre-Android 12: Use deprecated Vibrator
            @Suppress("DEPRECATION")
            val v = getSystemService(VIBRATOR_SERVICE) as? Vibrator
            v?.cancel()
        }
    }

    // =========================================================================
    // ALARM CONTROL METHOD
    // =========================================================================

    /**
     * Stops the alarm completely.
     *
     * This method is called when the user stops monitoring via pattern lock.
     * It stops all alarm-related activities:
     * - Sets the alarm playing flag to false
     * - Cancels the SMS sending job
     * - Stops the alarm sound and vibration
     */
    private fun stopAlarm() {
        // Mark alarm as not playing
        // This stops the SMS loop
        isAlarmPlaying = false

        // Cancel the SMS sending job
        smsJob?.cancel()
        smsJob = null

        // Stop sound and vibration
        stopAlarmSound()
    }

    // =========================================================================
    // SERVICE STOP METHOD
    // =========================================================================

    /**
     * Safely stops the service and all monitoring activities.
     *
     * This method is called when:
     * - User stops monitoring via pattern lock
     * - ACTION_STOP intent is received
     *
     * Cleanup performed:
     * 1. Stop motion detector (unregister from sensor)
     * 2. Stop alarm (sound, vibration, SMS)
     * 3. Remove foreground notification
     * 4. Stop the service
     *
     * This method ensures clean shutdown without leaving any
     * resources active.
     */
    private fun stopSelfSafely() {
        // Stop listening for motion events
        detector.stop()

        // Stop alarm and SMS sending
        stopAlarm()

        // Stop the alarm sound and SMS loop
        // Note: stopAlarm() already does this, but we call stopAlarmSound()
        // again to ensure vibration is cancelled
        stopAlarmSound()

        // Remove the foreground notification and stop foreground mode
        // STOP_FOREGROUND_REMOVE: Removes the notification entirely
        stopForeground(STOP_FOREGROUND_REMOVE)

        // Stop the service
        stopSelf()
    }

    // =========================================================================
    // NOTIFICATION BUILDER
    // =========================================================================

    /**
     * Creates a notification for the foreground service.
     *
     * This notification is required for foreground services on Android 8.0+.
     * It provides the user with:
     * - Visible indication that monitoring is active
     * - Quick access to the app via tap
     * - Easy way to stop monitoring via action button
     *
     * Notification Contents:
     * - Small icon: Alarm icon (system resource)
     * - Title: "SPOTit"
     * - Content text: Current status (e.g., "Monitoring active" or "ALARM")
     * - Ongoing: true (can't be swiped away)
     * - Sound: null (we handle sound separately)
     * - Content intent: Opens MainActivity
     * - Action button: "Stop" to stop monitoring
     *
     * @param text The status text to display in the notification
     * @return Notification The built notification object
     */
    private fun notification(text: String) = NotificationCompat.Builder(this, CHANNEL_ID)
        // Small icon shown in status bar
        .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
        // Notification title
        .setContentTitle("SPOTit")
        // Status text (changes based on monitoring/alarm state)
        .setContentText(text)
        // Ongoing notification (can't be dismissed by user)
        .setOngoing(true)
        // No sound on notification (we handle alarm separately)
        .setSound(null)
        // Intent to open MainActivity when notification is tapped
        .setContentIntent(
            PendingIntent.getActivity(
                this,
                1, // Request code for this PendingIntent
                Intent(this, MainActivity::class.java),
                // Flags for security and behavior
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        )
        // Add "Stop" action button
        .addAction(
            0, // Icon (0 = no icon, text only)
            "Stop", // Button text
            // PendingIntent to stop the service
            PendingIntent.getService(
                this,
                2, // Different request code
                Intent(this, SpotItForegroundService::class.java).apply {
                    action = ACTION_STOP // Signal to stop
                },
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
        )
        .build()

    // =========================================================================
    // NOTIFICATION CHANNEL CREATION
    // =========================================================================

    /**
     * Creates the notification channel for the foreground service.
     *
     * Notification channels are required on Android 8.0+ (API 26+).
     * They allow users to control notification preferences per channel.
     *
     * Channel Configuration:
     * - ID: "spotit_service" (unique identifier)
     * - Name: "SPOTit" (shown in system settings)
     * - Importance: HIGH (shows immediately, makes sound)
     * - Sound: null (we handle sound separately for alarm)
     *
     * The channel is created with the NotificationManager system service.
     */
    private fun createChannel() {
        // Get the NotificationManager system service
        val manager = getSystemService(NotificationManager::class.java)

        // Create the notification channel
        manager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_ID, // Unique channel ID
                "SPOTit", // Channel name (visible to user)
                NotificationManager.IMPORTANCE_HIGH // High importance for visibility
            ).apply {
                // Disable sound for this channel
                // We handle alarm sound separately to control it programmatically
                setSound(null, null)
            }
        )
    }

    // =========================================================================
    // SERVICE BINDING (NOT USED)
    // =========================================================================

    /**
     * Called when a client binds to the service.
     *
     * This service does not support binding - it runs independently.
     * Returning null indicates that binding is not supported.
     *
     * @param intent The intent used to bind to this service
     * @return IBinder? Always null (binding not supported)
     */
    override fun onBind(intent: Intent?): IBinder? = null

    // =========================================================================
    // COMPANION OBJECT - Constants and Static Methods
    // =========================================================================

    /**
     * Companion object containing constants and static methods.
     *
     * This provides:
     * - Channel and notification IDs
     * - Intent action constants
     * - Static start/stop methods for convenience
     */
    companion object {
        // Notification channel ID
        // Used to identify this app's notification channel
        private const val CHANNEL_ID = "spotit_service"

        // Notification ID
        // Used to identify the foreground service notification
        // Must be unique within this app
        private const val NOTIFICATION_ID = 1002

        // Intent action: Start monitoring
        // Used in start() method to start the service
        const val ACTION_START = "spotit.start"

        // Intent action: Stop monitoring
        // Used in stop() method and notification action button
        const val ACTION_STOP = "spotit.stop"

        /**
         * Starts the foreground service for monitoring.
         *
         * This is a convenience method that handles:
         * - Creating the start intent
         * - Setting the start action
         * - Starting as foreground service (for Android 8.0+)
         *
         * @param context Context used to start the service
         */
        fun start(context: Context) {
            // Use ContextCompat for compatibility
            // On Android 8.0+, this calls startForegroundService()
            ContextCompat.startForegroundService(
                context,
                Intent(context, SpotItForegroundService::class.java).apply {
                    action = ACTION_START
                }
            )
        }

        /**
         * Stops the foreground service.
         *
         * This is a convenience method that:
         * - Creates the stop intent
         * - Sets the stop action
         * - Starts the service with stop action
         *
         * Note: We use startService() instead of stopService() because
         * we want the service to handle the stop action in onStartCommand()
         * for proper cleanup.
         *
         * @param context Context used to start the service
         */
        fun stop(context: Context) {
            // Start service with stop action
            // The service will handle cleanup in onStartCommand()
            context.startService(
                Intent(context, SpotItForegroundService::class.java).apply {
                    action = ACTION_STOP
                }
            )
        }
    }
}

/**
 * Additional Architecture Notes:
 *
 * Foreground Service Requirements:
 * - Must call startForeground() within 5 seconds of starting
 * - Must show a notification with ongoing flag
 * - Must declare foregroundServiceType in manifest (Android 14+)
 * - Cannot be stopped by the system easily
 *
 * Battery Considerations:
 * - Sensor listeners use battery continuously
 * - Location requests use additional battery
 * - Alarm playback is resource-intensive
 * - SMS sending uses cellular radio
 *
 * The service is designed to be efficient:
 * - Uses SENSOR_DELAY_UI (not fastest) for balance
 * - Cooldown period prevents rapid re-triggering
 * - Location is cached, not constantly requested
 * - Coroutines allow non-blocking operations
 *
 * Security Considerations:
 * - Service must continue even if app is killed
 * - Pattern lock prevents unauthorized stopping
 * - Location permissions required for GPS
 * - SMS permission required for alerts
 *
 * Alternative Approaches Considered:
 * 1. WorkManager: Not suitable for real-time monitoring
 * 2. JobScheduler: Cannot guarantee continuous operation
 * 3. AlarmManager: Not suitable for sensor listening
 * 4. Background Service: Killed by system on modern Android
 */
