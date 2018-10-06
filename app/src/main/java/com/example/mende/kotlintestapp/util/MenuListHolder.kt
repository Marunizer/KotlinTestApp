package com.example.mende.kotlintestapp.util

import android.content.Context
import android.util.Log
import com.example.mende.kotlintestapp.objects.RestaurantMenuItem
import com.example.mende.kotlintestapp.util.SharedPref.Companion.mSharedPref
import java.util.ArrayList


/**
 * Created by Marunizer
 * Purpose of this Class is to always contain and maintain users location data
 * reference: https://stackoverflow.com/questions/19612993/writing-singleton-class-to-manage-android-sharedpreferences
 * -------------------------------------------------------------------------------------------------
 *  NOTES:
 *  Re-writing class to be a strict shared preferences singleton storage class for Location Data
 *
 *  Note on suppress-lint: suggests to use apply() rather than commit() to rather handle process in background
 *                         We want changes applied Immediately, so commit is being used instead
 *
 *  Only needs to be initialized once in application by MainActivity
 *
 *  ALL NUMBERS AND BOOLEANS MUST BE ATTAINED THROUGH A STRING WHEN READING/WRITING
 *       Technically writing is fine, but lets be consistent here
 * -------------------------------------------------------------------------------------------------
 * CONCERNS:
 *  - We are using val for keys rather than var (editable)
 *      RATIONALITY: Doesn't matter, val is for the keys to the preferences, the preferences
 *                   themselves are still editable.
 *
 *  - why not directly handle Floats and Booleans ? Why use Strings?
 *          Reason: Strings are allowed to be null,  everything else is not :-(
 *          Follow up: We WANT to allow Reads to be null, to allow read at all times, check for null later
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