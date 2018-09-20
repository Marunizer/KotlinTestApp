package com.example.mende.kotlintestapp.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log

/**
 * Created by Marunizer
 *
 * Purpose of this Service is to remove all data related to models and restaurants when app is Destroyed
 * reference: https://stackoverflow.com/questions/19568315/how-to-handle-code-when-app-is-killed-by-swiping-in-android
 */

class OnClearFromRecentService : Service() {
    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        Log.d("ClearFromRecentService", "Service Started")
        return Service.START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("ClearFromRecentService", "Service Destroyed")
    }

    override fun onTaskRemoved(rootIntent: Intent) {


        //TODO: Delete future files here
        Log.e("ClearFromRecentService", "END")
        //Code here
        stopSelf()
    }

    //function I use to use to delete the model files, I call it in the function above
//    fun deleteFiles() {
//        Log.i("OnClearRecentService", "Deleting all files within model folder")
//        val file = File(filesDir.toString() + "/model")
//        if (file.exists()) {
//            try {
//                FileUtils.deleteDirectory(file)
//            } catch (e: IOException) {
//                e.printStackTrace()
//            }
//
//        }
//    }
}