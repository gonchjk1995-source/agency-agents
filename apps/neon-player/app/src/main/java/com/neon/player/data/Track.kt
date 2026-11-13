package com.neon.player.data

import android.content.ContentUris
import android.net.Uri
import androidx.core.net.toUri

data class Track(
    val id: Long,
    val title: String,
    val artist: String,
    val album: String,
    val albumId: Long,
    val durationMs: Long,
    val uri: Uri,
    val artUri: Uri = ContentUris.withAppendedId(ALBUM_ART, albumId),
) {
    fun matches(query: String): Boolean =
        query.isBlank() ||
            title.contains(query, ignoreCase = true) ||
            artist.contains(query, ignoreCase = true) ||
            album.contains(query, ignoreCase = true)

    companion object {
        private val ALBUM_ART: Uri = "content://media/external/audio/albumart".toUri()
    }
}
