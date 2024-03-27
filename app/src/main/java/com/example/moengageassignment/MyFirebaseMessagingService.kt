package com.example.moengageassignment

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import java.nio.charset.StandardCharsets

class MyFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        // Notification channel ID
        private const val CHANNEL_ID = "channel_id"

        // Notification ID
        private const val notificationId = 101
    }

    /**
     * Called when a message is received.
     * Handles both data messages and notification messages.
     */
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // Check if message contains data payload
        if (remoteMessage.data.isNotEmpty()) {
            // If so, send notification with the message data
            sendNotification(remoteMessage.data["message"])
        }

        // Check if message contains notification payload
        if (remoteMessage.notification != null) {
            // If so, send notification with the notification body
            sendNotification(remoteMessage.notification?.body)
        }
    }

    /**
     * Sends a notification with the given message body.
     * @param messageBody The body of the notification message.
     */
    @SuppressLint("MissingPermission")
    private fun sendNotification(messageBody: String?) {
        try {
            // Check for null or empty message body
            if (messageBody.isNullOrBlank()) {
                throw IllegalArgumentException("Message body is null or empty")
            }

            // Decode message body if it contains Unicode characters
            val decodedMessage = messageBody.toByteArray(StandardCharsets.ISO_8859_1).toString(StandardCharsets.UTF_8)

            // Create intent to launch MainActivity when notification is clicked
            val intent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            val pendingIntent: PendingIntent = PendingIntent.getActivity(this, 0, intent,
                PendingIntent.FLAG_IMMUTABLE)

            // Build notification with title, message body, and pending intent
            val builder = NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("My Notification")
                .setContentText(decodedMessage)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)

            // Display the notification
            with(NotificationManagerCompat.from(this)) {
                notify(notificationId, builder.build())
            }
        } catch (e: Exception) {
            // Handle input exceptions
            e.printStackTrace()
        }
    }
}
