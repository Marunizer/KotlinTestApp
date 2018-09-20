package com.example.mende.kotlintestapp

import android.Manifest
import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.ResultReceiver
import android.provider.Settings
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.text.InputFilter
import android.text.InputType
import android.util.Log
import android.view.Gravity
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Toast
import com.example.mende.kotlintestapp.objects.LocationConstants
import com.example.mende.kotlintestapp.services.FetchLocationService
import com.example.mende.kotlintestapp.services.FetchLocationWithZipService
import com.example.mende.kotlintestapp.util.SharedPref
import com.example.mende.kotlintestapp.views.HomeActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

/**
 * Created by Marunizer
 * Starting point of application
 * purpose: Permission checks, generate location data
 *
 * -------------------------------------------------------------------------------------------------
 * NOTES:
 *  We are not requesting read access to external storage because permissions are grouped. therefore
 *  requesting write access to external storage covers both
 *
 *  reference: https://demonuts.com/kotlin-runtime-permissions/
 *
 * -------------------------------------------------------------------------------------------------
 * CONCERNS:
 * - If user has denied a zip code, they are given a reasoning, but not asked again
 *       For now, closing the application should be enough for them to get the hint (-:
 *
 * -------------------------------------------------------------------------------------------------
 * Notes from Original Noni:  (Could still be applicable, must review and move up if so)
 *  * Implement using WIFI (network calls) over gps when available, consumes less battery, better accuracy
 *      afterwards will need to need to include feature android.hardware.wifi in manifest
 *
 * */
class MainActivity : AppCompatActivity() {

    private val TAG = MainActivity::class.java.simpleName
    private val ZIP_LENGTH = 5
    private var lastLocation : Location? = null
    private var lastZipCode : String? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var resultReceiver: AddressResultReceiver
    private var locationServiceRequest = ""
    private val EMPTY = ""

    //We use this to update progress bar
    //TODO: Make progress bar much cooler
    private var addressRequested = false
    private val ADDRESS_REQUESTED_KEY = "address-request-pending"
    private val LOCATION_ADDRESS_KEY = "location-address"


    // Visible while the address is being fetched.
    private lateinit var progressBar: ProgressBar
    private lateinit var alert : AlertDialog


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        resultReceiver = AddressResultReceiver(Handler())

        progressBar = findViewById(R.id.progress_bar)

        // Set defaults, then update using values stored in the Bundle.
        addressRequested = false
        locationServiceRequest = ""

        updateValuesFromBundle(savedInstanceState)

        //Initiate our SharedPreferences singleton class
        SharedPref().init(applicationContext)
        checkRadius()

