package com.google.sample.cast.refplayer.utils

import android.os.Bundle

data class MediaItem (
    var title: String,
    var subTitle: String,
    var studio: String,
    var url: String,
    var contentType: String,
    var duration: Int = 0,
    val imageList: MutableList<String> = mutableListOf()
) {
    fun addImage(url: String) {
        imageList.add(url)
    }

    fun addImage(url: String, index: Int) {
        if (index < imageList.size) {
            imageList[index] = url
        }
    }

    fun getImage(index: Int): String? {
        return if (index < imageList.size) {
            imageList[index]
        } else null
    }

    fun hasImage(): Boolean {
        return imageList.isNotEmpty()
    }

    fun getImages(): List<String> {
        return imageList
    }

    fun toBundle(): Bundle {
        val wrapper = Bundle()
        wrapper.putString(KEY_TITLE, title)
        wrapper.putString(KEY_SUBTITLE, subTitle)
        wrapper.putString(KEY_URL, url)
        wrapper.putString(KEY_STUDIO, studio)
        wrapper.putStringArrayList(KEY_IMAGES, ArrayList(imageList))
        wrapper.putString(KEY_CONTENT_TYPE, "video/mp4")
        return wrapper
    }

    companion object {
        const val KEY_TITLE = "title"
        const val KEY_SUBTITLE = "subtitle"
        const val KEY_STUDIO = "studio"
        const val KEY_URL = "movie-urls"
        const val KEY_IMAGES = "images"
        const val KEY_CONTENT_TYPE = "content-type"

        fun fromBundle(wrapper: Bundle?): MediaItem? {
            if (null == wrapper) {
                return null
            }
            wrapper.apply {
                getStringArrayList(KEY_IMAGES)?.let { list ->
                    return MediaItem(
                        getString(KEY_TITLE) ?: "",
                        getString(KEY_SUBTITLE) ?: "",
                        getString(KEY_STUDIO) ?: "",
                        getString(KEY_URL) ?: "",
                        getString(KEY_CONTENT_TYPE) ?: "",
                        imageList = list.toMutableList()
                    )
                }
            }
            return null
        }
    }
}