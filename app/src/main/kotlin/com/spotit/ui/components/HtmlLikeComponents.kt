/**
 * SPOTit Anti-Theft Application
 *
 * HtmlLikeComponents.kt - Reusable UI Components
 *
 * This file contains reusable UI components that mimic HTML-like layout patterns.
 * These components provide a consistent look and feel across different screens
 * in the application, following Material Design 3 guidelines.
 *
 * Components Provided:
 * - SectionHeader: Styled header for content sections (like <h2>)
 * - BorderedPanel: Container with border and header styling (like <fieldset>)
 * - StatusRow: Two-column row for label-value display (like <dl>)
 * - PatternDot: Interactive dot for pattern lock screen
 * - MockMiniCard: Simple card container with title
 * - Muted: Text component with muted styling
 *
 * @author Panos Biazis
 * @date 10/4/2026
 * @version 2.0
 *
 * Copyright (c) 2026 Panos Biazis. All rights reserved.
 */

// Package declaration - defines the UI components package
// The components package contains reusable UI elements
package com.spotit.ui.components

// =============================================================================
// COMPOSE FOUNDATION IMPORTS
// =============================================================================

import androidx.compose.foundation.BorderStroke // Border definition for cards
import androidx.compose.foundation.background // Background modifier
import androidx.compose.foundation.clickable // Click handling modifier
import androidx.compose.foundation.layout.Arrangement // Layout arrangement
import androidx.compose.foundation.layout.Box // Box layout container
import androidx.compose.foundation.layout.Column // Vertical layout container
import androidx.compose.foundation.layout.Row // Horizontal layout container
import androidx.compose.foundation.layout.ColumnScope // Column scope for composables
import androidx.compose.foundation.layout.aspectRatio // Aspect ratio modifier
import androidx.compose.foundation.layout.fillMaxWidth // Fill width modifier
import androidx.compose.foundation.layout.padding // Padding modifier
import androidx.compose.foundation.layout.size // Size modifier

// =============================================================================
// COMPOSE SHAPE IMPORTS
// =============================================================================

import androidx.compose.foundation.shape.CircleShape // Circular shape for dots
import androidx.compose.foundation.shape.RoundedCornerShape // Rounded corners

// =============================================================================
// COMPOSE MATERIAL 3 IMPORTS
// =============================================================================

import androidx.compose.material3.Card // Card container component
import androidx.compose.material3.CardDefaults // Card styling defaults
import androidx.compose.material3.Text // Text display component

// =============================================================================
// COMPOSE RUNTIME IMPORTS
// =============================================================================

import androidx.compose.runtime.Composable // Composable function annotation

// =============================================================================
// COMPOSE UI IMPORTS
// =============================================================================

import androidx.compose.ui.Alignment // Alignment options
import androidx.compose.ui.Modifier // UI modifier interface
import androidx.compose.ui.graphics.Color // Color definitions
import androidx.compose.ui.text.font.FontWeight // Text font weights
import androidx.compose.ui.unit.dp // Density-independent pixels

// =============================================================================
// THEME IMPORTS
// =============================================================================

import com.spotit.ui.theme.AccentBlue // Secondary blue color
import com.spotit.ui.theme.BorderGray // Border color
import com.spotit.ui.theme.MutedText // Muted text color
import com.spotit.ui.theme.PrimaryBlue // Primary brand color
import com.spotit.ui.theme.SurfaceGray // Surface background color

// =============================================================================
// SECTION HEADER COMPONENT
// =============================================================================

/**
 * SectionHeader - Styled Section Title Component
 *
 * This composable creates a styled header for content sections, similar to
 * HTML's `<h2>` or `<legend>` elements. It provides a visually distinct
 * header with a colored background that separates different sections of
 * content.
 *
 * Visual Design:
 * - Blue background (PrimaryBlue) spanning full width
 * - White text for high contrast and readability
 * - Rounded top corners (8dp radius) for modern appearance
 * - Horizontal padding (12dp) for content breathing room
 * - Vertical padding (8dp) for comfortable touch targets
 *
 * Usage Context:
 * - Used at the top of BorderedPanel components
 * - Separates different sections in SettingsScreen, HistoryScreen
 * - Provides clear visual hierarchy in the UI
 *
 * HTML Equivalent:
 * This is roughly equivalent to:
 * ```html
 * <h2 style="background: #1A237E; color: white; padding: 8px 12px;
 *            border-radius: 8px 8px 0 0;">Section Title</h2>
 * ```
 *
 * Design Rationale:
 * - Full-width header creates clear section boundaries
 * - Colored background makes sections easily identifiable
 * - Rounded top corners connect visually with BorderedPanel below
 * - White text on blue is a classic, accessible color combination
 *
 * Accessibility:
 * - High contrast ratio (white on PrimaryBlue: ~10.7:1, AAA rated)
 * - Bold font weight for visual emphasis
 * - Sufficient padding for readability
 *
 * @param text The header text to display. Should be concise and
 * descriptive of the section content. Examples: "System Status",
 * "Settings", "Ιστορικό" (History in Greek).
 *
 * @see BorderedPanel - Typically used below this component
 * @see SettingsScreen - Uses SectionHeader for "Settings"
 * @see HistoryScreen - Uses SectionHeader for "Ιστορικό"
 * @see HomeScreen - Uses SectionHeader for "System Status"
 */
