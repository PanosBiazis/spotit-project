/**
 * SPOTit Anti-Theft Application
 *
 * HomeScreen.kt - Main Dashboard Screen
 *
 * This file implements the main dashboard screen of the SPOTit application.
 * It serves as the primary user interface where users can view the current
 * system status and toggle the anti-theft monitoring on or off.
 *
 * Screen Components:
 * - System Status Section: Displays monitoring status, alarm status, and location
 * - Dashboard Card: Provides app information and the main toggle button
 * - Toggle Button: Starts or stops the monitoring service
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
// COMPOSE FOUNDATION IMPORTS
// =============================================================================

import androidx.compose.foundation.layout.Arrangement // Layout arrangement
import androidx.compose.foundation.layout.Column // Vertical layout container
import androidx.compose.foundation.layout.fillMaxWidth // Fill width modifier
import androidx.compose.foundation.layout.padding // Padding modifier

// =============================================================================
// COMPOSE UI IMPORTS
// =============================================================================

import androidx.compose.foundation.rememberScrollState // Scroll state for vertical scroll
import androidx.compose.foundation.verticalScroll // Vertical scroll modifier

// =============================================================================
// COMPOSE MATERIAL 3 IMPORTS
// =============================================================================

import androidx.compose.material3.Button // Button component
import androidx.compose.material3.Card // Card container component
import androidx.compose.material3.Text // Text display component

// =============================================================================
// COMPOSE RUNTIME IMPORTS
// =============================================================================

import androidx.compose.runtime.Composable // Composable function annotation

// =============================================================================
// COMPOSE UI UNIT IMPORTS
// =============================================================================

import androidx.compose.ui.Modifier // UI modifier interface
import androidx.compose.ui.text.font.FontWeight // Text font weights
import androidx.compose.ui.unit.dp // Density-independent pixels

// =============================================================================
// APPLICATION IMPORTS
// =============================================================================

import com.spotit.data.SpotItUiState // UI state data class
import com.spotit.ui.components.BorderedPanel // Bordered panel component
import com.spotit.ui.components.Item // Item wrapper component
import com.spotit.ui.components.SectionHeader // Section header component
import com.spotit.ui.components.StatusRow // Status row component

// =============================================================================
// THEME IMPORTS
// =============================================================================

import com.spotit.ui.theme.AlarmRed // Alert/error color
import com.spotit.ui.theme.PrimaryBlue // Primary brand color
import com.spotit.ui.theme.StatusGreen // Active/success color

// =============================================================================
// LOGGING IMPORT
// =============================================================================

import android.util.Log // Android logging system

// =============================================================================
// HOME SCREEN COMPOSABLE
// =============================================================================

/**
 * HomeScreen - Main Dashboard Screen Composable
 *
 * This composable function renders the main dashboard of the SPOTit anti-theft
 * application. It provides users with a quick overview of the system status
 * and a simple interface to control the monitoring service.
 *
 * Screen Layout:
 * ```
 * +----------------------------------+
 * | SectionHeader: "System Status"   |
 * +----------------------------------+
 * | BorderedPanel:                   |
 * |   StatusRow: Monitoring: ON/OFF |
 * |   StatusRow: Alarm: TRIGGERED/IDLE |
 * |   StatusRow: Location: ...       |
 * +----------------------------------+
 *                                  |
 * +----------------------------------+
 * | Card: "SPOTit Dashboard"        |
 * |   Description text              |
 * |   [START/STOP MONITORING] button|
 * +----------------------------------+
 * ```
 *
 * Key Features:
 * 1. **Real-time Status Display**: Shows current monitoring and alarm states
 * 2. **Location Information**: Displays current GPS coordinates
 * 3. **One-Tap Control**: Single button to start/stop monitoring
 * 4. **Visual Feedback**: Color-coded status indicators
 *
 * State Management:
 * - Receives state from parent (MainActivity) via SpotItUiState parameter
 * - State updates trigger recomposition automatically
 * - Toggle action is delegated to parent via onToggle callback
 *
 * Color Coding:
 * - Monitoring ON: StatusGreen (device is protected)
 * - Monitoring OFF: AlarmRed (device is at risk)
 * - Alarm TRIGGERED: AlarmRed (alert in progress)
 * - Alarm IDLE: PrimaryBlue (normal state)
 * - Location: PrimaryBlue (neutral information)
 *
 * Accessibility:
 * - Clear text labels with color coding as supplementary information
 * - Large touch target for toggle button
 * - Scrollable content for smaller screens
 *
 * @param state The current UI state containing monitoring status, alarm status,
 *              location, settings, and event history. This state is observed
 *              and the screen recomposes when state changes.
 * @param onToggle Callback function invoked when the user taps the toggle button.
 *                 This triggers either startMonitoring() or stopMonitoring()
 *                 in the ViewModel, depending on the current isMonitoring state.
 *
 * @see SpotItUiState - The data class providing all UI state
 * @see SpotItViewModel - Manages the state and handles toggle action
 * @see MainActivity - Hosts this screen and provides state
 */
