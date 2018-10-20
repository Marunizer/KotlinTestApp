package com.example.mende.kotlintestapp.views

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.android.volley.Request
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.testing_layout)

        //onInit()

    }

    private fun onDownloadInit(){
        //TODO: Add download logic for sfb model files
        //first must research if there is a way to compress it
    }


    private fun onInit() {


        // Get a RequestQueue
        val queue = VolleySingleton.getInstance(this.applicationContext).requestQueue

        // Instantiate the RequestQueue.
        val url = "https://api.noni.menu/v1/restaurants?lat="+ 28.469953+ "&lng=" + -81.341406 + "&radius=" + 5000


        val jsonArrayRequest = JsonArrayRequest(Request.Method.GET, url, null,
                Response.Listener { response ->


                    for ( i in 0 until response.length()) {

                        var restaurant = response.get(i) as JSONObject

                        val id : String = restaurant.getString("id")
                        val name : String = restaurant.getString("name")
                        val phone : String = restaurant.getString("phone")
                        val subtitle : String = restaurant.getString("subtitle")
                        val description : String = restaurant.getString("description")
                        val webUrl: String = restaurant.getString("webUrl")
                        val website : String = restaurant.getString("website")
                        val priceLevel : Int = restaurant.getInt("priceLevel")
                        val isLive : Boolean = restaurant.getBoolean("isLive")
                        val type : String = restaurant.getJSONObject("location").getString("type")
                        val longitude : Double = restaurant.getJSONObject("location").getJSONArray("coordinates").get(0) as Double
                        val latitude : Double = restaurant.getJSONObject("location").getJSONArray("coordinates").get(1) as Double


                        var types : ArrayList<String> = ArrayList()
                        for ( j in 0 until restaurant.getJSONArray("types").length()) {
                            types.add(restaurant.getJSONArray("types").get(j) as String)
                        }

                        var bannerImages : ArrayList<String> = ArrayList()
                        val bannerImagesJSON = restaurant.getJSONObject("bannerImages")
                        bannerImages.add(bannerImagesJSON.getString("at1x"))
                        bannerImages.add(bannerImagesJSON.getString("at2x"))
                        bannerImages.add(bannerImagesJSON.getString("at3x"))

                        var thumbNailImages : ArrayList<String> = ArrayList()
                        val thumbNailImagesJSON = restaurant.getJSONObject("thumbnailImages")
                        thumbNailImages.add(thumbNailImagesJSON.getString("at1x"))
                        thumbNailImages.add(thumbNailImagesJSON.getString("at2x"))
                        thumbNailImages.add(thumbNailImagesJSON.getString("at3x"))


                        var deliveryLinks : ArrayList<String> = ArrayList()
                        val deliveryLinksJSON = restaurant.getJSONObject("deliveryLinks")
                        deliveryLinks.add(deliveryLinksJSON.getString("postmates"))
                        deliveryLinks.add(deliveryLinksJSON.getString("caviar"))
                        deliveryLinks.add(deliveryLinksJSON.getString("uberEats"))
                        deliveryLinks.add(deliveryLinksJSON.getString("grubHub"))
                        deliveryLinks.add(deliveryLinksJSON.getString("eat24"))
                        deliveryLinks.add(deliveryLinksJSON.getString("doorDash"))

                        Log.d(TAG, "Response: %s".format(id))
                        Log.d(TAG, "Response: %s".format(name))
                        Log.d(TAG, "Response: %s".format(phone))
                        Log.d(TAG, "Response: %s".format(subtitle))
                        Log.d(TAG, "Response: %s".format(description))
                        Log.d(TAG, "Response: %s".format(webUrl))
                        Log.d(TAG, "Response: %s".format(website))
                        Log.d(TAG, "Response: %s".format(priceLevel.toString()))
                        Log.d(TAG, "Response: %s".format(isLive.toString()))
                        Log.d(TAG, "Response: %s".format(type))
                        Log.d(TAG, "Response: %s".format(longitude.toString()))
                        Log.d(TAG, "Response: %s".format(latitude.toString()))
                        Log.d(TAG, "Response: %s".format(types.toString()))
                        Log.d(TAG, "Response: %s".format(bannerImages.toString()))
                        Log.d(TAG, "Response: %s".format(thumbNailImages.toString()))
                        Log.d(TAG, "Response: %s".format(deliveryLinks.toString()))
                    }


//                    var str_user: String = ""
//                    for (i in 0 until jsonArray2.length()) {
//                        var jsonInner: JSONObject = jsonArray2.getJSONObject(i)
//                        str_user = str_user + "\n" + jsonInner.get("login")
//                    }

                },
                Response.ErrorListener { error ->
                    Log.e(TAG, "Response: %s ERROR :( $error ")
                }
        )
        VolleySingleton.getInstance(this).addToRequestQueue(jsonArrayRequest)
    }

    fun parseJSONtoRestaurant() : String {

        return  "[{\"id\":\"5b7c5ef2ca3be60001c7a990\",\"name\":\"SAM's Diner\",\"subtitle\":\"\uD83C\uDF73 \uD83C\uDF54 \uD83C\uDF2E\",\"description\":\"This narrow, down-to-earth diner with counter seating offers American & global plates, plus beer.\",\"priceLevel\":2,\"types\":[\"American\",\"Breakfast\",\"Brunch\"],\"bannerImages\":{\"at1x\":\"20180923_213045_at1x_samsbi.png\",\"at2x\":\"20180923_213045_at2x_samsbi.png\",\"at3x\":\"20180923_213045_at3x_samsbi.png\"},\"location\":{\"coordinates\":[-122.4156,37.7785],\"type\":\"Point\"},\"isLive\":true,\"phone\":\"(415) 626-8590\",\"website\":\"http:\\/\\/www.samssf.com\\/\",\"thumbnailImages\":{\"at1x\":\"\",\"at2x\":\"\",\"at3x\":\"\"},\"deliveryLinks\":{\"postmates\":\"https:\\/\\/postmates.com\\/merchant\\/sams-diner-san-francisco\",\"caviar\":\"\",\"uberEats\":\"\",\"grubHub\":\"\",\"eat24\":\"https:\\/\\/www.eat24.com\\/order-online\\/sams-diner-1220-market-st-san-francisco\\/559862\",\"doorDash\":\"https:\\/\\/www.doordash.com\\/store\\/sam-s-diner-san-francisco-297685\\/\"},\"webUrl\":\"\\/sf\\/sams\"},{\"id\":\"5ba2b1679cd1d5000186ff5a\",\"name\":\"Kiran's Kitchen\",\"subtitle\":\"\uD83C\uDF63\uD83D\uDC20\uD83C\uDF64\",\"description\":\"Amazing poke creations at an affordable price! \",\"priceLevel\":2,\"types\":[\"Falafel\",\"Shortcake\"],\"bannerImages\":{\"at1x\":\"20180924_015026_at1x_image 3.jpg\",\"at2x\":\"20180924_015026_at2x_image 3.jpg\",\"at3x\":\"20180924_015026_at3x_image 3.jpg\"},\"location\":{\"coordinates\":[-122.437226,37.791849],\"type\":\"Point\"},\"isLive\":true,\"phone\":\"954-804-3257\",\"website\":\"\",\"thumbnailImages\":{\"at1x\":\"\",\"at2x\":\"\",\"at3x\":\"\"},\"deliveryLinks\":{\"postmates\":\"\",\"caviar\":\"\",\"uberEats\":\"\",\"grubHub\":\"\",\"eat24\":\"\",\"doorDash\":\"\"},\"webUrl\":\"\\/sf\\/kirans-kitchen\"}]"

    }

}