/**
 * SPOTit Anti-Theft Application
 *
 * PatternLockScreen.kt - Security Pattern Lock Screen
 *
 * This file implements the pattern lock screen that requires the user to enter
 * a correct security pattern before stopping the monitoring. This prevents
 * unauthorized users (like thieves) from easily disabling the anti-theft
 * protection.
 *
 * Key Features:
 * - 3x3+1 grid of numbered dots (1-9 plus 0)
 * - Visual feedback for selected dots
 * - Pattern validation against expected pattern
 * - Error indication for wrong patterns
 * - Clear and cancel options
 *
 * Security Design:
 * The pattern lock adds a layer of security to prevent unauthorized stopping
 * of monitoring. Even if someone gains access to the device, they cannot
 * disable the anti-theft protection without knowing the pattern.
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

import androidx.compose.foundation.border // Border modifier
import androidx.compose.foundation.layout.Arrangement // Layout arrangement
import androidx.compose.foundation.layout.Column // Vertical layout
import androidx.compose.foundation.layout.Row // Horizontal layout
import androidx.compose.foundation.layout.Spacer // Flexible space
import androidx.compose.foundation.layout.fillMaxWidth // Fill width modifier
import androidx.compose.foundation.layout.padding // Padding modifier
import androidx.compose.foundation.layout.width // Fixed width

// Scrolling support for smaller screens
import androidx.compose.foundation.rememberScrollState // Scroll state
import androidx.compose.foundation.verticalScroll // Vertical scroll modifier

// Shape for border
import androidx.compose.foundation.shape.RoundedCornerShape // Rounded corners

// =============================================================================
// COMPOSE MATERIAL 3 IMPORTS
// =============================================================================

import androidx.compose.material3.Button // Button component
import androidx.compose.material3.Text // Text component

// =============================================================================
// COMPOSE RUNTIME IMPORTS
// =============================================================================

import androidx.compose.runtime.Composable // Composable annotation
import androidx.compose.runtime.getValue // State read delegate
import androidx.compose.runtime.setValue // State write delegate
import androidx.compose.runtime.mutableStateListOf // Mutable state list
import androidx.compose.runtime.mutableStateOf // Mutable state
import androidx.compose.runtime.remember // Remember state
import androidx.compose.runtime.saveable.rememberSaveable // Saved state

// =============================================================================
// COMPOSE UI IMPORTS
// =============================================================================

import androidx.compose.ui.Alignment // Alignment options
import androidx.compose.ui.Modifier // Modifier interface
import androidx.compose.ui.graphics.Color // Color definitions
import androidx.compose.ui.text.font.FontWeight // Font weights
import androidx.compose.ui.unit.dp // Density-independent pixels

// =============================================================================
// COMPONENT IMPORTS
// =============================================================================

import com.spotit.ui.components.Muted // Muted text component
import com.spotit.ui.components.PatternDot // Interactive pattern dot
import com.spotit.ui.theme.BorderGray // Border color

/**
 * PatternLockScreen - Security Pattern Lock Composable
 *
 * This screen displays a grid of numbered dots that the user must tap in the
 * correct sequence to unlock and stop monitoring. It provides a balance
 * between security and usability:
 *
 * Security Features:
 * - Pattern must match exactly (order matters)
 * - No visual indication of correct pattern
 * - Error shown for wrong attempts (no limit on attempts)
 * - Pattern dots highlight when selected for feedback
 *
 * User Experience:
 * - Large touch targets (48dp dots)
 * - Clear visual feedback (selected dots turn blue)
 * - Simple layout (3x3 grid plus centered 0)
 * - Clear and Cancel buttons for recovery
 *
 * Layout Structure:
 * ```
 * +------------------------+
 * |    "Pattern Lock"      |  <- Title
 * |    Instructions        |  <- Muted text
 * |   +---+---+---+        |  <- Row 1: Dots 1, 2, 3
 * |   | 1 | 2 | 3 |        |
 * |   +---+---+---+        |
 * |   +---+---+---+        |  <- Row 2: Dots 4, 5, 6
 * |   | 4 | 5 | 6 |        |
 * |   +---+---+---+        |
 * |   +---+---+---+        |  <- Row 3: Dots 7, 8, 9
 * |   | 7 | 8 | 9 |        |
 * |   +---+---+---+        |
 * |       +---+            |  <- Row 4: Dot 0 (centered)
 * |       | 0 |            |
 * |       +---+            |
 * |    "Pattern: 1-2-3"    |  <- Current pattern display
 * |    "Wrong pattern"     |  <- Error message (if applicable)
 * | [Unlock] [Clear] [X]   |  <- Action buttons
 * +------------------------+
 * ```
 *
 * Pattern Format:
 * The pattern is stored as a string like "1-2-3-6" where:
 * - Numbers represent dot positions
 * - Hyphens separate positions
 * - Order matters (1-2-3 ≠ 3-2-1)
 *
 * Validation:
 * - Entered pattern is compared string-to-string with expected pattern
 * - No partial matching or fuzzy matching
 * - Case-sensitive (though all numbers, so not applicable)
 *
 * Error Handling:
 * - Wrong pattern: Shows red "Wrong pattern" message
 * - User can retry unlimited times
 * - Clear button resets entered pattern
 * - Cancel button exits without unlocking
 *
 * @param expectedPattern The pattern that must be entered to unlock.
 * Format: "x-y-z-w" (e.g., "1-2-3-6")
 * @param onSuccess Callback invoked when correct pattern is entered.
 * Typically stops monitoring and dismisses the screen.
 * @param onCancel Callback invoked when user cancels (presses Cancel button).
 * Typically dismisses the screen without stopping monitoring.
 *
 * @see PatternDot - The individual dot components used in the grid
 * @see MainActivity - Handles pattern lock display logic
 */