@Composable
fun SectionHeader(text: String) {
    /**
     * Box container for the header.
     *
     * Using Box (instead of Surface) gives us more control over
     * the exact shape and styling without Material's opinionated
     * defaults.
     */
    Box(
        modifier = Modifier
            .fillMaxWidth() // Span the full width of parent
            .background(
                // Primary blue background for brand identity
                color = PrimaryBlue,
                // Rounded top corners only - bottom is flush with BorderedPanel
                shape = RoundedCornerShape(
                    topStart = 8.dp, // Top-left corner
                    topEnd = 8.dp    // Top-right corner
                )
            )
            .padding(
                horizontal = 12.dp, // Left/right padding for text
                vertical = 8.dp     // Top/bottom padding for height
            )
    ) {
        /**
         * Header text content.
         *
         * White color for contrast against blue background.
         * Bold weight for visual emphasis and hierarchy.
         */
        Text(
            text = text,
            color = Color.White, // White for contrast
            fontWeight = FontWeight.Bold // Bold for emphasis
        )
    }
}

// =============================================================================
// BORDERED PANEL COMPONENT
// =============================================================================

/**
 * BorderedPanel - Container with Border and Consistent Styling
 *
 * This composable creates a panel container with a border, similar to
 * HTML's `<fieldset>` element. It's designed to work with SectionHeader
 * to create visually cohesive section containers.
 *
 * Visual Design:
 * - White background for content area
 * - Light gray border (1dp, BorderGray)
 * - Rounded bottom corners (8dp radius)
 * - Top corners are square to align with SectionHeader above
 * - 12dp internal padding for content spacing
 *
 * Usage Pattern:
 * Typically used in combination with SectionHeader:
 * ```kotlin
 * Column {
 *     SectionHeader("Settings")
 *     BorderedPanel {
 *         // Settings content here
 *     }
 * }
 * ```
 *
 * The SectionHeader provides the rounded top corners and blue background,
 * while BorderedPanel provides the bordered content area below it.
 *
 * HTML Equivalent:
 * This is roughly equivalent to:
 * ```html
 * <fieldset style="background: white; border: 1px solid #BDBDBD;
 *                  border-radius: 0 0 8px 8px; padding: 12px;
 *                  border-top: none;">
 *     <!-- Content here -->
 * </fieldset>
 * ```
 *
 * Design Rationale:
 * - White background provides clean content area
 * - Border creates visual separation from surroundings
 * - No top border because SectionHeader provides visual top edge
 * - Rounded bottom corners soften the overall appearance
 * - Consistent padding ensures uniform content spacing
 *
 * Content Scoping:
 * The content parameter uses a custom ColumnScopeCompat wrapper to
 * provide proper column scoping to the content lambda. This allows
 * the content to access column-specific modifiers and arrangements.
 *
 * @param content The composable content to display inside the panel.
 * Typically contains a Column with various form elements or
 * information displays.
 *
 * @see SectionHeader - Used above this component
 * @see SettingsScreen - Uses BorderedPanel for settings form
 * @see HistoryScreen - Uses BorderedPanel for event list
 * @see HomeScreen - Uses BorderedPanel for status display
 */