@Composable
fun HomeScreen(
    // Current UI state from ViewModel
    state: SpotItUiState,
    // Toggle callback - called when user taps start/stop button
    onToggle: () -> Unit
) {
    /**
     * Main content container.
     *
     * The Column uses verticalScroll to handle content overflow on smaller
     * screens. The scroll state is remembered across recompositions.
     */
    Column(
        // Enable vertical scrolling for overflow content
        modifier = Modifier.verticalScroll(rememberScrollState()),
        // Spacing between child elements
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // =========================================================================
        // SYSTEM STATUS SECTION
        // =========================================================================

        /**
         * Section Header for System Status.
         *
         * Provides a visually distinct header that identifies this section
         * as containing the current system status information.
         */
        SectionHeader("System Status")

        /**
         * Bordered Panel containing status rows.
         *
         * This panel groups all status information into a bordered container,
         * creating visual separation from other content.
         */
        BorderedPanel {
            /**
             * Item wrapper for status content.
             *
             * Provides consistent styling and spacing for the status rows.
             */
            Item {
                /**
                 * Status rows with vertical spacing.
                 *
                 * Each StatusRow displays a label-value pair with appropriate
                 * color coding to indicate current state.
                 */
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    /**
                     * Monitoring Status Row.
                     *
                     * Shows whether the anti-theft monitoring is currently active.
                     * - ON (green): Service running, device protected
                     * - OFF (red): Service stopped, device at risk
                     */
                    StatusRow(
                        label = "Monitoring",
                        value = if (state.isMonitoring) "ON" else "OFF",
                        // Green when monitoring active, red when inactive
                        color = if (state.isMonitoring) StatusGreen else AlarmRed
                    )

                    /**
                     * Alarm Status Row.
                     *
                     * Shows whether an alarm is currently triggered.
                     * - TRIGGERED (red): Motion detected, alert active
                     * - IDLE (blue): No alert, normal operation
                     *
                     * Logs the alarm state for debugging purposes.
                     */
                    StatusRow(
                        label = "Alarm",
                        value = if (state.alarmTriggered) "TRIGGERED" else "IDLE",
                        // Red when triggered, blue when idle
                        color = if (state.alarmTriggered) AlarmRed else PrimaryBlue
                    )

                    /**
                     * Debug logging for alarm state.
                     *
                     * This log statement helps track alarm state changes
                     * during development and debugging. Can be removed
                     * in production builds.
                     */
                    Log.d("ALARM", if (state.alarmTriggered) "ALARM" else "NO ALARM")

                    /**
                     * Location Status Row.
                     *
                     * Shows the current GPS coordinates or location status.
                     * Format: "Lat: xx.xxxxx, Lng: xx.xxxxx"
                     * Or: "Unknown Location" if not yet available
                     *
                     * Uses PrimaryBlue as a neutral color since location
                     * is informational and doesn't indicate status.
                     */
                    StatusRow(
                        label = "Location",
                        value = state.locationText,
                        // Blue for neutral informational display
                        color = PrimaryBlue
                    )
                }
            }
        }

        // =========================================================================
        // DASHBOARD CARD
        // =========================================================================

        /**
         * Dashboard Information Card.
         *
         * This card provides:
         * - Application branding ("SPOTit Dashboard")
         * - Brief description of functionality
         * - Main action button for controlling monitoring
         *
         * The card fills the width and provides padding around content.
         */
        Card(
            modifier = Modifier.fillMaxWidth()
        ) {
            /**
             * Card content column.
             *
             * Contains the title, description, and toggle button
             * with consistent spacing.
             */
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                /**
                 * Dashboard Title.
                 *
                 * Displays "SPOTit Dashboard" in bold for branding
                 * and visual hierarchy.
                 */
                Text(
                    text = "SPOTit Dashboard",
                    fontWeight = FontWeight.Bold
                )

                /**
                 * Dashboard Description.
                 *
                 * Provides a brief explanation of the toggle button's
                 * functionality, helping users understand how to use
                 * the app.
                 */
                Text("Toggle monitoring activity with a single tap.")

                /**
                 * Toggle Button.
                 *
                 * The main action button for controlling monitoring:
                 * - When monitoring is OFF: Shows "START MONITORING"
                 * - When monitoring is ON: Shows "STOP MONITORING"
                 *
                 * Clicking triggers the onToggle callback, which:
                 * 1. If starting: Starts SpotItForegroundService
                 * 2. If stopping: Shows pattern lock for security
                 *
                 * The button fills the card width for easy tapping.
                 */
                Button(
                    onClick = onToggle,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    /**
                     * Dynamic button text based on monitoring state.
                     *
                     * This provides clear feedback about what action
                     * will be performed when the button is tapped.
                     */
                    Text(if (state.isMonitoring) "STOP MONITORING" else "START MONITORING")

                    /**
                     * Debug logging for monitoring state.
                     *
                     * Logs the current monitoring state when the button
                     * is rendered. Useful for debugging state changes.
                     */
                    Log.d("isMonitoring", if (state.isMonitoring) "MONITORING" else "NO MONITORING")
                }
            }
        }
    }
}

