package com.google.sample.cast.refplayer.browser

import android.content.Context
import android.util.Log
import androidx.loader.content.AsyncTaskLoader
import com.google.sample.cast.refplayer.utils.MediaItem

class VideoItemLoader(context: Context, private val url: String):
    AsyncTaskLoader<MutableList<MediaItem>>(context) {

    override fun loadInBackground(): MutableList<MediaItem>? =
        try {
            VideoProvider.buildMedia(url)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to fetch media data", e)
            null
        }

    override fun onStartLoading() {
        super.onStartLoading()
        forceLoad()
    }

    /**
     * Handles a request to stop the Loader.
     */
    override fun onStopLoading() {
        // Attempt to cancel the current load task if possible.
        cancelLoad()
    }

    companion object {
        private const val TAG = "VideoItemLoader"
    }
}