@Composable
fun BorderedPanel(
    // Content lambda with ColumnScopeCompat for column context
    content: @Composable ColumnScopeCompat.() -> Unit
) {
    /**
     * Card component as the panel container.
     *
     * Card is used instead of a plain Box because it provides:
     * - Built-in elevation (not used here, but available)
     * - Consistent Material styling
     * - Easy shape and border configuration
     */
    Card(
        modifier = Modifier.fillMaxWidth(), // Full width
        // Rounded bottom corners only - top is flush with SectionHeader
        shape = RoundedCornerShape(
            bottomStart = 8.dp, // Bottom-left corner
            bottomEnd = 8.dp,   // Bottom-right corner
            topStart = 0.dp,    // Square top-left (connects to header)
            topEnd = 0.dp       // Square top-right (connects to header)
        ),
        // White background for content area
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        ),
        // Light gray border for definition
        border = BorderStroke(
            width = 1.dp,
            color = BorderGray
        )
    ) {
        /**
         * Content column with padding.
         *
         * The padding provides breathing room between the border
         * and the content, improving readability.
         */
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            /**
             * Invoke content with ColumnScopeCompat.
             *
             * This provides the column scope context to the content,
             * allowing proper layout arrangements.
             */
            ColumnScopeCompat(this).content()
        }
    }
}

// =============================================================================
// COLUMN SCOPE COMPAT WRAPPER
// =============================================================================

/**
 * ColumnScopeCompat - Compatibility Wrapper for Column Scope
 *
 * This class wraps the standard ColumnScope to provide a compatible
 * interface for content lambdas. It's a workaround to ensure proper
 * scoping when using BorderedPanel.
 *
 * Why This Exists:
 * In some cases, Compose's type inference can have issues with
 * nested column scopes. This wrapper provides explicit scope
 * handling to ensure content is properly rendered within the column.
 *
 * Usage:
 * This is used internally by BorderedPanel and should not be
 * used directly by other composables.
 *
 * @property scope The underlying ColumnScope from the parent Column.
 */
class ColumnScopeCompat(
    // Reference to the actual ColumnScope
    private val scope: androidx.compose.foundation.layout.ColumnScope
) {
    /**
     * Item composable for rendering content within the scope.
     *
     * This method simply delegates to the content lambda, allowing
     * the content to be rendered within the column context.
     *
     * @param content The composable content to render.
     */
    @Composable
    fun Item(content: @Composable () -> Unit) = with(scope) {
        content()
    }
}

// =============================================================================
// STANDALONE ITEM COMPONENT
// =============================================================================

/**
 * Item - Simple Content Wrapper
 *
 * This is a standalone version of the Item composable that can be
 * used without ColumnScopeCompat. It simply invokes the content
 * lambda without any additional modifications.
 *
 * Usage:
 * ```kotlin
 * BorderedPanel {
 *     Item {
 *         // Content here
 *     }
 * }
 * ```
 *
 * This is provided for convenience and consistency in API design.
 *
 * @param content The composable content to render.
 */
@Composable
fun Item(content: @Composable () -> Unit) {
    content()
}

// =============================================================================
// STATUS ROW COMPONENT
// =============================================================================

/**
 * StatusRow - Two-Column Label-Value Display
 *
 * This composable creates a two-column row displaying a label on the
 * left and a value on the right, similar to HTML's `<dl>` (definition
 * list) with `<dt>` and `<dd>` elements.
 *
 * Visual Design:
 * - Full-width row with space-between arrangement
 * - Label on the left side
 * - Value on the right side with optional color
 * - Used for displaying status information
 *
 * Usage Context:
 * - HomeScreen: Displaying monitoring status, alarm status, location
 * - SettingsScreen: Displaying current setting values
 * - Any screen needing label-value pairs
 *
 * Example Usage:
 * ```kotlin
 * StatusRow(
 *     label = "Monitoring",
 *     value = if (isMonitoring) "ON" else "OFF",
 *     color = if (isMonitoring) StatusGreen else AlarmRed
 * )
 * ```
 *
 * HTML Equivalent:
 * This is roughly equivalent to:
 * ```html
 * <dl style="display: flex; justify-content: space-between;">
 *     <dt>Label</dt>
 *     <dd style="color: blue;">Value</dd>
 * </dl>
 * ```
 *
 * Design Rationale:
 * - Space-between arrangement creates clear visual separation
 * - Value color can indicate status (green = on, red = off)
 * - Semi-bold value weight draws attention to current state
 * - Clean, minimal design matches overall app aesthetic
 *
 * Accessibility:
 * - High contrast between label and value positions
 * - Color alone is not used to convey information
 * - The text content ("ON"/"OFF") conveys the meaning
 *
 * @param label The label text displayed on the left side.
 * Should be concise, like "Monitoring", "Alarm", "Location".
 * @param value The value text displayed on the right side.
 * Examples: "ON", "OFF", "TRIGGERED", "IDLE", or location text.
 * @param color The color of the value text. Use semantic colors
 * like StatusGreen for active states, AlarmRed for alerts,
 * or PrimaryBlue for neutral values.
 *
 * @see HomeScreen - Uses StatusRow for system status display
 */
