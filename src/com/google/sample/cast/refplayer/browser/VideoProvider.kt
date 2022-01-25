package com.google.sample.cast.refplayer.browser

import android.util.Log
import com.google.sample.cast.refplayer.utils.MediaItem
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.*
import java.net.URL

open class VideoProvider {

    fun parseUrl(urlString: String?): JSONObject? {
        var inputStream: InputStream? = null
        return try {
            val url = URL(urlString)
            val urlConnection = url.openConnection()
            inputStream = BufferedInputStream(urlConnection.getInputStream())
            val reader = BufferedReader(InputStreamReader(
                    urlConnection.getInputStream(), "iso-8859-1"), 1024)
            val sb = StringBuilder()
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                sb.append(line)
            }
            val json = sb.toString()
            JSONObject(json)
        } catch (e: Exception) {
            Log.d(TAG, "Failed to parse the json for media list", e)
            null
        } finally {
            if (null != inputStream) {
                try {
                    inputStream.close()
                } catch (e: IOException) {
                    // ignore
                }
            }
        }
    }

    companion object {
        private const val TAG = "VideoProvider"
        private const val TAG_VIDEOS = "videos"
        private const val TAG_HLS = "hls"
        private const val TAG_DASH = "dash"
        private const val TAG_MP4 = "mp4"
        private const val TAG_IMAGES = "images"
        private const val TAG_VIDEO_TYPE = "type"
        private const val TAG_VIDEO_URL = "url"
        private const val TAG_VIDEO_MIME = "mime"

        private const val TAG_CATEGORIES = "categories"
        private const val TAG_NAME = "name"
        private const val TAG_STUDIO = "studio"
        private const val TAG_SOURCES = "sources"
        private const val TAG_SUBTITLE = "subtitle"
        private const val TAG_DURATION = "duration"
        private const val TAG_THUMB = "image-480x270" // "thumb";

        private const val TAG_IMG_780_1200 = "image-780x1200"
        private const val TAG_TITLE = "title"

        const val KEY_DESCRIPTION = "description"

        private const val TARGET_FORMAT = TAG_MP4

        private var mediaList: MutableList<MediaItem>? = null

        fun buildMedia(url: String?): MutableList<MediaItem>? {
            if (null != mediaList) {
                return mediaList
            }
            val urlPrefixMap: MutableMap<String, String> = HashMap()
            mediaList = mutableListOf()
            val jsonObj = VideoProvider().parseUrl(url)
            val categories = jsonObj!!.getJSONArray(TAG_CATEGORIES)
            for (i in 0 until categories.length()) {
                val category = categories.getJSONObject(i)
                urlPrefixMap[TAG_HLS] = category.getString(TAG_HLS)
                urlPrefixMap[TAG_DASH] = category.getString(TAG_DASH)
                urlPrefixMap[TAG_MP4] = category.getString(TAG_MP4)
                urlPrefixMap[TAG_IMAGES] =
                    category.getString(TAG_IMAGES)
                category.getString(TAG_NAME)
                val videos = category.getJSONArray(TAG_VIDEOS)
                for (j in 0 until videos.length()) {
                    var videoUrl: String? = null
                    var mimeType: String? = null
                    val video = videos.getJSONObject(j)
                    val subTitle = video.getString(TAG_SUBTITLE)
                    val videoSpecs = video.getJSONArray(TAG_SOURCES)
                    if (videoSpecs.length() == 0) {
                        continue
                    }
                    for (k in 0 until videoSpecs.length()) {
                        val videoSpec = videoSpecs.getJSONObject(k)
                        if (TARGET_FORMAT == videoSpec.getString(TAG_VIDEO_TYPE)) {
                            videoUrl = urlPrefixMap[TARGET_FORMAT].toString() + videoSpec
                                .getString(TAG_VIDEO_URL)
                            mimeType = videoSpec.getString(TAG_VIDEO_MIME)
                        }
                    }
                    if (videoUrl == null) continue

                    val imageUrl = urlPrefixMap[TAG_IMAGES].toString() + video.getString(TAG_THUMB)
                    val bigImageUrl = urlPrefixMap[TAG_IMAGES].toString() +
                            video.getString(TAG_IMG_780_1200)
                    val title = video.getString(TAG_TITLE)
                    val studio = video.getString(TAG_STUDIO)
                    val duration = video.getInt(TAG_DURATION)
                    mediaList?.add(buildMediaInfo(title, studio, subTitle, duration, videoUrl, mimeType,
                        imageUrl, bigImageUrl))
                }
            }
            return mediaList
        }

        private fun buildMediaInfo(title: String, studio: String, subTitle: String, duration: Int,
                   url: String, mimeType: String?, imgUrl: String, bigImageUrl: String): MediaItem {
            val media = MediaItem()
            media.url = url
            media.title = title
            media.subTitle = subTitle
            media.studio = studio
            media.addImage(imgUrl)
            media.addImage(bigImageUrl)
            media.contentType = mimeType
            media.duration = duration
            return media
        }
    }
}