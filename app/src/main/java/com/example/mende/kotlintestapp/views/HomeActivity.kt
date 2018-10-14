package com.example.mende.kotlintestapp.views

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import com.example.mende.kotlintestapp.R
import com.example.mende.kotlintestapp.adapters.RestaurantCardAdapter
import com.example.mende.kotlintestapp.interfaces.LoadMore
import com.example.mende.kotlintestapp.objects.Restaurant
import com.example.mende.kotlintestapp.objects.RestaurantCard
import com.example.mende.kotlintestapp.objects.RestaurantMenuItem
import com.example.mende.kotlintestapp.util.MenuListHolder
import com.example.mende.kotlintestapp.util.SharedPref
import kotlinx.android.synthetic.main.activity_home.*
import kotlinx.android.synthetic.main.location_bar.*
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

    private val TAG = HomeActivity::class.java.simpleName
    private var EMPTY = ""

    private lateinit var mAdapter: RestaurantCardAdapter
    private lateinit var mHandler: Handler
    private lateinit var mRunnable: Runnable
    private var testData: ArrayList<RestaurantCard?> = ArrayList()
    private var testDataImages: ArrayList<Int> = ArrayList()
    private var restaurantDataList: ArrayList<Restaurant> = ArrayList()

    //TODO: REMOVE IF NEVER USE, AFTER FINISHED
    private var mRecyclerView: RecyclerView? = null
    private val mySwipeRefreshLayout: SwipeRefreshLayout by lazy {
        findViewById<SwipeRefreshLayout>(R.id.swipe_refresh_view)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // Loads animals into the ArrayList
        retrieveRestaurants()
        addTestData(restaurantDataList)
        addTestDataPictures()

        // Initialize the handler instance
        mHandler = Handler()
        onInit()

    }

    private fun onInit() {

        val tempString = SharedPref().read(SharedPref().CITY, EMPTY)
        val appBarText = text_bar
        appBarText.text = tempString

        // Creates a vertical Layout Manager
        restaurant_recycler_view.layoutManager = LinearLayoutManager(this)

        // Access the RecyclerView Adapter and load the data into it
        mAdapter = RestaurantCardAdapter(restaurant_recycler_view, this, testData, testDataImages)
        { card: RestaurantCard? -> onCardClick(card) }
        restaurant_recycler_view.adapter = mAdapter
        mAdapter.setLoadedMore(this)

        // Handles Refresh
        swipe_refresh_view.setOnRefreshListener {
            // Initialize a new Runnable
            mRunnable = Runnable {

                Toast.makeText(applicationContext, "Refreshing", Toast.LENGTH_SHORT).show()

                val temp = Restaurant("refreshAddition", 1, "test")
                MenuListHolder().addList(temp.name + temp.streetAddress, getItemList())
                testData.add(RestaurantCard(111, temp)) //TODO: <-- Only here to test if refresh works, delete  later, delete toast as well


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
        Handler().postDelayed({

            if (testData.size < 25) {//Max size =  25 restaurants near you
                testData.add(null)
                mAdapter.notifyItemInserted(testData.size - 1)

                testData.removeAt(testData.size - 1)//remove null item
                mAdapter.notifyItemRemoved(testData.size)

                //random new data?
                val index = testData.size
                val end = index + 10

                for (i in index until end) {
                    val temp = Restaurant("generated Store", 4, "test")
                    MenuListHolder().addList(temp.name + temp.streetAddress, getItemList())
                    testData.add(RestaurantCard(111, temp))
                }

                mAdapter.notifyDataSetChanged()
                mAdapter.setLoaded()
            } else {
                Toast.makeText(applicationContext, "Max Search Results near you", Toast.LENGTH_SHORT).show()
            }

        }, 2000)//delay 3 seconds TODO: Text if we want 3... maybe 2?1? MAKE DYNAMIC, WHEN READY
    }

    private fun onCardClick(card: RestaurantCard?) {

        Log.d(TAG, "Card = text: ${card?.restaurant?.name}")

        val i = Intent(this@HomeActivity, ModelContainerViewActivity::class.java)
        i.putExtra("card_key", card?.restaurant?.name + card?.restaurant?.streetAddress)
        startActivity(i)
        //finish()

    }


    //TODO:Remove, only here to test sample data
    fun addTestData(restaurantList: ArrayList<Restaurant>) {

        var id: Long = 112
        for (item in restaurantList) {
            testData.add(RestaurantCard(112, item))
            id++
        }
    }

    //intended to be used when filling up data from database provider, for now it is just a sample data 'factory'
    fun retrieveRestaurants() { //will return void in the future

        var tempRestaurantReference: Restaurant

        tempRestaurantReference = Restaurant("Weenie Hut Juniors", 3, "Bikini Bottom")
        restaurantDataList.add(tempRestaurantReference)
        MenuListHolder().addList(tempRestaurantReference.name + tempRestaurantReference.streetAddress, getItemList())


        tempRestaurantReference = Restaurant("Super Weenie Hut Juniors", 4, "MORDOR")
        restaurantDataList.add(tempRestaurantReference)
        MenuListHolder().addList(tempRestaurantReference.name + tempRestaurantReference.streetAddress, getItemList())


        tempRestaurantReference = Restaurant("Weenie Hut General", 5, "Pacific Ocean")
        restaurantDataList.add(tempRestaurantReference)
        MenuListHolder().addList(tempRestaurantReference.name + tempRestaurantReference.streetAddress, getItemList())


        tempRestaurantReference = Restaurant("The Krusty Krab", 2, "831 Bottom Feeder Lane")
        restaurantDataList.add(tempRestaurantReference)
        MenuListHolder().addList(tempRestaurantReference.name + tempRestaurantReference.streetAddress, getItemList())


        tempRestaurantReference = Restaurant("The Chum Bucket", 1, "832 Bottom Feeder Lane")
        restaurantDataList.add(tempRestaurantReference)
        MenuListHolder().addList(tempRestaurantReference.name + tempRestaurantReference.streetAddress, getItemList())

        //this will not return anything
    }

    //fake function for sample item data
    fun getItemList(): ArrayList<RestaurantMenuItem> {
        val restaurantMenuItemList: ArrayList<RestaurantMenuItem> = ArrayList()

        restaurantMenuItemList.add(RestaurantMenuItem("Cupcake", "5.00", "hyperbolic space cupcake of time"))
        restaurantMenuItemList.add(RestaurantMenuItem("Hamburger", "6.00", "hyperbolic space Hamburger of timex2"))
        restaurantMenuItemList.add(RestaurantMenuItem("Heart", "7.00", "hyperbolic space Heart of timex4"))
        restaurantMenuItemList.add(RestaurantMenuItem("Cupcake", "8.00", "hyperbolic space Cupcake of timex6"))
        restaurantMenuItemList.add(RestaurantMenuItem("Hamburger", "9.00", "hyperbolic space Hamburger of timex8"))
        restaurantMenuItemList.add(RestaurantMenuItem("Heart", "10.00", "hyperbolic space Heart of timex10"))
        return restaurantMenuItemList
    }

    fun addTestDataPictures() {
        testDataImages.add(R.drawable.weenie_hut_juniors_image)
        testDataImages.add(R.drawable.super_weenie_image)
        testDataImages.add(R.drawable.weenie_hut_general_image)
        testDataImages.add(R.drawable.the_krusty_krab_image)
        testDataImages.add(R.drawable.the_chum_bucket_image)
        testDataImages.add(R.drawable.omelet_bar_main_image)
        testDataImages.add(R.drawable.bento_main_image)
        testDataImages.add(R.drawable.dominoes_main_image)
        testDataImages.add(R.drawable.mecatos_main_image)
    }
}