@Composable
fun StatusRow(
    // Label text on the left side
    label: String,
    // Value text on the right side
    value: String,
    // Color for the value text (default is theme's onSurface)
    color: Color
) {
    /**
     * Row container with space-between arrangement.
     *
     * This pushes the label to the left and value to the right,
     * creating a clean two-column layout without explicit widths.
     */
    Row(
        modifier = Modifier.fillMaxWidth(), // Full width
        horizontalArrangement = Arrangement.SpaceBetween // Push to edges
    ) {
        /**
         * Label text on the left.
         *
         * Uses default text styling, inheriting from parent.
         */
        Text(label)

        /**
         * Value text on the right.
         *
         * Uses the provided color and semi-bold weight
         * for visual emphasis.
         */
        Text(
            text = value,
            color = color, // Semantic color (green, red, or blue)
            fontWeight = FontWeight.SemiBold // Slightly bold for emphasis
        )
    }
}

// =============================================================================
// MINI CARD COMPONENT
// =============================================================================

/**
 * MockMiniCard - Simple Card with Title and Content
 *
 * This composable creates a simple card with a title and content area.
 * It's a lightweight alternative to more complex card implementations,
 * useful for displaying grouped information.
 *
 * Visual Design:
 * - Rounded corners (10dp radius)
 * - Full width
 * - 12dp internal padding
 * - Bold title with content below
 *
 * Usage Context:
 * - Can be used for alert cards in history
 * - Useful for grouping related information
 * - Provides visual separation between content blocks
 *
 * Example Usage:
 * ```kotlin
 * MockMiniCard(title = "Alert Details") {
 *     Text("Movement detected at location")
 * }
 * ```
 *
 * Note: This is named "Mock" because it was likely created during
 * development/prototyping. For production use, consider renaming
 * to something more descriptive like "SimpleCard" or "TitledCard".
 *
 * @param title The title text displayed at the top of the card.
 * Rendered in bold for emphasis.
 * @param body The content composable displayed below the title.
 * Can contain any composable content.
 *
 * @see HistoryScreen - Similar card styling for alert events
 */
@Composable
fun MockMiniCard(
    // Title displayed at the top of the card
    title: String,
    // Content displayed below the title
    body: @Composable () -> Unit
) {
    /**
     * Card container with rounded corners.
     *
     * Uses Card for built-in Material styling and
     * potential elevation support.
     */
    Card(
        shape = RoundedCornerShape(10.dp), // Rounded corners
        modifier = Modifier.fillMaxWidth()  // Full width
    ) {
        /**
         * Content column with padding.
         */
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            /**
             * Title text in bold.
             */
            Text(
                text = title,
                fontWeight = FontWeight.Bold
            )

            /**
             * Body content below the title.
             */
            body()
        }
    }
}

// =============================================================================
// MUTED TEXT COMPONENT
// =============================================================================

/**
 * Muted - Secondary/Hint Text Component
 *
 * This composable creates text with muted styling, suitable for
 * secondary information, hints, or less important content.
 *
 * Visual Design:
 * - Uses MutedText color (gray, not black)
 * - Normal (not bold) font weight
 * - Inherits other text styling from parent
 *
 * Usage Context:
 * - PatternLockScreen: Instruction text
 * - Any screen needing secondary text
 * - Hint text below form fields
 * - Timestamp displays
 *
 * Example Usage:
 * ```kotlin
 * Muted("Touch the dots to create a pattern.")
 * ```
 *
 * HTML Equivalent:
 * This is roughly equivalent to:
 * ```html
 * <span style="color: #616161; font-weight: normal;">
 *     Secondary text
 * </span>
 * ```
 *
 * Design Rationale:
 * - Muted color reduces visual weight
 * - Creates clear hierarchy with primary content
 * - Normal weight doesn't compete for attention
 * - Simple API for consistent secondary text styling
 *
 * Accessibility:
 * - Color contrast ratio ~5.1:1 on white (AA for large text)
 * - Should be used for supplementary information only
 * - Important information should use higher contrast
 *
 * @param text The text to display with muted styling.
 *
 * @see PatternLockScreen - Uses Muted for instructions
 */
@Composable
fun Muted(text: String) {
    Text(
        text = text,
        color = MutedText, // Gray color for secondary text
        fontWeight = FontWeight.Normal // Normal weight, not bold
    )
}

