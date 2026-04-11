/**
 * SPOTit Anti-Theft Application
 *
 * HistoryScreen.kt - Alert History Display Screen
 *
 * This file implements the History screen that displays a list of past alert
 * events. Users can view their alert history to see when motion was detected,
 * what location was recorded, and when each alert occurred.
 *
 * Key Features:
 * - Displays list of AlertEvent objects in reverse chronological order
 * - Shows alert title, details, and timestamp
 * - Handles empty state with user-friendly message
 * - Scrollable list for viewing many events
 * - Card-based layout for each event
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

import androidx.compose.foundation.layout.Arrangement // Layout arrangement options
import androidx.compose.foundation.layout.Column // Vertical layout container
import androidx.compose.foundation.layout.padding // Padding modifier
import androidx.compose.foundation.lazy.LazyColumn // Efficient scrollable list
import androidx.compose.foundation.lazy.items // Lazy list item builder

// =============================================================================
// COMPOSE MATERIAL 3 IMPORTS
// =============================================================================

import androidx.compose.material3.Card // Card container component
import androidx.compose.material3.Text // Text display component

// =============================================================================
// COMPOSE RUNTIME IMPORTS
// =============================================================================

import androidx.compose.runtime.Composable // Composable function annotation

// =============================================================================
// COMPOSE UI IMPORTS
// =============================================================================

import androidx.compose.ui.Modifier // UI modifier interface
import androidx.compose.ui.text.font.FontWeight // Text font weights
import androidx.compose.ui.unit.dp // Density-independent pixels

// =============================================================================
// APPLICATION IMPORTS
// =============================================================================

import com.spotit.data.AlertEvent // Alert event data model
import com.spotit.ui.components.BorderedPanel // Bordered container component
import com.spotit.ui.components.Item // Content wrapper component
import com.spotit.ui.components.SectionHeader // Section header component

/**
 * HistoryScreen - Alert Event History Display
 *
 * This composable creates the History screen that shows a list of all recorded
 * alert events. Each event displays the alert title, details (including location),
 * and timestamp.
 *
 * Screen Layout:
 * ```
 * +----------------------------------+
 * |       Ιστορικό (History)         |  <- SectionHeader (blue)
 * +----------------------------------+
 * |                                  |
 * |  +----------------------------+ |
 * |  | Alert                      | |
 * |  | Movement detected at...    | |
 * |  | 10/04/2026 14:30:25        | |
 * |  +----------------------------+ |
 * |                                  |
 * |  +----------------------------+ |
 * |  | Alert                      | |
 * |  | Movement detected at...    | |
 * |  | 10/04/2026 12:15:10        | |
 * |  +----------------------------+ |
 * |                                  |
 * |         ... more events          |
 * |                                  |
 * +----------------------------------+
 * ```
 *
 * Empty State:
 * When there are no events to display, the screen shows a message in Greek:
 * "Δεν υπάρχουν ακόμη καταγεγραμμένα συμβάντα" (No recorded events yet).
 *
 * Event Order:
 * Events are displayed in reverse chronological order (most recent first).
 * This is handled by the ViewModel which adds new events to the front of the list.
 *
 * Scroll Behavior:
 * - Uses LazyColumn for efficient rendering of large lists
 * - Only visible items are composed (performance optimization)
 * - Smooth scrolling for good user experience
 *
 * Accessibility:
 * - Each event is contained in a Card for clear visual separation
 * - Bold title for visual hierarchy
 * - Sufficient padding for touch targets
 * - High contrast text colors
 *
 * Greek Language:
 * The header uses Greek text "Ιστορικό" (History) and the empty state message
 * is in Greek, indicating the target audience is Greek speakers.
 *
 * @param events List of AlertEvent objects to display. The list should be
 * ordered with most recent events first. May be empty, in which
 * case an empty state message is shown.
 *
 * @see AlertEvent - The data model for alert events
 * @see SpotItViewModel.movAlert - Creates AlertEvent instances
 * @see SectionHeader - Blue header component
 * @see BorderedPanel - Bordered container component
 */
