package com.example.mende.kotlintestapp.util

import android.content.Context
import android.graphics.Bitmap
import android.support.v4.util.LruCache
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.*


/**
 * reference: https://developer.android.com/training/volley/requestqueue
 *
 */

//// Get a RequestQueue
//val queue = MySingleton.getInstance(this.applicationContext).requestQueue
//
//// Add a request (in this example, called stringRequest) to your RequestQueue.
//MySingleton.getInstance(this).addToRequestQueue(stringRequest)


class VolleySingleton constructor(context: Context) { //cache: DiskBasedCache,network: BasicNetwork) {
    companion object {
        @Volatile
        private var INSTANCE: VolleySingleton? = null
        fun getInstance(context: Context) =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: VolleySingleton(context).also {
                    INSTANCE = it
                }
            }
    }

//    val imageLoader: ImageLoader by lazy {
//        ImageLoader(requestQueue,
//                object : ImageLoader.ImageCache {
//                    private val cache = LruCache<String, Bitmap>(20)
//                    override fun getBitmap(url: String): Bitmap? {
//                        return cache.get(url)
//                    }
//                    override fun putBitmap(url: String, bitmap: Bitmap) {
//                        cache.put(url, bitmap)
//                    }
//                })
//    }


    val requestQueue: RequestQueue by lazy {
        // applicationContext is key, it keeps you from leaking the
        // Activity or BroadcastReceiver if someone passes one in.
        Volley.newRequestQueue(context.applicationContext)
    }

    fun <T> addToRequestQueue(req: Request<T>) {
        requestQueue.add(req)
    }

    fun <T> cancelRequest(tag: Any)
    {
        if (requestQueue != null) {
            requestQueue.cancelAll(tag)
        }
    }
}