// =============================================================================
// SCREEN USAGE NOTES
// =============================================================================

/**
 * HomeScreen Integration:
 *
 * This screen is displayed in MainActivity when tab == 0 (Home tab).
 * It receives state from the ViewModel and delegates actions via callbacks.
 *
 * Example Usage in MainActivity:
 * ```kotlin
 * when (tab) {
 *     0 -> HomeScreen(
 *         state = state,
 *         onToggle = {
 *             if (state.isMonitoring) {
 *                 // Show pattern lock before stopping
 *                 showPattern = true
 *             } else {
 *                 // Check permissions and start monitoring
 *                 val missingPerms = missingPermissions()
 *                 if (missingPerms.isEmpty()) {
 *                     viewModel.startMonitoring(this@MainActivity)
 *                 } else {
 *                     pendingStart = true
 *                     launcher.launch(missingPerms)
 *                 }
 *             }
 *         }
 *     )
 *     // ... other tabs
 * }
 * ```
 *
 * State Flow:
 * 1. User taps toggle button
 * 2. onToggle callback invoked in MainActivity
 * 3. MainActivity either:
 *    - Starts monitoring (via viewModel.startMonitoring())
 *    - Shows pattern lock (sets showPattern = true)
 * 4. ViewModel updates state
 * 5. HomeScreen recomposes with new state
 *
 * Future Enhancements:
 * - Add animated status indicators
 * - Add last alert time display
 * - Add quick settings shortcuts
 * - Add battery optimization status
 * - Add sensor status indicators
 */
