/**
 * SPOTit Anti-Theft Application
 *
 * SpotItRepository.kt - Repository Layer for Data Access
 *
 * This file implements the Repository pattern for the SPOTit application,
 * providing a clean abstraction layer between the data sources (SettingsStore)
 * and the rest of the application (ViewModel). The Repository pattern is a
 * key component of modern Android architecture.
 *
 * Repository Pattern Benefits:
 * - Separates data access logic from business logic
 * - Provides a single source of truth for application data
 * - Makes data sources interchangeable (e.g., could add remote storage)
 * - Simplifies testing by allowing mock repositories
 * - Centralizes data-related operations
 *
 * @author Panos Biazis
 * @date 10/4/2026
 * @version 2.0
 *
 * Copyright (c) 2026 Panos Biazis. All rights reserved.
 */

// Package declaration - defines the domain layer package
// The domain layer contains business logic and repository interfaces
package com.spotit.domain

// =============================================================================
// APPLICATION DATA MODEL IMPORTS
// =============================================================================

// Import the data classes used in this repository
import com.spotit.data.AlertEvent     // Alert event model for creating alerts
import com.spotit.data.AppSettings    // Settings model for configuration data
import com.spotit.data.SettingsStore  // DataStore wrapper for persistence

// =============================================================================
// JAVA UTILITY IMPORTS
// =============================================================================

// Date formatting utilities for creating human-readable timestamps
import java.text.SimpleDateFormat  // Formats dates to strings
import java.util.Date              // Represents a point in time
import java.util.Locale            // For locale-specific formatting

// =============================================================================
// KOTLIN COROUTINES IMPORTS
// =============================================================================

// Flow is Kotlin's cold asynchronous data stream
import kotlinx.coroutines.flow.Flow  // Reactive stream for data observation

/**
 * SpotItRepository - Data Access Repository
 *
 * This class implements the Repository pattern, serving as the single source
 * of truth for application data. It abstracts the data layer from the
 * presentation layer (ViewModel), following Clean Architecture principles.
 *
 * Responsibilities:
 *
 * 1. **Settings Management**:
 *    - Exposes settings as a Flow for reactive observation
 *    - Delegates persistence operations to SettingsStore
 *    - Provides save method for updating settings
 *
 * 2. **Alert Event Creation**:
 *    - Factory method for creating AlertEvent instances
 *    - Handles timestamp formatting consistently
 *    - Ensures events have proper structure
 *
 * Architecture Position:
 * ```
 * ViewModel -> Repository -> SettingsStore -> DataStore -> File System
 * ```
 *
 * The Repository sits between the ViewModel and data sources, providing:
 * - Abstraction: ViewModel doesn't know about DataStore
 * - Transformation: Can modify data before it reaches ViewModel
 * - Orchestration: Can combine data from multiple sources
 * - Caching: Could cache data for performance (not implemented here)
 *
 * Dependency Injection:
 * This class is instantiated in SpotItViewModel.initialize() with:
 * ```kotlin
 * val repo = SpotItRepository(SettingsStore(appContext))
 * ```
 *
 * Testing:
 * The repository can be easily mocked for testing ViewModels:
 * ```kotlin
 * class MockRepository : SpotItRepository(mockSettingsStore) {
 *     override val settings = flowOf(testSettings)
 * }
 * ```
 *
 * Future Extensions:
 * - Add remote storage for backup
 * - Add local database for event history
 * - Add caching layer
 * - Add data validation logic
 *
 * @property store The SettingsStore instance used for persisting settings.
 *                 Injected via constructor for testability.
 *
 * @see SettingsStore - The underlying persistence mechanism
 * @see AppSettings - The settings data model
 * @see AlertEvent - The alert event data model
 * @see SpotItViewModel - Uses this repository for data access
 */
class SpotItRepository(private val store: SettingsStore) {

    // =========================================================================
    // SETTINGS FLOW - Reactive Settings Access
    // =========================================================================

    /**
     * Flow of AppSettings that emits whenever settings change.
     *
     * This property exposes settings as a Flow, allowing the ViewModel
     * to observe changes reactively. The Flow is created by the
     * SettingsStore and forwarded unchanged.
     *
     * Flow Characteristics:
     * - Cold: Doesn't start emitting until collected
     * - Active: Continues emitting while being collected
     * - Sequential: Events are delivered in order
     * - Thread-safe: Multiple collectors are supported
     *
     * Usage in ViewModel:
     * ```kotlin
     * viewModelScope.launch {
     *     repository.settings.collect { settings ->
     *         _uiState.update { it.copy(settings = settings) }
     *     }
     * }
     * ```
     *
     * Data Flow:
     * 1. User changes settings in SettingsScreen
     * 2. ViewModel.saveSettings() calls repository.saveSettings()
     * 3. SettingsStore writes to DataStore
     * 4. DataStore emits new preferences
     * 5. SettingsStore.settingsFlow emits new AppSettings
     * 6. This settings Flow emits to collectors
     * 7. ViewModel receives update and updates UI state
     *
     * Lifecycle:
     * - Created when repository is instantiated
     * - Active while being collected
     * - Cancelled when collecting coroutine is cancelled
     *
     * @return Flow<AppSettings> that emits the current settings
     *         and any subsequent updates.
     */
    val settings: Flow<AppSettings> = store.settingsFlow

