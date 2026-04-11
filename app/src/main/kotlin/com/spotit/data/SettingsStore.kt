/**
 * SPOTit Anti-Theft Application
 *
 * SettingsStore.kt - Persistent Settings Management with DataStore
 *
 * This file provides a wrapper around Android's DataStore Preferences API
 * for persisting user settings. DataStore is the modern replacement for
 * SharedPreferences, offering better type safety, coroutines support,
 * and error handling.
 *
 * Key Features:
 * - Type-safe preference keys
 * - Coroutines-based async operations
 * - Versioned settings for migration support
 * - Default values with fallback handling
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
// ANDROID FRAMEWORK IMPORTS
// =============================================================================

import android.content.Context // Android context for accessing storage

// =============================================================================
// DATASTORE PREFERENCES IMPORTS
// =============================================================================

// Preference key type definitions for type-safe access
import androidx.datastore.preferences.core.booleanPreferencesKey // Boolean key
import androidx.datastore.preferences.core.edit // Extension for editing preferences
import androidx.datastore.preferences.core.floatPreferencesKey // Float key
import androidx.datastore.preferences.core.intPreferencesKey // Int key
import androidx.datastore.preferences.core.stringPreferencesKey // String key

// DataStore Preferences delegate
import androidx.datastore.preferences.preferencesDataStore // Creates DataStore instance

// =============================================================================
// KOTLIN COROUTINES IMPORTS
// =============================================================================

import kotlinx.coroutines.flow.Flow // Cold asynchronous data stream
import kotlinx.coroutines.flow.map // Transforms Flow emissions

// =============================================================================
// DATASTORE DELEGATE - Extension Property
// =============================================================================

/**
 * DataStore extension property for Context.
 *
 * This delegate creates a named DataStore instance for storing preferences.
 * The 'by preferencesDataStore' delegate ensures:
 * - Singleton behavior (same instance for same name)
 * - Automatic file creation and management
 * - Thread-safe access
 *
 * File location: /data/data/com.spotit/files/datastore/spotit_settings.preferences_pb
 *
 * The DataStore file is created lazily on first access and persists across
 * app restarts and device reboots.
 *
 * Parameter explanation:
 * - name: "spotit_settings" - Unique identifier for this DataStore instance
 *   Having a unique name allows multiple DataStores in one app if needed.
 */
private val Context.dataStore by preferencesDataStore(
    name = "spotit_settings" // DataStore file name (without extension)
)

// =============================================================================
// SETTINGS STORE CLASS
// =============================================================================

/**
 * SettingsStore - Wrapper for DataStore Preferences Operations
 *
 * This class provides a clean API for reading and writing application settings
 * using Android's DataStore Preferences. It abstracts away the complexity of
 * DataStore's Flow-based API and provides:
 *
 * 1. **Type-Safe Keys**: Centralized key definitions in the Keys object
 * 2. **Default Values**: Sensible defaults for all settings
 * 3. **Version Migration**: Automatic settings migration when defaults change
 * 4. **Flow-Based Reading**: Reactive settings that update automatically
 * 5. **Suspend Writing**: Coroutine-based async writes
 *
 * Architecture:
 * - This class is created in SpotItRepository (repository layer)
 * - It's scoped to the application context (not activity)
 * - Settings are exposed as a Flow (not LiveData) for coroutines support
 *
 * DataStore vs SharedPreferences:
 * - DataStore is async (coroutines), SharedPreferences is sync
 * - DataStore has better error handling and corruption recovery
 * - DataStore ensures data consistency with transactions
 * - SharedPreferences is deprecated for new development
 *
 * Usage Example:
 * ```kotlin
 * // Reading settings
 * settingsStore.settingsFlow.collect { settings ->
 *     updateUi(settings)
 * }
 *
 * // Writing settings
 * settingsStore.save(newSettings)
 * ```
 *
 * @property context The application context used to access DataStore.
 *                   Must be application context to avoid memory leaks.
 *
 * @see AppSettings - The data class representing settings values
 * @see SpotItRepository - Uses this class for persistence
 */
class SettingsStore(private val context: Context) {

    // =========================================================================
    // PREFERENCE KEYS - Type-Safe Key Definitions
    // =========================================================================

