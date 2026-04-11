/**
 * SPOTit Anti-Theft Application
 *
 * Color.kt - Theme Color Definitions
 *
 * This file defines the color palette used throughout the SPOTit application.
 * These colors follow Material Design 3 guidelines and provide a consistent
 * visual identity across all screens and components.
 *
 * Color Philosophy:
 * - Primary Blue: Trust, security, and professionalism
 * - Accent Blue: Highlights and interactive elements
 * - Status Green: Active/positive states (monitoring active)
 * - Alarm Red: Alerts, warnings, and critical states
 * - Neutral Grays: Backgrounds, borders, and muted text
 *
 * @author Panos Biazis
 * @date 10/4/2026
 * @version 2.0
 *
 * Copyright (c) 2026 Panos Biazis. All rights reserved.
 */

// Package declaration - defines the UI theme package
// The theme package contains styling, colors, and typography definitions
package com.spotit.ui.theme

// =============================================================================
// COMPOSE UI IMPORTS
// =============================================================================

import androidx.compose.ui.graphics.Color // Compose Color class for UI

// =============================================================================
// PRIMARY COLORS - Main Brand Colors
// =============================================================================

/**
 * Primary Blue - Main Brand Color
 *
 * This is the primary color used throughout the application for:
 * - Top app bar background
 * - Navigation bar selected items
 * - Section headers
 * - Primary buttons
 * - Active/selected states
 *
 * Color Value: 0xFF1A237E (Dark Indigo)
 * - Red: 26 (0x1A) - Very low
 * - Green: 35 (0x23) - Very low
 * - Blue: 126 (0x7E) - Medium-high
 * - Alpha: 255 (0xFF) - Fully opaque
 *
 * Psychology: Dark blue conveys trust, security, and professionalism.
 * This is appropriate for an anti-theft/security application as it
 * projects reliability and authority.
 *
 * Material Design: This is a shade from the Material Design Indigo palette.
 * It provides good contrast against white backgrounds and is accessible
 * for text overlay (white text is readable).
 *
 * Usage Example:
 * ```kotlin
 * Text(
 *     text = "SPOTit",
 *     color = Color.White, // White text on PrimaryBlue background
 *     modifier = Modifier.background(PrimaryBlue)
 * )
 * ```
 */
val PrimaryBlue = Color(0xFF1A237E)

/**
 * Accent Blue - Secondary Brand Color
 *
 * This is the accent/secondary color used for:
 * - Highlights and emphasis
 * - Secondary buttons
 * - Icons and decorative elements
 * - Borders and dividers
 * - Hover states
 *
 * Color Value: 0xFF283593 (Indigo 800)
 * - Red: 40 (0x28) - Low
 * - Green: 53 (0x35) - Low
 * - Blue: 147 (0x93) - Medium-high
 * - Alpha: 255 (0xFF) - Fully opaque
 *
 * Relationship to PrimaryBlue:
 * - Slightly lighter than PrimaryBlue
 * - Same hue family (Indigo)
 * - Creates cohesive monochromatic scheme
 * - Used together for depth and hierarchy
 *
 * Psychology: A slightly brighter blue that maintains the professional
 * feel while providing visual interest and hierarchy.
 *
 * Usage Example:
 * ```kotlin
 * Card(
 *     border = BorderStroke(1.dp, AccentBlue),
 *     content = { ... }
 * )
 * ```
 */
val AccentBlue = Color(0xFF283593)

// =============================================================================
// NEUTRAL COLORS - Backgrounds, Borders, and Text
// =============================================================================

/**
 * Surface Gray - Background Color
 *
 * This is used as a light background color for:
 * - Card backgrounds
 * - Disabled states
 * - Unselected navigation items
 * - Surface/container backgrounds
 * - Pattern lock dots (unselected)
 *
 * Color Value: 0xFFF5F5F5 (Gray 50)
 * - Red: 245 (0xF5) - Very high
 * - Green: 245 (0xF5) - Very high
 * - Blue: 245 (0xF5) - Very high
 * - Alpha: 255 (0xFF) - Fully opaque
 *
 * Visual Purpose:
 * - Provides subtle contrast against white
 * - Creates visual hierarchy without harsh boundaries
 * - Easy on the eyes for extended viewing
 * - Neutral backdrop for content
 *
 * Material Design: This is the standard surface color for light themes.
 * It's slightly darker than pure white to create depth perception.
 *
 * Usage Example:
 * ```kotlin
 * Box(
 *     modifier = Modifier
 *         .background(SurfaceGray)
 *         .padding(16.dp)
 * ) {
 *     // Content with subtle gray background
 * }
 * ```
 */
val SurfaceGray = Color(0xFFF5F5F5)

