package com.example.mende.kotlintestapp.util

import android.content.Context
import android.view.Gravity
import android.widget.Toast
import com.google.ar.sceneform.rendering.Light
import android.animation.ValueAnimator
import android.animation.ObjectAnimator

/**
 *
 * Good little helper functions for model
 * so far: used for description bubble of food
 *
 * possible: can have animations/light usage
 *
 *
 * https://www.raywenderlich.com/5485-arcore-sceneform-sdk-getting-started
 *
 */

fun Any?.toast(ctx: Context): Void? {
  val msg = if (this is Throwable) {
    this.printStackTrace()
    this.localizedMessage
  } else {
    this.toString()
  }

  Toast.makeText(ctx, msg, Toast.LENGTH_LONG)
      .apply { setGravity(Gravity.CENTER, 0, 0) }.show()
  return null
}

fun Light.blink(times: Int = 1, from: Float = 0F, to: Float = 100000F, inMs: Long = 100) {
  val intensityAnimator = ObjectAnimator.ofFloat(this, "intensity", from, to)
  intensityAnimator.duration = inMs
  intensityAnimator.repeatCount = times * 2 - 1
  intensityAnimator.repeatMode = ValueAnimator.REVERSE
  intensityAnimator.start()
}