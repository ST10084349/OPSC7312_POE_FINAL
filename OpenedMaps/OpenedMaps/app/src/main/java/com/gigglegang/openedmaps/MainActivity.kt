package com.gigglegang.openedmaps

//import com.mikirinkode.openstreetmapandroid.databinding.ActivityMainBinding
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Rect
import android.location.GpsStatus
import android.location.Location
import android.os.Bundle
import android.preference.PreferenceManager
import android.view.MotionEvent
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.res.ResourcesCompat
import com.gigglegang.openedmaps.databinding.ActivityMainBinding
import com.google.firebase.database.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.osmdroid.api.IMapController
import org.osmdroid.bonuspack.routing.OSRMRoadManager
import org.osmdroid.bonuspack.routing.Road
import org.osmdroid.bonuspack.routing.RoadManager
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapListener
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Overlay
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.IMyLocationConsumer
import org.osmdroid.views.overlay.mylocation.IMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import java.util.*
import kotlin.collections.HashMap
import kotlin.math.pow


class MainActivity : AppCompatActivity(), IMyLocationProvider, MapListener, GpsStatus.Listener {

    //variables
    private val LOCATION_REQUEST_CODE = 100
    private lateinit var mapView: MapView
    private lateinit var mapController: IMapController
    private lateinit var mMyLocationOverlay : MyLocationNewOverlay
    private lateinit var controller: IMapController
    private lateinit var showRoutesButton: Button
    private lateinit var pictureBirdButton: Button
    private lateinit var showNotesButton: Button
    private var alertDialog: AlertDialog? = null // ui to place data on

    var latitude: Double = 0.0
    var longitude: Double = 0.0

    val hotspotsLocations = listOf(
        Pair("Hluhluwe iMfolozi Park", GeoPoint(-28.219831,31.951865)),
        Pair("Umgeni River Bird Park", GeoPoint(-29.808167,31.017467)),
        Pair("Table Mountain", GeoPoint(-33.9625,18.4039)),
        Pair("Kruger National Park", GeoPoint(-24.9279,31.5249)),
        Pair("Robben Island", GeoPoint(-33.8054,18.3666)),
        Pair("Blyde River Canyon Nature Reserve", GeoPoint(-24.5570,30.8323)),
        Pair("Victoria & Alfred Waterfront", GeoPoint(-33.9036,18.4210)),
        Pair("Durban Beachfront", GeoPoint(-29.8526,31.0257)),
        Pair("Golden Gate Highlands National Park", GeoPoint(-28.5823, 28.5823)),
        Pair("Tsitsikamma National Park", GeoPoint(-33.9702,23.8866)),
        Pair("Sun City", GeoPoint(-29.7999,31.03758)),
        Pair("Drakensberg Mountains", GeoPoint(-29.7999,31.03758)),
        Pair("Addo Elephant National Park", GeoPoint(-33.569469,25.750720)),
        Pair("iSimangaliso Wetland Park", GeoPoint(-27.803262, 32.547096)),
        Pair("Wakkerstroom", GeoPoint(-27.355249,30.162390)),
        Pair("West Coast National Park", GeoPoint(-32.962216,18.115601)),
        Pair("Wilderness National Park", GeoPoint(-33.985609,22.635741)),
        Pair("Mkuze Game Reserve", GeoPoint(-27.595486, 32.037622)),
    )

    private val hotspotMarkers = mutableListOf<Marker>() //initialise an empty list for hotspot markers
    private val noteMap = HashMap<GeoPoint, String>()
    private lateinit var database: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //init binding
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        showRoutesButton = findViewById(R.id.showRoutesButton)
        pictureBirdButton = findViewById(R.id.pictureBirdButton)
        showNotesButton = findViewById(R.id.showNotesButton)

        // Initialize Firebase Database
        database = FirebaseDatabase.getInstance().reference

        Configuration.getInstance().load(
            applicationContext,
            getSharedPreferences(getString(R.string.app_name), MODE_PRIVATE)
        )
        mapView = binding.mapView
        mapView.setTileSource(TileSourceFactory.MAPNIK)
        mapView.mapCenter
        mapView.setMultiTouchControls(true)
        mapView.getLocalVisibleRect(Rect())

        mMyLocationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(this),mapView)
        controller = mapView.controller

        mMyLocationOverlay.enableMyLocation()
        mMyLocationOverlay.enableFollowLocation()
        mMyLocationOverlay.isDrawAccuracyEnabled = true

        //set the initial zoom level
        controller.setZoom(6.0)

        mapView.overlays.add(mMyLocationOverlay)
        setupMap()
        mapView.addMapListener(this)

        //check and request location permissions
        managePermissions()


        //create a custom overlay for the animated marker
        val animatedMarkerOverlay = object : Overlay(this) {
            override fun onSingleTapConfirmed(e: MotionEvent?, mapView: MapView?): Boolean {
                //calculate the latitude and longitude from the geopoint
                val geoPoint = mMyLocationOverlay.myLocation
                latitude = geoPoint.latitude
                longitude = geoPoint.longitude

                //create a custom dialog or info window to display the latitude and longitude
                val dialog = Dialog(this@MainActivity)
                dialog.setContentView(R.layout.custom)

                val latitudeTextView = dialog.findViewById<TextView>(R.id.latitudeTextView)
                val longitudeTextView = dialog.findViewById<TextView>(R.id.longitudeTextView)

                latitudeTextView.text = "Latitude: $latitude"
                longitudeTextView.text = "Longitude: $longitude"

                dialog.show()

                return true
            }
        }

        val waitMessage = AlertDialog.Builder(this)
        waitMessage.setTitle("User Entries")
        waitMessage.setMessage("Please wait for a few minutes for your live location to load." +
                "This will be displayed as a man/symbol on the map. " +
                "Thereafter click on any part of the map for your location to be displayed")

        //box buttons to close the entry
        waitMessage.setPositiveButton("OK") { dialog, _ ->
            dialog.dismiss() //closes the dialog
        }

        alertDialog = waitMessage.create()
        alertDialog?.show()

        // add the animatedmarkeroverlay to the map
        mapView.overlays.add(animatedMarkerOverlay)
        //get a reference to the "view hotspots" button
        val viewHotspotsButton = findViewById<Button>(R.id.viewHotspotsButton)

        //add on onclicklistener to the button
        viewHotspotsButton.setOnClickListener {
            val sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
            val isMetric = sharedPreferences.getBoolean("isMetric", true)
            val maxDistance = sharedPreferences.getInt("maxDistance", 50)
            //call a function to add hotspot markers when the button is clicked
            addHotspotMarkers(maxDistance, isMetric)
        }

        showRoutesButton.setOnClickListener {
            calculateAndDisplayRoutes()
        }


        pictureBirdButton.setOnClickListener {
            val intent = Intent(this, Camera::class.java)
            startActivity(intent)
        }

        showNotesButton.setOnClickListener {
            fetchEntriesFromFirebase()
        }
    }

    private fun calculateAndDisplayRoutes() {
        val startPoint = mMyLocationOverlay.myLocation

        if (startPoint == null)
        {
            Toast.makeText(this,"Location loading error", Toast.LENGTH_SHORT).show()
            return
        }

        for ((startLocationName, endPoint) in hotspotsLocations){
            GlobalScope.launch(Dispatchers.IO){
                val roadManager = OSRMRoadManager(this@MainActivity,"OBP_Tuto/1.0")
                var road: Road? = null
                var retryCount = 0

                while (road == null && retryCount < 3) {
                    road = try{
                        roadManager.getRoad(arrayListOf(startPoint, endPoint))
                    } catch (e: Exception) {
                        null
                    }
                    retryCount++
                }

                withContext(Dispatchers.Main) {
                    if (road != null && road.mStatus == Road.STATUS_OK) {
                        val roadOverlay = RoadManager.buildRoadOverlay(road)
                        mapView.overlays.add(roadOverlay)

                        //Display the route details in an AlertDialog
                        val routeDetails = "Start Location: Your Current Location\nEnd Location: $startLocationName\nDistance: ${road.mLength}"
                        showRouteDetailsDialog(routeDetails)

                        mapView.invalidate()
                    }else {
                        Toast.makeText(this@MainActivity,"Error when loading road - Status=${road?.mStatus ?: "unknown"}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    //show direction to hotspots
    private fun showRouteDetailsDialog(routeDetails: String) {
        runOnUiThread {
            val alertDialog = AlertDialog.Builder(this@MainActivity)
            alertDialog.setTitle("Route Details")
            alertDialog.setMessage(routeDetails)
            alertDialog.setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
            }
            alertDialog.create().show()
        }
    }

    //setting up the map
    private fun setupMap(){
        Configuration.getInstance().load(this,
            PreferenceManager.getDefaultSharedPreferences(this))
        //mapView = binding.mapView
        mapController = mapView.controller
        mapView.setMultiTouchControls(true)

        //init the start point
        val startPoint = GeoPoint(-29.8587, 31.0218)
        mapController.setCenter(startPoint)
        mapController.setZoom(12.0)

        //create marker for the start point (ic_location)
        val icLocationMarker = Marker(mapView)
        icLocationMarker.position = startPoint
        icLocationMarker.icon = ResourcesCompat.getDrawable(resources, R.drawable.ic_location, null)

        //add a click listener to the ic_location marker
        icLocationMarker.setOnMarkerClickListener{marker , mapView ->
            val latitude = marker.position.latitude
            val longitude = marker.position.longitude

            val dialog = Dialog(this@MainActivity)
            dialog.setContentView(R.layout.custom)

            val latitudeTextView = dialog.findViewById<TextView>(R.id.latitudeTextView)
            val longitudeTextView = dialog.findViewById<TextView>(R.id.longitudeTextView)

            latitudeTextView.text = "Latitude: $latitude"
            longitudeTextView.text = "Longitude: $longitude"

            dialog.show()

            true //Return true to indicate that the event is consumed
        }

        // Add the ic_location marker to the mapView
        mapView.overlays.add(icLocationMarker)
    }

    //add markers to map
    private fun addHotspotMarkers(maxDistance: Int, isMetric: Boolean){
        //clear any existing hotspot markers from the map
        mapView.overlays.removeAll(hotspotMarkers)

        for ((name, location) in hotspotsLocations){
            // Calculate the distance between user location and hotspot
            val distance = calculateDistance(latitude, longitude, location.latitude, location.longitude, isMetric)

            if (distance <= maxDistance) {
                val marker = Marker(mapView)
                marker.position = location
                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                marker.icon = ResourcesCompat.getDrawable(resources, R.drawable.ic_location, null)

                // create a custom dialog for displaying location name and adding a note
                marker.setOnMarkerClickListener { marker, mapView ->
                    val dialog = Dialog(this@MainActivity)
                    dialog.setContentView(R.layout.custom_marker_dialog)

                    val noteEditText = dialog.findViewById<EditText>(R.id.noteEditText)
                    val saveNoteButton = dialog.findViewById<Button>(R.id.saveNoteButton)

                    saveNoteButton.setOnClickListener {
                        val note = noteEditText.text.toString()
                        saveToFirebase(latitude, longitude, note)
                    }

                    dialog.show()
                    true
                }
                hotspotMarkers.add(marker) // Add the marker to the list
            }
        }
        //add the new hotspot markers to the map
        mapView.overlays.addAll(hotspotMarkers)
        mapView.invalidate() //refresh the map to display the new markers
    }

    // calculating distances to locations
    private fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double, isMetric: Boolean): Double {
        val earthRadius: Double
        if (isMetric) {
            // Earth radius in kilometers
            earthRadius = 6371.0
        } else {
            // Earth radius in miles
            earthRadius = 3958.8
        }

        // Convert latitude and longitude from degrees to radians
        val lat1Rad = Math.toRadians(lat1)
        val lon1Rad = Math.toRadians(lon1)
        val lat2Rad = Math.toRadians(lat2)
        val lon2Rad = Math.toRadians(lon2)

        // Calculate the change in coordinates
        val deltaLat = lat2Rad - lat1Rad
        val deltaLon = lon2Rad - lon1Rad

        // Haversine formula
        val a = Math.sin(deltaLat / 2).pow(2) + Math.cos(lat1Rad) * Math.cos(lat2Rad) *
                Math.sin(deltaLon / 2).pow(2)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))

        // Calculate the distance
        val distance = earthRadius * c // Distance in selected units (kilometers or miles)

        return distance
    }

    override fun onScroll(event: ScrollEvent?): Boolean {
        //handle map scroll event here
        return true
    }

    override fun onZoom(event: ZoomEvent?): Boolean {
        //handle map zoom event here
        return false
    }

    override fun onGpsStatusChanged(p0: Int) {
        //handle GPS status changes here
    }

    override fun startLocationProvider(myLocationConsumer: IMyLocationConsumer?): Boolean {
        //start location provider here
        return true
    }

    override fun stopLocationProvider() {
        //stop location provider here
    }

    override fun getLastKnownLocation(): Location {
        // get last known location here
        return Location("last_known_location")
    }

    override fun destroy() {
        //destroy resources here
    }

    //handle permissions
    private fun isLocationPermissionGranted(): Boolean
    {
        val fineLocation = ActivityCompat.checkSelfPermission(this,
            android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val coarseLocation = ActivityCompat.checkSelfPermission(this,
            android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        return fineLocation && coarseLocation
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_REQUEST_CODE)
        {
            if (grantResults.isNotEmpty())
            {
                for (result in grantResults)
                {
                    if (result == PackageManager.PERMISSION_GRANTED){
                        //handle permission granted
                        //you can re-initialize the map here if needed
                        //setupMap()
                    }
                    else{
                        //Toast.makeText(this,"Location permissions denied",Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun managePermissions()
    {
        val requestPermissions = mutableListOf<String>()

        //location permissions
        if (!isLocationPermissionGranted())
        {
            //if theses weren't granted
            requestPermissions.add(android.Manifest.permission.ACCESS_FINE_LOCATION)
            requestPermissions.add(android.Manifest.permission.ACCESS_COARSE_LOCATION)
        }

        if (requestPermissions.isNotEmpty())
        {
            ActivityCompat.requestPermissions(this,requestPermissions.toTypedArray(),LOCATION_REQUEST_CODE)
        }
    }

    //code to save user's notes to firebase
    private fun saveToFirebase(latitude: Double, longitude: Double ,userNotes: String) {
        //key value pair to pass into firebase
        val key = database.child("items").push().key
        if (key != null) {
            val task = TaskModel(latitude, longitude, userNotes)
            database.child("items").child(key).setValue(task)
                .addOnSuccessListener {
                    Toast.makeText(this, "Data saved to firebase", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to save to db ", Toast.LENGTH_SHORT).show()
                }
        }else
        {
            Toast.makeText(this, "A database error has occurred", Toast.LENGTH_SHORT).show()
        }
    } //save to firebase ends


    private fun fetchEntriesFromFirebase() {
        val query = database.child("items")

        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val entries: MutableList<TaskModel> = mutableListOf()
                for (entrySnapshot in snapshot.children) {
                    val entry = entrySnapshot.getValue(TaskModel::class.java)
                    entry?.let {
                        entries.add(it)
                    }
                }

                showEntriesAlert(entries)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MainActivity, "Failed to read entry", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun showEntriesAlert(entries: List<TaskModel>) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("User Entries")

        if (entries.isEmpty()) {
            builder.setMessage("No entries found")
        } else {
            val entriesText = buildEntriesText(entries)
            builder.setMessage(entriesText)
        }

        //box buttons to close the entry
        builder.setPositiveButton("OK") { dialog, _ ->
            dialog.dismiss() //closes the dialog
        }

        alertDialog = builder.create()
        alertDialog?.show()
    }

    private fun buildEntriesText(entries: List<TaskModel>): String {
        val stringBuilder = StringBuilder()
        entries.forEachIndexed { index, entry ->
            stringBuilder.append("Entry ${index + 1}:\n")
                .append("Notes: ${entry.userNotes}\n")
                .append("Latitude: ${entry.latitude}\n")
                .append("Longitude: ${entry.longitude}\n\n")
        }
        return stringBuilder.toString()
    }
}

data class TaskModel(
    var latitude: Double = 0.0,
    var longitude: Double = 0.0,
    var userNotes: String? = null,
)