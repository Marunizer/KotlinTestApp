package com.example.mende.kotlintestapp.objects

/**
 * Created by Marunizer
 *
 * Purpose of this class is to contain menu category information gathered from Database
 *
 * -------------------------------------------------------------------------------------------------
 * NOTES:
 *
 * -------------------------------------------------------------------------------------------------
 * CONCERNS:
 *  - using val within a real time database
 */

data class RestaurantMenuCategory(
        val categoryName: String,
        val iconPath: String,      // name of string will match up with icon image to be downloaded in storage.
        val listOfItems : ArrayList<String> //TODO: Find out if we need to remove this, or keep (Should only hold names)
)
{

}