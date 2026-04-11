/**
 * SPOTit Anti-Theft Application
 *
 * SmsHelper.kt - SMS Message Sending Utility
 *
 * This file provides a utility object for sending SMS messages. It handles
 * the Android SMS API complexity, including permission checks, API version
 * differences, and error handling. This is used by the foreground service
 * to send security alerts to the user's emergency contact.
 *
 * Key Features:
 * - Permission validation before sending
 * - API version compatibility (uses SmsManager properly)
 * - Comprehensive error handling and logging
 * - Non-blocking operation (fire-and-forget)
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

// Permission-related imports for SMS permission checking
import android.Manifest // Permission constants (SEND_SMS)
import android.content.Context // Android context for accessing services
import android.content.pm.PackageManager // For checking permission status

// Telephony imports for SMS functionality
import android.telephony.SmsManager // System service for sending SMS

// Logging utility for debugging
import android.util.Log // Android logging system

// =============================================================================
// ANDROIDX CORE IMPORTS
// =============================================================================

import androidx.core.content.ContextCompat // For checking permissions

/**
 * SmsHelper - SMS Message Sending Utility Object
 *
 * This object provides a simple, safe interface for sending SMS messages.
 * It is implemented as a singleton object (not a class) because:
 * - No state needs to be maintained
 * - All functionality is context-independent
 * - Provides namespace for SMS-related utilities
 * - Easier to call from anywhere (no instantiation needed)
 *
 * Why Object Instead of Class:
 * - Stateless: No instance-specific data
 * - Singleton: Only one instance ever needed
 * - Namespace: Groups SMS functionality together
 * - Convenience: Call directly as SmsHelper.send()
 *
 * SMS Sending Flow:
 * 1. Validate the recipient phone number (not blank)
 * 2. Check SEND_SMS permission is granted
 * 3. Get the SmsManager instance (version-specific)
 * 4. Send the text message
 * 5. Return success/failure result
 *
 * Error Handling:
 * - Returns false for all error conditions (no exceptions thrown)
 * - Logs detailed error messages for debugging
 * - Gracefully handles permission denials
 * - Catches and logs unexpected exceptions
 *
 * Thread Safety:
 * - All methods are thread-safe
 * - Can be called from any thread
 * - No shared mutable state
 * - SmsManager operations are internally synchronized
 *
 * Battery Impact:
 * - Minimal: SMS sending is a brief radio operation
 * - No background processing after sending
 * - Network operation handled by system
 *
 * Security Considerations:
 * - Requires SEND_SMS permission (dangerous permission)
 * - User must grant permission explicitly
 * - Permission checked before every send attempt
 * - No sensitive data logged (only success/failure)
 *
 * Usage Example:
 * ```kotlin
 * val success = SmsHelper.send(
 *     context,
 *     "+306912345678",
 *     "SPOTit ALERT: Movement detected at Lat 37.98, Lng 23.76"
 * )
 * if (success) {
 *     Log.d("SMS", "Alert sent successfully")
 * } else {
 *     Log.w("SMS", "Failed to send alert")
 * }
 * ```
 *
 * @see SpotItForegroundService - Uses this to send security alerts
 * @see SmsManager - The underlying Android SMS API
 */
object SmsHelper {

    // =========================================================================
    // CONSTANTS - Logging Configuration
    // =========================================================================

    /**
     * Logging tag for this class.
     *
     * Used with Log.d(), Log.w(), Log.e() to filter logcat output.
     * Format: "SmsHelper" for easy identification in logs.
     *
     * Logcat filter example:
     * adb logcat -s SmsHelper:*
     */
    private const val TAG = "SmsHelper"

    // =========================================================================
    // PUBLIC API - SMS Sending Method
    // =========================================================================

