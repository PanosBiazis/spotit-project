/**
 * SPOTit Anti-Theft Application
 *
 * Models.kt - Data Classes for Application State
 *
 * This file contains the data classes that represent the core data models
 * used throughout the SPOTit anti-theft application. These immutable data
 * classes follow Kotlin best practices and are used for:
 * - Representing alert events
 * - Storing application settings
 * - Managing UI state
 *
 * @author Panos Biazis
 * @date 10/4/2026
 * @version 2.0
 *
 * Copyright (c) 2026 Panos Biazis. All rights reserved.
 */

// Package declaration - defines the data layer package
package com.spotit.data

// =============================================================================
// ALERT EVENT DATA CLASS
// =============================================================================

/**
 * AlertEvent - Represents a Security Alert Event
 *
 * This data class captures information about a single security alert event
 * that occurs when motion is detected by the anti-theft monitoring service.
 *
 * Each alert event contains:
 * - A unique identifier (timestamp-based)
 * - A title for the alert
 * - Detailed description of what was detected
 * - Timestamp of when the alert occurred
 * - Location information (GPS coordinates)
 *
 * Usage:
 * - Created in SpotItViewModel when motion is detected
 * - Stored in SpotItUiState.events list for history display
 * - Displayed in HistoryScreen as a list of past alerts
 * - Used for debugging and audit trails
 *
 * Immutability:
 * - All properties are val (immutable) except location
 * - location is var because it may be updated after initial creation
 *   when GPS coordinates become available
 *
 * @property id Unique identifier for this event. Defaults to current
 *               timestamp in milliseconds, ensuring uniqueness and
 *               chronological ordering.
 * @property title The headline text for this alert (e.g., "Alert").
 * @property details A more detailed description of what was detected
 *                   (e.g., "Movement detected").
 * @property timestamp Human-readable timestamp string formatted as
 *                     "dd/MM/yyyy HH:mm:ss" showing when the alert occurred.
 * @property location Optional GPS coordinates or location description.
 *                    Updated when location becomes available. May be null
 *                    if location permission was not granted or GPS is
 *                    unavailable.
 *
 * @see SpotItViewModel.movAlert() - Creates AlertEvent instances
 * @see HistoryScreen - Displays AlertEvent list
 */
data class AlertEvent(
    // Unique identifier using current time in milliseconds
    // This ensures:
    // 1. Uniqueness across all events
    // 2. Chronological ordering (later events have higher IDs)
    // 3. Can be used for event deduplication
    val id: Long = System.currentTimeMillis(),

    // Alert title - displayed prominently in the UI
    // Examples: "Alert", "Κίνηση εντοπίστηκε" (Greek: "Motion detected")
    val title: String,

    // Detailed description of the alert
    // Provides context about what triggered the alert
    // Example: "Movement detected" or "Ύποπτη μετακίνηση συσκευής" (Greek)
    val details: String,

    // Human-readable timestamp
    // Format: "dd/MM/yyyy HH:mm:ss" (e.g., "10/04/2026 14:30:25")
    // Created using SimpleDateFormat with Locale.getDefault()
    val timestamp: String,

    // GPS location information
    // Format: "Lat: xx.xxxxx, Lng: xx.xxxxx" or null
    // This is mutable (var) because location may be updated
    // after the initial alert creation when GPS coordinates
    // become available
    var location: String? = null
)

// =============================================================================
// APPLICATION SETTINGS DATA CLASS
// =============================================================================

