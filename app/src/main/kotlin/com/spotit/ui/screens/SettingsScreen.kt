/**
 * SPOTit Anti-Theft Application
 *
 * SettingsScreen.kt - Settings Configuration Screen
 *
 * This file implements the settings screen where users configure the anti-theft
 * application. It provides a form interface for editing emergency contact,
 * security pattern, motion sensitivity, and alert preferences.
 *
 * Key Features:
 * - Emergency contact phone number input
 * - Security unlock pattern configuration
 * - Motion detection sensitivity slider
 * - SMS and siren toggle switches
 * - Form validation and save functionality
 *
 * @author Panos Biazis
 * @date 10/4/2026
 * @version 2.0
 *
 * Copyright (c) 2026 Panos Biazis. All rights reserved.
 */

// Package declaration - defines the UI screens package
// The screens package contains the main screen composables
package com.spotit.ui.screens

// =============================================================================
// ANDROID FRAMEWORK IMPORTS
// =============================================================================

import android.widget.Toast // Android toast notifications for user feedback

// =============================================================================
// COMPOSE FOUNDATION IMPORTS
// =============================================================================

import androidx.compose.foundation.layout.Arrangement // Layout arrangement options
import androidx.compose.foundation.layout.Column // Vertical layout container
import androidx.compose.foundation.layout.fillMaxWidth // Fill width modifier
import androidx.compose.foundation.layout.padding // Padding modifier

// =============================================================================
// COMPOSE MATERIAL 3 IMPORTS
// =============================================================================

import androidx.compose.material3.Button // Button component
import androidx.compose.material3.OutlinedTextField // Text input field with outline
import androidx.compose.material3.Slider // Slider for continuous value selection
import androidx.compose.material3.Switch // Toggle switch component
import androidx.compose.material3.Text // Text display component

// =============================================================================
// COMPOSE RUNTIME IMPORTS
// =============================================================================

import androidx.compose.runtime.Composable // Composable function annotation
import androidx.compose.runtime.getValue // State value delegate
import androidx.compose.runtime.mutableStateOf // Mutable state holder
import androidx.compose.runtime.saveable.rememberSaveable // State that survives config changes
import androidx.compose.runtime.setValue // State value setter delegate

// =============================================================================
// COMPOSE UI IMPORTS
// =============================================================================

import androidx.compose.ui.Modifier // UI modifier interface
import androidx.compose.ui.graphics.Color // Color definitions
import androidx.compose.ui.platform.LocalContext // Access to Android context
import androidx.compose.ui.unit.dp // Density-independent pixels

// =============================================================================
// APPLICATION IMPORTS
// =============================================================================

import com.spotit.data.AppSettings // Settings data class
import com.spotit.ui.components.BorderedPanel // Bordered container component
import com.spotit.ui.components.Item // Content wrapper component
import com.spotit.ui.components.SectionHeader // Section header component
import com.spotit.ui.components.StatusRow // Label-value display component

