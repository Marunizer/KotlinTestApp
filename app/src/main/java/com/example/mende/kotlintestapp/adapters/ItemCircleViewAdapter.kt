package com.example.mende.kotlintestapp.adapters

import android.app.Activity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.mende.kotlintestapp.R
import com.example.mende.kotlintestapp.objects.ItemCircle
import kotlinx.android.synthetic.main.circle_menu_item.view.*

internal var lastVisibleItem : Int = 0
internal var totalItemCount : Int = 0

class ItemCircleViewAdapter(recyclerView: RecyclerView,
                            internal var activity: Activity,
                            internal var items : ArrayList<ItemCircle?>,
                            val clickListener: (ItemCircle?) -> Unit) :
        RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    init {
        val linearLayoutManager = recyclerView.layoutManager as LinearLayoutManager

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                totalItemCount = linearLayoutManager.itemCount
                lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition()            }
        })
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        val view = LayoutInflater.from(activity).inflate(R.layout.circle_menu_item, parent, false)
        return ItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        if(holder is ItemViewHolder) {
            val item = items[position]

            holder.bind(item, clickListener)
            holder.itemName?.text = item?.restaurantMenuItem?.name
//            holder.itemImage.background = getItemViewType(R.drawable.abc_btn_check_material)
        }
    }

    class ItemViewHolder(view : View) : RecyclerView.ViewHolder(view)
{
    fun bind(circle : ItemCircle?, clickListener: (ItemCircle?) -> Unit)
    {
        itemView.setOnClickListener {clickListener(circle)}
    }

    var itemName = view.circle_text
    var itemImage = view.circle_image

}

    override fun getItemCount(): Int {
        return items.size
    }

    //function sample to make new one of
//    fun setLoadedMore(loadMoreCards: LoadMore) {
//        this.loadMore = loadMoreCards
//    }

}