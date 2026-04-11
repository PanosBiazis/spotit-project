/**
 * SPOTit Anti-Theft Application
 *
 * LocationHelper.kt - GPS Location Retrieval Utility
 *
 * This file provides a utility class for retrieving the device's current GPS
 * location. It wraps Google Play Services' FusedLocationProviderClient to
 * provide a simple, coroutine-based API for getting location coordinates.
 *
 * Key Features:
 * - Uses FusedLocationProviderClient for best accuracy and battery efficiency
 * - Coroutine-based async API with suspend functions
 * - Handles missing permissions gracefully
 * - Returns formatted location string ready for display/SMS
 *
 * @author Panos Biazis
 * @date 10/4/2026
 * @version 2.0
 *
 * Copyright (c) 2026 Panos Biazis. All rights reserved.
 */

// Package declaration - defines the utility layer package
// The util package contains helper classes and utilities
package com.spotit.util

// =============================================================================
// ANDROID FRAMEWORK IMPORTS
// =============================================================================

// Permission-related imports
import android.Manifest // Permission constants
import android.content.Context // Android context for accessing services
import android.content.pm.PackageManager // For checking permission status

// Annotation for suppressing lint warnings
import android.annotation.SuppressLint // Suppresses permission lint warnings

// =============================================================================
// ANDROIDX CORE IMPORTS
// =============================================================================

import androidx.core.content.ContextCompat // For checking permissions

// =============================================================================
// GOOGLE PLAY SERVICES IMPORTS
// =============================================================================

import com.google.android.gms.location.LocationServices // Entry point for location services

// =============================================================================
// KOTLIN COROUTINES IMPORTS
// =============================================================================

import kotlin.coroutines.resume // Resumes a suspended coroutine
import kotlinx.coroutines.suspendCancellableCoroutine // Bridge between callbacks and coroutines

/**
 * LocationHelper - GPS Location Retrieval Utility
 *
 * This class provides a simple interface for getting the device's current
 * location. It uses Google Play Services' FusedLocationProviderClient, which
 * is the recommended way to access location on Android.
 *
 * Why FusedLocationProviderClient:
 * - Combines GPS, network, and sensor data for best accuracy
 * - Manages battery usage intelligently
 * - Handles location availability changes
 * - Provides caching for faster subsequent requests
 * - Handles multiple location providers automatically
 *
 * Location Accuracy:
 * The accuracy depends on the permissions granted:
 * - FINE_LOCATION: GPS-level accuracy (~10 meters)
 * - COARSE_LOCATION: Network-level accuracy (~100 meters)
 *
 * This class requests the last known location, which:
 * - Returns immediately (non-blocking)
 * - May be null if no recent location fix
 * - May be stale (cached from earlier)
 * - Is sufficient for anti-theft alerts (approximate location is useful)
 *
 * Coroutine Integration:
 * The main method (lastLocationText()) is a suspend function that uses
 * suspendCancellableCoroutine to convert the callback-based FusedLocationClient
 * API into a coroutine-friendly API.
 *
 * Thread Safety:
 * - Can be called from any thread
 * - Uses suspendCancellableCoroutine for thread-safe suspension
 * - Callbacks are properly handled on the calling coroutine context
 *
 * Error Handling:
 * - Returns "No GPS permission" if permissions not granted
 * - Returns "Location not available" if location is null
 * - Returns "GPS Error" on any unexpected error
 *
 * Usage Example:
 * ```kotlin
 * val locationHelper = LocationHelper(context)
 * val locationText = locationHelper.lastLocationText()
 * // locationText = "Lat 37.98765, Lng 23.76543" or error message
 * ```
 *
 * @property context The Android context used to access location services.
 * Should be a service or application context for long-lived usage.
 *
 * @see SpotItForegroundService - Uses this for getting alert location
 * @see SpotItViewModel - Uses FusedLocationProviderClient for continuous updates
 */
class LocationHelper(private val context: Context) {

    // =========================================================================
    // PUBLIC API - Location Retrieval Method
    // =========================================================================

