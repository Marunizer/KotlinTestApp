package com.example.mende.kotlintestapp.adapters

import android.app.Activity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.mende.kotlintestapp.R
import com.example.mende.kotlintestapp.interfaces.LoadMore
import com.example.mende.kotlintestapp.objects.RestaurantCard
import kotlinx.android.synthetic.main.activity_main.view.*
import kotlinx.android.synthetic.main.card_restaurant.view.*
import java.util.ArrayList

internal class LoadingViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    val progressBar = view.progress_bar
}

internal class ItemViewHolder(view : View) : RecyclerView.ViewHolder(view)
{

    fun bind(card : RestaurantCard?, clickListener: (RestaurantCard?) -> Unit)
    {
       itemView.setOnClickListener {clickListener(card)}
    }

    // Holds the TextView that will add each animal to
    var restaurantName = view.restaurant_name
    var restaurantDistance = view.restaurant_distance
    var restaurantEmoji = view.restaurant_emoji
    var restaurantCost = view.restaurant_cost
    var restaurantImage = view.restaurant_image
}

class RestaurantCardAdapter(recyclerView: RecyclerView,
                            internal var activity: Activity,
                            internal var items : ArrayList<RestaurantCard?>,
                            internal var pictures: ArrayList<Int>,
                            val clickListener: (RestaurantCard?) -> Unit) :
        RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    val VIEW_TYPE_ITEM = 0
    val VIEW_TYPE_LOADING = 1

    internal var loadMore : LoadMore? = null
    internal var isLoading : Boolean = false
    internal val visibleThreshhold : Int = 5
    internal var lastVisibleItem : Int = 0
    internal var totalItemCount : Int = 0

    init {
        val linearLayoutManager = recyclerView.layoutManager as LinearLayoutManager

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                totalItemCount = linearLayoutManager.itemCount
                lastVisibleItem = linearLayoutManager.findLastVisibleItemPosition()
                if (!isLoading && totalItemCount <= lastVisibleItem+visibleThreshhold)
                {
                    if (loadMore != null)
                        loadMore!!.onLoadMore()
                    isLoading = true
                }
            }
        })
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {

        if(viewType == VIEW_TYPE_ITEM) {
            val view = LayoutInflater.from(activity).inflate(R.layout.card_restaurant, parent, false)
            return ItemViewHolder(view)
        }
        else {
            //if (viewType == VIEW_TYPE_LOADING)
            val view = LayoutInflater.from(activity).inflate(R.layout.item_loading, parent, false)
            return ItemViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        if(holder is ItemViewHolder) {

            val item = items[position]
            if(position==0) {
                val picture = pictures[0]
                holder.restaurantImage.setImageResource(picture)
            }
            if(position==1) {
                val picture = pictures[1]
                holder.restaurantImage.setImageResource(picture)
            }
            if(position==2) {
                val picture = pictures[2]
                holder.restaurantImage.setImageResource(picture)
            }
            if(position==3) {
                val picture = pictures[3]
                holder.restaurantImage.setImageResource(picture)
            }
            if(position==4) {
                val picture = pictures[4]
                holder.restaurantImage.setImageResource(picture)
            }
            if(position==5) {
                val picture = pictures[5]
                holder.restaurantImage.setImageResource(picture)
            }
            if(position==6) {
                val picture = pictures[6]
                holder.restaurantImage.setImageResource(picture)
            }
            if(position==7) {
                val picture = pictures[7]
                holder.restaurantImage.setImageResource(picture)
            }
            holder.bind(item, clickListener)
            holder.restaurantName?.text = item?.restaurant?.name
            holder.restaurantDistance?.text = "2.0 miles away"
            holder.restaurantEmoji?.text = item?.restaurant?.streetAddress//"emoji_area"

            if (item?.restaurant?.generalCost == 5)
            {
                holder.restaurantCost?.text = "$$$$$"
            }
            else if (item?.restaurant?.generalCost == 4)
            {
                holder.restaurantCost?.text = "$$$$"
            }
            else if (item?.restaurant?.generalCost == 3)
            {
                holder.restaurantCost?.text = "$$$"
            }
            else if (item?.restaurant?.generalCost == 2)
            {
                holder.restaurantCost?.text = "$$"
            }
            else if (item?.restaurant?.generalCost == 1)
            {
                holder.restaurantCost?.text = "$"
            }

        }
        else if(holder is LoadingViewHolder) {
            holder.progressBar.isIndeterminate = true
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if(items[position] == null)VIEW_TYPE_LOADING else VIEW_TYPE_ITEM
     //   return super.getItemViewType(position)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    fun setLoaded() {
        isLoading = false
    }

    fun setLoadedMore(loadMoreCards: LoadMore) {
        this.loadMore = loadMoreCards
    }

}