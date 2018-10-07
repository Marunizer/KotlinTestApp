package com.example.mende.kotlintestapp.views

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout
import com.example.mende.kotlintestapp.R
import kotlinx.android.synthetic.main.model_description_view.view.*

/**
 * Marunizer
 *
 * https://www.raywenderlich.com/5485-arcore-sceneform-sdk-getting-started
 *
 */

class DescriptionBubble(context: Context, attrs: AttributeSet? = null, defStyle: Int = -1)
  : FrameLayout(context, attrs, defStyle) {

  init {
    inflate(context, R.layout.model_description_view, this)

    xml_btn.setOnClickListener {
      it.isEnabled = false
      onStartTapped?.invoke()
    }
  }

  var onStartTapped: (() -> Unit)? = null

}