/**
 * Border Gray - Border and Divider Color
 *
 * This is used for:
 * - Card borders
 * - Section dividers
 * - Outline text field borders
 * - Pattern lock screen border
 * - Separator lines
 *
 * Color Value: 0xFFBDBDBD (Gray 400)
 * - Red: 189 (0xBD) - Medium-high
 * - Green: 189 (0xBD) - Medium-high
 * - Blue: 189 (0xBD) - Medium-high
 * - Alpha: 255 (0xFF) - Fully opaque
 *
 * Visual Purpose:
 * - Visible but not distracting
 * - Creates clear boundaries
 * - Works well with both white and gray backgrounds
 * - Standard Material Design border color
 *
 * Contrast: Provides sufficient contrast against white backgrounds
 * (ratio ~2.7:1) for decorative borders. Not suitable for text.
 *
 * Usage Example:
 * ```kotlin
 * Card(
 *     shape = RoundedCornerShape(8.dp),
 *     border = BorderStroke(1.dp, BorderGray),
 *     content = { ... }
 * )
 * ```
 */
val BorderGray = Color(0xFFBDBDBD)

/**
 * Text Dark - Primary Text Color
 *
 * This is the main text color used for:
 * - Body text
 * - Headlines and titles
 * - Card content
 * - Primary readable content
 *
 * Color Value: 0xFF212121 (Gray 900)
 * - Red: 33 (0x21) - Very low
 * - Green: 33 (0x21) - Very low
 * - Blue: 33 (0x21) - Very low
 * - Alpha: 255 (0xFF) - Fully opaque
 *
 * Accessibility:
 * - Contrast ratio against white: ~16:1 (AAA rated)
 * - Highly readable for all users
 * - Suitable for body text of any size
 * - Works well for users with visual impairments
 *
 * Material Design: Standard primary text color for light themes.
 * Not pure black (which can be harsh) but very dark gray.
 *
 * Usage Example:
 * ```kotlin
 * Text(
 *     text = "Alert Details",
 *     color = TextDark,
 *     style = MaterialTheme.typography.bodyLarge
 * )
 * ```
 */
val TextDark = Color(0xFF212121)

/**
 * Muted Text - Secondary/Hint Text Color
 *
 * This is used for:
 * - Secondary information
 * - Labels and hints
 * - Timestamps
 * - Unselected navigation labels
 * - Pattern lock instructions
 * - Supporting text
 *
 * Color Value: 0xFF616161 (Gray 700)
 * - Red: 97 (0x61) - Medium-low
 * - Green: 97 (0x61) - Medium-low
 * - Blue: 97 (0x61) - Medium-low
 * - Alpha: 255 (0xFF) - Fully opaque
 *
 * Accessibility:
 * - Contrast ratio against white: ~5.1:1 (AA rated for large text)
 * - Suitable for secondary content that doesn't need to be prominent
 * - May need larger font for accessibility compliance
 *
 * Visual Hierarchy:
 * - Creates clear hierarchy: TextDark > MutedText
 * - De-emphasizes secondary information
 * - Reduces visual noise
 *
 * Usage Example:
 * ```kotlin
 * Text(
 *     text = "Touch the dots to create a pattern.",
 *     color = MutedText,
 *     style = MaterialTheme.typography.bodyMedium
 * )
 * ```
 */
val MutedText = Color(0xFF616161)

// =============================================================================
// SEMANTIC COLORS - Status and Meaning
// =============================================================================

/**
 * Status Green - Positive/Active Status Color
 *
 * This is used to indicate positive/active states:
 * - "Monitoring ON" status
 * - Active/connected indicators
 * - Success states
 * - Positive status indicators
 *
 * Color Value: 0xFF2E7D32 (Green 800)
 * - Red: 46 (0x2E) - Low
 * - Green: 125 (0x7D) - Medium
 * - Blue: 50 (0x32) - Low
 * - Alpha: 255 (0xFF) - Fully opaque
 *
 * Psychology: Green universally indicates "good", "active", "safe", or "success".
 * In the context of an anti-theft app, it shows that protection is active.
 *
 * Accessibility:
 * - Contrast ratio against white: ~5.2:1 (AA rated)
 * - Suitable for status indicators and labels
 * - Visible to most color-blind users (dark enough)
 *
 * Usage in SPOTit:
 * - HomeScreen: "Monitoring: ON" label
 * - StatusRow: When monitoring is active
 *
 * Usage Example:
 * ```kotlin
 * StatusRow(
 *     label = "Monitoring",
 *     value = "ON",
 *     color = StatusGreen // Green when active
 * )
 * ```
 */
val StatusGreen = Color(0xFF2E7D32)

