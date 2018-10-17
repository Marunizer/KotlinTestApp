package com.example.mende.kotlintestapp.views

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.*
import com.example.mende.kotlintestapp.R
import com.example.mende.kotlintestapp.util.VolleySingleton
import kotlinx.android.synthetic.main.testing_layout.*
import org.json.JSONObject

//TODO: Purpose is undecided, may remove in future, currently just here to be a sandbox activity
class DownloadTestActivity : AppCompatActivity() {

    private val TAG = HomeActivity::class.java.simpleName
    private var EMPTY = ""
    private lateinit var testList : ArrayList<Any>


    lateinit var id : String
    lateinit var name : String
    lateinit var subtitle : String
    lateinit var description : String
    lateinit var type : String
    lateinit var webUrl: String
    var priceLevel : Int = 0
    var longitude : Double = 0.0
    var latitude : Double = 0.0
    var isLive : Boolean = false
    var types : ArrayList<String> = ArrayList()
    var bannerImages : ArrayList<String> = ArrayList()
    var deliveryLinks : ArrayList<String> = ArrayList()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.testing_layout)

        onInit()

    }

    private fun onInit() {


        // Get a RequestQueue
        val queue = VolleySingleton.getInstance(this.applicationContext).requestQueue


        // Instantiate the RequestQueue.
        val url = "https://api.noni.menu/v1/restaurants?lat="+ 28.469953+ "&lng=" + -81.341406 + "&radius=" + 5000

        var thisd = 0


        val jsonObjectRequest = JsonArrayRequest(Request.Method.GET, url, null,
                Response.Listener { response ->

                     thisd = 5




//                    val news = response
//                            .getJSONObject("query")
//                            .getJSONObject("results")
//                            .getJSONArray("item")
//
//
//                    Log.d(TAG, "Response: %s".format(news))


                    Log.d(TAG, "Response: %s".format(response.toString()) + thisd)
                   // test_text.text = "Response: %s".format(response.toString())
                },
                Response.ErrorListener { error ->
                    Log.e(TAG, "Response: %s ERROR :( $error " + thisd)
                }
        )
        VolleySingleton.getInstance(this).addToRequestQueue(jsonObjectRequest)
    }

    fun parseJSONtoRestaurant() {

        val theString = "[{\"id\":\"5b7c5ef2ca3be60001c7a990\",\"name\":\"SAM's Diner\",\"subtitle\":\"\uD83C\uDF73 \uD83C\uDF54 \uD83C\uDF2E\",\"description\":\"This narrow, down-to-earth diner with counter seating offers American & global plates, plus beer.\",\"priceLevel\":2,\"types\":[\"American\",\"Breakfast\",\"Brunch\"],\"bannerImages\":{\"at1x\":\"20180923_213045_at1x_samsbi.png\",\"at2x\":\"20180923_213045_at2x_samsbi.png\",\"at3x\":\"20180923_213045_at3x_samsbi.png\"},\"location\":{\"coordinates\":[-122.4156,37.7785],\"type\":\"Point\"},\"isLive\":true,\"phone\":\"(415) 626-8590\",\"website\":\"http:\\/\\/www.samssf.com\\/\",\"thumbnailImages\":{\"at1x\":\"\",\"at2x\":\"\",\"at3x\":\"\"},\"deliveryLinks\":{\"postmates\":\"https:\\/\\/postmates.com\\/merchant\\/sams-diner-san-francisco\",\"caviar\":\"\",\"uberEats\":\"\",\"grubHub\":\"\",\"eat24\":\"https:\\/\\/www.eat24.com\\/order-online\\/sams-diner-1220-market-st-san-francisco\\/559862\",\"doorDash\":\"https:\\/\\/www.doordash.com\\/store\\/sam-s-diner-san-francisco-297685\\/\"},\"webUrl\":\"\\/sf\\/sams\"},{\"id\":\"5ba2b1679cd1d5000186ff5a\",\"name\":\"Kiran's Kitchen\",\"subtitle\":\"\uD83C\uDF63\uD83D\uDC20\uD83C\uDF64\",\"description\":\"Amazing poke creations at an affordable price! \",\"priceLevel\":2,\"types\":[\"Falafel\",\"Shortcake\"],\"bannerImages\":{\"at1x\":\"20180924_015026_at1x_image 3.jpg\",\"at2x\":\"20180924_015026_at2x_image 3.jpg\",\"at3x\":\"20180924_015026_at3x_image 3.jpg\"},\"location\":{\"coordinates\":[-122.437226,37.791849],\"type\":\"Point\"},\"isLive\":true,\"phone\":\"954-804-3257\",\"website\":\"\",\"thumbnailImages\":{\"at1x\":\"\",\"at2x\":\"\",\"at3x\":\"\"},\"deliveryLinks\":{\"postmates\":\"\",\"caviar\":\"\",\"uberEats\":\"\",\"grubHub\":\"\",\"eat24\":\"\",\"doorDash\":\"\"},\"webUrl\":\"\\/sf\\/kirans-kitchen\"}]"

    }

}