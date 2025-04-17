package fr.isen.ahmedyahia.isensmartcompagnion.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import fr.isen.ahmedyahia.isensmartcompagnion.Event
import fr.isen.ahmedyahia.isensmartcompagnion.EventDetailActivity
import fr.isen.ahmedyahia.isensmartcompagnion.R

/**
 * Helper class to manage notifications for events
 */
class NotificationHelper(private val context: Context) {
    
    companion object {
        private const val CHANNEL_ID = "event_notifications"
        private const val NOTIFICATION_GROUP = "event_group"
        private const val TAG = "NotificationHelper"
    }
    
    init {
        createNotificationChannel()
    }
    
    /**
     * Creates the notification channel for Android O and above
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Rappels d'événements"
            val descriptionText = "Notifications pour les rappels d'événements"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                enableVibration(true)
                enableLights(true)
                setShowBadge(true)
            }
            
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    /**
     * Shows a notification for an event
     */
    fun showEventNotification(event: Event) {
        Log.d(TAG, "Attempting to show notification for event: ${event.title}")
        
        // Create an intent to open the event details when notification is tapped
        val intent = Intent(context, EventDetailActivity::class.java).apply {
            putExtra("event", event)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context, 
            event.id, 
            intent, 
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        
        // Get default notification sound
        val defaultSoundUri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        
        // Build the notification
        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Using existing icon
            .setContentTitle("Rappel d'événement")
            .setContentText("${event.title} - ${event.date}")
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText("${event.title}\n${event.description}\nLieu: ${event.location}\nDate: ${event.date}"))
            .setPriority(NotificationCompat.PRIORITY_MAX) // Maximum priority
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .setGroup(NOTIFICATION_GROUP)
            .setSound(defaultSoundUri)
            .setVibrate(longArrayOf(1000, 1000, 1000, 1000)) // Vibration pattern
            .setDefaults(NotificationCompat.DEFAULT_LIGHTS) // Enable lights
        
        // Get the notification manager
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        // Show the notification
        try {
            Log.d(TAG, "Sending notification for event: ${event.title}")
            notificationManager.notify(event.id, builder.build())
        } catch (e: Exception) {
            // Handle any exceptions
            Log.e(TAG, "Error showing notification: ${e.message}", e)
            e.printStackTrace()
        }
    }
}