/**
 * Alarm Red - Alert/Critical Status Color
 *
 * This is used to indicate alerts, warnings, and critical states:
 * - "TRIGGERED" alarm status
 * - "Monitoring OFF" status
 * - Error messages
 * - Warning indicators
 * - Pattern lock errors ("Wrong pattern")
 *
 * Color Value: 0xFFB71C1C (Red 900)
 * - Red: 183 (0xB7) - High
 * - Green: 28 (0x1C) - Very low
 * - Blue: 28 (0x1C) - Very low
 * - Alpha: 255 (0xFF) - Fully opaque
 *
 * Psychology: Red universally indicates "danger", "alert", "stop", or "error".
 * In the context of an anti-theft app, it shows that an alarm is triggered
 * or monitoring is inactive (device is at risk).
 *
 * Accessibility:
 * - Contrast ratio against white: ~5.9:1 (AA rated)
 * - Suitable for status indicators and error messages
 * - Dark enough to be distinguishable for most color-blind users
 *
 * Urgency: This is a dark red (not bright red) to convey seriousness
 * without being overly aggressive. It's visible but not jarring.
 *
 * Usage in SPOTit:
 * - HomeScreen: "Alarm: TRIGGERED" label
 * - HomeScreen: "Monitoring: OFF" label
 * - PatternLockScreen: "Wrong pattern" error
 *
 * Usage Example:
 * ```kotlin
 * StatusRow(
 *     label = "Alarm",
 *     value = if (triggered) "TRIGGERED" else "IDLE",
 *     color = if (triggered) AlarmRed else PrimaryBlue
 * )
 * ```
 */
val AlarmRed = Color(0xFFB71C1C)

// =============================================================================
// ADDITIONAL COLORS - Special Purpose
// =============================================================================

/**
 * Code Background - Code/Snippet Background Color
 *
 * This is used for:
 * - Code snippet backgrounds (if used in future)
 * - Technical information display
 * - Monospace text backgrounds
 * - Debug information panels
 *
 * Color Value: 0xFFECEFF1 (Blue Gray 50)
 * - Red: 236 (0xEC) - Very high
 * - Green: 239 (0xEF) - Very high
 * - Blue: 241 (0xF1) - Very high
 * - Alpha: 255 (0xFF) - Fully opaque
 *
 * Visual Purpose:
 * - Subtle blue-gray tint (cooler than SurfaceGray)
 * - Creates distinct appearance for technical content
 * - Easy on the eyes for code reading
 * - Similar to IDE code backgrounds
 *
 * Currently Reserved: This color is defined but may not be actively used
 * in the current version. It's available for future features that might
 * display technical information or debug data.
 *
 * Usage Example (Future):
 * ```kotlin
 * Text(
 *     text = "Lat: 37.98765, Lng: 23.76543",
 *     fontFamily = FontFamily.Monospace,
 *     modifier = Modifier
 *         .background(CodeBg)
 *         .padding(8.dp)
 * )
 * ```
 */
val CodeBg = Color(0xFFECEFF1)

// =============================================================================
// COLOR PALETTE SUMMARY
// =============================================================================

/**
 * Complete Color Palette Overview
 *
 * Brand Colors (Blue):
 * - PrimaryBlue (0xFF1A237E): Main brand color, headers, primary actions
 * - AccentBlue (0xFF283593): Secondary brand color, accents, highlights
 *
 * Neutral Colors (Gray):
 * - SurfaceGray (0xFFF5F5F5): Light background, unselected states
 * - BorderGray (0xFFBDBDBD): Borders, dividers, outlines
 * - TextDark (0xFF212121): Primary text, headlines
 * - MutedText (0xFF616161): Secondary text, labels, hints
 *
 * Semantic Colors:
 * - StatusGreen (0xFF2E7D32): Active/success states
 * - AlarmRed (0xFFB71C1C): Alerts/warnings/errors
 *
 * Special Colors:
 * - CodeBg (0xFFECEFF1): Technical content backgrounds
 *
 * White (Built-in):
 * - Color.White: Card backgrounds, text on dark backgrounds
 *
 * Black (Built-in):
 * - Color.Black: Not used (TextDark is preferred for softer appearance)
 *
 * Color Contrast Pairs (Accessible):
 * - TextDark on White: 16:1 (AAA)
 * - MutedText on White: 5.1:1 (AA for large text)
 * - White on PrimaryBlue: 10.7:1 (AAA)
 * - StatusGreen on White: 5.2:1 (AA)
 * - AlarmRed on White: 5.9:1 (AA)
 *
 * Theme Consistency:
 * - All colors are from Material Design palette
 * - Consistent light theme (no dark theme colors defined)
 * - Sufficient contrast for accessibility
 * - Cohesive professional appearance
 */
