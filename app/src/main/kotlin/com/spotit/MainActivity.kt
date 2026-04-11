/**
 * SPOTit Anti-Theft Application
 *
 * MainActivity.kt - Main Entry Point
 *
 * This file contains the primary activity for the SPOTit anti-theft application.
 * It handles the main user interface, permission management, and navigation
 * between the Home, Settings, and History screens.
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

// Permission-related imports for runtime permission handling
import android.Manifest                                      // Constants for permission names
import android.content.pm.PackageManager                    // For checking permission status

// Android OS imports for system-level functionality
import android.os.Build                                      // For checking Android API version
import android.os.Bundle                                     // For saving/restoring activity state

// Logging utility for debugging
import android.util.Log                                      // Android logging system

// =============================================================================
// ANDROIDX ACTIVITY IMPORTS
// =============================================================================

// Core Activity base class with modern Android features
import androidx.activity.ComponentActivity                   // Base activity for Compose
import androidx.activity.compose.setContent                 // Extension to set Compose content
import androidx.activity.enableEdgeToEdge                   // For edge-to-edge display support

// Permission request handling using modern Activity Result API
import androidx.activity.result.contract.ActivityResultContracts  // Contract for permission requests

// ViewModel integration
import androidx.activity.viewModels                          // Extension for ViewModel delegation

// =============================================================================
// JETPACK COMPOSE IMPORTS
// =============================================================================

// Foundation layout components
import androidx.compose.foundation.Image                     // For displaying images
import androidx.compose.foundation.background                // For background colors
import androidx.compose.foundation.layout.Arrangement        // For arranging children in layouts
import androidx.compose.foundation.layout.Box               // Box layout container
import androidx.compose.foundation.layout.Column             // Vertical layout container
import androidx.compose.foundation.layout.fillMaxSize        // Modifier to fill maximum size
import androidx.compose.foundation.layout.fillMaxWidth       // Modifier to fill maximum width
import androidx.compose.foundation.layout.padding            // Padding modifier
import androidx.compose.foundation.layout.size               // Size modifier

// Material 3 UI components
import androidx.compose.material3.Button                    // Button component
import androidx.compose.material3.ExperimentalMaterial3Api   // Annotation for experimental APIs
import androidx.compose.material3.NavigationBar              // Bottom navigation bar
import androidx.compose.material3.NavigationBarItem          // Individual navigation item
import androidx.compose.material3.Scaffold                   // Basic Material Design layout structure
import androidx.compose.material3.Text                       // Text display component
import androidx.compose.material3.TopAppBar                  // Top app bar component

// Compose state management
import androidx.compose.runtime.getValue                      // Delegate for reading State values
import androidx.compose.runtime.mutableStateOf               // Creates a mutable state holder
import androidx.compose.runtime.saveable.rememberSaveable    // Remembers state across configuration changes
import androidx.compose.runtime.setValue                     // Delegate for writing State values

// Compose UI modifications
import androidx.compose.ui.Modifier                          // Base modifier interface
import androidx.compose.ui.graphics.Color                    // Color definitions
import androidx.compose.ui.layout.ContentScale               // How content should be scaled
import androidx.compose.ui.platform.LocalContext             // Access to Android Context
import androidx.compose.ui.res.painterResource               // For loading drawable resources
import androidx.compose.ui.unit.dp                           // Density-independent pixels

// =============================================================================
// ANDROIDX CORE IMPORTS
// =============================================================================

// Context compatibility utilities for permission checking
import androidx.core.content.ContextCompat                    // For checking permissions

// Lifecycle-aware state collection
import androidx.lifecycle.compose.collectAsStateWithLifecycle // Collects Flow with lifecycle awareness

// =============================================================================
// APPLICATION-SPECIFIC IMPORTS
// =============================================================================

// Screen imports - the main screens of the application
import com.spotit.ui.screens.HistoryScreen                   // Screen showing alert history
import com.spotit.ui.screens.HomeScreen                      // Main dashboard screen
import com.spotit.ui.screens.PatternLockScreen               // Pattern lock for disabling alarm
import com.spotit.ui.screens.SettingsScreen                  // Settings configuration screen

// Theme import for consistent styling
import com.spotit.ui.theme.SpotItTheme                       // Application theme definition

/**
 * MainActivity - The Primary Entry Point of SPOTit
 *
 * This activity serves as the main entry point for the SPOTit anti-theft application.
 * It is responsible for:
 *
 * 1. **UI Management**: Hosting the Jetpack Compose-based user interface
 *    with three main tabs: Home, Settings, and History.
 *
 * 2. **Permission Handling**: Requesting and managing runtime permissions:
 *    - Location permissions (fine and coarse) for GPS tracking
 *    - SMS permission for sending security alerts
 *    - Post notifications permission (Android 13+) for alerts
 *
 * 3. **Navigation**: Providing bottom navigation between screens using
 *    a tab-based interface.
 *
 * 4. **Monitoring Control**: Starting and stopping the foreground service
 *    that monitors for motion/intrusion detection.
 *
 * 5. **Pattern Lock**: Displaying a pattern lock screen to authorize
 *    disabling of monitoring (security feature to prevent unauthorized stopping).
 *
 * Architecture:
 * - Uses MVVM pattern with SpotItViewModel
 * - Jetpack Compose for UI
 * - DataStore for persistent settings
 * - Foreground Service for background monitoring
 *
 * @see SpotItViewModel - The ViewModel managing UI state and business logic
 * @see SpotItForegroundService - Background service for motion detection
 * @see PatternLockScreen - Security pattern lock for disabling monitoring
 */