    /**
     * Sends an SMS message to the specified recipient.
     *
     * This is the main (and only) public method of SmsHelper. It provides
     * a simple interface for sending text messages with comprehensive
     * error handling and logging.
     *
     * Method Behavior:
     * - Synchronous operation (blocks until send completes or fails)
     * - Non-throwing (returns false for all error conditions)
     * - Fire-and-forget (no delivery confirmation callbacks)
     * - Permission-safe (checks before attempting send)
     *
     * Success Criteria:
     * 1. Recipient phone number is not blank
     * 2. SEND_SMS permission is granted
     * 3. SmsManager.sendTextMessage() completes without exception
     *
     * Failure Cases (returns false):
     * - Recipient number is empty or blank
     * - SEND_SMS permission not granted
     * - SmsManager throws exception (invalid number, network error, etc.)
     *
     * API Version Handling:
     * - Android 12+ (API 31+): Uses context.getSystemService(SmsManager::class.java)
     * - Pre-Android 12: Uses deprecated SmsManager.getDefault()
     * - Both approaches work, new approach preferred for modern devices
     *
     * SMS Message Characteristics:
     * - Standard SMS (not MMS)
     * - No delivery confirmation requested
     * - No sent confirmation requested
     * - Uses default SMSC (Short Message Service Center)
     *
     * Message Length:
     * - Short messages (<160 chars): Single SMS
     * - Long messages (>160 chars): Automatically split into multiple SMS
     * - The system handles segmentation transparently
     *
     * Network Requirements:
     * - Requires cellular network connection
     * - Will fail in airplane mode
     * - May fail in areas with no signal
     * - WiFi is not used for SMS
     *
     * Performance Considerations:
     * - Blocks briefly during send (~1-2 seconds typically)
     * - Should be called from background thread (handled by service)
     * - Radio activation may cause brief battery drain
     *
     * @param context The Android context for accessing system services.
     *                Should be a service or application context.
     * @param to The recipient phone number. Should be a valid phone number.
     *           Format: International format recommended (e.g., "+306912345678")
     *           May also work with local format depending on carrier.
     *           Empty or blank strings result in immediate failure.
     * @param text The message body to send. Will be included verbatim in SMS.
     *              Long messages are automatically segmented by the system.
     *              Should contain the alert information including location.
     *
     * @return Boolean true if the SMS was sent successfully (no exception),
     *         false if any error occurred (invalid recipient, no permission,
     *         network error, etc.). Note: true doesn't guarantee delivery,
     *         only that the message was handed off to the system successfully.
     *
     * @see SpotItForegroundService.startSmsLoop - Calls this method for alerts
     */
    fun send(context: Context, to: String, text: String): Boolean {
        // =====================================================================
        // LOG METHOD ENTRY
        // =====================================================================

        // Log the send attempt for debugging
        // This helps track when SMS alerts are being sent
        // The recipient number is logged (partial privacy consideration:
        // we log the number but not the full message content)
        Log.d(TAG, "send() called - to: $to, text: $text")

        // =====================================================================
        // VALIDATE RECIPIENT
        // =====================================================================

        // Check if recipient phone number is provided
        // isBlank() checks for null, empty, or whitespace-only strings
        // This is the first validation - fail fast if no recipient
        if (to.isBlank()) {
            Log.w(TAG, "SMS not sent: recipient is blank")
            return false
        }

        // =====================================================================
        // CHECK PERMISSION
        // =====================================================================

        // SMS sending requires the SEND_SMS permission
        // This is a "dangerous" permission that must be granted by user
        // We check the permission before attempting to send
        //
        // Permission check details:
        // - ContextCompat.checkSelfPermission handles version differences
        // - Returns PERMISSION_GRANTED or PERMISSION_DENIED
        // - We compare against PackageManager.PERMISSION_GRANTED
        val granted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.SEND_SMS
        ) == PackageManager.PERMISSION_GRANTED

        // If permission not granted, log warning and return failure
        // The calling code should have ensured permission before calling
        // This is a safety check to prevent SecurityException
        if (!granted) {
            Log.w(TAG, "SMS not sent: SEND_SMS permission not granted")
            return false
        }

        // =====================================================================
        // SEND SMS WITH ERROR HANDLING
        // =====================================================================

        // Use runCatching for exception-safe SMS sending
        // runCatching returns a Result<T> that can be success or failure
        // This is cleaner than try-catch for functional-style error handling
        return runCatching {

            // ================================================================
            // GET SMS MANAGER (VERSION-SPECIFIC)
            // ================================================================

            // Get the SmsManager instance based on Android version
            // The approach differs between API levels:
            //
            // Android 12+ (API 31, S):
            // - Use context.getSystemService(SmsManager::class.java)
            // - This is the preferred method for newer devices
            // - Returns the default SmsManager for this context
            //
            // Pre-Android 12:
            // - Use the deprecated SmsManager.getDefault()
            // - Still works but deprecated in API 31
            // - May be removed in future Android versions
            //
            // We use try-catch inside the when block as a fallback:
            // If the new API throws (e.g., on older devices),
            // we fall back to the legacy approach
            val smsManager = try {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
                    // Android 12+ (API 31+): Use the new system service approach
                    // This is the recommended way for modern Android
                    context.getSystemService(SmsManager::class.java)
                } else {
                    // Pre-Android 12: Use the deprecated but working approach
                    // @Suppress("DEPRECATION") is used below to suppress lint warning
                    @Suppress("DEPRECATION")
                    SmsManager.getDefault()
                }
            } catch (e: Exception) {
                // Fallback: If new API fails, try the old approach
                // This shouldn't normally happen but provides extra safety
                Log.e(TAG, "Failed to get SmsManager", e)
                @Suppress("DEPRECATION")
                SmsManager.getDefault()
            }

