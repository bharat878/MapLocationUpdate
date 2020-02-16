package bharat.group.maplocationupdate.service

import android.annotation.SuppressLint
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.IntentSender.SendIntentException
import android.location.Location
import android.location.LocationListener
import android.os.Bundle
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import bharat.group.maplocationupdate.model.APIUtils
import bharat.group.maplocationupdate.model.LocationData
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import java.util.*


class BackgroundLocationService : Service(), GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {

    private val TAG = "BackgroundLocation"
    private val TAG_LOCATION = "TAG_LOCATION"
    private var context: Context? = null
    private var stopService = false
    protected var mGoogleApiClient: GoogleApiClient? = null
    protected var mLocationSettingsRequest: LocationSettingsRequest? = null
    private var latitude = "0.0"
    private  var longitude:String? = "0.0"
    private var mFusedLocationClient: FusedLocationProviderClient? = null
    private var mSettingsClient: SettingsClient? = null
    private var mLocationCallback: LocationCallback? = null
    private var mLocationRequest: LocationRequest? = null
    private var mCurrentLocation: Location? = null

    private var mMap: GoogleMap? = null

    var mCurrLocationMarker: Marker? = null
    private var currentLatitude :Double = 0.0
    private var currentLongitude :Double = 0.0
    var change = true
    var Sender: LatLng? = null
    var Reciver:LatLng? = null

    var locationData:LocationData = LocationData()


    override fun onCreate() {
        super.onCreate()
        context = this
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val delay = 300000
        Timer().scheduleAtFixedRate(object : TimerTask() {
            override fun run() { //your method
                Log.d("finallocationretrofit", "$latitude -longitude-  $longitude")
            }
        }, 6000, delay.toLong()) //put here time 1000 milliseconds=1 second
        buildGoogleApiClient()
        return START_STICKY
    }

    override fun onDestroy() {
        Log.e(TAG, "Service Stopped")
        stopService = true
        if (mFusedLocationClient != null) {
            mFusedLocationClient!!.removeLocationUpdates(mLocationCallback)
            Log.e(TAG_LOCATION, "Location Update Callback Removed")
        }
        super.onDestroy()
    }

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onConnected(bundle: Bundle?) {
        mLocationRequest = LocationRequest()
        mLocationRequest!!.interval = 600 * 1000.toLong()
        mLocationRequest!!.fastestInterval = 300 * 1000.toLong()
        mLocationRequest!!.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        val builder = LocationSettingsRequest.Builder()
        builder.addLocationRequest(mLocationRequest!!)
        builder.setAlwaysShow(true)
        mLocationSettingsRequest = builder.build()

        mSettingsClient!!
            .checkLocationSettings(mLocationSettingsRequest)
            .addOnSuccessListener {
                Log.e(TAG_LOCATION, "GPS Success")
                requestLocationUpdate()
            }.addOnFailureListener { e ->
                val statusCode = (e as ApiException).statusCode
                when (statusCode) {
                    LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> try {
                        val REQUEST_CHECK_SETTINGS = 214
                        val rae = e as ResolvableApiException
                        rae.startResolutionForResult(
                            context as AppCompatActivity?,
                            REQUEST_CHECK_SETTINGS
                        )
                    } catch (sie: SendIntentException) {
                        Log.e(TAG_LOCATION, "Unable to execute request.")
                    }
                    LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> Log.e(
                        TAG_LOCATION,
                        "Location settings are inadequate, and cannot be fixed here. Fix in Settings."
                    )
                }
            }.addOnCanceledListener { Log.e(TAG_LOCATION, "checkLocationSettings -> onCanceled") }
    }

    override fun onConnectionSuspended(p0: Int) {
        connectGoogleClient()
    }

    override fun onConnectionFailed(p0: ConnectionResult) {
        buildGoogleApiClient()
    }


    override fun onLocationChanged(location: Location?) {
        Log.e(
            TAG_LOCATION,
            "Location Changed Latitude : " + location!!.latitude + "\tLongitude : " + location.getLongitude()
        )

        latitude = location.latitude.toString()
        longitude = location.longitude.toString()
        locationData.time = System.currentTimeMillis().toString()
        locationData.loc = arrayListOf(location.latitude,location.longitude)
        locationData.name = "Bharat"
        if (latitude.equals("0.0", ignoreCase = true) && longitude.equals(
                "0.0",
                ignoreCase = true
            )
        ) {
            requestLocationUpdate()
        } else {
            APIUtils().sendLocation(locationData,applicationContext)
            Log.e(TAG_LOCATION, "Latitude : " + location.latitude + "\tLongitude : " + location.getLongitude())
        }

    }

    override fun onStatusChanged(p0: String?, p1: Int, p2: Bundle?) {
    }

    override fun onProviderEnabled(p0: String?) {
    }

    override fun onProviderDisabled(p0: String?) {
    }


    private fun connectGoogleClient() {
        val googleAPI = GoogleApiAvailability.getInstance()
        val resultCode = googleAPI.isGooglePlayServicesAvailable(context)
        if (resultCode == ConnectionResult.SUCCESS) {
            mGoogleApiClient!!.connect()
        }
    }


    @Synchronized
    protected fun buildGoogleApiClient() {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(context!!)
        mSettingsClient = LocationServices.getSettingsClient(context!!)
        mGoogleApiClient = GoogleApiClient.Builder(context!!)
            .addConnectionCallbacks(this)
            .addOnConnectionFailedListener(this)
            .addApi(LocationServices.API)
            .build()
        connectGoogleClient()
        mLocationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                Log.e(TAG_LOCATION, "Location Received")
                mCurrentLocation = locationResult.lastLocation
                onLocationChanged(mCurrentLocation)
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun requestLocationUpdate() {
        mFusedLocationClient!!.requestLocationUpdates(
            mLocationRequest,
            mLocationCallback,
            Looper.myLooper()
        )
    }


}