        setupPermissions()
    }

    private fun setupPermissions() {

        if (checkAndRequestPermissions()) {
            // carry on the normal flow, as the case of  permissions  granted.
            Handler().postDelayed({
                // This method will be executed once the timer is over

                gatherLocationData()

            }, SPLASH_TIME_OUT.toLong())
        }
    }

    //Initiate first steps in application
    @SuppressLint("MissingPermission") //Reason: Permissions are being handled
    private fun gatherLocationData(){

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        fusedLocationClient.lastLocation
                .addOnSuccessListener { location : Location? ->
                    // Got last known location. In some rare situations this can be null.
                    if (location != null) {
                        Log.d(TAG, "Location Granted: $location")

                        if (!Geocoder.isPresent()) {
                            Toast.makeText(this@MainActivity,
                                    "Cannot retrieve Location at this time",
                                    Toast.LENGTH_LONG).show()
                            return@addOnSuccessListener
                        }
                        lastLocation = location

                        SharedPref().write(SharedPref().LOCATION_CHOSEN, "true")
                        startLocationIntentService()
                        addressRequested = true
                    }
                    else{
                        if (zipCodeExists()) {
                            Log.d(TAG, "Location : NULL ... Sadface ... Luckily Zip-code info exists!...Proceed")
                            SharedPref().write(SharedPref().LOCATION_CHOSEN, "false")
                            nextActivtiy()
                        }
                        else{
                            Log.d(TAG, "Location : NULL ... Sadface ... Opening Dialog requesting zip code")
                            showZipCodeRequestDialog()
                        }
                    }
                }
    }

    private fun checkAndRequestPermissions(): Boolean {

        val writePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        val permissionLocation = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)

        val listPermissionsNeeded = ArrayList<String>()

        if (writePermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }
        if (permissionLocation != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }
        if (!listPermissionsNeeded.isEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toTypedArray(), REQUEST_ID_MULTIPLE_PERMISSIONS)
            return false
        }
        return true
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>,
                                            grantResults: IntArray) {
        Log.d(TAG, "Permission callback called-------")
        when (requestCode) {
            REQUEST_ID_MULTIPLE_PERMISSIONS -> {

                val perms = HashMap<String, Int>()
                // Initialize the map with both permissions
                perms[Manifest.permission.WRITE_EXTERNAL_STORAGE] = PackageManager.PERMISSION_GRANTED
                perms[Manifest.permission.ACCESS_FINE_LOCATION] = PackageManager.PERMISSION_GRANTED

                // Fill with actual results from user
                if (grantResults.isNotEmpty()) {
                    for (i in permissions.indices)
                        perms[permissions[i]] = grantResults[i]
                    // Check for both permissions
                    if (perms[Manifest.permission.WRITE_EXTERNAL_STORAGE] == PackageManager.PERMISSION_GRANTED
                            && perms[Manifest.permission.ACCESS_FINE_LOCATION] == PackageManager.PERMISSION_GRANTED) {

                        Log.d(TAG, "storage & location services permission granted")
                        gatherLocationData()
                    }
                    else if (perms[Manifest.permission.WRITE_EXTERNAL_STORAGE] == PackageManager.PERMISSION_GRANTED
                            && perms[Manifest.permission.ACCESS_FINE_LOCATION] == PackageManager.PERMISSION_DENIED) {

                        if (zipCodeExists()) {
                            Log.d(TAG, "yay, zip-code exists, proceed safely, can assume location data exists ")
                            SharedPref().write(SharedPref().LOCATION_CHOSEN, "false")
                            nextActivtiy()
                        } else {
                            Log.d(TAG, "No existing zip-code known: Request zip code from User")
                            showZipCodeRequestDialog()
                        }

                    } else {
                        Log.d(TAG, "Storage permissions are not granted ask again ")
                        //permission is denied (this is the first time, when "never ask again" is not checked) so ask again explaining the usage of permission
                        // shouldShowRequestPermissionRationale will return true

                        //show the dialog or snackbar saying its necessary and try again otherwise proceed with setup.
                        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                            showDialogOK("Storage Permissions are required for this app",
                                    DialogInterface.OnClickListener { dialog, which ->
                                        when (which) {
                                            DialogInterface.BUTTON_POSITIVE -> checkAndRequestPermissions()
                                            DialogInterface.BUTTON_NEGATIVE ->
                                                // proceed with logic by disabling the related features or quit the app.
                                                finish()
                                        }
                                    })
                        } else {
                            explain("You need to give some mandatory permissions to continue. Do you want to go to app settings?")
                                        //proceed with logic by disabling the related features or quit the app.
                        }//permission is denied (and never ask again is  checked)
                        //shouldShowRequestPermissionRationale will return false
                    }
                }
            }
        }
    }

    // Request zip-code form user suing a Dialog
    private fun showZipCodeRequestDialog() {
        // build alert dialog
        val dialogBuilder = AlertDialog.Builder(this)
        var editTextZip: EditText? = null

        // set message of alert dialog
        dialogBuilder.setMessage("Please provide Zip code :)")
                // if the dialog is cancelable -> auto closes app if no user cancels on using zip code
                .setCancelable(false)

                // positive button text
                .setPositiveButton("Submit") { dialog, id ->
                    Log.d(TAG, "zipcode entry: " + editTextZip?.text.toString().toInt())
                    // action logic moved to final dialog Builder to avoid only getting one chance
                }
                // negative button text and action
                .setNegativeButton("Cancel") { dialog, id ->
                    explain("Sorry, a zip code or location is required to use this application")
                }

        with(dialogBuilder)
        {
            // Add any  input field here
            editTextZip = EditText(context)
            editTextZip!!.hint = "zip code"
            editTextZip!!.inputType = InputType.TYPE_CLASS_NUMBER
            editTextZip!!.filters = arrayOf(InputFilter.LengthFilter(5))
            editTextZip!!.gravity = Gravity.CENTER
        }

        // create dialog box
        alert = dialogBuilder.create()
        // set title for alert dialog box
        alert.setTitle("No Location? That's Okay !")
        // set the editText for alert dialog box
        alert.setView(editTextZip)

        // set logic for positive button
        alert.setOnShowListener {
            alert.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                Log.d(TAG, "zip code entry: " + editTextZip?.text.toString().toInt())

                if ((editTextZip?.text.toString().length) == ZIP_LENGTH) {
                    lastZipCode = editTextZip?.text.toString()
                    SharedPref().write(SharedPref().LOCATION_CHOSEN, "false")
                    startZipCodeIntentService()
                    addressRequested = true
                    updateUIWidgets()
                }
                else{
                    Toast.makeText(this,"Zip code is not valid, Please try again", Toast.LENGTH_SHORT).show()
                }
            }
        }
        // show alert dialog
        alert.show()
    }

    private fun showDialogOK(message: String, okListener: DialogInterface.OnClickListener) {
        AlertDialog.Builder(this)
                .setMessage(message)
                .setPositiveButton("OK", okListener)
                .setNegativeButton("Cancel", okListener)
                .create()
                .show()
    }

    private fun explain(msg: String) {
        val dialog = android.support.v7.app.AlertDialog.Builder(this)
        dialog.setMessage(msg)
                .setPositiveButton("OK") { paramDialogInterface, paramInt ->
                    startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:com.example.mende.kotlintestapp")))
                }
        dialog.show()
    }

    // Check Shared Preferences if there currently exists a zip-code: Good for readability
    private fun zipCodeExists(): Boolean {
        val exists = SharedPref().read(SharedPref().ZIPCODE, EMPTY)
        return !exists.isNullOrBlank()
    }

    //If user has no strict radius in place, set radius to default + check if a radius is mismatched
    private fun checkRadius() {
        //TODO: Not sure if we want this !! for turning a String into a float
        val radiusKM  = SharedPref().read(SharedPref().RADIUS_KM, EMPTY)
        val radiusMiles  = SharedPref().read(SharedPref().RADIUS_MILES, EMPTY)

        if (radiusKM.isNullOrBlank() && radiusMiles.isNullOrBlank())
        {
            SharedPref().write(SharedPref().RADIUS_KM, SharedPref().DEFAULT_RADIUS_KM.toString())
            SharedPref().write(SharedPref().RADIUS_MILES, SharedPref().DEFAULT_RADIUS_MILES.toFloat().toString())
        }
        else if (radiusKM.isNullOrBlank() && !radiusMiles.isNullOrBlank()) {
            val newKM : Float = radiusMiles!!.toFloat() * SharedPref().MILES_TO_KILOMETERS.toFloat()
            SharedPref().write(SharedPref().RADIUS_KM, newKM.toString())
        }
        else if (!radiusKM.isNullOrBlank() && radiusMiles.isNullOrBlank()) {
            val newMiles : Float = radiusKM!!.toFloat() / SharedPref().MILES_TO_KILOMETERS.toFloat()
            SharedPref().write(SharedPref().RADIUS_MILES, newMiles.toString())
        }
    }

    private fun startLocationIntentService() {
        val intent = Intent(this, FetchLocationService::class.java).apply {
            putExtra(LocationConstants.RECEIVER, resultReceiver)
            putExtra(LocationConstants.LOCATION_DATA_EXTRA, lastLocation)
        }
        startService(intent)
    }

    private fun startZipCodeIntentService() {
        val intent = Intent(this, FetchLocationWithZipService::class.java).apply {
            putExtra(LocationConstants.RECEIVER, resultReceiver)
            putExtra(LocationConstants.ZIP_DATA_EXTRA, lastZipCode)
        }
        startService(intent)
    }

    private fun nextActivtiy(){

        if(this::alert.isInitialized)
            alert.dismiss()

        val i = Intent(this@MainActivity, HomeActivity::class.java)
        startActivity(i)
        finish()
    }

    //make sure if by chance, an alert is leaked made after activity is destroyed, get rid of it
    override fun onDestroy() {
        super.onDestroy()
        if(this::alert.isInitialized)
            alert.dismiss()
    }

    /**
     * Toggles the visibility of the progress bar. Enables or disables the Fetch Address button.
     */
    private fun updateUIWidgets() {
        if (addressRequested) {
            progressBar.visibility = ProgressBar.VISIBLE
        } else {
            progressBar.visibility = ProgressBar.GONE
        }
    }

    companion object {
        const val REQUEST_ID_MULTIPLE_PERMISSIONS = 1
        private const val SPLASH_TIME_OUT = 1000
    }

    /**
     * Receiver for data sent from FetchAddressIntentService.
     */
    private inner class AddressResultReceiver internal constructor(
            handler: Handler
    ) : ResultReceiver(handler) {

        /**
         * Receives data sent from FetchAddressIntentService and updates the UI in MainActivity.
         */
        override fun onReceiveResult(resultCode: Int, resultData: Bundle) {

            // Display the address string or an error message sent from the intent service.
            locationServiceRequest = resultData.getBoolean(LocationConstants.RESULT_DATA_KEY).toString()
           // displayAddressOutput()

            // Show a toast message if an address was found.
            if (resultCode == LocationConstants.SUCCESS_RESULT) {
                Toast.makeText(this@MainActivity,"address found", Toast.LENGTH_SHORT).show()
                nextActivtiy()
                updateUIWidgets()
            }
            else {
                Toast.makeText(this@MainActivity,"service failed _ Try again later", Toast.LENGTH_SHORT).show()
                finish()
            }

            // Reset - stop showing the progress bar.
            addressRequested = false
            updateUIWidgets()
        }
    }

    /**
     * Updates fields based on data stored in the bundle.
     */
    private fun updateValuesFromBundle(savedInstanceState: Bundle?) {
        savedInstanceState ?: return

        ADDRESS_REQUESTED_KEY.let {
            // Check savedInstanceState to see if the address was previously requested.
            if (savedInstanceState.keySet().contains(it)) {
                addressRequested = savedInstanceState.getBoolean(it)
            }
        }

        LOCATION_ADDRESS_KEY.let {
            // Check savedInstanceState to see if the location address string was previously found
            // and stored in the Bundle. If it was found, display the address string in the UI.
            if (savedInstanceState.keySet().contains(it)) {
                locationServiceRequest = savedInstanceState.getString(it)
            }
        }
    }

}
