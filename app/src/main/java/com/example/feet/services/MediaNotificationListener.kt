package com.example.feet.services

import android.app.Notification
import android.content.Intent
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager

class MediaNotificationListener : NotificationListenerService() {

    companion object {
        const val ACTION_MEDIA_UPDATE = "com.example.feet.MEDIA_UPDATE"
        const val EXTRA_TRACK = "track"
        const val EXTRA_ARTIST = "artist"
        const val ACTION_MEDIA_CLEAR = "com.example.feet.MEDIA_CLEAR"
        private const val TAG = "MediaNotificationListener"

        // Common music app package names
        private val MUSIC_PACKAGES = setOf(
            "com.spotify.music",
            "com.google.android.youtube",
            "com.google.android.apps.youtube.music",
            "com.apple.android.music",
            "com.amazon.mp3",
            "com.pandora.android",
            "deezer.android.app",
            "com.aspiro.tidal"
        )
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        if (sbn == null) return

        val packageName = sbn.packageName

        // Only process notifications from known music apps
        if (!MUSIC_PACKAGES.contains(packageName)) return

        val extras = sbn.notification.extras

        // Log for debugging
        Log.d(TAG, "Notification from: $packageName")

        // Try multiple ways to extract track and artist info
        val track = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString()
            ?: extras.getString("android.title")

        val artist = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()
            ?: extras.getString("android.text")
            ?: extras.getCharSequence(Notification.EXTRA_SUB_TEXT)?.toString()
            ?: extras.getString("android.subText")

        Log.d(TAG, "Track: $track, Artist: $artist")

        // Send update if we have at least a track name
        if (!track.isNullOrBlank()) {
            sendMediaUpdate(
                track = track,
                artist = artist ?: "Unknown Artist"
            )
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)

        val packageName = sbn?.packageName

        // Clear the display when music notification is removed
        if (MUSIC_PACKAGES.contains(packageName)) {
            Log.d(TAG, "Music notification removed from: $packageName")
            sendMediaClear()
        }
    }

    private fun sendMediaUpdate(track: String, artist: String) {
        val intent = Intent(ACTION_MEDIA_UPDATE).apply {
            putExtra(EXTRA_TRACK, track)
            putExtra(EXTRA_ARTIST, artist)
        }
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
        Log.d(TAG, "Sent media update: $track - $artist")
    }

    private fun sendMediaClear() {
        val intent = Intent(ACTION_MEDIA_CLEAR)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
        Log.d(TAG, "Sent media clear")
    }
}