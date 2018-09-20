package com.example.mende.kotlintestapp.services

import android.app.IntentService
import android.content.Intent
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Bundle
import android.os.ResultReceiver
import android.util.Log
import com.example.mende.kotlintestapp.objects.LocationConstants
import com.example.mende.kotlintestapp.util.SharedPref
import java.io.IOException
import java.util.*

/**
 * Created by Marunizer
 * Purpose of this Class is to attain address information depending on location information
 * references:https://github.com/googlesamples/android-play-location/tree/master/LocationAddress
 *            https://developer.android.com/training/location/display-address#kotlin
 *
 * -------------------------------------------------------------------------------------------------
 *  NOTES:
 *  geocoder is Synchronous and takes a while to work, which is why we run it in this service instead
 *
 *  The process of converting a geographic location to an address is called reverse geocoding.
 *
 *  Deviated from reference, not passing Strings found, instead, action taken is here,
 *                          and a boolean of pass/fail is sent instead
 *
 * -------------------------------------------------------------------------------------------------
 * CONCERNS:
 *  - What happens if this fails?? LOL - I guess just close the app due to error
 *    better solution: just stall and wait until works???? maybe re-try like 3 times if failed before quiting
 */

class FetchLocationService : IntentService("FETCH_LOCATION_SERVICE") {

    private val TAG = FetchLocationService::class.java.getSimpleName()

    // The receiver where results are forwarded from this service.
    private var receiver: ResultReceiver? = null


    /**
    * This service calls this method from the default worker thread with the intent that started
    * the service. When this method returns, the service automatically stops.
    */
    override fun onHandleIntent(intent: Intent?) {

        receiver = intent?.getParcelableExtra(LocationConstants.RECEIVER)

        // Check if receiver was properly registered.
        if (intent == null || receiver == null) {
            Log.wtf(TAG, "No receiver received. There is nowhere to send the results.")
            return
        }

        var errorMessage = ""

        // Get the location passed to this service through an extra.
        val location = intent.getParcelableExtra<Location>(
                LocationConstants.LOCATION_DATA_EXTRA)

        // Make sure that the location data was really sent over through an extra. If it wasn't,
        // send an error error message and return.
        if (location == null) {
            errorMessage = "no_location_data_provided"
            Log.wtf(TAG, errorMessage)
            deliverResultToReceiver(LocationConstants.FAILURE_RESULT, false)
            return
        }

        val geocoder = Geocoder(this, Locale.getDefault())

        var addresses: List<Address> = emptyList()

        try {
            addresses = geocoder.getFromLocation(
                    location.latitude,
                    location.longitude,
                    //we get just a single address -> use 1
                    1)
        } catch (ioException: IOException) {
            // Catch network or other I/O problems.
            errorMessage = "service_not_available"
            Log.e(TAG, errorMessage, ioException)
        } catch (illegalArgumentException: IllegalArgumentException) {
            // Catch invalid latitude or longitude values.
            errorMessage = "invalid_lat_long_used"
            Log.e(TAG, "$errorMessage. Latitude = $location.latitude , " +
                    "Longitude =  $location.longitude", illegalArgumentException)
        }

        // Handle case where no address was found.
        if (addresses.isEmpty()) {
            if (errorMessage.isEmpty()) {
                errorMessage = "no_address_found"
                Log.e(TAG, errorMessage)
            }
            deliverResultToReceiver(LocationConstants.FAILURE_RESULT, false)
        } else {
            val address = addresses[0]

            SharedPref().write(SharedPref().LOCATION_LAT, location.latitude.toString())
            SharedPref().write(SharedPref().LOCATION_LON, location.longitude.toString())
            SharedPref().write(SharedPref().ADDRESS, address.getAddressLine(0))
            SharedPref().write(SharedPref().STREET, address.thoroughfare)
            SharedPref().write(SharedPref().CITY, address.locality)
            SharedPref().write(SharedPref().STATE, address.adminArea)
            SharedPref().write(SharedPref().ZIPCODE, address.postalCode)

            Log.i(TAG, "address_found")
            deliverResultToReceiver(LocationConstants.SUCCESS_RESULT, true)
        }
    }

    private fun deliverResultToReceiver(resultCode: Int, result: Boolean) {
        val bundle = Bundle().apply { putBoolean(LocationConstants.RESULT_DATA_KEY, result) }
        receiver?.send(resultCode, bundle)
    }

}