@Composable
fun PatternLockScreen(
    // The expected pattern that must be matched to unlock
    // Format: "x-y-z-w" where x,y,z,w are numbers 0-9
    expectedPattern: String,
    // Callback for successful pattern entry
    onSuccess: () -> Unit,
    // Callback for user cancellation
    onCancel: () -> Unit
) {
    // =========================================================================
    // STATE MANAGEMENT - Pattern Entry State
    // =========================================================================

    /**
     * List of currently selected pattern dots.
     *
     * This mutableStateListOf holds the sequence of dot numbers that the
     * user has selected. It's used to:
     * - Track which dots are selected (for visual feedback)
     * - Build the pattern string for validation
     * - Reset on clear
     *
     * Using mutableStateListOf ensures UI recomposition when dots are
     * added or cleared.
     *
     * Example: If user taps 1, then 2, then 3, this list contains
     * ["1", "2", "3"] and the pattern would be "1-2-3".
     */
    val selected = remember { mutableStateListOf<String>() }

    /**
     * Error state flag.
     *
     * When true, displays "Wrong pattern" in red below the pattern display.
     * Set to true when user submits wrong pattern.
     * Reset to false when user clears or modifies pattern.
     */
    var error by remember { mutableStateOf(false) }

    // =========================================================================
    // MAIN CONTAINER - Screen Layout
    // =========================================================================

    /**
     * Main column container for the pattern lock screen.
     *
     * Structure:
     * - Title and instructions at top
     * - 3x3+1 grid of pattern dots
     * - Pattern display showing current entry
     * - Error message (conditional)
     * - Action buttons at bottom
     *
     * The entire layout is scrollable to handle smaller screens,
     * though on most devices it fits without scrolling.
     */
    Column(
        modifier = Modifier
            .fillMaxWidth() // Take full width
            .verticalScroll(rememberScrollState()) // Enable scrolling on small screens
            .border(
                width = 1.dp, // Thin border
                color = BorderGray, // Subtle gray color
                shape = RoundedCornerShape(10.dp) // Rounded corners
            )
            .padding(16.dp), // Internal padding
        // Center content horizontally
        horizontalAlignment = Alignment.CenterHorizontally,
        // Space between elements
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // =====================================================================
        // TITLE AND INSTRUCTIONS
        // =====================================================================

        /**
         * Screen title.
         *
         * Bold font weight for visual emphasis.
         * Simple, clear title indicating the purpose of this screen.
         */
        Text(
            text = "Pattern Lock",
            fontWeight = FontWeight.Bold
        )

        /**
         * Instructions text.
         *
         * Uses Muted component for secondary text styling.
         * Provides clear guidance on how to use the pattern lock.
         */
        Muted("Touch the dots to create a pattern.")

        // =====================================================================
        // PATTERN DOT GRID - 3x3 Plus One
        // =====================================================================

        /**
         * Pattern dot grid layout.
         *
         * The grid consists of:
         * - Rows 1-3: 3 dots each (positions 1-9)
         * - Row 4: Single dot centered (position 0)
         *
         * This matches Android's pattern lock convention where 0 is
         * positioned separately below the main grid.
         */

        // Repeat 3 times for rows 1-3
        // Each row contains 3 dots numbered 1-9
        repeat(3) { row ->
            /**
             * Single row of pattern dots.
             *
             * Each row contains 3 dots with 16dp spacing between them.
             */
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Create 3 dots per row
                repeat(3) { col ->
                    // Calculate dot number from row and column
                    // Row 0: 1, 2, 3
                    // Row 1: 4, 5, 6
                    // Row 2: 7, 8, 9
                    val number = (row * 3 + col + 1).toString()

                    /**
                     * Single pattern dot.
                     *
                     * - number: The dot's position identifier
                     * - selected: Whether this dot is in the current pattern
                     * - onClick: Adds this dot to the pattern if not already selected
                     */
                    PatternDot(
                        number = number,
                        selected = number in selected // Check if this dot is selected
                    ) {
                        // Only add if not already in pattern (prevent duplicates)
                        if (number !in selected) {
                            selected.add(number)
                            // Clear error when user modifies pattern
                            error = false
                        }
                    }
                }
            }
        }

        // =====================================================================
        // ROW 4 - DOT 0 (CENTERED)
        // =====================================================================

        /**
         * Fourth row containing only dot 0, centered.
         *
         * This follows Android pattern lock convention where 0 is
         * positioned below the main 3x3 grid.
         */
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            /**
             * Dot 0 - the "zero" position.
             *
             * Centered below the main grid, following Android convention.
             */
            PatternDot(
                number = "0",
                selected = "0" in selected // Check if 0 is selected
            ) {
                // Only add if not already in pattern
                if ("0" !in selected) {
                    selected.add("0")
                    error = false
                }
            }
        }

        // =====================================================================
        // PATTERN DISPLAY - Current Entry
        // =====================================================================

        /**
         * Current pattern display.
         *
         * Shows the user what pattern they've entered so far.
         * Format: "Pattern: 1-2-3-6" or "Pattern: -" if empty.
         *
         * This provides visual confirmation of the input.
         */
        Text(
            text = if (selected.isEmpty()) {
                // Empty pattern indicator
                "Pattern: -"
            } else {
                // Show pattern as hyphen-separated numbers
                // Example: "Pattern: 1-2-3-6"
                "Pattern: ${selected.joinToString("-")}"
            }
        )

        // =====================================================================
        // ERROR MESSAGE - Conditional Display
        // =====================================================================

        /**
         * Error message for wrong pattern.
         *
         * Only displayed when error flag is true.
         * Shown in red to indicate a problem.
         */
        if (error) {
            Text(
                text = "Wrong pattern",
                color = Color.Red // Red indicates error
            )
        }

        // =====================================================================
        // ACTION BUTTONS - Unlock, Clear, Cancel
        // =====================================================================

        /**
         * Action buttons row.
         *
         * Three buttons:
         * - Unlock: Validate pattern and unlock if correct
         * - Clear: Reset entered pattern
         * - Cancel: Dismiss without unlocking
         */
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            /**
             * Unlock button.
             *
             * Validates the entered pattern against the expected pattern.
             * If correct, calls onSuccess to unlock.
             * If wrong, sets error flag to show error message.
             */
            Button(
                onClick = {
                    // Build pattern string from selected dots
                    val enteredPattern = selected.joinToString("-")

                    // Compare with expected pattern
                    if (enteredPattern == expectedPattern) {
                        // Correct pattern - unlock
                        onSuccess()
                    } else {
                        // Wrong pattern - show error
                        error = true
                    }
                }
            ) {
                Text("Unlock")
            }

            /**
             * Clear button.
             *
             * Resets the entered pattern and clears any error.
             * Allows user to start over without canceling.
             */
            Button(
                onClick = {
                    // Clear all selected dots
                    selected.clear()
                    // Clear error state
                    error = false
                }
            ) {
                Text("Clear")
            }

            /**
             * Cancel button.
             *
             * Dismisses the pattern lock screen without unlocking.
             * Monitoring continues after cancel.
             */
            Button(
                onClick = onCancel
            ) {
                Text("Cancel")
            }
        }
    }
}

/**
 * Additional Architecture Notes:
 *
 * Security Considerations:
 * - Pattern is validated on device (no server round-trip)
 * - No lockout after failed attempts (user convenience)
 * - Pattern visible while entering (not hidden like password)
 * - Consider adding haptic feedback on dot selection
 *
 * Alternative Approaches Considered:
 * 1. Gesture-based pattern (drag between dots):
 * - More like Android's native pattern lock
 * - More complex to implement
 * - Better UX for pattern entry
 * - Current approach uses tap-tap-tap for simplicity
 *
 * 2. PIN entry instead of pattern:
 * - Simpler implementation
 * - Less intuitive visual feedback
 * - Pattern is more visual and memorable
 *
 * 3. Biometric unlock:
 * - Uses fingerprint or face unlock
 * - More secure when available
 * - Not all devices support biometrics
 * - Pattern is universal fallback
 *
 * Future Improvements:
 * 1. Add gesture-based pattern drawing
 * 2. Add haptic feedback on dot selection
 * 3. Add pattern preview while drawing
 * 4. Add biometric authentication option
 * 5. Add pattern visibility toggle (hide/show)
 * 6. Add minimum pattern length requirement
 * 7. Add lockout after too many failed attempts
 * 8. Add pattern change from settings
 */