    /**
     * Keys object containing all preference key definitions.
     *
     * This object centralizes all preference keys in one place, providing:
     * - Type safety (each key has a specific type)
     * - Compile-time checking of key names
     * - Easy refactoring if key names need to change
     * - Single source of truth for all preference keys
     *
     * Key naming convention: Simple, lowercase names that describe the value.
     * The actual key name is the string passed to the *PreferencesKey()
     * constructor, which must match when reading and writing.
     *
     * Available Key Types:
     * - stringPreferencesKey: For String values (text, phone numbers, patterns)
     * - intPreferencesKey: For Int values (version numbers, counters)
     * - floatPreferencesKey: For Float values (sensitivity, thresholds)
     * - booleanPreferencesKey: For Boolean values (enable/disable flags)
     * - longPreferencesKey: For Long values (timestamps, large counters)
     * - doublePreferencesKey: For Double values (precise measurements)
     */
    private object Keys {
        /**
         * Key for emergency contact phone number.
         * Type: String
         * Example value: "+306912345678"
         * Default: "" (empty string)
         */
        val contact = stringPreferencesKey("contact")

        /**
         * Key for security unlock pattern.
         * Type: String
         * Example value: "1-2-3-6" (dot sequence)
         * Default: "1-2-3-6"
         */
        val pattern = stringPreferencesKey("pattern")

        /**
         * Key for motion detection sensitivity threshold.
         * Type: Float
         * Range: 0.1f to 5.0f
         * Default: 0.8f
         */
        val sensitivity = floatPreferencesKey("sensitivity")

        /**
         * Key for SMS alert enable/disable flag.
         * Type: Boolean
         * Default: true (enabled)
         */
        val sms = booleanPreferencesKey("sms")

        /**
         * Key for siren/alarm sound enable/disable flag.
         * Type: Boolean
         * Default: true (enabled)
         */
        val siren = booleanPreferencesKey("siren")

        /**
         * Key for settings schema version number.
         * Type: Int
         * Used for migration when default values change.
         * Default: 1 (if not set, assume oldest version)
         */
        val version = intPreferencesKey("settings_version")
    }

    // =========================================================================
    // COMPANION OBJECT - Constants and Static Members
    // =========================================================================

    /**
     * Companion object containing static constants.
     *
     * This provides the settings version constant that is used for
     * migrating settings when default values change between app versions.
     */
    companion object {
        /**
         * Current settings schema version.
         *
         * This version number is incremented when:
         * - Default values change
         * - New settings are added
         * - Settings are removed
         * - Settings are renamed or restructured
         *
         * Migration Strategy:
         * When a user updates the app, their saved settings may have an older
         * version number. The settingsFlow checks this and applies migration
         * logic to update outdated values to current defaults.
         *
         * Version History:
         * - Version 1: Initial release (sensitivity ~12f)
         * - Version 2: Lowered sensitivity to 2.5f
         * - Version 3: Lowered sensitivity to 0.8f for linear_acceleration sensor
         *              (linear_acceleration reports in m/s², much smaller values)
         *
         * Migration Example:
         * If saved version is 2 and current is 3, and sensitivity > 5f,
         * reset to 0.8f (the new appropriate default for this sensor type).
         */
        // Current settings version - increment this when default values change
        // Version 3: Lowered sensitivity threshold to 0.8f for better detection
        private const val CURRENT_VERSION = 3
    }

    // =========================================================================
    // SETTINGS FLOW - Reactive Settings Reading
    // =========================================================================