/**
 * AppSettings - Application Configuration Settings
 *
 * This data class holds all user-configurable settings for the SPOTit
 * anti-theft application. Settings are persisted using DataStore and
 * loaded when the app starts.
 *
 * Settings Categories:
 *
 * 1. **Emergency Contact**: The phone number that will receive SMS alerts
 *    when motion is detected. This should be a trusted contact who can
 *    respond to potential theft situations.
 *
 * 2. **Security Pattern**: A dot pattern (like Android's pattern lock)
 *    that must be entered to disable monitoring. This prevents unauthorized
 *    users (like thieves) from easily stopping the anti-theft protection.
 *
 * 3. **Sensitivity**: The motion detection threshold. Lower values make
 *    the system more sensitive (detects smaller movements), higher values
 *    make it less sensitive (requires larger movements to trigger).
 *
 * 4. **SMS Alerts**: Whether to send SMS messages to the emergency contact
 *    when an alert is triggered.
 *
 * 5. **Siren**: Whether to play an alarm sound when motion is detected.
 *
 * Default Values:
 * - contactNumber: Empty string (user must configure)
 * - pattern: "1-2-3-6" (simple default pattern)
 * - sensitivity: 0.8f (appropriate for linear_acceleration sensor)
 * - smsEnabled: true (alerts on by default)
 * - sirenEnabled: true (alarm sound on by default)
 *
 * Persistence:
 * - Saved/loaded via SettingsStore using DataStore Preferences
 * - Key names: "contact", "pattern", "sensitivity", "sms", "siren"
 *
 * @property contactNumber Phone number for emergency SMS alerts.
 *                         Should include country code for international use.
 *                         Empty string means SMS alerts are disabled.
 * @property pattern Security pattern for disabling monitoring.
 *                   Format: "x-y-z-w" where each number is a dot position
 *                   (0-9). Example: "1-2-3-6" means press dots 1, 2, 3, 6
 *                   in sequence.
 * @property sensitivity Motion detection sensitivity threshold.
 *                       Range: 0.1f to 5.0f
 *                       - Lower values (0.1-1.0): Very sensitive, detects
 *                         small vibrations
 *                       - Middle values (1.0-2.5): Moderate sensitivity,
 *                         good for most situations
 *                       - Higher values (2.5-5.0): Less sensitive, only
 *                         detects significant movement
 *                       Default 0.8f is optimized for linear_acceleration
 *                       sensor readings.
 * @property smsEnabled Whether SMS alerts are enabled.
 *                      true: Send SMS to contactNumber when alert triggers
 *                      false: No SMS alerts (rely on siren only)
 * @property sirenEnabled Whether the alarm siren is enabled.
 *                        true: Play alarm sound and vibrate on alert
 *                        false: Silent mode (only visual notification)
 *
 * @see SettingsStore - Handles persistence of AppSettings
 * @see SettingsScreen - UI for editing AppSettings
 */
data class AppSettings(
    // Emergency contact phone number for SMS alerts
    // Must be configured by user for SMS functionality to work
    // Format: International format recommended (e.g., "+306912345678")
    // Empty string disables SMS alerts
    val contactNumber: String = "",

    // Security pattern for unlocking/disabling monitoring
    // Format: Dot numbers separated by dashes (e.g., "1-2-3-6")
    // Dots are numbered 0-9 in a 3x3+1 layout:
    //   1 2 3
    //   4 5 6
    //   7 8 9
    //     0
    // Default "1-2-3-6" is a simple pattern for demonstration
    // User should set a unique pattern for security
    val pattern: String = "1-2-3-6",

    // Motion detection sensitivity threshold
    // This value is compared against the magnitude of acceleration
    // detected by the sensor. When magnitude exceeds this threshold,
    // an alert is triggered.
    //
    // For LINEAR_ACCELERATION sensor (default):
    // - Values reported are in m/s², excluding gravity
    // - Typical resting device: ~0 m/s²
    // - Small movements: 0.1-1.0 m/s²
    // - Walking with device: 1.0-2.5 m/s²
    // - Running/shaking: 2.5+ m/s²
    //
    // Recommended values:
    // - 0.5f: Very sensitive, may trigger on vibrations
    // - 0.8f: Good balance (default)
    // - 1.5f: Less sensitive, reduces false positives
    // - 3.0f+: Only significant movement triggers
    //
    // Note: Was previously 12f then 2.5f, lowered to 0.8f for
    // linear_acceleration sensor compatibility
    val sensitivity: Float = 0.8f,

    // SMS alert enable flag
    // When true: Sends SMS to contactNumber every 5 seconds while
    //            alarm is active, containing alert message and location
    // When false: No SMS alerts sent
    // Requires SEND_SMS permission and valid contactNumber
    val smsEnabled: Boolean = true,

    // Siren/alarm sound enable flag
    // When true: Plays alarm ringtone at maximum volume and vibrates
    //            in a pattern when motion is detected
    // When false: Silent mode, no audible alert
    // Uses default alarm ringtone from system
    val sirenEnabled: Boolean = true
)

