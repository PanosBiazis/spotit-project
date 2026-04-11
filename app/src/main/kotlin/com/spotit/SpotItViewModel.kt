/**
 * SPOTit Anti-Theft Application
 *
 * SpotItViewModel.kt - UI State Management and Business Logic
 *
 * This file contains the ViewModel for the SPOTit anti-theft application.
 * It manages the UI state, handles location updates, coordinates with the
 * foreground service, and processes motion detection events.
 *
 * @author Panos Biazis
 * @date 10/4/2026
 * @version 2.0
 *
 * Copyright (c) 2026 Panos Biazis. All rights reserved.
 */

// Package declaration - defines the application's base package
package com.spotit

// =============================================================================
// ANDROID FRAMEWORK IMPORTS
// =============================================================================

// Annotation imports
import android.annotation.SuppressLint // Suppresses permission-related lint warnings

// Context and Location imports
import android.content.Context // Android context for accessing system services
import android.location.Location // Represents a geographic location

// Logging utility
import android.util.Log // Android logging system for debugging

// =============================================================================
// ANDROIDX LIFECYCLE IMPORTS
// =============================================================================

import androidx.lifecycle.ViewModel // Base class for ViewModels
import androidx.lifecycle.viewModelScope // Coroutine scope tied to ViewModel lifecycle

// =============================================================================
// GOOGLE PLAY SERVICES LOCATION IMPORTS
// =============================================================================

import com.google.android.gms.location.* // Location services (FusedLocationProviderClient, LocationRequest, etc.)
import com.google.android.gms.location.LocationRequest.* // Builder for LocationRequest

// =============================================================================
// APPLICATION-SPECIFIC IMPORTS
// =============================================================================

// Data models
import com.spotit.data.AlertEvent // Alert event data class
import com.spotit.data.AppSettings // Application settings data class
import com.spotit.data.SettingsStore // DataStore-based settings persistence
import com.spotit.data.SpotItUiState // UI state data class

// Repository for data access
import com.spotit.domain.SpotItRepository // Repository layer for settings

// Service communication
import com.spotit.service.MotionEventManager // Singleton for motion event broadcasting
import com.spotit.service.SpotItForegroundService // Foreground service for monitoring

// =============================================================================
// KOTLIN COROUTINES IMPORTS
// =============================================================================

import kotlinx.coroutines.flow.MutableStateFlow // Mutable state flow for UI state
import kotlinx.coroutines.flow.StateFlow // Read-only state flow
import kotlinx.coroutines.flow.asStateFlow // Converts MutableStateFlow to StateFlow
import kotlinx.coroutines.flow.update // Atomic updates to StateFlow
import kotlinx.coroutines.launch // Launches a coroutine

// =============================================================================
// JAVA UTILITY IMPORTS
// =============================================================================

import java.text.SimpleDateFormat // Date formatting
import java.util.Date // Date representation
import java.util.Locale // Locale for formatting

/**
 * SpotItViewModel - The Central UI State Manager
 *
 * This ViewModel serves as the bridge between the UI (MainActivity/Composables)
 * and the data/business logic layer of the application. It follows the MVVM
 * (Model-View-ViewModel) architecture pattern.
 *
 * Responsibilities:
 *
 * 1. **State Management**: Maintains and exposes UI state through StateFlow.
 *    The UI observes this state and updates accordingly. State includes:
 *    - Monitoring status (is the service running?)
 *    - Alarm status (has motion been detected?)
 *    - Current location text
 *    - Application settings
 *    - Alert event history
 *
 * 2. **Location Updates**: Manages continuous GPS location updates using
 *    Google Play Services FusedLocationProviderClient. This provides
 *    real-time location data that is included in alert messages.
 *
 * 3. **Settings Management**: Loads and saves user preferences through
 *    the repository layer. Settings include emergency contact, security
 *    pattern, sensitivity, and notification preferences.
 *
 * 4. **Service Coordination**: Starts and stops the foreground service
 *    that performs actual motion detection. Also receives motion events
 *    from the service through the MotionEventManager singleton.
 *
 * 5. **Alert Generation**: Creates alert events when motion is detected,
 *    including timestamp and current location information.
 *
 * Architecture Notes:
 * - Uses StateFlow for reactive UI updates (not LiveData)
 * - Coroutines run in viewModelScope (automatically cancelled when ViewModel cleared)
 * - Repository pattern abstracts data access (SettingsStore)
 * - Service communication via SharedFlow (MotionEventManager)
 *
 * @see SpotItUiState - The data class holding all UI state
 * @see SpotItRepository - Repository for accessing settings
 * @see SpotItForegroundService - Background service for motion detection
 * @see MotionEventManager - Singleton for service-to-ViewModel communication
 */