    // =========================================================================
    // SETTINGS SAVE METHOD
    // =========================================================================

    /**
     * Saves the provided settings to persistent storage.
     *
     * This is a suspend function that performs an asynchronous write
     * operation through the SettingsStore. It must be called from a
     * coroutine scope (e.g., viewModelScope).
     *
     * Implementation Details:
     * - Simply delegates to SettingsStore.save()
     * - Could add validation logic here if needed
     * - Could add error handling/retry logic
     * - Could trigger side effects (e.g., sync to cloud)
     *
     * Potential Future Enhancements:
     * - Validate settings before saving
     * - Log settings changes for analytics
     * - Sync to remote storage
     * - Backup to secure storage
     *
     * Error Handling:
     * - Exceptions are propagated to caller
     * - Caller should handle IOException, etc.
     * - Could add try-catch and return Result type
     *
     * @param settings The AppSettings object to persist.
     *                 All fields will be saved, replacing
     *                 any existing values.
     *
     * @see SettingsStore.save - The underlying save implementation
     */
    suspend fun saveSettings(settings: AppSettings) = store.save(settings)

    // =========================================================================
    // ALERT EVENT FACTORY METHOD
    // =========================================================================

    /**
     * Creates a new AlertEvent with the provided location information.
     *
     * This factory method creates properly formatted AlertEvent instances
     * with a human-readable timestamp. It ensures consistency in how
     * alert events are created throughout the application.
     *
     * Design Decision - Factory Method:
     * Using a factory method instead of direct AlertEvent construction
     * allows this repository to:
     * - Enforce consistent timestamp formatting
     * - Add default values or computed fields
     * - Centralize event creation logic
     * - Easily modify event structure in one place
     *
     * Timestamp Format:
     * - Pattern: "dd/MM/yyyy HH:mm:ss"
     * - Example: "10/04/2026 14:30:25"
     * - Locale: System default (user's preferred locale)
     *
     * Why Not Use Instant or LocalDateTime:
     * - SimpleDateFormat is more widely compatible
     * - Human-readable format is easier for debugging
     * - Matches the format expected by HistoryScreen
     * - No need for timezone handling complexity
     *
     * Usage Example:
     * ```kotlin
     * val event = repository.newAlert("Lat: 37.98, Lng: 23.73")
     * // event.title = "Κίνηση εντοπίστηκε"
     * // event.details = "Ύποπτη μετακίνηση συσκευής • Lat: 37.98, Lng: 23.73"
     * // event.timestamp = "10/04/2026 14:30:25"
     * // event.location = "Lat: 37.98, Lng: 23.73"
     * ```
     *
     * Greek Language Notes:
     * - Title: "Κίνηση εντοπίστηκε" means "Movement detected"
     * - Details: "Ύποπτη μετακίνηση συσκευής" means "Suspicious device movement"
     * - Used for Greek-speaking users, could be internationalized
     *
     * @param location The location string to include in the alert.
     *                 Format: "Lat: xx.xxxxx, Lng: xx.xxxxx"
     *                 May include additional context like address.
     *
     * @return AlertEvent A new alert event instance with:
     *         - id: Current timestamp in milliseconds (unique)
     *         - title: "Κίνηση εντοπίστηκε" (Greek for "Movement detected")
     *         - details: Location information combined with description
     *         - timestamp: Human-readable date/time string
     *         - location: The provided location parameter
     *
     * @see AlertEvent - The data class representing alert events
     * @see SpotItViewModel.movAlert - Creates AlertEvents using this method
     */
    fun newAlert(location: String): AlertEvent {
        // Create a SimpleDateFormat instance with the desired pattern
        // Pattern breakdown:
        // dd - Day of month (01-31)
        // MM - Month of year (01-12)
        // yyyy - Four-digit year
        // HH - Hour of day (00-23), 24-hour format
        // mm - Minute of hour (00-59)
        // ss - Second of minute (00-59)
        val ts = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Date())

        // Build and return the AlertEvent
        // Using Greek text for the target audience
        return AlertEvent(
            // Title shown in HistoryScreen card header
            // "Κίνηση εντοπίστηκε" = "Movement detected" in Greek
            title = "Κίνηση εντοπίστηκε",

            // Detailed description with location
            // The bullet character (•) provides visual separation
            // "Ύποπτη μετακίνηση συσκευής" = "Suspicious device movement"
            details = "Ύποπτη μετακίνηση συσκευής • $location",

            // Human-readable timestamp for display
            timestamp = ts
            // Note: 'location' parameter is NOT set here
            // It's set separately by the caller if available
            // This allows for deferred location updates
        )
    }
}
