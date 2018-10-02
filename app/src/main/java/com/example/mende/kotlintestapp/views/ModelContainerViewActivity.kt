package com.example.mende.kotlintestapp.views

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.View
import com.example.mende.kotlintestapp.R
import com.example.mende.kotlintestapp.adapters.ItemCircleViewAdapter
import com.example.mende.kotlintestapp.objects.ItemCircle
import com.example.mende.kotlintestapp.objects.EmojiObjects
import com.example.mende.kotlintestapp.objects.RestaurantMenuItem
import kotlinx.android.synthetic.main.activity_model_container.*


/**
 * Created by Marunizer
 * Model View Screen
 * purpose: Container and main screen to view and interact with models
 *
 * -------------------------------------------------------------------------------------------------
 * NOTES:
 *  references:
 *
 *
 *  #2 https://medium.com/thoughts-overflow/how-to-add-a-fragment-in-kotlin-way-73203c5a450b
 *          - for lambda expression on changing fragment, not yet used
 *
 * -------------------------------------------------------------------------------------------------
 * CONCERNS:
 * - not using AppCompacActivity, instead using FragmentActivity, should be fine
 *          But better, to do better research, look at ref #2 for potential more information
 *
 * -------------------------------------------------------------------------------------------------
 * Notes from Original Noni:  (Could still be applicable, must review and move up if so)
 *  *
 *
 * */


class ModelContainerViewActivity: FragmentActivity(){//, MyCircleAdapter.AdapterCallback, CategoryPickerAdapter.AdapterCallbackCategory {
 //eventually try to have deeper levels of control, but for now one folder for all works


//    private var storageFile: File? = null
//private var externalFile:File? = null

    private lateinit var currentFragment : Fragment
    private lateinit var mAdapter: ItemCircleViewAdapter
    private lateinit var mHandler: Handler
    private val TAG = ModelContainerViewActivity::class.java.simpleName
    private var testData: ArrayList<ItemCircle?> = ArrayList()
    lateinit var restaurantRef : String

    //remove this in the future
    lateinit var currentSelectedItem : RestaurantMenuItem

    //Consider doing this in a service instead since it is a task of decoding a file
//    external fun stringFromJNI(dracoFile: String, objFile: String)
//    static
//    {
//        System.loadLibrary("hello-libs");
//    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_model_container)

        restaurantRef = intent.getStringExtra("card_name")

        addTestData(getItemList())

        // Initialize the handler instance
        mHandler = Handler()

        setUpGUI()
}

    @SuppressLint("SetTextI18n")
    fun setUpGUI(){

        title_text.text = currentSelectedItem.name + "  " + restaurantRef
        item_cost.text = "$${currentSelectedItem.cost}"
//        item_description.text = "Best Burger NA"

        circle_item_recycler_view.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        switch_button.text = "Magic " + EmojiObjects.CAMERA
        switch_button.setOnClickListener { onSwitchClick()}
        order_button.text = "Order Now"

        // Access the RecyclerView Adapter and load the data into it

        mAdapter = ItemCircleViewAdapter(circle_item_recycler_view, this, testData)
            { itemCircle : ItemCircle?-> onCircleClick(itemCircle) }
        circle_item_recycler_view.adapter = mAdapter


        setFragmentView()
    }

    private fun onCircleClick(circleView : ItemCircle?) {

        Log.d(TAG, "CLICKED: circleView = text: ${circleView?.restaurantMenuItem?.name}")
        title_text.text = circleView?.restaurantMenuItem?.name
        item_cost.text = circleView?.restaurantMenuItem?.cost
        onChangeModel(circleView?.restaurantMenuItem)
        //TODO: Replace cupcake model with hamburger model  as a first step
        //then Link cards to specific models to test if being accessed correctly
        //then try to make a model on the fly programmatically using obj,mtl,jpg, NOTE: gltf models are the best for sceneform
        //then path the models to particular circles(menuItems)
        //do stuff
    }

    private fun onChangeModel(restaurantMenuItem : RestaurantMenuItem?) {
        (currentFragment as ModelSceneViewFragment).passData(restaurantMenuItem)
    }

    private fun onSwitchClick() {
        val i = Intent(this@ModelContainerViewActivity, TestButtonARActivity::class.java)
        startActivity(i)
        finish()
    }

    private fun setFragmentView() {

        //currentFragment = TestButtonARActivity.newInstance()
        currentFragment = ModelSceneViewFragment.newInstance()
        //SAMPLE CODE
        supportFragmentManager
                .beginTransaction()
                //Possible to add a ( , "text"   ) after second parameter, maybe passes in content
                .add(R.id.model_frame, currentFragment)
                .commit()
    }


    //if we are able to make ARFragment be held by a Fragment in the future
//    //TODO: Use this lambda expression to change switch fragments
//    inline fun FragmentManager.inTransaction(func: FragmentTransaction.() -> FragmentTransaction) {
//        beginTransaction().func().commit()
//        //If Cannot use, for whatever reason: this is the alternative:
////        supportFragmentManager.inTransaction {
////            remove(fragmentA)
////            add(R.id.frameLayoutContent, fragmentB)
////        }
//    }

    //fake function for sample item data
    fun getItemList() : ArrayList<RestaurantMenuItem> {
        val restaurantMenuItemList : ArrayList<RestaurantMenuItem> = ArrayList()

        restaurantMenuItemList.add(RestaurantMenuItem("Cupcake","5.00","hyperbolic space cupcake of time"))
        restaurantMenuItemList.add(RestaurantMenuItem("Hamburger","6.00","hyperbolic space Hamburger of timex2"))
        restaurantMenuItemList.add(RestaurantMenuItem("Heart","7.00","hyperbolic space Heart of timex4"))
        restaurantMenuItemList.add(RestaurantMenuItem("Cupcake","8.00","hyperbolic space Cupcake of timex6"))
        restaurantMenuItemList.add(RestaurantMenuItem("Hamburger","9.00","hyperbolic space Hamburger of timex8"))
        restaurantMenuItemList.add(RestaurantMenuItem("Heart","10.00","hyperbolic space Heart of timex10"))
        return restaurantMenuItemList
    }

    fun addTestData(itemList: ArrayList<RestaurantMenuItem>) {

        var id : Long = 112

        currentSelectedItem = itemList[0]

        for(item in itemList)
        {
            testData.add(ItemCircle(id,item))
            id++
        }
    }

    fun begin() {
        onChangeModel(currentSelectedItem)
    }


    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
        }
    }
}