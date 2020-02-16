package bharat.group.maplocationupdate.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.location.LocationListener
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import bharat.group.maplocationupdate.R
import bharat.group.maplocationupdate.model.APIUtils
import bharat.group.maplocationupdate.service.BackgroundLocationService
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.GoogleMap.OnMapClickListener
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions


class MapActivity : FragmentActivity(), OnMapReadyCallback, LocationListener, OnMapClickListener ,  GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener{

    private var mMap: GoogleMap? = null
    var mGoogleApiClient: GoogleApiClient? = null
    var mLastLocation: Location? = null
    var mCurrLocationMarker: Marker? = null
    private var currentLatitude :Double = 0.0
    private var currentLongitude :Double = 0.0
    var change = true
    var Sender: LatLng? = null
    var Reciver:LatLng? = null

    private val TAG = "BackgroundLocation"
    private val TAG_LOCATION = "TAG_LOCATION"
    private var context: Context? = null
    private var stopService = false
    protected var mLocationSettingsRequest: LocationSettingsRequest? = null
    private var latitude = "0.0"
    private  var longitude:String? = "0.0"
    private var mFusedLocationClient: FusedLocationProviderClient? = null
    private var mSettingsClient: SettingsClient? = null
    private var mLocationCallback: LocationCallback? = null
    private var mLocationRequest: LocationRequest? = null
    private var mCurrentLocation: Location? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        init()
    }

    private fun init() {
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment!!.getMapAsync(this)

        startService(Intent(this, BackgroundLocationService::class.java))

    }



    override fun onStatusChanged(p0: String?, p1: Int, p2: Bundle?) {
    }

    override fun onProviderEnabled(p0: String?) {
    }

    override fun onProviderDisabled(p0: String?) {
    }

    override fun onMapReady(googleMap: GoogleMap?) {
        mMap = googleMap

        // Add a marker in Sydney, Australia, and move the camera.
        // Add a marker in Sydney, Australia, and move the camera.
//        val sydney:LatLng = LatLng((-34).toDouble(), 151.0)

        mMap!!.isMyLocationEnabled = true
        mMap!!.animateCamera(CameraUpdateFactory.zoomTo(18.5f))
        mMap!!.uiSettings.isZoomControlsEnabled = true

        //Initialize Google Play Services
        //Initialize Google Play Services
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
                == PackageManager.PERMISSION_GRANTED
            ) {
                buildGoogleApiClient()
                mMap!!.isMyLocationEnabled = true
                mMap!!.setOnMapClickListener(this)
                mMap!!.animateCamera(CameraUpdateFactory.zoomTo(18.5f))
                mMap!!.uiSettings.isZoomControlsEnabled = true
                //                mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
//                mMap.getUiSettings().setZoomControlsEnabled(true);
//                mMap.getUiSettings().setCompassEnabled(true);
//                mMap.getUiSettings().setMyLocationButtonEnabled(true);
//                mMap.getUiSettings().setAllGesturesEnabled(true);
//                mMap.setTrafficEnabled(true);
//                mMap.animateCamera(CameraUpdateFactory.zoomTo(30));
            }
        } else {
            buildGoogleApiClient()
            mMap!!.isMyLocationEnabled = true
        }
    }

    override fun onMapClick(p0: LatLng?) {

    }



    private fun connectGoogleClient() {
        val googleAPI = GoogleApiAvailability.getInstance()
        val resultCode = googleAPI.isGooglePlayServicesAvailable(this)
        if (resultCode == ConnectionResult.SUCCESS) {
            mGoogleApiClient!!.connect()
        }
    }


    @Synchronized
    protected fun buildGoogleApiClient() {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        mSettingsClient = LocationServices.getSettingsClient(this)
        mGoogleApiClient = GoogleApiClient.Builder(this)
            .addApi(LocationServices.API)
            .addConnectionCallbacks(this)
            .addOnConnectionFailedListener(this)
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
    override fun onLocationChanged(location: Location?) {

        mMap!!.clear()
        Toast.makeText(this, "location changed ", Toast.LENGTH_SHORT).show()
        mLastLocation = location
        if (mCurrLocationMarker != null) {
            mCurrLocationMarker!!.remove()
        }
        val latLng = LatLng(location!!.latitude, location.longitude)


        //move map camera
        //move map camera
        mMap!!.moveCamera(CameraUpdateFactory.newLatLng(latLng))
        mMap!!.animateCamera(CameraUpdateFactory.zoomTo(25f))

        //stop location updates
        //stop location updates
        /*if (mGoogleApiClient != null) {
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this)
        }*/

        currentLatitude = location.latitude
        currentLongitude = location.longitude
        Log.d(
            "locationchanguing",
            location.latitude.toString() + "," + location.longitude
        )
        Toast.makeText(
            this,
            location.latitude.toString() + "," + location.longitude,
            Toast.LENGTH_SHORT
        ).show()


        if (change) {
            change = false
            val DlatLng = LatLng(12.9767, 77.5713)
            mMap!!.addMarker(MarkerOptions().position(DlatLng))
                .setTitle("Destination:Majestic")
            drawRoute(
                currentLatitude, currentLongitude,
                DlatLng.latitude, DlatLng.longitude
            )
        }
    }

    private fun drawRoute(
        senderLatitude: Double,
        senderLongitude: Double,
        receiverLatitude: Double?,
        receiverLongitude: Double
    ) {
        if (receiverLatitude != null) {
            Sender = LatLng(senderLatitude, senderLongitude)
            Reciver = LatLng(receiverLatitude, receiverLongitude)
            val origin: LatLng = Sender as LatLng
            val dest: LatLng = Reciver as LatLng
            mMap!!.addMarker(MarkerOptions().position(dest))
            APIUtils().getPolyGonPoints(origin,dest,object : APIUtils.OnPolyLineListener{
                override fun onSuccess(list: List<LatLng>) {
                    drawPolyLines(list)
                }
            })
            mMap!!.moveCamera(CameraUpdateFactory.newLatLng(dest))
            mMap!!.animateCamera(CameraUpdateFactory.zoomTo(18f))
        }
    }

    private fun drawPolyLines(list: List<LatLng>) {
        val lineOptions: PolylineOptions? = PolylineOptions()
        lineOptions!!.addAll(list)
        lineOptions.width(10f)
        lineOptions.color(Color.RED)
        mMap!!.addPolyline(lineOptions)
    }

    private fun getUrl(origin: LatLng, dest: LatLng): String? { // Origin of route
        val str_origin = "origin=" + origin.latitude + "," + origin.longitude
        // Destination of route
        val str_dest = "destination=" + dest.latitude + "," + dest.longitude
        // Sensor enabled
        val sensor = "sensor=false"
        // Building the parameters to the web service
        val parameters = "$str_origin&$str_dest&$sensor"
        // Output format
        val output = "json"
        // Building the url to the web service
        return "https://maps.googleapis.com/maps/api/directions/$output?$parameters&key=AIzaSyD4DlnbbSz59_XzTRevFF7_TGUGyiN2Dj0"
    }


    override fun onConnected(bundle: Bundle?) {
        mLocationRequest = LocationRequest()
        mLocationRequest!!.interval = 40 * 1000.toLong()
        mLocationRequest!!.fastestInterval = 20 * 1000.toLong()
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
                    } catch (sie: IntentSender.SendIntentException) {
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

    @SuppressLint("MissingPermission")
    private fun requestLocationUpdate() {
        mFusedLocationClient!!.requestLocationUpdates(
            mLocationRequest,
            mLocationCallback,
            Looper.myLooper()
        )
    }


}