class SpotItViewModel : ViewModel() {

    // =========================================================================
    // COMPANION OBJECT - Static constants and utilities
    // =========================================================================

    /**
     * Companion object containing static members for the SpotItViewModel class.
     *
     * This provides a logging tag that remains constant across all instances
     * of SpotItViewModel, used for filtering logcat output during debugging.
     */
    companion object {
        // Logging tag for this ViewModel - used with Log.d(), Log.e(), etc.
        // Format: "SpotItViewModel" for easy filtering in logcat
        private const val TAG = "SpotItViewModel"
    }

    // =========================================================================
    // UI STATE - StateFlow for reactive UI updates
    // =========================================================================

    /**
     * Mutable backing state for the UI.
     *
     * MutableStateFlow allows the ViewModel to update state while exposing
     * a read-only StateFlow to the UI. This follows the principle of
     * encapsulation - the UI can observe but not directly modify state.
     *
     * Initial state:
     * - isMonitoring: false (service not running)
     * - alarmTriggered: false (no motion detected)
     * - locationText: "Unknown Location" (no GPS fix yet)
     * - settings: default AppSettings
     * - events: empty list (no alerts yet)
     *
     * Updates are performed atomically using _uiState.update { } to ensure
     * thread safety when multiple coroutines modify state concurrently.
     */
    private val _uiState = MutableStateFlow(SpotItUiState())

    /**
     * Read-only state flow exposed to the UI.
     *
     * The UI (MainActivity) collects this StateFlow using collectAsStateWithLifecycle()
     * which ensures the UI only receives updates when the activity is in foreground.
     *
     * This StateFlow provides:
     * - Current monitoring status
     * - Current alarm status
     * - Current location text
     * - Current settings
     * - List of alert events (most recent first)
     *
     * The UI automatically recomposes when this state changes.
     */
    val uiState: StateFlow<SpotItUiState> = _uiState.asStateFlow()

    // =========================================================================
    // DEPENDENCIES - Repository and Location Services
    // =========================================================================

    /**
     * Repository for accessing application settings.
     *
     * The repository abstracts the data layer, providing a clean interface
     * for loading and saving settings. It uses DataStore internally for
     * persistent storage.
     *
     * Initialized in initialize() or initializeWithoutLocation() with
     * the application context.
     *
     * May be null if not yet initialized (before initialize() is called).
     */
    private var repository: SpotItRepository? = null

    /**
     * Google Play Services location client for GPS updates.
     *
     * FusedLocationProviderClient is the recommended way to get location
     * on Android. It combines GPS, network, and sensor data to provide
     * accurate location while optimizing battery usage.
     *
     * Features used:
     * - High accuracy mode for precise location
     * - Continuous location updates (not single fix)
     * - Automatic location caching (lastLocation)
     *
     * Initialized in initialize() with the application context.
     */
    private var fusedLocationClient: FusedLocationProviderClient? = null

    /**
     * Callback for receiving location updates.
     *
     * This callback is invoked by the FusedLocationProviderClient whenever
     * a new location is available. The callback updates the UI state with
     * the new location coordinates.
     *
     * Created in startContinuousLocationUpdates() when location updates
     * are first requested. The same callback instance is reused for all
     * location updates to avoid creating multiple callbacks.
     */
    private var locationCallback: LocationCallback? = null

