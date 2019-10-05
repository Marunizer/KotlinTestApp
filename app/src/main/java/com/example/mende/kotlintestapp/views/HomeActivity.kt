package com.example.mende.kotlintestapp.views

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import kotlinx.android.synthetic.main.activity_home.*
import com.example.mende.kotlintestapp.R
import com.example.mende.kotlintestapp.adapters.RestaurantCardAdapter
import com.example.mende.kotlintestapp.interfaces.LoadMore
import com.example.mende.kotlintestapp.objects.Restaurant
import com.example.mende.kotlintestapp.objects.RestaurantCard
import com.example.mende.kotlintestapp.objects.RestaurantMenuItem
import com.example.mende.kotlintestapp.util.MenuListHolder
import com.example.mende.kotlintestapp.util.SharedPref
import com.example.mende.kotlintestapp.util.VolleySingleton
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

        // Initialize the handler instance
        mHandler = Handler()

        //send network request for DB
        requestRestaurants()

        onInit()

    }

    private fun requestRestaurants() {
        // Get a RequestQueue
        val queue = VolleySingleton.getInstance(this.applicationContext).requestQueue
        val userLongitude = SharedPref().read(SharedPref().LOCATION_LON, EMPTY).toFloat()
        val userLatitude = SharedPref().read(SharedPref().LOCATION_LAT, EMPTY).toFloat()
        // Instantiate the RequestQueue.

                                                              //userLongitude       userLatitude
        val url = "https://api.noni.menu/v1/restaurants?lat="+ 28.469953+ "&lng=" + -81.341406 + "&radius=" + 5000

//        val jsonArrayRequest = JsonArrayRequest(Request.Method.GET, url, null,
//                Response.Listener { response ->
//
//
//                    for ( i in 0 until response.length()) {
//
//                        val restaurant = response.get(i) as JSONObject
//
//                        val id : String = restaurant.getString("id")
//                        val name : String = restaurant.getString("name")
//                        val phone : String = restaurant.getString("phone")
//                        val subtitle : String = restaurant.getString("subtitle")
//                        val description : String = restaurant.getString("description")
//                        val webUrl: String = restaurant.getString("webUrl")
//                        val website : String = restaurant.getString("website")
//                        val priceLevel : Int = restaurant.getInt("priceLevel")
//                        val isLive : Boolean = restaurant.getBoolean("isLive")
//                        val type : String = restaurant.getJSONObject("location").getString("type")
//                        val longitude : Double = restaurant.getJSONObject("location").getJSONArray("coordinates").get(0) as Double
//                        val latitude : Double = restaurant.getJSONObject("location").getJSONArray("coordinates").get(1) as Double
//
//
//                        var types : ArrayList<String> = ArrayList()
//                        for ( j in 0 until restaurant.getJSONArray("types").length()) {
//                            types.add(restaurant.getJSONArray("types").get(j) as String)
//                        }
//
//                        var bannerImages : ArrayList<String> = ArrayList()
//                        val bannerImagesJSON = restaurant.getJSONObject("bannerImages")
//                        bannerImages.add(bannerImagesJSON.getString("at1x"))
//                        bannerImages.add(bannerImagesJSON.getString("at2x"))
//                        bannerImages.add(bannerImagesJSON.getString("at3x"))
//
//                        var thumbNailImages : ArrayList<String> = ArrayList()
//                        val thumbNailImagesJSON = restaurant.getJSONObject("thumbnailImages")
//                        thumbNailImages.add(thumbNailImagesJSON.getString("at1x"))
//                        thumbNailImages.add(thumbNailImagesJSON.getString("at2x"))
//                        thumbNailImages.add(thumbNailImagesJSON.getString("at3x"))
//
//
//                        var deliveryLinks : ArrayList<String> = ArrayList()
//                        val deliveryLinksJSON = restaurant.getJSONObject("deliveryLinks")
//                        deliveryLinks.add(deliveryLinksJSON.getString("postmates"))
//                        deliveryLinks.add(deliveryLinksJSON.getString("caviar"))
//                        deliveryLinks.add(deliveryLinksJSON.getString("uberEats"))
//                        deliveryLinks.add(deliveryLinksJSON.getString("grubHub"))
//                        deliveryLinks.add(deliveryLinksJSON.getString("eat24"))
//                        deliveryLinks.add(deliveryLinksJSON.getString("doorDash"))
//
//                        Log.d(TAG, "Response: %s".format(id))
//                        Log.d(TAG, "Response: %s".format(name))
//                        Log.d(TAG, "Response: %s".format(phone))
//                        Log.d(TAG, "Response: %s".format(subtitle))
//                        Log.d(TAG, "Response: %s".format(description))
//                        Log.d(TAG, "Response: %s".format(webUrl))
//                        Log.d(TAG, "Response: %s".format(website))
//                        Log.d(TAG, "Response: %s".format(priceLevel.toString()))
//                        Log.d(TAG, "Response: %s".format(isLive.toString()))
//                        Log.d(TAG, "Response: %s".format(type))
//                        Log.d(TAG, "Response: %s".format(longitude.toString()))
//                        Log.d(TAG, "Response: %s".format(latitude.toString()))
//                        Log.d(TAG, "Response: %s".format(types.toString()))
//                        Log.d(TAG, "Response: %s".format(bannerImages.toString()))
//                        Log.d(TAG, "Response: %s".format(thumbNailImages.toString()))
//                        Log.d(TAG, "Response: %s".format(deliveryLinks.toString()))
//
////                        val tempRestaurantReference = Restaurant(id,name,phone,subtitle,description,
////                                webUrl, website,priceLevel,isLive, type,longitude,latitude,types,
////                                bannerImages, thumbNailImages,deliveryLinks)
//
////                        restaurantDataList.add(tempRestaurantReference)
////                        MenuListHolder().addList(tempRestaurantReference.id, getItemList())
//                    }
////                    addTestData(restaurantDataList)
////                    addTestDataPictures()
////                    onLoadMore()
//                },
//                Response.ErrorListener { error ->
//                    Log.e(TAG, "Response: %s ERROR :( $error ")
//                }
//        )
        val tempRestaurantReference = Restaurant("123214","isDizDaKrustyKrab","12345678910","no!DizIzPatrick","no!DizIzPatrick",
                "ww", "34",3,true, "good",40.6782,73.9442)
        restaurantDataList.add(tempRestaurantReference)
        MenuListHolder().addList(tempRestaurantReference.id, getItemList())
        addTestData(restaurantDataList)
        addTestDataPictures()
        onLoadMore()
//        VolleySingleton.getInstance(this).addToRequestQueue(jsonArrayRequest)
        VolleySingleton.getInstance(this).requestQueue
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
                mAdapter.notifyDataSetChanged()
                mAdapter.setLoaded()
            } else {
                Toast.makeText(applicationContext, "Max Search Results near you", Toast.LENGTH_SHORT).show()
            }
        }, 1000)//delay 1 second
    }

    private fun onCardClick(card: RestaurantCard?) {

//        val i = Intent(this@HomeActivity, DownloadTestActivity::class.java)
        val i = Intent(this@HomeActivity, ModelContainerViewActivity::class.java)
        i.putExtra("card_key", card?.restaurant?.id)
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

    //fake function for sample item data
    fun getItemList(): ArrayList<RestaurantMenuItem> {
        val restaurantMenuItemList: ArrayList<RestaurantMenuItem> = ArrayList()

        restaurantMenuItemList.add(RestaurantMenuItem("Cupcake", "5.00", "hyperbolic space cupcake of time"))
        restaurantMenuItemList.add(RestaurantMenuItem("Hamburger", "6.00", "hyperbolic space Hamburger of timex2"))
        restaurantMenuItemList.add(RestaurantMenuItem("Heart", "7.00", "hyperbolic space Heart of timex4"))
        restaurantMenuItemList.add(RestaurantMenuItem("TheRyanBurger", "11.00", "hyperbolic space Heart of timex20"))
        restaurantMenuItemList.add(RestaurantMenuItem("FishFilet", "12.00", "hyperbolic space Heart of timex30"))
        restaurantMenuItemList.add(RestaurantMenuItem("Octopus_Sushi", "13.00", "hyperbolic space Heart of timex40"))
        restaurantMenuItemList.add(RestaurantMenuItem("Salmon_Sushi", "14.00", "hyperbolic space Heart of timex50"))
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
