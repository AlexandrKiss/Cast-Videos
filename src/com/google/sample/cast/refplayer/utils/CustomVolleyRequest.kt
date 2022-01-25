package com.google.sample.cast.refplayer.utils

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import androidx.collection.LruCache
import com.android.volley.Cache
import com.android.volley.Network
import com.android.volley.RequestQueue
import com.android.volley.toolbox.BasicNetwork
import com.android.volley.toolbox.DiskBasedCache
import com.android.volley.toolbox.HurlStack
import com.android.volley.toolbox.ImageLoader
import com.android.volley.toolbox.ImageLoader.ImageCache

class CustomVolleyRequest(mContext: Context) {

    private var requestQueue: RequestQueue? = null
    var imageLoader: ImageLoader? = null

    init {
        context = mContext
        requestQueue = getRequestQueue()

        imageLoader = ImageLoader(requestQueue, object : ImageCache {
            private val cache = LruCache<String, Bitmap>(20)

            override fun getBitmap(url: String): Bitmap? {
                return cache[url]
            }

            override fun putBitmap(url: String, bitmap: Bitmap) {
                cache.put(url, bitmap)
            }
        })
    }

    private fun getRequestQueue(): RequestQueue? {
        if (requestQueue == null) {
            val cache: Cache =
                DiskBasedCache(CustomVolleyRequest.context!!.cacheDir, 10 * 1024 * 1024)
            val network: Network = BasicNetwork(HurlStack())
            requestQueue = RequestQueue(cache, network)
            requestQueue!!.start()
        }
        return requestQueue
    }

//    fun getImageLoader(): ImageLoader? {
//        return imageLoader
//    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        private var customVolleyRequest: CustomVolleyRequest? = null
        @SuppressLint("StaticFieldLeak")
        private var context: Context? = null

        @Synchronized
        fun getInstance(context: Context?): CustomVolleyRequest? {
            if (customVolleyRequest == null) {
                customVolleyRequest = CustomVolleyRequest(
                    context!!
                )
            }
            return customVolleyRequest
        }
    }
}