package com.example.mende.kotlintestapp.views

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentActivity
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentTransaction
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.View
import com.example.mende.kotlintestapp.R
import com.example.mende.kotlintestapp.adapters.ItemCircleViewAdapter
import com.example.mende.kotlintestapp.objects.ItemCircle
import com.example.mende.kotlintestapp.objects.EmojiObjects
import com.google.ar.sceneform.ux.ArFragment
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


//external fun stringFromJNI(dracoFile:String, objFile:String)

    private lateinit var currentFragment : Fragment
    lateinit var fakeFragment : ArFragment
    private lateinit var mAdapter: ItemCircleViewAdapter
    private lateinit var mHandler: Handler
    private val TAG = ModelContainerViewActivity::class.java.simpleName
    private var testData: ArrayList<ItemCircle?> = ArrayList()
    lateinit var restaurantRef : String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_model_container)

        restaurantRef = intent.getStringExtra("card_name")

        addTestData()
        // Initialize the handler instance
        mHandler = Handler()

        setUpGUI()
}

    @SuppressLint("SetTextI18n")
    fun setUpGUI(){

        title_text.text = "Party Cupcake $restaurantRef"
        item_cost.text = "$10.99"
//        item_description.text = "Best Burger NA"

        circle_item_recycler_view.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        switch_button.text = "Magic " + EmojiObjects.CAMERA
        switch_button.setOnClickListener { onSwitchClick()}
        order_button.text = "Order Now"

        // Access the RecyclerView Adapter and load the data into it

        mAdapter = ItemCircleViewAdapter(circle_item_recycler_view, this, testData)
            { itemCircle : ItemCircle?-> onCircleClick(itemCircle) }
        circle_item_recycler_view.adapter = mAdapter


        fakeFragment = fake_fragment as ArFragment
        setFragmentView()
    }

    private fun onCircleClick(circleView : ItemCircle?) {

        Log.d(TAG, "CLICKED: circleView = text: ${circleView?.itemName}")

        //do stuff

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

//    //TODO: Use this lambda expression to change switch fragments
//    inline fun FragmentManager.inTransaction(func: FragmentTransaction.() -> FragmentTransaction) {
//        beginTransaction().func().commit()
//        //If Cannot use, for whatever reason: this is the alternative:
////        supportFragmentManager.inTransaction {
////            remove(fragmentA)
////            add(R.id.frameLayoutContent, fragmentB)
////        }
//    }

    fun addTestData() {
        testData.add(ItemCircle(112,"one"))
        testData.add(ItemCircle(113,"two"))
        testData.add(ItemCircle(114,"three"))
        testData.add(ItemCircle(115,"elf"))
        testData.add(ItemCircle(116,"ok"))
        testData.add(ItemCircle(117,"work"))
        testData.add(ItemCircle(118,"we dat best"))
        testData.add(ItemCircle(119,"ftw"))
        testData.add(ItemCircle(110,"wat"))
        testData.add(ItemCircle(121,"heheheh"))
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