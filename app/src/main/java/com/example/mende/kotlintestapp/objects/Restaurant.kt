package com.example.mende.kotlintestapp.objects

import android.location.Location

import java.util.ArrayList

/**
 * Created by Marunizer
 *
 * Purpose of this class is to contain Restaurant information gathered from Database
 *
 * -------------------------------------------------------------------------------------------------
 * NOTES:
 *
 * -------------------------------------------------------------------------------------------------
 * CONCERNS:
 *  - using val for the Restaurant data, if we are using a real time database that auto refreshes
 *    then this will be a problem.(Will try to re-write values that may have changed, like distanceAway)
 *    BUT if no auto update and instead, only updates when user wants to update, then we will be
 *    remaking the restaurant, in which case there should be no problem.
 */

data class  Restaurant(
    val name: String,
    val location: Location,
    val coordinateKey: String,
    val generalCost: Int, //assigns number of '$'
    val streetAddress: String,
    val userLocation: Location,  // TODO: Remove this, should be accessed from sharedPref.
//  val distanceAway: Float -
    val restaurantMenu: RestaurantMenu //TODO: Find out if we need multiple menu's for one restaurant, or only 1 menu per restaurant(1 for now)
){
    val distanceAway: Float //to be used when finally calculating distance from user

    //Will hold keywords that will be used for emojis, Just text for now
    private val emojiList = ArrayList<String>()

    init {
        this.distanceAway = location.distanceTo(userLocation)
    }
}