/**
 * SettingsScreen - Application Settings Configuration Screen
 *
 * This composable provides the complete settings interface for the SPOTit
 * application. Users can configure all aspects of the anti-theft protection
 * through this form-based interface.
 *
 * Screen Layout:
 * ```
 * +----------------------------------+
 * | SectionHeader: "Settings"        |
 * +----------------------------------+
 * | BorderedPanel:                   |
 * |   Contact Number Input           |
 * |   Pattern Input                  |
 * |   Sensitivity Slider             |
 * |   SMS Toggle + Status            |
 * |   Siren Toggle + Status          |
 * |   Save Button                    |
 * +----------------------------------+
 * ```
 *
 * Settings Configured:
 *
 * 1. **Notification Contact**: The phone number that receives SMS alerts
 *    when motion is detected. Should be a trusted emergency contact.
 *
 * 2. **Security Pattern**: The dot sequence required to disable monitoring.
 *    Provides security against unauthorized stopping of protection.
 *
 * 3. **Motion Sensitivity**: The threshold for motion detection.
 *    Lower values = more sensitive (detects smaller movements).
 *    Higher values = less sensitive (requires larger movements).
 *
 * 4. **SMS Alerts**: Toggle for sending SMS messages to the contact.
 *    When enabled, sends location-based alerts every 5 seconds during alarm.
 *
 * 5. **Siren**: Toggle for audible alarm and vibration.
 *    When enabled, plays alarm sound and vibrates on motion detection.
 *
 * State Management:
 * - Uses rememberSaveable to survive configuration changes (rotation)
 * - Initial values from the passed-in settings parameter
 * - Local state updated via onValueChange callbacks
 * - Save creates new AppSettings object and calls onSave callback
 *
 * Form Validation:
 * - Contact number: Accepts any string (validation could be added)
 * - Pattern: Accepts any string in "x-y-z" format
 * - Sensitivity: Clamped by Slider valueRange (0.1f to 5.0f)
 * - SMS/Siren: Boolean toggles, no validation needed
 *
 * User Feedback:
 * - Toast message shown after successful save
 * - "Settings saved successfully!" message
 *
 * Accessibility:
 * - All inputs have labels for screen readers
 * - Touch targets meet minimum size requirements
 * - Sufficient contrast for all text elements
 *
 * @param settings Current application settings to display in the form.
 * Used to initialize form field values.
 * @param onSave Callback invoked when user taps "Save" button.
 * Receives the new AppSettings object with all form values.
 *
 * @see AppSettings - The data class containing all settings
 * @see SettingsStore - Handles persistence of settings
 * @see SpotItViewModel.saveSettings - Processes the save operation
 */
