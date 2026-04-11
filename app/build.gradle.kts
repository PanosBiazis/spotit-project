/**
 * SPOTit Anti-Theft Application
 *
 * app/build.gradle.kts - App-Level Build Configuration
 *
 * This file contains the build configuration for the SPOTit Android application module.
 * It defines the compile options, build features, and dependencies required for the app.
 *
 * Build System: Gradle with Kotlin DSL (Domain Specific Language)
 * - Uses Kotlin script syntax (.kts extension)
 * - Type-safe configuration with IDE support
 * - Better refactoring and code navigation
 *
 * Build Configuration Overview:
 * - Plugin declarations (Android, Kotlin, Compose)
 * - Android SDK configuration (compileSdk, minSdk, targetSdk)
 * - Build types (debug, release)
 * - Compile options (Java version, Kotlin JVM target)
 * - Build features (Jetpack Compose)
 * - Dependencies (AndroidX, Compose, Google Play Services)
 *
 * @author Panos Biazis
 * @date 10/4/2026
 * @version 2.0
 *
 * Copyright (c) 2026 Panos Biazis. All rights reserved.
 */

// =============================================================================
// PLUGIN DECLARATIONS
// =============================================================================

/**
 * Plugins block - Declares all Gradle plugins used by this module.
 *
 * Plugins extend Gradle's capabilities for specific build tasks.
 * Each plugin provides pre-configured tasks and extensions.
 */
plugins {
    /**
     * Android Application Plugin.
     *
     * ID: com.android.application
     * Source: Declared in root build.gradle.kts (version 9.1.0)
     *
     * This is the core Android Gradle plugin that:
     * - Compiles Kotlin/Java source code
     * - Packages resources (layouts, drawables, etc.)
     * - Builds APK/AAB (Android App Bundle)
     * - Manages build variants (debug, release)
     * - Handles code shrinking (Proguard/R8)
     * - Signs APKs for distribution
     *
     * Version 9.1.0 is the latest stable release supporting:
     * - JDK 17 as required toolchain
     * - Kotlin 2.0+ compatibility
     * - Compose compiler integration
     */
    id("com.android.application")

    /**
     * Kotlin Android Plugin.
     *
     * ID: org.jetbrains.kotlin.android
     * Source: Declared in root build.gradle.kts (version 2.2.10)
     *
     * This plugin enables Kotlin support for Android development:
     * - Compiles Kotlin source files (.kt)
     * - Provides Kotlin-specific Android extensions
     * - Integrates with Android build process
     * - Supports Kotlin synthetics (deprecated) and view binding
     *
     * Version 2.2.10 provides:
     * - Latest language features
     * - Performance improvements
     * - Better error messages
     * - K2 compiler support
     */
    id("org.jetbrains.kotlin.android")

    /**
     * Kotlin Compose Compiler Plugin.
     *
     * ID: org.jetbrains.kotlin.plugin.compose
     * Source: Declared in root build.gradle.kts (version 2.2.10)
     *
     * This plugin enables Jetpack Compose compilation:
     * - Transforms @Composable functions
     * - Generates compose runtime code
     * - Enables live literals for preview
     * - Optimizes compose performance
     *
     * Version must match the Kotlin plugin version (2.2.10).
     * Using the same version ensures compatibility between
     * Kotlin compiler and Compose compiler.
     *
     * Required for Jetpack Compose UI framework.
     */
    id("org.jetbrains.kotlin.plugin.compose")
}

// =============================================================================
// ANDROID CONFIGURATION BLOCK
// =============================================================================

/**
 * Android extension block - Configures the Android application module.
 *
 * This block defines all Android-specific build settings:
 * - Namespace for resource and manifest merging
 * - SDK versions (compile, minimum, target)
 * - Application ID (unique package identifier)
 * - Version information
 * - Build types and their configurations
 * - Compile options
 * - Build features
 */
