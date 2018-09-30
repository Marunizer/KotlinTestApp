package com.example.mende.kotlintestapp.views

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.DefaultItemAnimator
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import com.example.mende.kotlintestapp.R
import com.example.mende.kotlintestapp.adapters.RestaurantCardAdapter
import com.example.mende.kotlintestapp.interfaces.LoadMore
import com.example.mende.kotlintestapp.objects.RestaurantCard
import com.example.mende.kotlintestapp.util.SharedPref
import com.google.ar.core.ArCoreApk
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.location_bar.*
import java.util.*
import kotlin.collections.ArrayList

/**
 * Created by Marunizer
 * Home screen
 * purpose: Displays restaurant selection near user
 *
 * -------------------------------------------------------------------------------------------------
 * NOTES:
 *
 * if we reset entire array from maybe new location data further down the road,
 *  arraylist.recycle() may be useful to clear our unnecessary allotted space for data
 *
 *
 * -------------------------------------------------------------------------------------------------
 * CONCERNS:
 * -
 *
 * -------------------------------------------------------------------------------------------------
 * Notes from Original Noni:  (Could still be applicable, must review and move up if so)
 *  *
 *
 * */

class HomeActivity : AppCompatActivity(), LoadMore {

    private var ziptextView: TextView? = null
    private val TAG = HomeActivity::class.java.simpleName
    private var EMPTY = ""
    private lateinit var toolbar: Toolbar
//internal
    private lateinit var mAdapter: RestaurantCardAdapter
    private lateinit var mHandler: Handler
    private lateinit var mRunnable:Runnable
    private var testData: ArrayList<RestaurantCard?> = ArrayList()
    private var testDataImages: ArrayList<Int> = ArrayList()

    //TODO: REMOVE IF NEVER USE, AFTER FINISHED
    private var mRecyclerView: RecyclerView? = null
    private val mySwipeRefreshLayout: SwipeRefreshLayout by lazy {
        findViewById<SwipeRefreshLayout>(R.id.swipe_refresh_view)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // Loads animals into the ArrayList
        addTestData()
        addTestDataPictures()
        // Initialize the handler instance
        mHandler = Handler()

        onInit()

        val newString  = SharedPref().read(SharedPref().CITY, EMPTY)
        val appBarText = text_bar
        appBarText.text = newString

//        toolbar = app_bar as Toolbar
//        setSupportActionBar(toolbar)
        Log.d(TAG, "CHECKING READING: $newString")
    }

    private fun onInit() {

        // Creates a vertical Layout Manager
        restaurant_recycler_view.layoutManager = LinearLayoutManager(this)

        // Access the RecyclerView Adapter and load the data into it
        mAdapter = RestaurantCardAdapter(restaurant_recycler_view, this, testData, testDataImages)
            { card : RestaurantCard?-> onCardClick(card) }
        restaurant_recycler_view.adapter = mAdapter
        mAdapter.setLoadedMore(this)

        // Handles Refresh
        swipe_refresh_view.setOnRefreshListener {
            // Initialize a new Runnable
            mRunnable = Runnable {

                Toast.makeText(applicationContext, "Refreshing", Toast.LENGTH_SHORT).show()

                testData.add(RestaurantCard(111,"ewd")) //TODO: <-- Only here to test if refresh works, delete  later, delete toast as well


                onInit() //re-initaites list, I'm not sure if this is what wa want, to remake everything, but sounds right when I think about it

                // Hide swipe to refresh icon animation
                swipe_refresh_view.isRefreshing = false
            }
            // Execute the task after specified time
            mHandler.postDelayed(
                    mRunnable,
                    //TODO: Have view disappear only when the process has finished updating: Check this isn't the case already
                    (2000).toLong() // Delay 2 seconds
            )
        }
    }

    override fun onLoadMore() {
            //run on thread
            //Handler().postDelayed()
            Handler().postDelayed({

                if(testData.size < 25) {//Max size =  25 restaurants near you
                    testData.add(null)
                    mAdapter.notifyItemInserted(testData.size - 1)

                    testData.removeAt(testData.size - 1)//remove null item
                    mAdapter.notifyItemRemoved(testData.size)

                    //random new data?
                    val index = testData.size
                    val end = index + 10

                    for (i in index until end) {
                        testData.add(RestaurantCard(111, "generatedItem"))
                    }

                    mAdapter.notifyDataSetChanged()
                    mAdapter.setLoaded()
                }else
                {
                    Toast.makeText(applicationContext, "Max Search Results near you", Toast.LENGTH_SHORT).show()
                }

            },2000)//delay 3 seconds TODO: Text if we want 3... maybe 2?1? MAKE DYNAMIC, WHEN READY
    }

    private fun onCardClick(card : RestaurantCard?) {

        Log.d(TAG, "Card = text: ${card?.itemName}")

        val i = Intent(this@HomeActivity, ModelContainerViewActivity::class.java)
//        val i = Intent(this@HomeActivity, ModelARViewActivity::class.java)
        startActivity(i)
        //finish()

        //TODO: Make an AR/3D selection before making this assumption
        // If Device does not support ARCore, remove access to Camera button
//        if (ArCoreApk.getInstance().checkAvailability(applicationContext).isUnsupported()) run {
//            println("Device does not support ARCore")
//            Toast.makeText(applicationContext, "Sorry, Device does not support ARCore", Toast.LENGTH_SHORT).show()
//        }

    }

    //TODO:Remove, only here to test sample data
    fun addTestData() {
        testData.add(RestaurantCard(112,"one"))
        testData.add(RestaurantCard(113,"two"))
        testData.add(RestaurantCard(114,"three"))
        testData.add(RestaurantCard(115,"elf"))
        testData.add(RestaurantCard(116,"ok"))
        testData.add(RestaurantCard(117,"work"))
        testData.add(RestaurantCard(118,"we dat best"))
        testData.add(RestaurantCard(119,"ftw"))
        testData.add(RestaurantCard(110,"wat"))
        testData.add(RestaurantCard(121,"heheheh"))
    }

    fun addTestDataPictures() {
        testDataImages.add(R.drawable.bento_main_image)
        testDataImages.add(R.drawable.omelet_bar_main_image)
        testDataImages.add(R.drawable.mecatos_main_image)
//        testDataImages.add("R.drawable.neighbors_house_main_image")
//        testDataImages.add("R.drawable.bento_main_image")
    }
}