    /**
     * Flag indicating whether location updates have been started.
     *
     * This flag prevents duplicate calls to requestLocationUpdates(), which
     * would waste resources and could cause multiple callbacks.
     *
     * Set to true when location updates are successfully started.
     * Set to false when updates are stopped or need to be restarted.
     *
     * Usage:
     * - Checked in startContinuousLocationUpdates() to skip if already started
     * - Reset to false in restartLocationUpdates() to allow restarting
     * - Checked in onResume() callback to handle permission changes
     */
    private var isLocationUpdatesStarted = false

    // =========================================================================
    // INIT BLOCK - Initialization code
    // =========================================================================

    /**
     * Initialization block - runs when ViewModel is created.
     *
     * This block sets up the motion event collection from the foreground service.
     * When the service detects motion, it emits an event through MotionEventManager,
     * which this ViewModel collects and processes to trigger an alarm.
     *
     * The collection runs in viewModelScope, so it automatically stops when
     * the ViewModel is cleared (when the activity is destroyed).
     */
    init {
        // Collect motion events from the service
        // MotionEventManager is a singleton that broadcasts motion detection events
        // from the foreground service to all interested listeners
        viewModelScope.launch {
            // Collect is a terminal operator - suspends and processes each event
            MotionEventManager.motionEvent.collect {
                // Log that we received a motion event from the service
                Log.d(TAG, "Motion event received from service")
                // Trigger the alert - updates UI state and potentially starts alarm
                movAlert()
            }
        }
    }

    // =========================================================================
    // INITIALIZATION METHODS
    // =========================================================================

    /**
     * Initializes the ViewModel with full functionality including location updates.
     *
     * This method should be called from MainActivity.onCreate() when all
     * required permissions (especially location) are already granted.
     *
     * Initialization steps:
     * 1. Check if already initialized (early return to prevent duplicates)
     * 2. Create the repository with application context
     * 3. Create the FusedLocationProviderClient
     * 4. Start collecting settings from the repository
     * 5. Start continuous location updates
     *
     * The application context is used (not activity context) to avoid memory leaks,
     * as the application context lives for the entire app lifetime.
     *
     * @param context The context used for initialization. Should be the
     *                activity context, but application context is extracted
     *                from it for long-lived references.
     *
     * @SuppressLint("MissingPermission") - Suppresses lint warning about
     * location permissions. The calling code (MainActivity) ensures permissions
     * are granted before calling this method.
     */
    @SuppressLint("MissingPermission")
    fun initialize(context: Context) {
        Log.d(TAG, "initialize() called, repository=$repository")

        // Guard clause: Skip if already initialized
        // This prevents creating duplicate repositories and location clients
        if (repository != null) {
            Log.d(TAG, "Already initialized, skipping")
            return
        }

        // Extract application context to avoid memory leaks
        // Activity context would leak when activity is destroyed
        val appContext = context.applicationContext

        Log.d(TAG, "Creating repository and location client")

        // Create repository with settings store
        // Repository provides access to persisted settings
        val repo = SpotItRepository(SettingsStore(appContext))
        repository = repo

        // Create the fused location provider client
        // This is Google's recommended location API
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(appContext)

        // Start collecting settings flow from repository
        // This updates UI state whenever settings change
        viewModelScope.launch {
            Log.d(TAG, "Starting to collect settings flow")
            // Collect is a terminal flow operator - runs indefinitely
            repo.settings.collect { settings ->
                Log.d(TAG, "Settings received: $settings")
                // Update UI state with new settings
                _uiState.update { it.copy(settings = settings) }
            }
        }

        // Start continuous location updates immediately
        // This ensures location is available when an alert is triggered
        Log.d(TAG, "Calling startContinuousLocationUpdates from initialize")
        startContinuousLocationUpdates(context)
    }

