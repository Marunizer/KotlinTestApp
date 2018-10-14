package com.example.mende.kotlintestapp.views

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.example.mende.kotlintestapp.R

//TODO: Purpose is undecided, may remove in future, currently just here to be a sandbox activity
class DownloadTestActivity : AppCompatActivity() {

    private val TAG = HomeActivity::class.java.simpleName
    private var EMPTY = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        onInit()

    }

    private fun onInit() {


    }

}