@Composable
fun SettingsScreen(
    // Current settings to display and edit
    settings: AppSettings,
    // Callback when user saves changes
    onSave: (AppSettings) -> Unit
) {
    // =========================================================================
    // LOCAL STATE - Form Field Values
    // =========================================================================

    /**
     * Context for showing Toast messages.
     *
     * LocalContext.current provides access to the Android context
     * within the composable. Used here to show a toast notification
     * after settings are saved.
     */
    val context = LocalContext.current

    /**
     * Contact number state.
     *
     * Holds the emergency contact phone number entered by the user.
     * Initialized from the current settings.
     * Remembered across configuration changes.
     *
     * Format: International format recommended (e.g., "+306912345678")
     * Empty string means SMS alerts are effectively disabled.
     */
    var contact by rememberSaveable {
        mutableStateOf(settings.contactNumber)
    }

    /**
     * Security pattern state.
     *
     * Holds the unlock pattern entered by the user.
     * Initialized from the current settings.
     * Remembered across configuration changes.
     *
     * Format: Dot numbers separated by dashes (e.g., "1-2-3-6")
     * Dots are numbered 0-9 in a 3x3+1 layout.
     */
    var pattern by rememberSaveable {
        mutableStateOf(settings.pattern)
    }

    /**
     * Motion sensitivity state.
     *
     * Holds the sensitivity threshold for motion detection.
     * Initialized from the current settings.
     * Remembered across configuration changes.
     *
     * Range: 0.1f (very sensitive) to 5.0f (less sensitive)
     * Default: 0.8f (appropriate for linear_acceleration sensor)
     */
    var sensitivity by rememberSaveable {
        mutableStateOf(settings.sensitivity)
    }

    /**
     * SMS enabled state.
     *
     * Whether SMS alerts are enabled.
     * Initialized from the current settings.
     * Remembered across configuration changes.
     *
     * true: Send SMS to contact during alarm
     * false: No SMS alerts (siren only)
     */
    var sms by rememberSaveable {
        mutableStateOf(settings.smsEnabled)
    }

    /**
     * Siren enabled state.
     *
     * Whether the audible alarm is enabled.
     * Initialized from the current settings.
     * Remembered across configuration changes.
     *
     * true: Play alarm sound and vibrate on motion
     * false: Silent mode (visual notification only)
     */
    var siren by rememberSaveable {
        mutableStateOf(settings.sirenEnabled)
    }

    // =========================================================================
    // SCREEN CONTENT - Settings Form
    // =========================================================================

    /**
     * Main content column with consistent spacing.
     *
     * Uses 16dp spacing between child elements for visual consistency.
     */
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // =====================================================================
        // SECTION HEADER
        // =====================================================================

        /**
         * Section header for settings.
         *
         * Provides a visually distinct header that separates settings
         * from other content in the app.
         */
        SectionHeader("Settings")

        // =====================================================================
        // SETTINGS FORM - Bordered Panel Container
        // =====================================================================

        /**
         * BorderedPanel containing all form fields.
         *
         * The panel provides a consistent container with border styling
         * that visually groups all settings together.
         */
        BorderedPanel {
            Item {
                /**
                 * Form fields column with consistent spacing.
                 *
                 * 14dp spacing provides comfortable separation between
                 * different form elements without being too spacious.
                 */
                Column(
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // =========================================================
                    // CONTACT NUMBER INPUT
                    // =========================================================

                    /**
                     * Emergency contact phone number input field.
                     *
                     * OutlinedTextField provides a Material Design text input
                     * with an outline border that becomes highlighted on focus.
                     *
                     * The user enters the phone number of the person who should
                     * receive SMS alerts when motion is detected.
                     */
                    OutlinedTextField(
                        // Current value from local state
                        value = contact,
                        // Update local state when text changes
                        onValueChange = { contact = it },
                        // Fill available width
                        modifier = Modifier.fillMaxWidth(),
                        // Label shown above/beside the field
                        label = {
                            Text("Notification Contact")
                        }
                    )

                    // =========================================================
                    // SECURITY PATTERN INPUT
                    // =========================================================

                    /**
                     * Security pattern input field.
                     *
                     * The user enters the dot sequence needed to disable
                     * monitoring. This prevents unauthorized stopping.
                     *
                     * Format: Numbers separated by dashes (e.g., "1-2-3-6")
                     */
                    OutlinedTextField(
                        // Current pattern from local state
                        value = pattern,
                        // Update local state when text changes
                        onValueChange = { pattern = it },
                        // Fill available width
                        modifier = Modifier.fillMaxWidth(),
                        // Label explaining the format
                        label = {
                            Text("Pattern like 0-1-2-5")
                        }
                    )

                    // =========================================================
                    // SENSITIVITY SLIDER DISPLAY
                    // =========================================================

                    /**
                     * Current sensitivity value display.
                     *
                     * Shows the user the current sensitivity setting
                     * formatted to one decimal place for readability.
                     *
                     * Examples: "Sensitivity Sensor: 0.8", "Sensitivity Sensor: 2.5"
                     */
                    Text("Sensitivity Sensor: ${"%.1f".format(sensitivity)}")

                    // =========================================================
                    // SENSITIVITY SLIDER
                    // =========================================================

                    /**
                     * Motion sensitivity slider control.
                     *
                     * Allows the user to adjust the motion detection threshold.
                     * Lower values = more sensitive (detects small movements)
                     * Higher values = less sensitive (requires larger movements)
                     *
                     * Value Range: 0.1f to 5.0f
                     * - Appropriate for linear_acceleration sensor (m/s²)
                     * - Previous ranges (12f) were for raw accelerometer
                     */
                    Slider(
                        // Current sensitivity from local state
                        value = sensitivity,
                        // Update local state when slider moves
                        onValueChange = { sensitivity = it },
                        // Constrain values to appropriate range
                        valueRange = 0.1f..5f
                        // Lower range for linear_acceleration sensor
                        // Linear acceleration reports in m/s² (smaller values)
                        // Raw accelerometer includes gravity (~9.8 m/s²)
                    )

                    // =========================================================
                    // SMS TOGGLE SECTION
                    // =========================================================

                    /**
                     * SMS alert status display.
                     *
                     * Shows the current SMS setting with visual indication
                     * of the state (ON/OFF).
                     */
                    StatusRow(
                        label = "SMS",
                        value = if (sms) "ON" else "OFF",
                        // Use default color (no semantic color here)
                        color = Color.Unspecified
                    )

                    /**
                     * SMS enable/disable switch.
                     *
                     * Toggle for enabling or disabling SMS alerts.
                     * When ON: Sends SMS to contact during alarm
                     * When OFF: No SMS alerts (rely on siren only)
                     */
                    Switch(
                        // Current state from local state
                        checked = sms,
                        // Update local state when toggled
                        onCheckedChange = { sms = it }
                    )

                    // =========================================================
                    // SIREN TOGGLE SECTION
                    // =========================================================

                    /**
                     * Siren status display.
                     *
                     * Shows the current siren setting with visual indication
                     * of the state (ON/OFF).
                     */
                    StatusRow(
                        label = "Siren",
                        value = if (siren) "ON" else "OFF",
                        // Use default color (no semantic color here)
                        color = Color.Unspecified
                    )

                    /**
                     * Siren enable/disable switch.
                     *
                     * Toggle for enabling or disabling the audible alarm.
                     * When ON: Plays alarm sound and vibrates on motion
                     * When OFF: Silent mode (visual notification only)
                     */
                    Switch(
                        // Current state from local state
                        checked = siren,
                        // Update local state when toggled
                        onCheckedChange = { siren = it }
                    )

                    // =========================================================
                    // SAVE BUTTON
                    // =========================================================

                    /**
                     * Save settings button.
                     *
                     * When clicked:
                     * 1. Creates new AppSettings object with all form values
                     * 2. Calls onSave callback with new settings
                     * 3. Shows toast notification for user feedback
                     *
                     * The settings are persisted by the ViewModel through
                     * the repository to DataStore.
                     */
                    Button(
                        onClick = {
                            // Create new settings object with all form values
                            val newSettings = AppSettings(
                                contactNumber = contact,
                                pattern = pattern,
                                sensitivity = sensitivity,
                                smsEnabled = sms,
                                sirenEnabled = siren
                            )

                            // Call the save callback
                            onSave(newSettings)

                            // Show success notification to user
                            Toast.makeText(
                                context,
                                "Settings saved successfully!",
                                Toast.LENGTH_SHORT
                            ).show()
                        },
                        // Fill available width for easy tapping
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Button text
                        Text("Save")
                    }
                }
            }
        }
    }
}

