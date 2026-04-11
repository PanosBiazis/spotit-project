/**
 * SPOTit Anti-Theft Application
 *
 * build.gradle.kts (Project Level) - Root Build Configuration
 *
 * This file contains the project-level build configuration for the SPOTit
 * Android application. It defines the plugins and versions used across all
 * modules in the project.
 *
 * Project Structure:
 * - This is a single-module Android application
 * - Uses Kotlin DSL for Gradle configuration
 * - Follows modern Android Gradle Plugin conventions
 *
 * Plugins Declared:
 * - Android Application Plugin: Core Android build support
 * - Kotlin Android Plugin: Kotlin language support for Android
 * - Kotlin Compose Plugin: Jetpack Compose compiler support
 *
 * @author Panos Biazis
 * @date 10/4/2026
 * @version 2.0
 *
 * Copyright (c) 2026 Panos Biazis. All rights reserved.
 */

// =============================================================================
// GRADLE PLUGINS BLOCK
// =============================================================================

/**
 * Plugins block - Declares all plugins used in the project.
 *
 * The plugins {} block is the modern way to declare Gradle plugins.
 * It provides a type-safe DSL for applying plugins.
 *
 * The 'apply false' syntax:
 * - Declares the plugin version at the project level
 * - Makes the plugin available to submodules
 * - Does NOT apply the plugin to the root project
 * - Submodules apply the plugin with 'id("...")' without version
 *
 * This centralizes version management in one place, making updates easier.
 */
plugins {
    /**
     * Android Application Plugin - Core Android Build Support
     *
     * Version: 9.1.0
     *
     * This plugin provides:
     * - Android application build configuration
     * - APK generation and signing
     * - Resource compilation and packaging
     * - Manifest merging and processing
     * - Support for Android-specific source sets
     * - Integration with Android SDK and build tools
     *
     * Version 9.1.0 Features:
     * - Support for Android 15 (API 35)
     * - Improved build performance
     * - Enhanced Kotlin support
     * - Better Gradle compatibility
     *
     * The actual application of this plugin happens in app/build.gradle.kts
     * using: id("com.android.application") (without version, inherited here)
     *
     * Note: 'apply false' means this plugin is NOT applied to the root project,
     * only declared. The app module will apply it.
     */
    id("com.android.application") version "9.1.0" apply false

    /**
     * Kotlin Android Plugin - Kotlin Language Support for Android
     *
     * Version: 2.2.10
     *
     * This plugin provides:
     * - Kotlin compilation for Android
     * - Kotlin Android Extensions (deprecated, not used here)
     * - Integration with Android build variants
     * - Kotlin annotation processing support
     * - Interoperability with Java code
     *
     * Version 2.2.10 Features:
     * - Latest Kotlin language features
     * - Improved compilation performance
     * - Better error messages
     * - Enhanced coroutine support
     * - Smart cast improvements
     *
     * Kotlin Version Compatibility:
     * - This version is compatible with Compose Compiler 2.2.10
     * - Matches the compose plugin version for optimal compatibility
     * - Supports all modern Kotlin language features
     *
     * Applied in: app/build.gradle.kts
     */
    id("org.jetbrains.kotlin.android") version "2.2.10" apply false

    /**
     * Kotlin Compose Plugin - Jetpack Compose Compiler Support
     *
     * Version: 2.2.10
     *
     * This plugin provides:
     * - Compose compiler integration
     * - Kotlin-specific Compose optimizations
     * - Support for Compose-specific language features
     * - @Composable function transformation
     * - Compose-specific code generation
     *
     * Version 2.2.10 Features:
     * - Compatible with Kotlin 2.2.10
     * - Improved recomposition performance
     * - Better debugging support for Compose
     * - Strong skipping mode for performance
     *
     * Why This Plugin:
     * In Kotlin 2.0+, the Compose compiler is distributed as a Gradle plugin
     * instead of a compiler plugin. This provides better integration and
     * version management.
     *
     * Compose BOM Version:
     * The Compose BOM (Bill of Materials) version is defined in app/build.gradle.kts
     * and should be compatible with this compiler version.
     *
     * Applied in: app/build.gradle.kts
     */
    id("org.jetbrains.kotlin.plugin.compose") version "2.2.10" apply false
}

// =============================================================================
// PROJECT-LEVEL CONFIGURATION NOTES
// =============================================================================

