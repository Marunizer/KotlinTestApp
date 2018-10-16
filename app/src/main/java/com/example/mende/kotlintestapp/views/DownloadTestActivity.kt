package com.example.mende.kotlintestapp.views

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.mende.kotlintestapp.R
import kotlinx.android.synthetic.main.testing_layout.*

//TODO: Purpose is undecided, may remove in future, currently just here to be a sandbox activity
class DownloadTestActivity : AppCompatActivity() {

    private val TAG = HomeActivity::class.java.simpleName
    private var EMPTY = ""
    private lateinit var testList : ArrayList<Any>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.testing_layout)

        onInit()

    }

    private fun onInit() {

       // testList =


        // Instantiate the RequestQueue.
        val queue = Volley.newRequestQueue(this)
        val url = "https://api.noni.menu/v1/restaurants?lat=28.469953&lng=-81.341406&radius=5000&limit=1"


        val jsonObjectRequest = JsonObjectRequest(Request.Method.GET, url, null,
                Response.Listener { response ->
                    Log.d(TAG, "Response: %s".format(response.toString()))
                   // test_text.text = "Response: %s".format(response.toString())
                },
                Response.ErrorListener { error ->
                    Log.e(TAG, "Response: %s ERROR")
                }
        )

        //queue.add(jsonObjectRequest)

//        Log.d(TAG, testList.toString())






    }

}