/**
 * SPOTit Anti-Theft Application
 *
 * Theme.kt - Application Theme Configuration
 *
 * This file defines the application's visual theme using Material Design 3
 * (Material You) components. It configures the color scheme that is applied
 * throughout the application, ensuring consistent styling across all screens
 * and components.
 *
 * Material Design 3 (Material You):
 * - Google's latest design system
 * - Emphasizes personalization and dynamic colors
 * - Provides improved accessibility
 * - Uses colorScheme instead of previous color palette approach
 *
 * Theme Architecture:
 * - SpotItTheme: Main composable that wraps all UI content
 * - lightColorScheme: Color configuration for light mode
 * - Color mappings: Connects Color.kt definitions to Material theme
 *
 * Note: This application currently only supports light mode. Dark mode
 * could be added in the future by creating a darkColorScheme and
 * detecting system theme preference.
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
// COMPOSE MATERIAL 3 IMPORTS
// =============================================================================

import androidx.compose.material3.MaterialTheme // Material Design 3 theme
import androidx.compose.material3.lightColorScheme // Light mode color scheme builder
import androidx.compose.runtime.Composable // Composable function annotation

// =============================================================================
// THEME CONFIGURATION - Color Scheme Definition
// =============================================================================

/**
 * Private color scheme for the light theme.
 *
 * This defines the complete color configuration for the application when
 * in light mode. Each color role is mapped to a specific color defined
 * in Color.kt, creating a cohesive visual identity.
 *
 * Color Roles in Material Design 3:
 *
 * 1. **primary**: Main brand color used for:
 *    - Top app bar background
 *    - Floating action buttons
 *    - Active/selected states
 *    - Key UI elements that need emphasis
 *
 * 2. **secondary**: Accent color used for:
 *    - Secondary buttons
 *    - Chips and filters
 *    - Less prominent components
 *    - Supporting elements
 *
 * 3. **surface**: Background color for:
 *    - Cards and sheets
 *    - Menu backgrounds
 *    - Dialog surfaces
 *    - Component backgrounds
 *
 * 4. **onSurface**: Content color on surface:
 *    - Text on cards
 *    - Icons on backgrounds
 *    - Any content on surface-colored components
 *
 * 5. **background**: Main background:
 *    - Screen backgrounds
 *    - App-wide background color
 *    - The default container color
 *
 * 6. **error**: Error and warning color:
 *    - Error text
 *    - Error indicators
 *    - Form validation errors
 *    - Critical alerts
 *
 * Additional Color Roles (not explicitly set here, use defaults):
 * - tertiary: Third emphasis color (defaults from primary)
 * - outline: Borders and dividers (defaults from secondary)
 * - surfaceVariant: Alternative surface for layered surfaces
 * - onPrimary: Content on primary color (defaults to white)
 * - onSecondary: Content on secondary color (defaults to white)
 * - onError: Content on error color (defaults to white)
 *
 * Color Mappings Used:
 * - primary -> PrimaryBlue (0xFF1A237E): Dark indigo for headers and primary actions
 * - secondary -> AccentBlue (0xFF283593): Lighter indigo for accents
 * - surface -> SurfaceGray (0xFFF5F5F5): Light gray for card backgrounds
 * - onSurface -> TextDark (0xFF212121): Dark text for readability
 * - background -> Color.White: Pure white for screen backgrounds
 * - error -> AlarmRed (0xFFB71C1C): Red for errors and warnings
 *
 * Why Private:
 * The color scheme is private to this file to prevent direct access.
 * All theming should go through SpotItTheme composable, ensuring
 * consistent theme application across the app.
 */
private val colors = lightColorScheme(
    // Primary brand color - used for main UI elements
    // Applied to: TopAppBar, selected NavigationBarItem, primary buttons
    primary = PrimaryBlue,

    // Secondary brand color - used for accents and secondary elements
    // Applied to: Secondary buttons, chips, less prominent components
    secondary = AccentBlue,

    // Surface color - used for card and container backgrounds
    // Applied to: Cards, sheets, menus, dialogs
    surface = SurfaceGray,

    // Content color on surface - text and icons on surface backgrounds
    // Applied to: Text in cards, icons on light backgrounds
    onSurface = TextDark,

    // Background color - main screen background
    // Applied to: Screen backgrounds, overall app background
    background = androidx.compose.ui.graphics.Color.White,

    // Error color - used for errors, warnings, and critical states
    // Applied to: Error text, error indicators, validation messages
    error = AlarmRed
)

// =============================================================================
// THEME COMPOSABLE - Main Theme Wrapper
// =============================================================================