// =============================================================================
// PATTERN DOT COMPONENT
// =============================================================================

/**
 * PatternDot - Interactive Dot for Pattern Lock
 *
 * This composable creates an interactive circular dot used in the
 * pattern lock screen. It represents a single position in the
 * unlock pattern, similar to Android's pattern lock dots.
 *
 * Visual Design:
 * - Circular shape (48dp diameter)
 * - Two visual states: selected (blue) or unselected (gray)
 * - Number displayed in center (0-9)
 * - Click handling for pattern input
 *
 * States:
 * - Unselected: SurfaceGray background, MutedText number
 * - Selected: PrimaryBlue background, White number
 *
 * Usage Context:
 * - PatternLockScreen: Grid of dots for pattern input
 * - Each dot represents a position in the security pattern
 *
 * Example Usage:
 * ```kotlin
 * PatternDot(
 *     number = "1",
 *     selected = "1" in selectedList,
 *     onClick = { selectedList.add("1") }
 * )
 * ```
 *
 * Interaction:
 * - Clickable with visual feedback
 * - Prevents duplicate selection (checked by caller)
 * - OnClick callback triggers pattern update
 *
 * Accessibility:
 * - Large touch target (48dp minimum recommended)
 * - Clear visual distinction between states
 * - Number provides clear identification
 *
 * @param number The number displayed in the dot center (0-9).
 * Used to identify the dot in the pattern sequence.
 * @param selected Whether this dot is currently selected (part of
 * the entered pattern). True: blue background, white text.
 * False: gray background, gray text.
 * @param onClick Callback invoked when the dot is clicked.
 * The caller should handle adding to selected list.
 *
 * @see PatternLockScreen - Uses PatternDot for the security pattern
 */
@Composable
fun PatternDot(
    // Number displayed in the dot (0-9)
    number: String,
    // Whether this dot is currently selected
    selected: Boolean,
    // Click callback for pattern input
    onClick: () -> Unit
) {
    /**
     * Box container with circular shape.
     *
     * The Box is clickable and displays the number in its center.
     * Background color changes based on selection state.
     */
    Box(
        modifier = Modifier
            .size(48.dp) // Fixed size for consistent touch target
            .background(
                // Background color based on selection state
                color = if (selected) PrimaryBlue else SurfaceGray,
                // Always circular
                shape = CircleShape
            )
            .clickable { onClick() }, // Handle click
        // Center the number text
        contentAlignment = Alignment.Center
    ) {
        /**
         * Number text in the center of the dot.
         *
         * Color changes based on selection:
         * - Selected: White (contrasts with PrimaryBlue)
         * - Unselected: MutedText (blends with SurfaceGray)
         */
        Text(
            text = number,
            // White when selected, gray when unselected
            color = if (selected) Color.White else MutedText,
            fontWeight = FontWeight.Bold // Bold for visibility
        )
    }
}

// =============================================================================
// COMPONENT USAGE SUMMARY
// =============================================================================

/**
 * Component Library Summary
 *
 * This file provides the following reusable components:
 *
 * 1. **SectionHeader**: Blue header for content sections
 *    - Used with BorderedPanel
 *    - Provides visual hierarchy
 *    - Examples: "System Status", "Settings"
 *
 * 2. **BorderedPanel**: Bordered container for section content
 *    - Used below SectionHeader
 *    - Provides consistent styling
 *    - Used in Home, Settings, History screens
 *
 * 3. **StatusRow**: Two-column label-value display
 *    - Shows status information
 *    - Semantic colors for state indication
 *    - Used in Home and Settings screens
 *
 * 4. **PatternDot**: Interactive dot for pattern lock
 *    - Used in PatternLockScreen
 *    - Two visual states (selected/unselected)
 *    - Clickable for pattern input
 *
 * 5. **MockMiniCard**: Simple card with title
 *    - Lightweight container
 *    - Used for grouped information
 *
 * 6. **Muted**: Secondary text styling
 *    - Gray color, normal weight
 *    - Used for hints and supplementary text
 *
 * Design Principles Applied:
 * - Consistent spacing and sizing
 * - Semantic color usage
 * - Material Design 3 alignment
 * - Accessible touch targets
 * - Clear visual hierarchy
 *
 * Future Enhancements:
 * - Add animation for PatternDot selection
 * - Add disabled state for StatusRow
 * - Add variants for different use cases
 * - Add preview composables for documentation
 */