android {

    // =========================================================================
    // NAMESPACE CONFIGURATION
    // =========================================================================

    /**
     * Application namespace.
     *
     * The namespace defines the package name for:
     * - Generated R.java class (R.com.spotit)
     * - BuildConfig class location
     * - AndroidManifest.xml package attribute
     * - Resource merging and access
     *
     * Package name: com.spotit
     * - Short and memorable
     * - Matches the main package structure
     * - Used in all source files
     *
     * Note: In older Android Gradle Plugin versions, this was defined
     * in AndroidManifest.xml as the 'package' attribute. Now it's
     * defined here in build.gradle.kts for better build configuration.
     *
     * All source files are under: app/src/main/kotlin/com/spotit/
     */
    namespace = "com.spotit"

    // =========================================================================
    // SDK VERSION CONFIGURATION
    // =========================================================================

    /**
     * Compile SDK version.
     *
     * Value: 35 (Android 15, Vanilla Ice Cream)
     *
     * This is the SDK version used to compile the application:
     * - Determines available APIs at compile time
     * - Sets the android.jar used for compilation
     * - Defines maximum API level that can be used
     *
     * Why SDK 35:
     * - Latest stable Android SDK
     * - Access to newest APIs and features
     * - Required for latest Material Design 3 components
     * - Better forward compatibility
     *
     * The compile SDK doesn't affect which devices can run the app;
     * that's determined by minSdk and targetSdk.
     */
    compileSdk = 35

    // =========================================================================
    // DEFAULT CONFIGURATION
    // =========================================================================

    /**
     * Default configuration for all build variants.
     *
     * These settings apply to both debug and release builds
     * unless overridden in specific build types.
     */
    defaultConfig {

        /**
         * Application ID.
         *
         * The unique identifier for the application on Google Play
         * and on devices. This is the "package name" that identifies
         * the app globally.
         *
         * Value: com.spotit
         * - Must be unique across all Android apps
         * - Used for Google Play Store listing
         * - Used for intent resolution
         * - Cannot be changed after first release
         *
         * Convention: Reverse domain notation (com.company.appname)
         */
        applicationId = "com.spotit"

        /**
         * Minimum SDK version.
         *
         * Value: 26 (Android 8.0, Oreo)
         *
         * The lowest Android version the app supports:
         * - Devices below this version cannot install the app
         * - Defines the minimum API level for code execution
         * - Affects available APIs and platform features
         *
         * Why minSdk 26:
         * - Android 8.0 (Oreo) released August 2017
         * - Covers 95%+ of active Android devices
         * - Provides notification channels (required for foreground service)
         * - Better background execution limits
         * - Improved security features
         *
         * Features available from API 26:
         * - Notification channels (essential for foreground service)
         * - Adaptive icons
         * - Autofill framework
         * - Better battery optimizations
         */
        minSdk = 26

        /**
         * Target SDK version.
         *
         * Value: 35 (Android 15, Vanilla Ice Cream)
         *
         * The Android version the app is designed and tested for:
         * - Indicates the app is optimized for this version
         * - Affects system behaviors and compatibility
         * - Required to be updated for Play Store policy compliance
         *
         * Why targetSdk 35:
         * - Latest stable Android version
         * - Required for Play Store (as of policy)
         * - Enables latest platform behaviors
         * - Better user experience on modern devices
         *
         * Setting targetSdk = compileSdk ensures:
         * - App uses latest platform features
         * - Full compatibility testing
         * - No unexpected compatibility behaviors
         */
        targetSdk = 35

        /**
         * Version code.
         *
         * Value: 2
         *
         * An integer representing the app version for update management:
         * - Must be incremented for each release
         * - Google Play uses this to determine update availability
         * - Must be greater than previous version
         * - Typically incremented by 1 for each release
         *
         * Version history:
         * - 1: Initial release
         * - 2: Second release (current)
         *
         * Important: Always increment for new releases,
         * even for minor updates.
         */
        versionCode = 2

        /**
         * Version name.
         *
         * Value: "2.0"
         *
         * Human-readable version string displayed to users:
         * - Shown in Google Play Store
         * - Shown in app settings
         * - Used for marketing and communication
         *
         * Format: Major.Minor.Patch (Semantic Versioning)
         * - Major (2): Breaking changes, significant features
         * - Minor (0): New features, backward compatible
         * - Patch (implicit): Bug fixes, minor improvements
         *
         * Current version 2.0 indicates:
         * - Second major release
         * - Significant improvements from version 1.x
         */
        versionName = "2.0"
    }

    // =========================================================================
    // BUILD TYPES CONFIGURATION
    // =========================================================================

    /**
     * Build types define different compilation configurations.
     *
     * Standard build types:
     * - debug: Development builds with debugging enabled
     * - release: Production builds with optimizations
     *
     * This block configures each build type's characteristics.
     */
    buildTypes {

        /**
         * Release build type configuration.
         *
         * Release builds are for production distribution:
         * - Optimized for performance and size
         * - Signed with release key
         * - Uploaded to Google Play Store
         */
        release {
            /**
             * Enable code shrinking (R8 compiler).
             *
             * Value: false (disabled)
             *
             * When enabled, R8 performs:
             * - Code shrinking: Removes unused code
             * - Obfuscation: Renames classes/methods (ProGuard rules)
             * - Optimization: Improves runtime performance
             *
             * Why disabled:
             * - Can cause issues with reflection (used by some libraries)
             * - Requires careful ProGuard rule configuration
             * - May break serialization/deserialization
             * - Enable for production release after testing
             *
             * Note: Should be enabled for production releases,
             * but requires proper testing and ProGuard rules.
             */
            isMinifyEnabled = false

            /**
             * ProGuard configuration files.
             *
             * These files define rules for code shrinking/obfuscation:
             * - Default Android ProGuard rules
             * - Project-specific rules (proguard-rules.pro)
             *
             * Only used when isMinifyEnabled = true.
             *
             * Files:
             * 1. getDefaultProguardFile("proguard-android-optimize.txt"):
             *    - Android SDK default rules
             *    - Optimization-specific rules
             *    - Keeps essential Android components
             *
             * 2. "proguard-rules.pro":
             *    - Project-specific rules
             *    - Keep rules for reflection
             *    - Library-specific rules
             */
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    // =========================================================================
    // COMPILE OPTIONS - Java Version
    // =========================================================================

    /**
     * Java compile options.
     *
     * Defines the Java version used for compilation.
     * This affects both Java and Kotlin code generation.
     */
    compileOptions {
        /**
         * Source compatibility.
         *
         * Value: JavaVersion.VERSION_17 (Java 17)
         *
         * The Java version used for source code:
         * - Defines available Java language features
         * - Affects syntax and API availability
         * - Must match targetCompatibility
         *
         * Java 17 features available:
         * - Sealed classes
         * - Pattern matching
         * - Records (limited use in Android)
         * - Text blocks
         * - Strong encapsulation
         */
        sourceCompatibility = JavaVersion.VERSION_17

        /**
         * Target compatibility.
         *
         * Value: JavaVersion.VERSION_17 (Java 17)
         *
         * The Java version for generated bytecode:
         * - Must be >= sourceCompatibility
         * - Defines bytecode format version
         * - Affects runtime compatibility
         *
         * Why Java 17:
         * - Required by latest Android Gradle Plugin
         * - Best performance and features
         * - LTS (Long Term Support) version
         * - Industry standard for modern Android
         */
        targetCompatibility = JavaVersion.VERSION_17
    }

    // =========================================================================
    // KOTLIN OPTIONS
    // =========================================================================

    /**
     * Kotlin compiler options.
     *
     * Defines JVM target for Kotlin compilation.
     */
    kotlinOptions {
        /**
         * JVM target version.
         *
         * Value: "17" (Java 17)
         *
         * The JVM version for Kotlin bytecode generation:
         * - Must match Java compileOptions
         * - Defines available JVM features
         * - Affects interop with Java code
         *
         * Note: This is a string ("17"), not JavaVersion enum.
         * Setting to "17" ensures consistency with Java 17 compilation.
         */
        jvmTarget = "17"
    }

    // =========================================================================
    // BUILD FEATURES
    // =========================================================================

    /**
     * Build features configuration.
     *
     * Enables or disables specific build features.
     * Disabled features reduce build time and APK size.
     */
    buildFeatures {
        /**
         * Jetpack Compose support.
         *
         * Value: true (enabled)
         *
         * Enables Jetpack Compose UI framework:
         * - Modern declarative UI toolkit
         * - Replaces traditional XML layouts
         * - Better state management
         * - Live preview support
         *
         * Required for this project because:
         * - All UI is built with Compose
         * - No XML layout files used
         * - Material Design 3 components
         *
         * Enabling this:
         * - Adds Compose compiler
         * - Enables @Composable annotations
         * - Adds Compose tooling support
         */
        compose = true
    }
}

// =============================================================================
// DEPENDENCIES BLOCK
// =============================================================================

/**
 * Dependencies block - Declares all external libraries used by the app.
 *
 * Dependencies are external code that the app needs to compile and run.
 * Each dependency is specified with:
 * - Configuration (implementation, debugImplementation, etc.)
 * - Group:Artifact:Version coordinates
 *
 * Configuration types:
 * - implementation: Included in compile and runtime classpath
 * - debugImplementation: Only for debug builds
 * - debugImplementation: Development/testing tools
 */
dependencies {

    // =========================================================================
    // ANDROIDX CORE LIBRARIES
    // =========================================================================

    /**
     * AndroidX Core KTX.
     *
     * Group: androidx.core
     * Artifact: core-ktx
     * Version: 1.15.0
     *
     * Kotlin extensions for Android platform:
     * - Extension functions for Context, View, etc.
     * - Simplified Android API usage
     * - Coroutines support
     * - Lifecycle-aware components
     *
     * Example usage:
     * - context.systemService() instead of getSystemService()
     * - view.doOnPreDraw { } for view tree observers
     * - Bundle.of() for creating bundles
     */
    implementation("androidx.core:core-ktx:1.15.0")

    // =========================================================================
    // ACTIVITY COMPOSE
    // =========================================================================

    /**
     * Activity Compose.
     *
     * Group: androidx.activity
     * Artifact: activity-compose
     * Version: 1.9.3
     *
     * Integration between Activities and Jetpack Compose:
     * - setContent {} extension for setting Compose UI
     * - Activity result APIs for Compose
     * - Permission request handling
     * - Back handler support
     *
     * Used in SPOTit for:
     * - MainActivity.setContent { }
     * - Permission request launcher
     * - Back button handling
     */
    implementation("androidx.activity:activity-compose:1.9.3")

    // =========================================================================
    // LIFECYCLE LIBRARIES
    // =========================================================================

    /**
     * Lifecycle Runtime KTX.
     *
     * Group: androidx.lifecycle
     * Artifact: lifecycle-runtime-ktx
     * Version: 2.8.7
     *
     * Lifecycle-aware runtime components:
     * - Lifecycle scope for coroutines
     * - Lifecycle-aware data streaming
     * - Process lifecycle owner
     *
     * Used for:
     * - Coroutine scopes tied to lifecycle
     * - lifecycleScope.launch { }
     */
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")

    /**
     * Lifecycle ViewModel Compose.
     *
     * Group: androidx.lifecycle
     * Artifact: lifecycle-viewmodel-compose
     * Version: 2.8.7
     *
     * ViewModel integration with Compose:
     * - viewModel() composable for obtaining ViewModels
     * - StateFlow to Compose state conversion
     * - ViewModel lifecycle management
     *
     * Used in SPOTit for:
     * - val viewModel by viewModels<SpotItViewModel>()
     * - Connecting ViewModel to Compose UI
     */
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")

    /**
     * Lifecycle Runtime Compose.
     *
     * Group: androidx.lifecycle
     * Artifact: lifecycle-runtime-compose
     * Version: 2.8.7
     *
     * Runtime integration between Lifecycle and Compose:
     * - collectAsStateWithLifecycle() for state collection
     * - Lifecycle-aware state flow collection
     * - Proper disposal on lifecycle events
     *
     * Used in SPOTit for:
     * - val state by viewModel.uiState.collectAsStateWithLifecycle()
     * - Ensures state updates respect lifecycle
     */
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.7")

    // =========================================================================
    // DATASTORE PREFERENCES
    // =========================================================================

    /**
     * DataStore Preferences.
     *
     * Group: androidx.datastore
     * Artifact: datastore-preferences
     * Version: 1.1.1
     *
     * Modern data storage solution:
     * - Replacement for SharedPreferences
     * - Coroutine-based async API
     * - Type-safe preferences
     * - Better error handling
     *
     * Used in SPOTit for:
     * - Storing user settings (contact, pattern, sensitivity)
     * - SettingsStore class implementation
     * - Persistent configuration storage
     *
     * Key features:
     * - PreferencesDataStore for simple key-value pairs
     * - Flow-based reading
     * - Transactional writes
     */
    implementation("androidx.datastore:datastore-preferences:1.1.1")

    // =========================================================================
    // KOTLIN COROUTINES
    // =========================================================================

    /**
     * Kotlinx Coroutines Android.
     *
     * Group: org.jetbrains.kotlinx
     * Artifact: kotlinx-coroutines-android
     * Version: 1.9.0
     *
     * Kotlin coroutines for Android:
     * - Structured concurrency
     * - Main dispatcher for UI thread
     * - IO dispatcher for background work
     * - Lifecycle-aware coroutine scopes
     *
     * Used throughout SPOTit for:
     * - viewModelScope.launch { }
     * - Non-blocking operations
     * - Async location updates
     * - SMS sending in background
     *
     * Dispatchers:
     * - Dispatchers.Main: UI operations
     * - Dispatchers.IO: Network, file, database
     * - Dispatchers.Default: CPU-intensive work
     */
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")

    // =========================================================================
    // GOOGLE PLAY SERVICES - LOCATION
    // =========================================================================

    /**
     * Google Play Services Location.
     *
     * Group: com.google.android.gms
     * Artifact: play-services-location
     * Version: 21.3.0
     *
     * Location services from Google Play:
     * - FusedLocationProviderClient (recommended location API)
     * - Geofencing capabilities
     * - Activity recognition
     * - Location updates and callbacks
     *
     * Used in SPOTit for:
     * - LocationHelper class
     * - SpotItViewModel location updates
     * - GPS coordinates for SMS alerts
     *
     * Advantages over LocationManager:
     * - Better battery efficiency
     * - Combines GPS, network, and sensors
     * - Automatic provider selection
     * - Location caching
     */
    implementation("com.google.android.gms:play-services-location:21.3.0")

    // =========================================================================
    // JETPACK COMPOSE - BOM (Bill of Materials)
    // =========================================================================

    /**
     * Compose BOM (Bill of Materials).
     *
     * Group: androidx.compose
     * Artifact: compose-bom
     * Version: 2024.09.00
     *
     * BOM defines compatible versions for all Compose libraries:
     * - Ensures version compatibility
     * - Single version to update
     * - Prevents version conflicts
     *
     * Using BOM:
     * - Don't specify versions for Compose dependencies
     * - BOM provides consistent versions
     * - Easy to update all Compose at once
     *
     * Included libraries:
     * - compose-ui
     * - compose-ui-graphics
     * - compose-ui-tooling-preview
     * - compose-material3
     * - And many more
     */
    implementation(platform("androidx.compose:compose-bom:2024.09.00"))

    // =========================================================================
    // COMPOSE UI COMPONENTS
    // =========================================================================

    /**
     * Compose UI Core.
     *
     * Group: androidx.compose.ui
     * Artifact: ui
     * Version: From BOM
     *
     * Core Compose UI primitives:
     * - Basic layout components (Box, Column, Row)
     * - Modifier system
     * - Composable foundations
     * - UI state management
     *
     * Fundamental for all Compose UIs.
     */
    implementation("androidx.compose.ui:ui")

    /**
     * Compose UI Graphics.
     *
     * Group: androidx.compose.ui
     * Artifact: ui-graphics
     * Version: From BOM
     *
     * Graphics primitives for Compose:
     * - Canvas drawing
     * - Path and shape operations
     * - Image and vector graphics
     * - Color management
     *
     * Used for custom drawing and graphics.
     */
    implementation("androidx.compose.ui:ui-graphics")

    /**
     * Compose UI Tooling Preview.
     *
     * Group: androidx.compose.ui
     * Artifact: ui-tooling-preview
     * Version: From BOM
     *
     * Preview support for Compose:
     * - @Preview annotations
     * - Android Studio preview rendering
     * - Live preview of composables
     *
     * Allows viewing composables in IDE without running app.
     */
    implementation("androidx.compose.ui:ui-tooling-preview")

    /**
     * Compose Material 3.
     *
     * Group: androidx.compose.material3
     * Artifact: material3
     * Version: From BOM
     *
     * Material Design 3 components for Compose:
     * - Material You design system
     * - Modern UI components
     * - Dynamic color support
     * - Accessibility improvements
     *
     * Components used in SPOTit:
     * - Scaffold, TopAppBar
     * - Card, Button, Text
     * - NavigationBar, NavigationBarItem
     * - OutlinedTextField, Slider, Switch
     *
     * Material 3 features:
     * - Updated color system
     * - Rounded corners
     * - Improved touch targets
     * - Better accessibility
     */
    implementation("androidx.compose.material3:material3")

    // =========================================================================
    // COMPOSE DEBUG TOOLING
    // =========================================================================

    /**
     * Compose UI Tooling (Debug only).
     *
     * Group: androidx.compose.ui
     * Artifact: ui-tooling
     * Version: From BOM
     *
     * Debug tooling for Compose:
     * - Layout inspector integration
     * - Compose inspection
     * - Recomposition counts
     * - Performance profiling
     *
     * Only included in debug builds:
     * - Reduces release APK size
     * - Development-only tools
     */
    debugImplementation("androidx.compose.ui:ui-tooling")

    // =========================================================================
    // MATERIAL COMPONENTS (XML Theme Support)
    // =========================================================================

    /**
     * Material Components Library.
     *
     * Group: com.google.android.material
     * Artifact: material
     * Version: 1.12.0
     *
     * Material Design Components for XML themes:
     * - Material theme attributes
     * - XML theme support
     * - Legacy component theming
     *
     * Why included:
     * - Provides theme attributes for res/values/themes.xml
     * - Enables Material 3 theming in XML
     * - Used by Activities for window theming
     *
     * Note: Even though SPOTit uses Compose UI,
     * this library provides theme attributes that
     * affect the activity window and system UI.
     */
    implementation("com.google.android.material:material:1.12.0")
}

// =============================================================================
// BUILD CONFIGURATION NOTES
// =============================================================================

/**
 * Additional Build Configuration Information:
 *
 * Gradle Build Process:
 * 1. Resolve dependencies from Maven repositories
 * 2. Compile Kotlin and Java source code
 * 3. Process resources (layouts, drawables, etc.)
 * 4. Generate BuildConfig and R classes
 * 5. Package into APK/AAB
 * 6. Sign with debug or release key
 *
 * Dependency Resolution:
 * - Maven Central: Primary repository for AndroidX
 * - Google Maven: Play Services and Material
 * - Gradle caches downloaded dependencies locally
 *
 * Build Variants:
 * - Debug: Quick builds, debugging enabled, signed with debug key
 * - Release: Optimized builds, code shrinking, signed with release key
 *
 * Performance Tips:
 * - Use specific dependency versions (not +)
 * - Enable Gradle configuration caching
 * - Use build scans for diagnosis
 * - Keep dependencies up to date
 *
 * Future Improvements:
 * 1. Enable R8 for release builds (isMinifyEnabled = true)
 * 2. Add build variants for different environments
 * 3. Add flavor dimensions for different app configurations
 * 4. Configure signing configurations for release
 * 5. Add test dependencies (unit tests, UI tests)
 * 6. Consider KMP (Kotlin Multiplatform) for shared code
 *
 * Version Update Guidelines:
 * - Update compileSdk with new Android releases
 * - Update dependencies for security fixes
 * - Keep targetSdk at latest stable
 * - Test thoroughly after updates
 */