/**
 * SpotItTheme - Application Theme Wrapper
 *
 * This is the main theme composable that should wrap all UI content in the
 * application. It applies the Material Design 3 theme with the configured
 * color scheme to all child composables.
 *
 * Usage Pattern:
 * All Compose content should be wrapped in SpotItTheme:
 * ```kotlin
 * // In MainActivity.onCreate()
 * setContent {
 *     SpotItTheme {
 *         // All app UI content here
 *         Scaffold { ... }
 *     }
 * }
 * ```
 *
 * What This Provides:
 *
 * 1. **MaterialTheme Context**: Makes Material 3 components (Button, Card,
 *    Text, etc.) use the configured colors, typography, and shapes.
 *
 * 2. **Consistent Styling**: All composables automatically inherit the
 *    theme colors, ensuring visual consistency.
 *
 * 3. **Color Access**: Colors can be accessed via MaterialTheme.colorScheme:
 *    ```kotlin
 *    Text(
 *        text = "Hello",
 *        color = MaterialTheme.colorScheme.primary
 *    )
 *    ```
 *
 * Design Decisions:
 *
 * 1. **Light Mode Only**: Currently, this app only supports light mode.
 *    This is appropriate for an anti-theft app where high visibility
 *    is important. Dark mode could be added in the future.
 *
 * 2. **No Typography/Shape Configuration**: The app uses default Material 3
 *    typography and shapes. Custom typography could be added for brand
 *    identity if needed.
 *
 * 3. **Composable Function**: Using a composable function (not an object)
 *    allows for future expansion like theme switching or dynamic colors.
 *
 * Parameters Explained:
 *
 * @param content The composable content to be themed. This is a lambda
 * that contains all the UI elements that should use this theme.
 * Typically, this is the entire app UI starting from Scaffold.
 *
 * Theme Propagation:
 * MaterialTheme uses CompositionLocalProvider internally to propagate
 * the color scheme to all descendant composables. This is why any
 * composable within SpotItTheme can access MaterialTheme.colorScheme.
 *
 * Future Enhancements:
 * - Add dark mode support with darkColorScheme
 * - Add system theme detection (follow system dark/light mode)
 * - Add custom typography configuration
 * - Add custom shape configuration
 * - Add dynamic colors (Material You) for Android 12+
 *
 * @param content The composable content to wrap with the theme.
 * All children will inherit the theme styling.
 */
@Composable
fun SpotItTheme(
    // Lambda parameter containing all UI content to be themed
    // The content is rendered with the MaterialTheme applied
    content: @Composable () -> Unit
) {
    /**
     * MaterialTheme Component
     *
     * This is the root of Material Design 3 theming. It provides:
     * - colorScheme: All color definitions
     * - typography: Text styles (using defaults)
     * - shapes: Component shapes (using defaults)
     *
     * All Material 3 composables (Button, Card, Text, etc.) will
     * automatically use these values.
     *
     * The content lambda is rendered within this theme context,
     * so all child composables inherit the theme.
     */
    MaterialTheme(
        // Apply the light color scheme defined above
        // This provides all color definitions to child composables
        colorScheme = colors,

        // Content is rendered with the theme applied
        // Any Material components inside will use the theme colors
        content = content
    )
}

// =============================================================================
// THEME USAGE NOTES
// =============================================================================

/**
 * How to Use Theme Colors in Composables:
 *
 * 1. **Direct Access via MaterialTheme**:
 *    ```kotlin
 *    Text(
 *        text = "Title",
 *        color = MaterialTheme.colorScheme.primary
 *    )
 *    ```
 *
 * 2. **Using in Modifier.background**:
 *    ```kotlin
 *    Box(
 *        modifier = Modifier.background(MaterialTheme.colorScheme.surface)
 *    )
 *    ```
 *
 * 3. **Using in Component Parameters**:
 *    ```kotlin
 *    Button(
 *        colors = ButtonDefaults.buttonColors(
 *            containerColor = MaterialTheme.colorScheme.primary
 *        )
 *    )
 *    ```
 *
 * Available Color Properties (from MaterialTheme.colorScheme):
 * - primary: Main brand color (PrimaryBlue)
 * - secondary: Accent color (AccentBlue)
 * - surface: Card backgrounds (SurfaceGray)
 * - onSurface: Text on surface (TextDark)
 * - background: Screen background (White)
 * - error: Error color (AlarmRed)
 * - onPrimary: Text on primary (White - automatic)
 * - onSecondary: Text on secondary (White - automatic)
 * - onError: Text on error (White - automatic)
 *
 * Best Practices:
 *
 * 1. **Use Theme Colors, Not Hardcoded**:
 *    - Good: MaterialTheme.colorScheme.primary
 *    - Bad: Color(0xFF1A237E)
 *    - This ensures consistency and makes theme changes easier
 *
 * 2. **Use Semantic Colors**:
 *    - For errors: MaterialTheme.colorScheme.error
 *    - For backgrounds: MaterialTheme.colorScheme.background
 *    - This makes code more readable and maintainable
 *
 * 3. **Keep Colors.kt as Single Source**:
 *    - All color definitions come from Color.kt
 *    - Theme.kt maps them to Material roles
 *    - Composables access via MaterialTheme
 *    - This creates a clear color hierarchy
 */

// =============================================================================
// THEME EXTENSION NOTES
// =============================================================================

/**
 * Future Theme Enhancements:
 *
 * 1. **Dark Mode Support**:
 *    ```kotlin
 *    private val darkColors = darkColorScheme(
 *        primary = Color(0xFF7986CB),  // Lighter indigo for dark mode
 *        secondary = Color(0xFF9FA8DA),
 *        surface = Color(0xFF121212),
 *        onSurface = Color.White,
 *        background = Color(0xFF121212),
 *        error = Color(0xFFCF6679)
 *    )
 *
 *    @Composable
 *    fun SpotItTheme(
 *        darkTheme: Boolean = isSystemInDarkTheme(),
 *        content: @Composable () -> Unit
 *    ) {
 *        val colors = if (darkTheme) darkColors else lightColors
 *        MaterialTheme(colorScheme = colors, content = content)
 *    }
 *    ```
 *
 * 2. **Custom Typography**:
 *    ```kotlin
 *    private val typography = Typography(
 *        headlineLarge = TextStyle(
 *            fontFamily = FontFamily.Default,
 *            fontWeight = FontWeight.Bold,
 *            fontSize = 32.sp
 *        ),
 *        // ... other text styles
 *    )
 *    ```
 *
 * 3. **Dynamic Colors (Android 12+)**:
 *    ```kotlin
 *    val dynamicColors = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
 *        dynamicLightColorScheme(context)
 *    } else {
 *        lightColors
 *    }
 *    ```
 *
 * 4. **Theme Switching**:
 *    - Store theme preference in DataStore
 *    - Provide settings option for theme selection
 *    - Support light, dark, and system-default options
 */
