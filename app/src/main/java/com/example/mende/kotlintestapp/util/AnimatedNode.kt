package com.example.mende.kotlintestapp.util

import android.view.MotionEvent
import com.google.ar.sceneform.FrameTime
import com.google.ar.sceneform.HitTestResult
import com.google.ar.sceneform.Node

class AnimatedNode : Node(), Node.OnTapListener {
    override fun onTap(p0: HitTestResult?, p1: MotionEvent?) {

    }

    var isFullSizeAnimationDone : Boolean = false
    var isRemoveAnimationDone: Boolean = false
    var isRemoving: Boolean = false
    var isSelected: Boolean = true
    var isInitialized: Boolean = false


    override fun onUpdate(frameTime: FrameTime?) {
        super.onUpdate(frameTime)



    }

}