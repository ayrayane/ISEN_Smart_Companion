package fr.isen.ahmedyahia.isensmartcompagnion.notification

import android.content.Context
import android.content.SharedPreferences

/**
 * Manager for user preferences related to notifications
 */
class PreferencesManager(context: Context) {
    
    private val sharedPreferences: SharedPreferences = context.getSharedPreferences(
        PREFERENCES_NAME, Context.MODE_PRIVATE
    )
    
    /**
     * Save notification preference for an event
     */
    fun saveEventNotificationPreference(eventId: Int, isEnabled: Boolean) {
        sharedPreferences.edit().putBoolean("${NOTIFICATION_PREFIX}$eventId", isEnabled).apply()
    }
    
    /**
     * Check if notifications are enabled for an event
     */
    fun isEventNotificationEnabled(eventId: Int): Boolean {
        return sharedPreferences.getBoolean("${NOTIFICATION_PREFIX}$eventId", false)
    }
    
    companion object {
        private const val PREFERENCES_NAME = "event_notification_preferences"
        private const val NOTIFICATION_PREFIX = "event_notification_"
    }
}
