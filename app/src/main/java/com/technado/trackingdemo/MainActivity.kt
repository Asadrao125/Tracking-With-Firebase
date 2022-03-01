package com.technado.trackingdemo

import android.annotation.SuppressLint
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.firebase.FirebaseApp
import com.google.firebase.database.*

class MainActivity : AppCompatActivity(), OnMapReadyCallback {
    var mMap: GoogleMap? = null
    var lat = 0.0
    var lng = 0.0
    var carMarker: Marker? = null
    var gpsTracker: GPSTracker? = null
    var dbRef: DatabaseReference? = null

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        FirebaseApp.initializeApp(this)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment!!.getMapAsync(this)

        dbRef = FirebaseDatabase.getInstance().getReference("Lat_Lng")
        gpsTracker = GPSTracker(this)
        lat = gpsTracker!!.getLatitude()
        lng = gpsTracker!!.getLongitude()
        dbRef!!.setValue(LocationModel(lat, lng))

        dbRef!!.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val lat = dataSnapshot.child("latitude").value.toString()
                val lng = dataSnapshot.child("longitude").value.toString()
                mMap!!.clear()
                val markerOptions = MarkerOptions()
                markerOptions.icon(BitmapDescriptorFactory.fromResource(R.mipmap.ic_car))
                markerOptions.position(LatLng(lat.toDouble(), lng.toDouble()))
                markerOptions.title("My Location")
                carMarker = mMap!!.addMarker(markerOptions)
                mMap!!.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        LatLng(
                            lat.toDouble(),
                            lng.toDouble()
                        ), 16.0f
                    )
                )
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })

        val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        val locationListener = LocationListener { location ->
            dbRef!!.setValue(
                LocationModel(
                    location.latitude,
                    location.longitude
                )
            )
        }
        locationManager.requestLocationUpdates(
            LocationManager.GPS_PROVIDER,
            0,
            1f,
            locationListener
        )
    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap!!.isMyLocationEnabled = true
        mMap!!.setMapStyle(MapStyleOptions(MapStyleJSON.MAP_STYLE_JSON))
        val sydney = LatLng(lat, lng)
        mMap!!.addMarker(MarkerOptions().position(sydney).title("My Location"))
        mMap!!.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(lat, lng), 5.0f))
        mMap!!.uiSettings.isZoomControlsEnabled = true
        mMap!!.setOnMarkerClickListener { marker ->
            mMap!!.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(lat, lng), 15.0f))
            marker.showInfoWindow()
            true
        }
        mMap!!.setOnMapClickListener {
            mMap!!.animateCamera(
                CameraUpdateFactory.newLatLngZoom(
                    LatLng(lat, lng),
                    10.0f
                )
            )
        }
    }
}