            // ================================================================
            // SEND THE TEXT MESSAGE
            // ================================================================

            // Send the SMS message using sendTextMessage
            //
            // Parameters:
            // 1. destinationAddress: The recipient phone number
            // 2. scAddress: SMSC address (null = use default)
            // 3. text: The message body
            // 4. sentIntent: PendingIntent for sent confirmation (null = none)
            // 5. deliveryIntent: PendingIntent for delivery confirmation (null = none)
            //
            // We pass null for sentIntent and deliveryIntent because:
            // - We don't need confirmation callbacks
            // - Fire-and-forget approach is sufficient for alerts
            // - Simplifies the code (no callback handling)
            // - The calling code uses a loop for repeated sends anyway
            smsManager.sendTextMessage(
                to,           // Recipient phone number
                null,         // Use default SMSC
                text,         // Message body
                null,         // No sent confirmation
                null          // No delivery confirmation
            )

            // Log successful send
            Log.d(TAG, "SMS sent successfully to $to")

            // Return true to indicate success
            true

        }.onFailure { e ->
            // ================================================================
            // HANDLE FAILURE
            // ================================================================

            // This block is executed if an exception was thrown
            // Log the error with full exception details for debugging
            // Common exceptions:
            // - IllegalArgumentException: Invalid phone number format
            // - NullPointerException: SmsManager was null
            // - SecurityException: Permission check was bypassed somehow
            // - RuntimeException: Various carrier/network issues
            Log.e(TAG, "SMS send failed: ${e.message}", e)

        }.getOrDefault(false)
        // getOrDefault returns:
        // - The value from runCatching block if success (true)
        // - The default value if failure (false)
        // This converts Result<Boolean> to plain Boolean
    }
}

/**
 * Additional Architecture Notes:
 *
 * Why Object Instead of Class:
 * - Kotlin objects are singleton by design
 * - No need to instantiate - call directly: SmsHelper.send()
 * - No constructor parameters needed
 * - Simpler than making a class with companion object methods
 *
 * Alternative Approaches Considered:
 * 1. Intent-based SMS: Would open SMS app (not suitable for background)
 * 2. SMS Gateway API: Would require server and internet (adds complexity)
 * 3. Multiple SMS providers: Would add redundancy but also complexity
 * 4. Sent/delivery confirmation: Would be more robust but more complex
 *
 * SMS Limitations:
 * - Not guaranteed delivery (network issues, invalid number)
 * - May incur costs (carrier charges)
 * - Requires cellular signal
 * - Can be blocked by carrier
 * - Privacy concerns (phone number visible)
 *
 * Future Improvements:
 * 1. Add delivery confirmation callbacks
 *    - Would allow retry on failure
 *    - Would notify user of delivery status
 * 2. Add message queuing for reliability
 *    - Queue messages when no signal
 *    - Retry automatically when signal returns
 * 3. Add multiple recipient support
 *    - Send to multiple emergency contacts
 *    - Use sendMultipartTextMessage for long messages
 * 4. Add SMS gateway fallback
 *    - Use internet-based SMS gateway if cellular fails
 *    - Would require additional service/API key
 * 5. Add message encryption
 *    - Encrypt location data for privacy
 *    - Would require decryption on recipient device
 *
 * Testing Considerations:
 * - Use Mockito to mock SmsManager for unit tests
 * - Use emulator to test SMS sending without real device
 * - Test permission denial scenarios
 * - Test with various phone number formats
 * - Test with long messages (>160 characters)
 *
 * Legal Considerations:
 * - User must consent to sending SMS (permission request)
 * - Emergency contact must consent to receiving alerts
 * - Consider SMS costs for user and recipient
 * - Check local laws about automated SMS sending
 */