// OptIn annotation to acknowledge use of experimental Material 3 APIs
@OptIn(ExperimentalMaterial3Api::class)
class MainActivity : ComponentActivity() {

    // =========================================================================
    // COMPANION OBJECT - Static constants and utilities
    // =========================================================================

    /**
     * Companion object containing static members for the MainActivity class.
     *
     * This provides a logging tag that remains constant across all instances
     * of MainActivity, used for filtering logcat output during debugging.
     */
    companion object {
        // Logging tag for this activity - used with Log.d(), Log.e(), etc.
        // Format: "MainActivity" for easy filtering in logcat
        private const val TAG = "MainActivity"
    }

    // =========================================================================
    // VIEW MODEL - UI State Management
    // =========================================================================

    /**
     * ViewModel instance for managing UI state.
     *
     * The 'by viewModels()' delegate creates a ViewModel that is scoped to this Activity.
     * It survives configuration changes (like screen rotations) and is cleared when
     * the Activity is permanently destroyed.
     *
     * The ViewModel provides:
     * - Monitoring state (is the service running?)
     * - Alarm state (has motion been detected?)
     * - Current location text
     * - Application settings
     * - Alert event history
     */
    private val viewModel by viewModels<SpotItViewModel>()

    // =========================================================================
    // PENDING ACTION FLAGS
    // =========================================================================

    /**
     * Flag indicating a pending request to start monitoring.
     *
     * This is set to true when the user attempts to start monitoring but
     * permissions are not yet granted. After the permission request completes,
     * if all permissions are granted and this flag is true, monitoring will
     * automatically start.
     *
     * Flow:
     * 1. User taps "START MONITORING"
     * 2. If permissions missing: pendingStart = true, request permissions
     * 3. In permission callback: if pendingStart && all granted -> startMonitoring()
     * 4. Reset pendingStart = false
     */
    private var pendingStart = false

    // =========================================================================
    // PERMISSION LAUNCHER - Activity Result API
    // =========================================================================