    /**
     * Initializes the ViewModel without starting location updates.
     *
     * This method is called when location permissions are not yet granted.
     * It sets up everything except location updates, which will be started
     * later when permissions are granted (via restartLocationUpdates()).
     *
     * Use case:
     * 1. App starts, some permissions missing
     * 2. Call initializeWithoutLocation() to set up basic functionality
     * 3. Request missing permissions
     * 4. When permissions granted, call restartLocationUpdates()
     *
     * @param context The context used for initialization.
     */
    @SuppressLint("MissingPermission")
    fun initializeWithoutLocation(context: Context) {
        Log.d(TAG, "initializeWithoutLocation() called, repository=$repository")

        // Guard clause: Skip if already initialized
        if (repository != null) {
            Log.d(TAG, "Already initialized, skipping")
            return
        }

        // Extract application context
        val appContext = context.applicationContext

        Log.d(TAG, "Creating repository and location client (without location)")

        // Create repository
        val repo = SpotItRepository(SettingsStore(appContext))
        repository = repo

        // Create location client (but don't start updates yet)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(appContext)

        // Start collecting settings
        viewModelScope.launch {
            Log.d(TAG, "Starting to collect settings flow")
            repo.settings.collect { settings ->
                Log.d(TAG, "Settings received: $settings")
                _uiState.update { it.copy(settings = settings) }
            }
        }

        // Don't start location updates yet
        // Will be called after permissions are granted
    }

    // =========================================================================
    // LOCATION MANAGEMENT METHODS
    // =========================================================================

    /**
     * Starts continuous GPS location updates.
     *
     * This method sets up the location callback and requests continuous location
     * updates from the FusedLocationProviderClient. The updates continue until
     * explicitly stopped (when ViewModel is cleared).
     *
     * Location request configuration:
     * - Priority: HIGH_ACCURACY (uses GPS for precise location)
     * - Update interval: 1000ms (updates every second)
     * - Min update interval: 500ms (can update more frequently if available)
     * - Wait for accurate location: false (prioritize speed over accuracy)
     *
     * The received location is formatted as "Lat: xx.xxxxx, Lng: xx.xxxxx"
     * and stored in UI state for display and inclusion in alert messages.
     *
     * Thread safety: Location updates are delivered on the main looper,
     * so UI state updates are safe.
     *
     * @param context The context used to access the main looper for callbacks.
     *
     * @SuppressLint("MissingPermission") - Caller must ensure location
     * permissions are granted before calling this method.
     */
    @SuppressLint("MissingPermission")
    private fun startContinuousLocationUpdates(context: Context) {
        Log.d(TAG, "startContinuousLocationUpdates called, isLocationUpdatesStarted: $isLocationUpdatesStarted")

        // Guard clause: Skip if location updates already running
        // This prevents duplicate location requests
        if (isLocationUpdatesStarted) {
            Log.d(TAG, "Location updates already started, returning")
            return
        }

        // Extract application context
        val appContext = context.applicationContext

        // Get or create the location client
        val client = fusedLocationClient ?: LocationServices.getFusedLocationProviderClient(appContext).also {
            fusedLocationClient = it
            Log.d(TAG, "Created new FusedLocationProviderClient")
        }

        // Build the location request with desired parameters
        val request = Builder(
            Priority.PRIORITY_HIGH_ACCURACY, // Use GPS for best accuracy
            1000L // Update interval: 1 second between updates
        )
            .setMinUpdateIntervalMillis(500L) // Can update as fast as every 500ms
            .setWaitForAccurateLocation(false) // Don't wait for high accuracy
            .build()

        Log.d(TAG, "LocationRequest created: priority=HIGH_ACCURACY, interval=1000ms, minInterval=500ms")

        // Create location callback if not already created
        // This callback is reused across all location updates
        if (locationCallback == null) {
            locationCallback = object : LocationCallback() {
                /**
                 * Called when a new location is available.
                 *
                 * This method is invoked on the main thread by the
                 * FusedLocationProviderClient when a new location fix
                 * is obtained.
                 *
                 * @param result Contains the list of locations, with
                 *               the most recent as lastLocation.
                 */
                override fun onLocationResult(result: LocationResult) {
                    // Get the most recent location
                    val location: Location? = result.lastLocation

                    if (location == null) {
                        Log.w(TAG, "onLocationResult: location is null")
                        return
                    }

                    // Log the received location with accuracy info
                    Log.d(
                        TAG,
                        "Location received: lat=${location.latitude}, lng=${location.longitude}, accuracy=${location.accuracy}"
                    )

                    // Format location for display
                    // Uses 5 decimal places for ~1 meter precision
                    val text = "Lat: ${location.latitude}, Lng: ${location.longitude}"

                    // Update UI state with new location
                    _uiState.update { it.copy(locationText = text) }
                }
            }
            Log.d(TAG, "LocationCallback created")
        }

        // Request location updates
        try {
            Log.d(TAG, "Requesting location updates...")

            // Request continuous location updates
            // The callback will be invoked on the main looper
            client.requestLocationUpdates(
                request,
                locationCallback!!,
                appContext.mainLooper
            ).addOnSuccessListener {
                // Success callback - updates request was accepted
                Log.d(TAG, "Location updates request SUCCESSFUL")
            }.addOnFailureListener { e ->
                // Failure callback - something went wrong
                Log.e(TAG, "Location updates request FAILED: ${e.message}", e)
                // Reset flag so retry is possible
                isLocationUpdatesStarted = false
            }

            // Mark as started (optimistically)
            isLocationUpdatesStarted = true
            Log.d(TAG, "Location updates request submitted, isLocationUpdatesStarted=$isLocationUpdatesStarted")

        } catch (e: SecurityException) {
            // Shouldn't happen if permissions are properly checked
            Log.e(TAG, "SecurityException when requesting location updates: ${e.message}", e)
        } catch (e: Exception) {
            // Catch any other unexpected errors
            Log.e(TAG, "Exception when requesting location updates: ${e.message}", e)
        }
    }

