package bharat.group.maplocationupdate.ui

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import bharat.group.maplocationupdate.R
import bharat.group.maplocationupdate.service.BackgroundLocationService
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment


class MapActivity : FragmentActivity(), OnMapReadyCallback {

    private var mMap: GoogleMap? = null
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

    override fun onMapReady(googleMap: GoogleMap?) {
        mMap = googleMap

        // Add a marker in Sydney, Australia, and move the camera.
        // Add a marker in Sydney, Australia, and move the camera.
//        val sydney:LatLng = LatLng((-34).toDouble(), 151.0)

        mMap!!.isMyLocationEnabled = true
        mMap!!.animateCamera(CameraUpdateFactory.zoomTo(18.5f))
        mMap!!.uiSettings.isZoomControlsEnabled = true
    }
}
