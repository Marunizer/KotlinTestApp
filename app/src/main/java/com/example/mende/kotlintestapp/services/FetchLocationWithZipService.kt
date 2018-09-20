package com.example.mende.kotlintestapp.services

import android.app.IntentService
import android.content.Intent
import android.location.Address
import android.location.Geocoder
import android.os.Bundle
import android.os.ResultReceiver
import android.util.Log
import com.example.mende.kotlintestapp.objects.LocationConstants
import com.example.mende.kotlintestapp.util.SharedPref
import java.io.IOException

/**
 * Created by Marunizer
 * Purpose of this Class is to attain address information depending on zip code information
 * references:https://github.com/googlesamples/android-play-location/tree/master/LocationAddress
 *            https://developer.android.com/training/location/display-address#kotlin
 *            Original noni project - locationHelper Java Class
 *
 * -------------------------------------------------------------------------------------------------
 *  NOTES:
 *  geocoder is Synchronous and takes a while to work, which is why we run it in this service instead
 *
 *  The process of converting a geographic location to an address is called reverse geocoding,
 *                                          so I think this is normal geocoding.
 *
 *  Somewhat of a copy of FetchLocationService but with a different intent, could put them together
 *                  but that would make code messy and icky, too many checks, better to split
 *
 * -------------------------------------------------------------------------------------------------
 * CONCERNS:
 *  - What happens if this fails?? LOL - I guess just close the app due to error
 *    better solution: just stall and wait until works???? maybe re-try like 3 times if failed before quiting
 */

class FetchLocationWithZipService : IntentService("FETCH_LOCATION_SERVICE") {

    private val TAG = FetchLocationWithZipService::class.java.simpleName

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

        // Get the zip code passed to this service through an extra.
        val zipCode = intent.getStringExtra(LocationConstants.ZIP_DATA_EXTRA)

        // Make sure that the location data was really sent over through an extra. If it wasn't,
        // send an error error message and return.
        if (zipCode.isNullOrBlank()) {
            errorMessage = "no_zip_code_data_provided"
            Log.wtf(TAG, errorMessage)
            deliverResultToReceiver(LocationConstants.FAILURE_RESULT, false)
            return
        }

        val geocoder = Geocoder(this)

        var addresses: List<Address> = emptyList()

        try {
            addresses = geocoder.getFromLocationName(
                    zipCode,
                    //we get just a single address -> use 1
                    1)
        } catch (ioException: IOException) {
            // Catch network or other I/O problems.
            errorMessage = "_zip_code_service_not_available"
            Log.e(TAG, errorMessage, ioException)
        } catch (illegalArgumentException: IllegalArgumentException) {
            // Catch invalid latitude or longitude values.
            errorMessage = "invalid_zip_used"
            Log.e(TAG, "$errorMessage. Zip = $zipCode.zip , " +
                    "Zip code =  $zipCode.longitude", illegalArgumentException)
        }

        // Handle case where no address was found.
        if (addresses.isEmpty()) {
            if (errorMessage.isEmpty()) {
                errorMessage = "no_address_found_using_zip"
                Log.e(TAG, errorMessage)
            }
            deliverResultToReceiver(LocationConstants.FAILURE_RESULT, false)
        } else {
            val address = addresses[0]

            SharedPref().write(SharedPref().LOCATION_LAT, address.latitude.toString())
            SharedPref().write(SharedPref().LOCATION_LON, address.longitude.toString())
            SharedPref().write(SharedPref().ADDRESS, address.getAddressLine(0))
            SharedPref().write(SharedPref().CITY, address.locality)
            SharedPref().write(SharedPref().STATE, address.adminArea)
            SharedPref().write(SharedPref().ZIPCODE, zipCode)

            //street is'nt always expected with zipcode:
            //TODO: this is a lot of effort, but can make a new geocoder with retrieved lat/long, return street address attained
            if (!address.thoroughfare.isNullOrBlank())
                SharedPref().write(SharedPref().STREET, address.thoroughfare)

            Log.i(TAG, "address_found_using_zip: $zipCode")
            deliverResultToReceiver(LocationConstants.SUCCESS_RESULT, true)
        }
    }

    private fun deliverResultToReceiver(resultCode: Int, result: Boolean) {
        val bundle = Bundle().apply { putBoolean(LocationConstants.RESULT_DATA_KEY, result) }
        receiver?.send(resultCode, bundle)
    }

}