    /**
     * Flow of AppSettings that emits whenever settings change.
     *
     * This Flow provides reactive access to settings. It:
     * - Emits the current settings when first collected
     * - Emits new values whenever settings are updated via save()
     * - Runs indefinitely (use collect in viewModelScope)
     * - Handles migration for older settings versions
     *
     * Migration Logic:
     * The flow includes automatic migration logic that:
     * 1. Checks the saved settings version
     * 2. If version is outdated, applies migration rules
     * 3. Returns updated settings with appropriate values
     *
     * Sensitivity Migration:
     * Linear acceleration sensor values are much smaller than raw accelerometer
     * values (m/s² vs including gravity). The migration ensures:
     * - Old high sensitivity values (>5) are reset to 0.8f
     * - Values appropriate for linear_acceleration are preserved
     *
     * Flow Operators Used:
     * - context.dataStore.data: Access the raw preferences Flow
     * - .map { }: Transform Preferences to AppSettings
     *
     * Thread Safety:
     * DataStore ensures all reads and writes are thread-safe.
     * The map operation runs on the collector's coroutine context.
     *
     * Error Handling:
     * DataStore automatically handles corruption by:
     * - Creating a new file if corrupted
     * - Using backup data if available
     * - Returning default values if all else fails
     *
     * @return Flow<AppSettings> that emits the current settings and
     *         any subsequent updates.
     */
    val settingsFlow: Flow<AppSettings> = context.dataStore.data.map { prefs ->
        // =================================================================
        // SETTINGS VERSION CHECK AND MIGRATION
        // =================================================================

        // Get the saved version, defaulting to 1 if never saved
        // Version 1 was the initial release without version tracking
        val savedVersion = prefs[Keys.version] ?: 1

        // ================================================================
        // SENSITIVITY MIGRATION LOGIC
        // ================================================================

        // Determine the appropriate sensitivity value:
        // 1. If version is outdated (< CURRENT_VERSION), use new default
        // 2. If sensitivity is too high (> 5f), it's from old accelerometer logic
        // 3. Otherwise, use the user's saved preference
        //
        // Linear acceleration sensor typically reports:
        // - 0.1-1.5 m/s² for normal movement (walking, picking up device)
        // - Values above 5 m/s² would be extreme movement
        //
        // Old accelerometer-based values (12f, 2.5f) are inappropriate
        // for linear_acceleration and would prevent detection
        val sensitivity = if (savedVersion < CURRENT_VERSION ||
            (prefs[Keys.sensitivity] ?: 0.8f) > 5f
        ) {
            // Reset to appropriate default for linear_acceleration sensor
            // This ensures detection works correctly with the new sensor
            0.8f // Appropriate default for linear_acceleration sensor
        } else {
            // Use the saved value - it's within appropriate range
            prefs[Keys.sensitivity] ?: 0.8f
        }

        // =================================================================
        // BUILD AND RETURN APP SETTINGS
        // =================================================================

        /**
         * Build AppSettings from preferences.
         *
         * For each setting:
         * - Try to get the saved value from preferences
         * - If not found (null), use the default value
         *
         * This handles:
         * - First run (no saved values)
         * - Missing keys (corruption recovery)
         * - New settings added in updates
         */
        AppSettings(
            // Emergency contact - default empty (user must configure)
            contactNumber = prefs[Keys.contact] ?: "",

            // Security pattern - default "1-2-3-6"
            pattern = prefs[Keys.pattern] ?: "1-2-3-6",

            // Sensitivity - use migration logic above
            sensitivity = sensitivity,

            // SMS alerts - default enabled
            smsEnabled = prefs[Keys.sms] ?: true,

            // Siren - default enabled
            sirenEnabled = prefs[Keys.siren] ?: true
        )
    }

    // =========================================================================
    // SAVE METHOD - Persist Settings to DataStore
    // =========================================================================

    /**
     * Saves the provided settings to DataStore.
     *
     * This is a suspend function that performs an asynchronous write
     * operation. It must be called from a coroutine scope.
     *
     * Write Behavior:
     * - Uses DataStore.edit {} for atomic, transactional updates
     * - Overwrites all settings values
     * - Updates the version to CURRENT_VERSION
     * - Completion is signaled by coroutine completion
     *
     * Thread Safety:
     * - DataStore handles concurrent access internally
     * - Multiple saves are serialized automatically
     * - No data loss from concurrent writes
     *
     * Error Handling:
     * - Exceptions are thrown to the caller
     * - Common exceptions: IOException (disk full), CorruptionException
     * - Caller should handle errors appropriately
     *
     * Usage:
     * ```kotlin
     * viewModelScope.launch {
     *     try {
     *         settingsStore.save(newSettings)
     *         // Success - settings saved
     *     } catch (e: Exception) {
     *         // Handle error
     *     }
     * }
     * ```
     *
     * @param settings The AppSettings object to persist.
     *                 All fields are saved, overwriting previous values.
     */
    suspend fun save(settings: AppSettings) {
        /**
         * DataStore.edit {} provides transactional updates.
         *
         * The edit function:
         * 1. Reads the current preferences
         * 2. Applies your modifications
         * 3. Writes atomically (all or nothing)
         * 4. Handles concurrent modifications
         *
         * The 'prefs' parameter is a MutablePreferences object
         * that you modify like a map.
         */
        context.dataStore.edit { prefs ->
            // ============================================================
            // SAVE ALL SETTINGS VALUES
            // ============================================================

            // Save emergency contact number
            // Empty string is valid (means SMS disabled)
            prefs[Keys.contact] = settings.contactNumber

            // Save security pattern
            // Format: "x-y-z-w" (dot sequence)
            prefs[Keys.pattern] = settings.pattern

            // Save motion sensitivity
            // Value should be between 0.1f and 5.0f
            prefs[Keys.sensitivity] = settings.sensitivity

            // Save SMS alert flag
            // true = send alerts, false = silent
            prefs[Keys.sms] = settings.smsEnabled

            // Save siren flag
            // true = audible alarm, false = silent
            prefs[Keys.siren] = settings.sirenEnabled

            // ============================================================
            // UPDATE VERSION NUMBER
            // ============================================================

            // Always update to current version when saving
            // This marks that settings have been migrated
            // Future reads will see CURRENT_VERSION
            prefs[Keys.version] = CURRENT_VERSION
        }
    }
}