/**
 * Additional Architecture Notes:
 *
 * State Management:
 * - Each form field has its own local state using rememberSaveable
 * - rememberSaveable ensures state survives configuration changes
 * - Initial values come from the settings parameter
 * - onSave callback returns new AppSettings object
 *
 * Alternative Approaches Considered:
 * 1. ViewModel-managed state: More complex, overkill for simple form
 * 2. Unidirectional data flow: Would require more boilerplate
 * 3. Form validation library: Not needed for this simple use case
 *
 * Future Enhancements:
 * 1. Add phone number validation (format checking)
 * 2. Add pattern validation (valid dot numbers, minimum length)
 * 3. Add pattern preview/visualization
 * 4. Add sensitivity preset options (Low/Medium/High)
 * 5. Add contact picker integration
 * 6. Add explanation tooltips for each setting
 * 7. Add reset to defaults button
 * 8. Add undo functionality for changes
 *
 * Form Validation Ideas:
 * - Contact: Validate phone number format
 * - Pattern: Validate format and minimum dots
 * - Sensitivity: Add presets with descriptions
 *
 * Accessibility Improvements:
 * - Add content descriptions for switches
 * - Add helper text explaining each setting
 * - Add keyboard navigation support
 * - Add larger touch targets for switches
 *
 * User Experience Notes:
 * - Form uses standard Material Design components
 * - Consistent spacing throughout
 * - Clear labels for all inputs
 * - Visual feedback on save (toast)
 */