    /**
     * Restarts location updates after permissions are granted.
     *
     * This method is called from MainActivity when:
     * 1. Permission callback receives location permission grant
     * 2. onResume detects that location permissions are now granted
     *
     * It ensures location updates are running even if they were not
     * started initially due to missing permissions.
     *
     * Steps:
     * 1. Remove existing location updates (if any)
     * 2. Reset the location updates started flag
     * 3. Start fresh location updates
     *
     * @param context The context for location updates.
     *
     * @SuppressLint("MissingPermission") - Caller ensures permissions granted.
     */
    @SuppressLint("MissingPermission")
    fun restartLocationUpdates(context: Context) {
        Log.d(TAG, "restartLocationUpdates called")

        // Stop existing updates if any
        // This prevents duplicate callbacks if updates were already running
        fusedLocationClient?.let { client ->
            locationCallback?.let { callback ->
                Log.d(TAG, "Removing existing location updates")
                client.removeLocationUpdates(callback)
            }
        }

        // Reset flag to allow starting fresh
        isLocationUpdatesStarted = false

        // Start location updates
        startContinuousLocationUpdates(context)
    }

    // =========================================================================
    // SETTINGS MANAGEMENT
    // =========================================================================

    /**
     * Saves new settings to persistent storage.
     *
     * This method is called from SettingsScreen when the user saves
     * their configuration changes. The settings are persisted using
     * DataStore through the repository.
     *
     * After saving, the UI state is immediately updated to reflect
     * the new settings (optimistic update).
     *
     * Settings saved:
     * - Emergency contact phone number
     * - Security pattern for disabling monitoring
     * - Motion detection sensitivity
     * - SMS alert enable/disable
     * - Siren enable/disable
     *
     * @param settings The new settings to save.
     */
    fun saveSettings(settings: AppSettings) {
        Log.d(TAG, "saveSettings called: $settings")

        // Launch a coroutine in the ViewModel scope
        viewModelScope.launch {
            // Save settings through the repository
            repository?.saveSettings(settings)

            // Immediately update UI state
            // This provides instant feedback without waiting for the
            // settings flow to emit (which may have a small delay)
            _uiState.update { it.copy(settings = settings) }
        }
    }

    // =========================================================================
    // MONITORING CONTROL
    // =========================================================================