@Composable
fun HistoryScreen(
    // List of alert events to display, ordered newest first
    events: List<AlertEvent>
) {
    /**
     * Main column container for the screen content.
     *
     * Uses vertical arrangement with 16dp spacing between elements,
     * matching the overall app layout pattern.
     */
    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        /**
         * Section Header - Displays "Ιστορικό" (History in Greek).
         *
         * This blue header provides clear identification of the screen
         * content and maintains visual consistency with other screens.
         */
        SectionHeader("Ιστορικό")

        /**
         * Bordered Panel - Contains the event list.
         *
         * The panel provides a bordered container with white background
         * for the event content, matching the app's visual design system.
         */
        BorderedPanel {
            /**
             * Item wrapper for content within the bordered panel.
             *
             * This provides proper scoping for the panel content.
             */
            Item {
                /**
                 * Empty State Check.
                 *
                 * If there are no events to display, show a user-friendly
                 * message in Greek explaining that no events have been
                 * recorded yet.
                 */
                if (events.isEmpty()) {
                    /**
                     * Empty state message in Greek.
                     *
                     * "Δεν υπάρχουν ακόμη καταγεγραμμένα συμβάντα"
                     * Translation: "There are no recorded events yet"
                     *
                     * This provides helpful feedback to users who haven't
                     * experienced any alerts, letting them know the history
                     * will populate when alerts occur.
                     */
                    Text("Δεν υπάρχουν ακόμη καταγεγραμμένα συμβάντα")
                } else {
                    /**
                     * Event List - LazyColumn for efficient scrolling.
                     *
                     * LazyColumn is used instead of a regular Column with
                     * modifier.verticalScroll because:
                     * - Better performance for large lists
                     * - Only composes visible items
                     * - Built-in scrolling optimization
                     * - Supports very long lists without performance issues
                     *
                     * The items are ordered with most recent first (handled
                     * by ViewModel), providing a natural reverse-chronological
                     * view of alert history.
                     */
                    LazyColumn(
                        // Vertical spacing between event cards
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        /**
                         * items() - Renders each AlertEvent as a Card.
                         *
                         * Parameters:
                         * - events: The list of items to render
                         * - key: Unique identifier for each item (used for
                         *   efficient recomposition and animations)
                         *
                         * The key uses event.id which is a timestamp, ensuring
                         * unique identification and proper ordering.
                         */
                        items(
                            items = events,
                            key = { event -> event.id } // Unique key for each event
                        ) { event ->
                            /**
                             * Event Card Container.
                             *
                             * Each event is displayed in its own Card, providing:
                             * - Visual separation between events
                             * - Elevated appearance (Material default)
                             * - Rounded corners (Card default)
                             * - Clear boundaries for each event's content
                             */
                            Card {
                                /**
                                 * Card Content Column.
                                 *
                                 * Contains the event details arranged vertically:
                                 * - Title (bold)
                                 * - Details (location and description)
                                 * - Timestamp (when the alert occurred)
                                 *
                                 * 14dp padding provides comfortable spacing
                                 * and breathing room for the content.
                                 */
                                Column(
                                    modifier = Modifier.padding(14.dp),
                                    // 6dp spacing between title, details, and timestamp
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    /**
                                     * Event Title.
                                     *
                                     * Displays the alert title, typically "Alert" or
                                     * "Κίνηση εντοπίστηκε" (Movement detected).
                                     *
                                     * Bold weight provides visual hierarchy,
                                     * making the title stand out as the primary
                                     * identifier of each event.
                                     */
                                    Text(
                                        text = event.title,
                                        fontWeight = FontWeight.Bold
                                    )

                                    /**
                                     * Event Details.
                                     *
                                     * Displays the detailed description of the alert,
                                     * including location information.
                                     *
                                     * Format: "Ύποπτη μετακίνηση συσκευής • Lat: xx.xxxxx, Lng: xx.xxxxx"
                                     * Translation: "Suspicious device movement • [location]"
                                     *
                                     * Regular weight (default) makes this less prominent
                                     * than the title but still easily readable.
                                     */
                                    Text(event.details)

                                    /**
                                     * Event Timestamp.
                                     *
                                     * Displays when the alert occurred.
                                     *
                                     * Format: "dd/MM/yyyy HH:mm:ss"
                                     * Example: "10/04/2026 14:30:25"
                                     *
                                     * Regular weight (default) with smaller visual
                                     * emphasis than title and details.
                                     */
                                    Text(event.timestamp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// =============================================================================
// SCREEN USAGE NOTES
// =============================================================================

/**
 * Usage Example:
 *
 * This screen is used in MainActivity as one of the navigation tabs:
 *
 * ```kotlin
 * when (tab) {
 *     0 -> HomeScreen(state = state, onToggle = { ... })
 *     1 -> SettingsScreen(settings = state.settings, onSave = { ... })
 *     2 -> HistoryScreen(events = state.events) // This screen
 * }
 * ```
 *
 * Data Flow:
 * 1. SpotItViewModel.movAlert() creates AlertEvent when motion detected
 * 2. Event added to SpotItUiState.events list (newest first)
 * 3. UI state flows to MainActivity via collectAsStateWithLifecycle()
 * 4. HistoryScreen receives events list and renders it
 *
 * Performance Considerations:
 * - LazyColumn only composes visible items
 * - Key-based items() enables efficient updates
 * - Events list limited to 30 items in ViewModel to prevent memory issues
 * - Cards provide Material styling with minimal overhead
 *
 * Future Enhancements:
 * 1. Add click-to-expand for full event details
 * 2. Add location map preview for each event
 * 3. Add share/export functionality for event history
 * 4. Add filtering by date range
 * 5. Add search functionality
 * 6. Add swipe-to-delete for clearing history
 * 7. Add export to file (CSV, JSON) for records
 * 8. Add pagination for very large event histories
 * 9. Add animations for new events appearing
 * 10. Add pull-to-refresh to reload events
 *
 * Localization Notes:
 * - Header: "Ιστορικό" (Greek for "History")
 * - Empty message: Greek text for no events
 * - Event content uses Greek from AlertEvent creation
 * - Could be internationalized for other languages
 *
 * @see AlertEvent - Data model for alert events
 * @see SpotItViewModel - Manages the events list
 * @see MainActivity - Hosts this screen in navigation
 */