// =============================================================================
// UI STATE DATA CLASS
// =============================================================================

/**
 * SpotItUiState - Complete UI State for the Application
 *
 * This data class represents the entire state of the SPOTit application's
 * user interface. It follows the Unidirectional Data Flow (UDF) pattern
 * where:
 * - The ViewModel holds this state
 * - The UI observes this state via StateFlow
 * - User actions trigger state updates through the ViewModel
 * - UI automatically recomposes when state changes
 *
 * State Categories:
 *
 * 1. **Monitoring Status**: Whether the anti-theft service is running
 *
 * 2. **Alarm Status**: Whether an alarm is currently triggered
 *
 * 3. **Location**: Current GPS coordinates for display and alerts
 *
 * 4. **Settings**: Current application configuration
 *
 * 5. **Events**: History of alert events (most recent first)
 *
 * State Flow:
 * ```
 * User Action -> ViewModel.method() -> _uiState.update { }
 *                                    -> uiState (StateFlow) emits
 *                                    -> UI recomposes
 * ```
 *
 * Thread Safety:
 * - State updates use StateFlow.update { } for atomic operations
 * - All properties are immutable (val)
 * - Collections are immutable (List instead of MutableList)
 *
 * @property isMonitoring Whether the anti-theft monitoring service is
 *                        currently running.
 *                        true: Foreground service active, sensor listening
 *                        false: Service stopped, no motion detection
 *                        Initial: false
 *                        Updated by: startMonitoring(), stopMonitoring()
 * @property alarmTriggered Whether an alarm is currently active.
 *                          true: Motion detected, alert in progress
 *                          false: No alarm, normal state
 *                          Initial: false
 *                          Updated by: movAlert(), stopMonitoring()
 * @property locationText Human-readable current location string.
 *                        Format: "Lat: xx.xxxxx, Lng: xx.xxxxx"
 *                        Initial: "Unknown Location"
 *                        Updated by: LocationCallback in ViewModel
 *                        Used in: HomeScreen display, alert messages
 * @property settings Current application settings.
 *                    Contains contact number, pattern, sensitivity, etc.
 *                    Initial: AppSettings() with defaults
 *                    Updated by: Settings flow from repository
 * @property events List of alert events, most recent first.
 *                  Limited to last 30 events to prevent memory growth.
 *                  Initial: emptyList()
 *                  Updated by: movAlert() - adds new event to front
 *
 * @see SpotItViewModel - Manages this state
 * @see HomeScreen - Displays monitoring and alarm status
 * @see HistoryScreen - Displays events list
 */
data class SpotItUiState(
    // Monitoring status flag
    // Controls: HomeScreen button text, service running state
    // true: Shows "STOP MONITORING", service running
    // false: Shows "START MONITORING", service stopped
    val isMonitoring: Boolean = false,

    // Alarm triggered status flag
    // Controls: HomeScreen alarm status display, visual alerts
    // true: Shows "TRIGGERED" in red, alarm active
    // false: Shows "IDLE" in blue, no alarm
    val alarmTriggered: Boolean = false,

    // Current location text for display and alerts
    // Updated continuously by FusedLocationProviderClient
    // Format: "Lat: 37.98765, Lng: 23.76543"
    // Used in SMS alerts and UI display
    // Initial value before GPS fix obtained
    val locationText: String = "Unknown Location",

    // Current application settings
    // Loaded from DataStore via repository
    // Contains all user-configurable options
    val settings: AppSettings = AppSettings(),

    // Alert event history (most recent first)
    // New events prepended to list
    // Limited to 30 events to prevent unbounded growth
    // Displayed in HistoryScreen
    val events: List<AlertEvent> = emptyList()
)