    /**
     * Permission request launcher using the modern Activity Result API.
     *
     * This replaces the deprecated startActivityForResult() approach with a
     * type-safe, lifecycle-aware contract system. The launcher handles
     * multiple permissions in a single request.
     *
     * Callback behavior:
     * - Returns a Map of permission names to their grant status
     * - Checks if location permissions were granted for location updates
     * - If pendingStart is true and all permissions granted, starts monitoring
     *
     * Permissions requested:
     * - ACCESS_FINE_LOCATION: GPS-level accuracy for location tracking
     * - ACCESS_COARSE_LOCATION: Network-level accuracy (fallback)
     * - SEND_SMS: For sending security alerts to emergency contact
     * - POST_NOTIFICATIONS: For showing alerts on Android 13+
     */
    private val launcher = registerForActivityResult(
        // Contract for requesting multiple permissions
        ActivityResultContracts.RequestMultiplePermissions()
    ) { result ->
        // 'result' is a Map<String, Boolean> where:
        // - Key: permission name (e.g., "android.permission.ACCESS_FINE_LOCATION")
        // - Value: true if granted, false if denied

        // Log the complete permission result for debugging
        Log.d(TAG, "Permission result: $result")

        // Check if location permissions were granted
        // We need either FINE or COARSE location permission
        val locationGranted = result[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                result[Manifest.permission.ACCESS_COARSE_LOCATION] == true

        Log.d(TAG, "Location permission granted: $locationGranted")

        // If location permission granted, start/restart location updates
        // This ensures we have real-time GPS coordinates for alerts
        if (locationGranted) {
            Log.d(TAG, "Calling restartLocationUpdates from permission callback")
            viewModel.restartLocationUpdates(this)
        }

        // Handle pending monitoring start request
        if (pendingStart) {
            // Check if ALL requested permissions were granted
            // result.values.all { it } returns true only if every permission is granted
            val allGranted = result.values.all { it }

            if (allGranted) {
                // All permissions granted - safe to start monitoring
                viewModel.startMonitoring(this)
            } else {
                // Not all permissions granted - log warning, cannot start
                Log.w(TAG, "Cannot start monitoring - not all permissions granted")
            }

            // Reset the pending flag regardless of outcome
            pendingStart = false
        }
    }

    // =========================================================================
    // LIFECYCLE METHODS
    // =========================================================================

    /**
     * Called when the activity is first created.
     *
     * This is the main initialization method where we:
     * 1. Call super.onCreate() to perform default Activity initialization
     * 2. Enable edge-to-edge display for modern Android UI
     * 3. Check for required permissions
     * 4. Initialize the ViewModel (with or without location updates)
     * 5. Request missing permissions if needed
     * 6. Set up the Compose UI content
     *
     * Permission flow:
     * - If all permissions granted: initialize() with location updates
     * - If permissions missing: initializeWithoutLocation() then request permissions
     *
     * @param savedInstanceState The previously saved instance state, if any.
     *                           Null on first creation, contains saved state
     *                           on recreation after process death.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        // Call superclass to perform standard Activity initialization
        // This must be the first statement in onCreate()
        super.onCreate(savedInstanceState)

        // Enable edge-to-edge display
        // This allows content to draw behind system bars (status bar, navigation bar)
        // for a more immersive experience on modern Android devices
        enableEdgeToEdge()

        // Determine which permissions are still needed
        // missingPermissions() returns an array of permissions not yet granted
        val missing = missingPermissions()
        Log.d(TAG, "Missing permissions: ${missing.toList()}")

        if (missing.isEmpty()) {
            // All permissions already granted - full initialization
            // This includes starting continuous location updates
            Log.d(TAG, "Permissions already granted, calling initialize()")
            viewModel.initialize(this)
        } else {
            // Some permissions are missing
            // Initialize without location updates first (will be enabled after permission grant)
            Log.d(TAG, "Permissions missing, calling initializeWithoutLocation()")
            viewModel.initializeWithoutLocation(this)

            // Request the missing permissions
            // The launcher callback will handle the result
            Log.d(TAG, "Launching permission request for: ${missing.toList()}")
            launcher.launch(missing)
        }

        // Set up the Jetpack Compose UI content
        // This replaces the traditional setContentView() with Compose
        setContent {
            // Apply the application theme to all composables
            SpotItTheme {
                // Collect UI state from ViewModel with lifecycle awareness
                // This ensures the UI only updates when the activity is in foreground
                val state by viewModel.uiState.collectAsStateWithLifecycle()

                // Get the Android Context for use in composables
                val context = LocalContext.current

                // Current tab state - persists across configuration changes
                // 0 = Home, 1 = Settings, 2 = History
                var tab by rememberSaveable { mutableStateOf(0) }

                // Pattern lock visibility state
                // When true, the pattern lock overlay is shown
                var showPattern by rememberSaveable { mutableStateOf(false) }

                // =================================================================
                // SCAFFOLD - Main UI Structure
                // =================================================================

                /**
                 * Scaffold provides the basic Material Design layout structure.
                 *
                 * It includes:
                 * - Top app bar (topBar parameter)
                 * - Bottom navigation bar (bottomBar parameter)
                 * - Content area (content lambda)
                 *
                 * The 'padding' parameter in the content lambda provides
                 * appropriate padding to avoid overlapping with system bars.
                 */
                Scaffold(
                    // Top App Bar with application title
                    topBar = {
                        TopAppBar(
                            title = {
                                Text("SPOTit Anti-Theft")  // Application name displayed in header
                            }
                        )
                    },

                    // Bottom Navigation Bar with three tabs
                    bottomBar = {
                        NavigationBar {
                            // Define navigation items using Triple:
                            // (index, label text, icon resource ID)
                            listOf(
                                Triple(0, "Home", R.drawable.ic_home),
                                Triple(1, "Settings", R.drawable.ic_settings),
                                Triple(2, "History", R.drawable.ic_history)
                            ).forEach { (index, label, iconRes) ->
                                /**
                                 * NavigationBarItem - Individual tab in bottom navigation.
                                 *
                                 * Parameters:
                                 * - selected: Whether this item is currently active
                                 * - onClick: Callback when this item is tapped
                                 * - icon: The icon to display
                                 * - label: The text label below the icon
                                 */
                                NavigationBarItem(
                                    // Highlight this item if it's the current tab
                                    selected = tab == index,

                                    // Update tab state when clicked
                                    onClick = { tab = index },

                                    // Icon image loaded from drawable resources
                                    icon = {
                                        Image(
                                            painter = painterResource(id = iconRes),
                                            contentDescription = label,  // Accessibility description
                                            modifier = Modifier.size(20.dp),  // Fixed icon size
                                            contentScale = ContentScale.Fit  // Scale to fit bounds
                                        )
                                    },

                                    // Text label below the icon
                                    label = { Text(label) }
                                )
                            }
                        }
                    }
                ) { padding ->
                    // =============================================================
                    // MAIN CONTENT AREA
                    // =============================================================

                    /**
                     * Main content container.
                     *
                     * The Box layout allows for:
                     * 1. Background coloring
                     * 2. Padding from system bars (provided by Scaffold)
                     * 3. Layering of pattern lock overlay on top of content
                     */
                    Box(
                        modifier = Modifier
                            .fillMaxSize()          // Fill available space
                            .background(Color.White) // White background
                            .padding(padding)       // Apply Scaffold padding (avoids overlap with bars)
                            .padding(16.dp)         // Additional content padding
                    ) {
                        /**
                         * Column layout for vertical arrangement of content.
                         *
                         * Items are arranged vertically with 16dp spacing between them.
                         */
                        Column(
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            /**
                             * Content container box.
                             *
                             * Wraps the screen content to allow for conditional
                             * rendering based on current tab selection.
                             */
                            Box(modifier = Modifier.fillMaxWidth()) {
                                // =================================================
                                // TAB CONTENT ROUTING
                                // =================================================

                                /**
                                 * When expression to show different screens based on tab.
                                 *
                                 * tab 0: HomeScreen - Main dashboard with monitoring controls
                                 * tab 1: SettingsScreen - Configuration options
                                 * tab 2: HistoryScreen - Alert event history
                                 */
                                when (tab) {
                                    // HOME TAB
                                    0 -> HomeScreen(
                                        // Pass current UI state to HomeScreen
                                        state = state,

                                        // Toggle callback for starting/stopping monitoring
                                        onToggle = {
                                            if (state.isMonitoring) {
                                                // Monitoring is active - user wants to stop
                                                // Show pattern lock for security verification
                                                showPattern = true
                                            } else {
                                                // Monitoring is inactive - user wants to start
                                                // Check if permissions are granted first
                                                val missingPerms = missingPermissions()

                                                if (missingPerms.isEmpty()) {
                                                    // All permissions granted - start monitoring immediately
                                                    viewModel.startMonitoring(this@MainActivity)
                                                } else {
                                                    // Permissions missing - set pending flag and request
                                                    pendingStart = true
                                                    launcher.launch(missingPerms)
                                                }
                                            }
                                        }
                                    )

                                    // SETTINGS TAB
                                    1 -> SettingsScreen(
                                        // Pass current settings to display in form
                                        settings = state.settings,

                                        // Callback when user saves settings changes
                                        onSave = viewModel::saveSettings
                                    )

                                    // HISTORY TAB (else catches tab index 2)
                                    else -> HistoryScreen(
                                        // Pass the list of alert events for display
                                        events = state.events
                                    )
                                }
                            }

                            // =================================================
                            // DEMO BUTTON (COMMENTED OUT)
                            // =================================================

                            /**
                             * Demo button for presentation purposes.
                             *
                             * This button is commented out in production builds.
                             * It was used during development/presentation to simulate
                             * an alert without actual motion detection.
                             *
                             * Uncomment this code to enable demo mode for testing.
                             */
                            // if (tab == 0) {
                            //     Button(
                            //         onClick = { viewModel.simulateAlert() },
                            //         modifier = Modifier.fillMaxWidth()
                            //     ) {
                            //         Text("Demo alert για παρουσίαση")
                            //     }
                            // }

                            // =================================================
                            // PATTERN LOCK OVERLAY
                            // =================================================

                            /**
                             * Pattern lock screen overlay.
                             *
                             * Shown when the user attempts to stop monitoring.
                             * Requires entering the correct pattern to disable,
                             * preventing unauthorized stopping of the anti-theft service.
                             *
                             * This provides security against thieves who might try
                             * to disable the alarm.
                             */
                            if (showPattern) {
                                PatternLockScreen(
                                    // The expected pattern that must be entered
                                    expectedPattern = state.settings.pattern,

                                    // Callback when correct pattern is entered
                                    onSuccess = {
                                        // Pattern correct - stop monitoring
                                        viewModel.stopMonitoring(context)
                                        showPattern = false  // Hide pattern lock
                                    },

                                    // Callback when user cancels pattern entry
                                    onCancel = {
                                        showPattern = false  // Hide pattern lock, keep monitoring
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // =========================================================================
    // ON RESUME - Lifecycle callback
    // =========================================================================

    /**
     * Called when the activity is becoming visible to the user.
     *
     * This callback is invoked when:
     * - The activity is first created (after onCreate)
     * - The activity is returning from the background
     * - The activity is being re-activated after being paused
     *
     * Here we use it to ensure location updates are running when the user
     * returns to the app. This handles the case where:
     * 1. User denied location permission initially
     * 2. User goes to Settings and grants location permission
     * 3. User returns to app - onResume detects and starts location updates
     *
     * This is important because location updates may have been stopped
     * or not started due to missing permissions.
     */
    override fun onResume() {
        // Call superclass to perform standard Activity resume
        super.onResume()

        Log.d(TAG, "onResume called, checking location permissions...")

        // Check if location permissions are currently granted
        // Need either FINE or COARSE location permission
        val locationPermissionsGranted =
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED ||
                    ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED

        Log.d(TAG, "Location permissions granted in onResume: $locationPermissionsGranted")

        // If permissions are granted, ensure location updates are active
        if (locationPermissionsGranted) {
            Log.d(TAG, "Location permissions granted, ensuring location updates are started")
            // Restart location updates to ensure they're running
            // This is safe to call multiple times - it will only start if not already running
            viewModel.restartLocationUpdates(this)
        }
    }

    // =========================================================================
    // PERMISSION HELPER METHODS
    // =========================================================================

    /**
     * Returns the list of required permissions for the application.
     *
     * This method defines all the permissions that SPOTit needs to function:
     *
     * 1. **ACCESS_FINE_LOCATION**: High-accuracy GPS location
     *    - Provides precise location for security alerts
     *    - Used to track device position when motion detected
     *
     * 2. **ACCESS_COARSE_LOCATION**: Approximate network-based location
     *    - Fallback when fine location is unavailable
     *    - Less battery drain but lower accuracy
     *
     * 3. **SEND_SMS**: Ability to send SMS messages
     *    - Core feature: sends alert messages to emergency contact
     *    - Contains location and alert information
     *
     * 4. **POST_NOTIFICATIONS**: Show notifications (Android 13+)
     *    - Required on API 33+ for foreground service notification
     *    - Only added for Android 13 and above
     *
     * @return List of permission string constants required by the app
     */
    private fun requiredPermissions(): List<String> {
        // Mutable list to build the required permissions
        val items = mutableListOf(
            Manifest.permission.ACCESS_FINE_LOCATION,   // High-accuracy GPS
            Manifest.permission.ACCESS_COARSE_LOCATION,  // Network-based location
            Manifest.permission.SEND_SMS                  // SMS sending capability
        )

        // Add POST_NOTIFICATIONS permission only for Android 13 (Tiramisu) and above
        // This permission didn't exist before API 33
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            items += Manifest.permission.POST_NOTIFICATIONS
        }

        return items
    }

    /**
     * Returns an array of permissions that are not yet granted.
     *
     * This method is used to determine which permissions need to be requested.
     * It filters the required permissions list to only include those that
     * have not been granted by the user.
     *
     * The returned array can be passed directly to the permission launcher
     * to request the missing permissions.
     *
     * Flow:
     * 1. Get list of all required permissions
     * 2. Filter to only those where permission is NOT granted
     * 3. Convert to array for use with permission launcher
     *
     * @return Array of permission strings that need to be requested
     */
    private fun missingPermissions(): Array<String> {
        return requiredPermissions()
            // Filter to only permissions not yet granted
            .filter {
                ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
            }
            // Convert to array for the permission launcher
            .toTypedArray()
    }
}
