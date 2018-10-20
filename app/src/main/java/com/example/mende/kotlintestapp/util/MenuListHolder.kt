package com.example.mende.kotlintestapp.util

import com.example.mende.kotlintestapp.objects.RestaurantMenuItem
import java.util.ArrayList


/**
 * Created by Marunizer
 * Purpose of this Class is to always contain and maintain list of menu items
 * -------------------------------------------------------------------------------------------------
 *  NOTES:
 *
 * -------------------------------------------------------------------------------------------------
 * CONCERNS:
 *
 */

class MenuListHolder {

    val TAG = MenuListHolder::class.java.simpleName

    //Kotlin does not recognize static variables, a companion object helps us get around this
    companion object {
        lateinit var listOfMenuLists: HashMap<String,ArrayList<RestaurantMenuItem>>
        //string will be restaurant name + streetAddress
    }

    fun init() {
       listOfMenuLists = HashMap()
    }

    fun getList(key: String): ArrayList<RestaurantMenuItem>? {
        return listOfMenuLists[key]
    }

    fun addList(newKey: String, newList: ArrayList<RestaurantMenuItem>) {
        listOfMenuLists[newKey] = newList

    }
}