    /**
     * Starts the anti-theft monitoring service.
     *
     * This method launches the foreground service that performs motion
     * detection. The service runs continuously until explicitly stopped,
     * even if the app is backgrounded.
     *
     * Steps:
     * 1. Update UI state to show monitoring is active
     * 2. Clear any existing alarm state
     * 3. Start the SpotItForegroundService
     *
     * The foreground service:
     * - Shows a persistent notification (required by Android)
     * - Registers for accelerometer sensor events
     * - Triggers alerts when motion above threshold is detected
     *
     * @param context Context used to start the service.
     */
    fun startMonitoring(context: Context) {
        Log.d(TAG, "startMonitoring called")

        // Update UI state
        _uiState.update {
            it.copy(
                isMonitoring = true, // Show monitoring is active
                alarmTriggered = false // Clear any previous alarm
            )
        }

        // Start the foreground service
        SpotItForegroundService.start(context)
    }

    /**
     * Stops the anti-theft monitoring service.
     *
     * This method stops the foreground service. It should only be called
     * after the user has successfully entered the security pattern
     * (pattern lock prevents unauthorized stopping).
     *
     * Steps:
     * 1. Update UI state to show monitoring is inactive
     * 2. Clear any alarm state
     * 3. Stop the SpotItForegroundService
     *
     * @param context Context used to stop the service.
     */
    fun stopMonitoring(context: Context) {
        Log.d(TAG, "stopMonitoring called")

        // Update UI state
        _uiState.update {
            it.copy(
                isMonitoring = false, // Show monitoring is inactive
                alarmTriggered = false // Clear alarm state
            )
        }

        // Stop the foreground service
        SpotItForegroundService.stop(context)
    }

    // =========================================================================
    // ALERT HANDLING
    // =========================================================================

    /**
     * Handles motion detection alerts.
     *
     * This method is called when the MotionEventManager emits a motion event.
     * It creates an alert event with timestamp and current location, then
     * updates the UI state to trigger the alarm.
     *
     * Alert flow:
     * 1. Check if monitoring is active (ignore motion if not)
     * 2. Create AlertEvent with current location
     * 3. Update UI state with alarm triggered
     * 4. Add event to history (keeping last 30 events)
     *
     * The actual alarm (sound, vibration, SMS) is handled by the
     * foreground service, not the ViewModel. The ViewModel just updates
     * the UI to show the alarm state.
     *
     * Event history:
     * - New events are added to the front of the list
     * - Only the last 30 events are kept (prevents memory growth)
     */
    fun movAlert() {
        Log.d(TAG, "movAlert called, isMonitoring=${_uiState.value.isMonitoring}")

        // Guard clause: Only trigger alarm if monitoring is active
        // This prevents false alarms if motion is detected when monitoring is off
        if (!_uiState.value.isMonitoring) {
            Log.d(TAG, "Not monitoring, ignoring motion event")
            return
        }

        // Create an alert event with current information
        val event = AlertEvent(
            title = "Alert", // Alert title
            details = "Movement detected", // Alert description
            // Format timestamp as "dd/MM/yyyy HH:mm:ss"
            timestamp = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Date()),
            // Include current location from UI state
            location = _uiState.value.locationText
        )

        Log.d(TAG, "Triggering alarm!")

        // Update UI state
        _uiState.update {
            it.copy(
                alarmTriggered = true, // Show alarm state in UI
                // Add new event to front of list, keep last 30 events
                // This provides a rolling history of alerts
                events = listOf(event) + it.events.take(29)
            )
        }
    }

    // =========================================================================
    // LIFECYCLE CLEANUP
    // =========================================================================

    /**
     * Called when the ViewModel is being cleared.
     *
     * This method is invoked when the associated activity is permanently
     * destroyed (not during configuration changes like rotation).
     *
     * Cleanup performed:
     * - Remove location updates to stop GPS usage
     * - Release location client resources
     *
     * Note: The foreground service is NOT stopped here because:
     * - The service should continue running even if the UI is closed
     * - Service is managed by user action (start/stop monitoring)
     * - Service handles its own lifecycle
     */
    override fun onCleared() {
        Log.d(TAG, "onCleared called")

        // Clean up location updates
        fusedLocationClient?.let { client ->
            locationCallback?.let { callback ->
                // Remove location updates to stop GPS and save battery
                client.removeLocationUpdates(callback)
            }
        }

        // Call super to complete cleanup
        super.onCleared()
    }
}
