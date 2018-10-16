package com.example.mende.kotlintestapp.adapters

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.Color
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.mende.kotlintestapp.R
import com.example.mende.kotlintestapp.objects.ItemCircle
import kotlinx.android.synthetic.main.circle_menu_item.view.*

internal var lastVisibleItem: Int = 0
internal var totalItemCount: Int = 0

class ItemCircleViewAdapter(recyclerView: RecyclerView,
                            internal var activity: Activity,
                            internal var items: ArrayList<ItemCircle?>,
                            val arMode: Boolean,
                            val clickListener: (ItemCircle?, ItemViewHolder) -> Unit) :
        RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    init {

        val linearLayoutManager = recyclerView.layoutManager as LinearLayoutManager

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                totalItemCount = linearLayoutManager.itemCount
                lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition()
            }
        })
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        lateinit var view: View

        if (arMode)
            view = LayoutInflater.from(activity).inflate(R.layout.circle_menu_item_ar, parent, false)
        else
            view = LayoutInflater.from(activity).inflate(R.layout.circle_menu_item, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        if (holder is ItemViewHolder) {
            val item = items[position]

            holder.bind(item, holder, clickListener)
            holder.itemName?.text = item?.restaurantMenuItem?.name
            if (arMode)
                holder.itemName?.setTextColor(Color.parseColor("#ffffff"))

        }
    }

    class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        fun bind(circle: ItemCircle?, lilHolder: ItemViewHolder, clickListener: (ItemCircle?, ItemViewHolder) -> Unit) {
            itemView.setOnClickListener {
                clickListener(circle, lilHolder)
            }

        }

        var itemName = view.circle_text
        var itemImage = view.circle_image

    }

    override fun getItemCount(): Int {
        return items.size
    }

    fun updateCircle(oldHolder : ItemViewHolder?, newHolder: ItemViewHolder) {

//        if (firstItem == false)
//        {
//            firstItem = true
//        }
        if (oldHolder == null)
        {
            Log.d("MAGIC SPEAKER", "first item set to true")
                    //set first item blue
        //    newHolder.itemImage.borderColor = R.color.noni_theme
        }
        else
        {
//            if (firstItem)
//            {
//                Log.d("MAGIC SPEAKER", "First item hit ")
//                firstHolder.itemImage.borderColor = Color.WHITE
//                firstItem = false
//            }
//
//            Log.d("MAGIC SPEAKER", "norm behavior ")

            oldHolder.itemImage.borderColor = Color.WHITE
            newHolder.itemImage.borderColor = Color.CYAN
        }

        oldHolder!!.itemImage.isOval =  true
        oldHolder.itemImage.setBorderWidthDP(08f)
        newHolder.itemImage.isOval = true
        newHolder.itemImage.setBorderWidthDP(13f)

    }

}