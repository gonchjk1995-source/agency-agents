package com.neon.player.data

import android.content.ContentUris
import android.content.Context
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Reads the audio files already on the device from MediaStore. */
class MusicRepository(private val context: Context) {

    suspend fun loadTracks(): List<Track> = withContext(Dispatchers.IO) {
        val collection = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.DURATION,
        )
        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0 AND ${MediaStore.Audio.Media.DURATION} >= ?"
        val args = arrayOf(MIN_DURATION_MS.toString())
        val order = "${MediaStore.Audio.Media.TITLE} COLLATE NOCASE ASC"

        val result = mutableListOf<Track>()
        context.contentResolver.query(collection, projection, selection, args, order)?.use { c ->
            val idCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val albumCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val albumIdCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
            val durationCol = c.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            while (c.moveToNext()) {
                val id = c.getLong(idCol)
                result += Track(
                    id = id,
                    title = c.getString(titleCol).orEmpty().ifBlank { "Без названия" },
                    artist = c.getString(artistCol).cleanUnknown("Неизвестный исполнитель"),
                    album = c.getString(albumCol).cleanUnknown("Без альбома"),
                    albumId = c.getLong(albumIdCol),
                    durationMs = c.getLong(durationCol),
                    uri = ContentUris.withAppendedId(collection, id),
                )
            }
        }
        result
    }

    private fun String?.cleanUnknown(fallback: String): String =
        if (this.isNullOrBlank() || this == "<unknown>") fallback else this

    private companion object {
        // Skips ringtones, notification blips and voice-memo fragments.
        const val MIN_DURATION_MS = 20_000L
    }
}
