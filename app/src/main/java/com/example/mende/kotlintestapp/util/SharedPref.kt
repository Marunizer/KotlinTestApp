package com.example.mende.kotlintestapp.util

import android.app.Activity
import android.content.Context
import android.content.SharedPreferences
import android.util.Log


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

class SharedPref {

    val TAG = SharedPref::class.java.simpleName

    val LOCATION_CHOSEN = "LOCATION_CHOSEN"                 //String -> BOOLEAN    TRUE=LOCATION,FALSE=ZIP
    val LOCATION_LAT = "LOCATION_LAT"                       //String -> Probz:Float
    val LOCATION_LON = "LOCATION_LON"                       //String -> Probz:Float
    val ADDRESS = "ADDRESS"                                 //String
    val STREET = "STREET"                                   //String
    val CITY = "CITY"                                       //String
    val STATE = "STATE"                                     //String
    val ZIPCODE = "ZIP_CODE"                                //String
    val RADIUS_KM = "RADIUS_KILOMETER"                      //STRING -> FLOAT
    val RADIUS_MILES = "RADIUS_MILES"                       //STRING -> FLOAT
    val AR_CAPABLE = "AR_CAPABLE"                           //STRING -> BOOLEAN //Might Remove, would be helpful in making app flow smoother, but ignore for now

    //HELPERS CONSTANTS
    val MILES_TO_KILOMETERS : Number = 1.621
    val DEFAULT_RADIUS_MILES : Int = 5 //if radius = 8.0 KM //In Kilometers = 5 miles
    val DEFAULT_RADIUS_KM : Float = (DEFAULT_RADIUS_MILES*MILES_TO_KILOMETERS.toFloat())

    //Kotlin does not recognize static variables, a companion object helps us get around this
    companion object {
        lateinit var mSharedPref: SharedPreferences
    }

    fun init(context: Context) {
        Log.d(TAG, "Shared Preference : INITIALIZED")
        mSharedPref = context.getSharedPreferences(context.packageName, Activity.MODE_PRIVATE)
    }

    // Read and Write a String
    fun read(key: String, defValue: String): String {

        return mSharedPref.getString(key, defValue)
    }

    fun write(key: String, value: String) {
      val prefsEditor = mSharedPref.edit()
         prefsEditor?.putString(key, value)
         prefsEditor?.commit()
    }
}