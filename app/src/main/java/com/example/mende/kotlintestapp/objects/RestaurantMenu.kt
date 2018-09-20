package com.example.mende.kotlintestapp.objects

import java.util.*

/**
 * Created by Marunizer
 *
 * Purpose of this class is to contain Restaurant menu information gathered from Database
 *
 * -------------------------------------------------------------------------------------------------
 * NOTES:
 *
 * -------------------------------------------------------------------------------------------------
 * CONCERNS:
 *  - using val with a real time database
 *
 *  ------------------------------------------------------------------------------------------------
 *  TODO: Need to link the Categories to the items, without putting items in categories/thus re-making the items
 *  Solution_one: instead of having items inside categories, have category type inside the item,
 *                Impact: no need for storing items in category class, separate entities
 */

data class RestaurantMenu(
        val listOfCategories : ArrayList<RestaurantMenuCategory>,
        val listOfItems : ArrayList<RestaurantMenuitem>
) {


}