/**
 * Project Structure Overview:
 *
 * Root Project (spotit-project/)
 * ├── build.gradle.kts        <- This file (project-level config)
 * ├── settings.gradle.kts     <- Project settings and module discovery
 * ├── gradle.properties       <- Gradle properties (JVM args, etc.)
 * └── app/                    <- Single application module
 *     ├── build.gradle.kts    <- Module-level build config
 *     └── src/main/           <- Application source code
 *
 * Single-Module Project:
 * - This is a single-module Android application
 * - All source code is in the 'app' module
 * - No library modules or feature modules
 * - Simpler build configuration than multi-module projects
 *
 * Version Catalog (Future):
 * - Consider using Gradle Version Catalogs for dependency management
 * - Provides type-safe access to dependencies
 * - Centralized in gradle/libs.versions.toml
 * - Not used in current configuration but recommended for larger projects
 *
 * Build Performance:
 * - Gradle 9.x has improved build caching
 * - Kotlin 2.x has faster compilation
 * - Consider enabling build scan for performance monitoring
 * - Use Gradle daemon for faster incremental builds
 *
 * CI/CD Considerations:
 * - This configuration works with common CI systems
 * - No custom Gradle tasks defined at project level
 * - Standard Android build and test tasks available
 * - Consider adding detekt or ktlint for code quality
 */

// =============================================================================
// VERSION COMPATIBILITY NOTES
// =============================================================================

/**
 * Version Compatibility Matrix:
 *
 * | Component              | Version    | Notes                        |
 * |------------------------|------------|------------------------------|
 * | Android Gradle Plugin  | 9.1.0      | Latest stable, supports API 35|
 * | Kotlin                 | 2.2.10     | Matches Compose compiler     |
 * | Compose Compiler       | 2.2.10     | Kotlin Compose plugin        |
 * | Compose BOM            | 2024.09.00 | Defined in app/build.gradle  |
 * | Target SDK             | 35         | Android 15                   |
 * | Min SDK                | 26         | Android 8.0 Oreo             |
 * | Java Compatibility     | 17         | Required by AGP 9.x          |
 *
 * Upgrade Path:
 * - Check Android Gradle Plugin release notes before upgrading
 * - Ensure Kotlin version matches Compose compiler version
 * - Update Compose BOM when updating Compose compiler
 * - Test thoroughly after version updates
 *
 * Breaking Changes Awareness:
 * - AGP 9.x requires Java 17
 * - Kotlin 2.x has different Compose compiler integration
 * - Newer AGP versions may require Gradle version updates
 * - Check migration guides when updating major versions
 */

// =============================================================================
// GRADLE CONFIGURATION BEST PRACTICES
// =============================================================================

/**
 * Best Practices Applied:
 *
 * 1. Centralized Version Management:
 *    - Plugin versions declared once at project level
 *    - Submodules inherit versions without duplication
 *    - Easier to update and maintain
 *
 * 2. Modern Plugin DSL:
 *    - Using plugins {} block (recommended approach)
 *    - Type-safe plugin references
 *    - Better IDE support and auto-completion
 *
 * 3. Version Consistency:
 *    - Kotlin version matches Compose compiler version
 *    - Prevents compatibility issues
 *    - Follows official recommendations
 *
 * Potential Improvements:
 * - Add Gradle Version Catalogs for dependencies
 * - Add custom tasks for project-specific needs
 * - Add buildSrc for custom Gradle plugins
 * - Add detekt/ktlint for code quality
 * - Add dependency update automation (Renovate/Dependabot)
 */

// =============================================================================
// ADDITIONAL DOCUMENTATION
// =============================================================================

/**
 * External Resources:
 *
 * Android Gradle Plugin:
 * - Release Notes: https://developer.android.com/build/releases/gradle-plugin
 * - Migration Guide: https://developer.android.com/build/migrate-to-catalogs
 * - DSL Reference: https://developer.android.com/reference/tools/gradle-api/
 *
 * Kotlin Gradle Plugin:
 * - Documentation: https://kotlinlang.org/docs/gradle-configure-project.html
 * - Compiler Options: https://kotlinlang.org/docs/compiler-options.html
 *
 * Jetpack Compose:
 * - Setup Guide: https://developer.android.com/jetpack/compose/setup
 * - Compose Compiler: https://developer.android.com/jetpack/compose/compiler
 *
 * Gradle:
 * - Plugin DSL: https://docs.gradle.org/current/userguide/plugins.html
 * - Build Environment: https://docs.gradle.org/current/userguide/build_environment.html
 *
 * Project-Specific Notes:
 * - This project targets anti-theft functionality
 * - Uses foreground service for background monitoring
 * - Requires location and SMS permissions
 * - See app/build.gradle.kts for module-specific configuration
 */