    /**
     * Retrieves the last known location as a formatted string.
     *
     * This is a suspend function that asynchronously retrieves the device's
     * last known GPS coordinates and returns them as a human-readable string.
     * The function handles all permission checks and error cases internally.
     *
     * Return Value Format:
     * - Success: "Lat xx.xxxxx, Lng xx.xxxxx" (5 decimal places = ~1 meter precision)
     * - No permission: "No GPS permission"
     * - No location: "Location not available"
     * - Error: "GPS Error"
     *
     * Location Precision:
     * The 5 decimal places in the output provide approximately 1.1 meters
     * of precision at the equator, which is suitable for:
     * - Identifying the general location of a stolen device
     * - Providing location in SMS alerts
     * - Displaying in the app UI
     *
     * The precision is more than enough for anti-theft purposes while not
     * being excessive (more digits would imply false precision).
     *
     * Implementation Details:
     * 1. Check if location permissions are granted
     * 2. Get FusedLocationProviderClient instance
     * 3. Request last known location via lastLocation
     * 4. Convert the async callback to coroutine suspension
     * 5. Format the result or handle errors
     *
     * SuspendCancellableCoroutine Usage:
     * This function uses suspendCancellableCoroutine to bridge between:
     * - The callback-based FusedLocationProviderClient API
     * - Kotlin coroutines' structured concurrency
     *
     * The coroutine is:
     * - Suspended until the location callback completes
     * - Cancellable (if the calling coroutine is cancelled)
     * - Thread-safe (can be called from any dispatcher)
     *
     * Permission Requirements:
     * Requires either ACCESS_FINE_LOCATION or ACCESS_COARSE_LOCATION.
     * Without these permissions, the function returns immediately with
     * an error message rather than throwing an exception.
     *
     * Battery Impact:
     * This method uses getLastLocation() which:
     * - Does NOT trigger a new GPS fix
     * - Returns a cached location if available
     * - Has minimal battery impact
     * - Is appropriate for frequent calls
     *
     * For continuous location updates (used elsewhere in the app),
     * see SpotItViewModel.startContinuousLocationUpdates() which
     * uses requestLocationUpdates() instead.
     *
     * @return String The formatted location or an error message.
     * Never returns null.
     *
     * @see SpotItForegroundService.onMotionDetected - Calls this method for alerts
     */
    @SuppressLint("MissingPermission")
    suspend fun lastLocationText(): String {
        // =====================================================================
        // PERMISSION CHECK
        // =====================================================================

        // Check if the app has been granted either fine or coarse location permission
        // FINE_LOCATION provides GPS-level accuracy (~10 meters)
        // COARSE_LOCATION provides network-level accuracy (~100 meters)
        // Either is acceptable for anti-theft purposes
        val fine = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarse = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        // If neither permission is granted, return an informative message
        // This shouldn't happen in normal flow as permissions are checked
        // before calling this method, but we handle it gracefully
        if (!fine && !coarse) {
            return "No GPS permission"
        }

        // =====================================================================
        // GET LOCATION CLIENT
        // =====================================================================

        // Get the FusedLocationProviderClient instance
        // This is Google's recommended location API that combines multiple
        // location sources (GPS, network, sensors) for optimal results
        val client = LocationServices.getFusedLocationProviderClient(context)

        // =====================================================================
        // RETRIEVE LOCATION USING SUSPEND_COROUTINE
        // =====================================================================

        // Use suspendCancellableCoroutine to convert callback-based API to coroutine
        // This suspends the coroutine until the location result is available
        // The 'cont' parameter is a CancellableContinuation that we resume
        // when we have a result
        return suspendCancellableCoroutine { cont ->

            // Request the last known location
            // lastLocation is a Task that completes asynchronously
            // It returns the most recent location fix, or null if unavailable
            client.lastLocation
                // Add success listener - called when location is retrieved
                .addOnSuccessListener { loc ->
                    // Resume the coroutine with the formatted result
                    // cont.resume() will unsuspend the calling coroutine
                    cont.resume(
                        if (loc == null) {
                            // Location can be null if:
                            // - No GPS fix has been obtained yet
                            // - GPS is disabled
                            // - Location services are off
                            "Location not available"
                        } else {
                            // Format the location with 5 decimal places
                            // This provides ~1 meter precision
                            // Format: "Lat 37.98765, Lng 23.76543"
                            "Lat %.5f, Lng %.5f".format(loc.latitude, loc.longitude)
                        }
                    )
                }
                // Add failure listener - called if location request fails
                .addOnFailureListener {
                    // Resume with error message
                    // This handles unexpected errors like:
                    // - Google Play Services not available
                    // - Location services disabled
                    // - Internal errors
                    cont.resume("GPS Error")
                }
        }
    }
}

/**
 * Additional Architecture Notes:
 *
 * FusedLocationProviderClient vs LocationManager:
 * - FusedLocationProviderClient is the modern, recommended API
 * - LocationManager is the older, deprecated API
 * - FusedLocationProviderClient handles multiple providers automatically
 * - Better battery efficiency through intelligent provider selection
 * - More accurate results by combining multiple sources
 *
 * Alternative Approaches Considered:
 * 1. LocationManager: Older API, requires manual provider selection
 * 2. Geocoder: Adds reverse geocoding (address from coordinates)
 * - Would be nice but adds latency and network dependency
 * - Current implementation keeps it simple with coordinates only
 * 3. Continuous location updates: Used in ViewModel, not here
 * - This class provides one-shot location retrieval
 * - ViewModel handles continuous updates separately
 *
 * Cancellation Handling:
 * - The suspendCancellableCoroutine supports cancellation
 * - If the calling coroutine is cancelled, the location request
 *   may continue but the result won't be processed
 * - For production, could add cancellation listener to abort request
 *
 * Future Improvements:
 * 1. Add reverse geocoding for address display
 * 2. Add accuracy information to the output
 * 3. Add timestamp of when location was obtained
 * 4. Cache location with timestamp to avoid stale data
 * 5. Add timeout for location retrieval
 * 6. Implement fallback to older LocationManager if Play Services unavailable
 *
 * Testing Considerations:
 * - Can be mocked by creating a test implementation
 * - Can use dependency injection to provide fake locations
 * - Consider creating an interface for easier testing
 */
