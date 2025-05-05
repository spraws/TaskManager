package jonty.example.taskmanager;
import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.Toast;
import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.Insets;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.WriteBatch;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.events.MapEventsReceiver;
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.util.MapTileIndex;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;

import java.util.HashMap;
import java.util.Map;

public class OMapActivity extends AppCompatActivity {

    MapView mapView;
    ActivityResultLauncher<String[]> locationPermissionRequest;

    //Locations
    Map<String, StoredLocation> storedLocations =
            new HashMap<String, StoredLocation>();
    //Locations end-------------------------

    //Firestore
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    ListenerRegistration listenerRegistration;

    CollectionReference collection = db.collection("locations");

    //Firestore end-------------------------

    //Location updates
    long minimumTimeBetweenUpdates = 50000; // 10 seconds (in milliseconds)
    float minimumDistanceBetweenUpdates = 0.05f; // 0.5 meters
    LocationListener locationListener;
    LocationManager locationManager;

    NotificationManagerCompat notificationManager;


    double triggerDistance = 10; // 100 meters

    //Location update end-------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Configuration.getInstance().setUserAgentValue(getPackageName());
        setContentView(R.layout.activity_omap);
        mapView = findViewById(R.id.map);
        //map tile source
        mapView.setTileSource(new OnlineTileSourceBase(
                "Carto Dark",
                1, 20, 256, ".png",
                new String[]{"https://basemaps.cartocdn.com/dark_all/"}
        ) {
            @Override
            public String getTileURLString(long pMapTileIndex) {
                return getBaseUrl()
                        + MapTileIndex.getZoom(pMapTileIndex) + "/"
                        + MapTileIndex.getX(pMapTileIndex) + "/"
                        + MapTileIndex.getY(pMapTileIndex)
                        + mImageFilenameEnding;
            }
        });

        //Map interaction
        MapEventsOverlay mapEventsOverlay = new MapEventsOverlay(new MapEventsReceiver() {
            @Override
            public boolean singleTapConfirmedHelper(GeoPoint p) {
                return false;
            }

            @Override
            public boolean longPressHelper(GeoPoint p) {
                //Add a new location
                addNewLocationDialogue(p);
                return true;
            }
        });
        mapView.getOverlays().add(mapEventsOverlay);

        //zooming functionality
        mapView.setMultiTouchControls(true);


        //Request location permission
        locationPermissionRequest = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                new ActivityResultCallback<Map<String, Boolean>>() {
                    @Override
                    public void onActivityResult(Map<String, Boolean> o) {
//Check to see if the permission granted is the same
//as requested – ACCESS_FINE_LOCATION
                        boolean fineLocationAllowed =
                                o.get(Manifest.permission.ACCESS_FINE_LOCATION);
                        if (fineLocationAllowed) {
                            Log.d("MyLocation", "Permission granted");
// ADD - call the updateLocation method
                            updateLocation();
                        } else {
                            Log.d("MyLocation", "Permission requesting");
                        }
                    }
                });
        //Retrieve a Map Controller from the MapView
        IMapController mapController = mapView.getController();
        mapController.setZoom(6.0);

//        Location location = new Location(LocationManager.GPS_PROVIDER);
        GeoPoint startPoint = new GeoPoint(54.5, -3);
        mapController.setCenter(startPoint);


        StoredLocation location1 = new StoredLocation("Home", 52.37496005586914, -2.30763083411824);
        StoredLocation location2 = new StoredLocation("Work", 53.37496005586914, -2.30763083411824);

        storedLocations.put(location1.locationName, location1);

        DocumentReference documentReference = collection.document(location1.locationName);
        DocumentReference documentReference2 = collection.document(location2.locationName);

        WriteBatch batch = db.batch();
        batch.set(documentReference, location1);
        batch.set(documentReference2, location2);
        batch.commit().addOnSuccessListener(aVoid -> {
            Log.d("MyLocation", "Locations added to Firestore");
        }).addOnFailureListener(e -> {
            Log.d("MyLocation", "Error adding locations to Firestore: " + e.getMessage());
        });

        //Notifications
        notificationManager = NotificationManagerCompat.from(getApplicationContext());
        createNotificationChannel();



    }

    static final int NOTIFICATION_INTENT_CODE = 0;
    private void createNotificationChannel() {
        String channel_name = "MyLocationChannel";
        int channel_importance = NotificationManager.IMPORTANCE_DEFAULT;
        NotificationChannel channel;
        channel = new NotificationChannel(NOTIFICATION_KEY, channel_name,channel_importance);
        channel.setDescription("MyLocation updates");
        notificationManager.createNotificationChannel(channel);
    }

    static final String NOTIFICATION_KEY = "MyLocation";

    private Notification createNotification(StoredLocation storedLocation, double
            distance) {

        Intent intent = getIntent();
        intent.putExtra(NOTIFICATION_KEY, storedLocation.locationName );
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                NOTIFICATION_INTENT_CODE,
                intent,
                PendingIntent.FLAG_IMMUTABLE);

        Notification notification;
        notification = new NotificationCompat.Builder(this, NOTIFICATION_KEY)
                .setSmallIcon(R.drawable.map_pin_white)
                .setContentTitle("MyLocation update: " + storedLocation.locationName)
                .setContentText("You are " + ((int) distance) + " metres from " + storedLocation.locationName)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build();
        return notification;
    }


    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        String locationName = intent.getStringExtra(NOTIFICATION_KEY);

        for (StoredLocation storedLocation : storedLocations.values()) {
            if (storedLocation.locationName.equals(locationName)) {
                showNotificationDialog(storedLocation);
                return;
            }
        }
    }

    private void showNotificationDialog(StoredLocation storedLocation) {



        new AlertDialog.Builder(this)
                .setTitle(storedLocation.locationName)
                .setMessage("You are close to " + storedLocation.locationName +
                        ". Do you want to receive future notifications for this location?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        storedLocation.notificationActive = false;
                        storedLocation.notificationsRequired = true;
                        db.collection("locations").document(storedLocation.locationName).set(storedLocation);

                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        storedLocation.notificationActive = false;
                        storedLocation.notificationsRequired = false;
                        //push to firestore
                        db.collection("locations").document(storedLocation.locationName).set(storedLocation);

                    }
                })
                .create()
                .show();

    }

    //Update the location
    @SuppressLint("MissingPermission")
    public void updateLocation() {
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

                GeoPoint currentLocation = new GeoPoint(
                        location.getLatitude(),
                        location.getLongitude());

                for (StoredLocation storedLocation : storedLocations.values()) {

                    GeoPoint geoPoint = new
                            GeoPoint(storedLocation.latitude, storedLocation.longitude);
                    double distance = currentLocation.distanceToAsDouble(geoPoint);
                    if (distance < triggerDistance) {
                        // Create a notification
                        if (!storedLocation.notificationActive && storedLocation.notificationsRequired) {

                            int notificationID = storedLocation.locationName.hashCode();
                            Notification notification = createNotification(storedLocation, distance);
                            notificationManager.notify(notificationID, notification);

                            storedLocation.notificationActive = true;
                        }
                    } else {
                        storedLocation.notificationActive = false;
                    }

                }


                // a less ugly marker
                Marker currentLocationMarker = new Marker(mapView);
                currentLocationMarker.setPosition(currentLocation);
                //use a custom marker icon as vector is less pixelated
                Drawable markerIcon = ResourcesCompat.getDrawable(
                        getResources(),
                        R.drawable.map_pin,
                        null);
                if (markerIcon != null) {
                    DrawableCompat.setTint(markerIcon, Color.RED); // makes it red
                    currentLocationMarker.setIcon(markerIcon);
                }

                mapView.getOverlays().

                        add(currentLocationMarker);
                // Ensures it points to the actual location, not off-center
                currentLocationMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);


                mapView.getController().

                        setCenter(currentLocation);
                mapView.invalidate(); // redraw map
            }
        }

        ;

        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    minimumTimeBetweenUpdates,
                    minimumDistanceBetweenUpdates,
                    locationListener);
        } else {
            Log.d("MyLocation", "GPS Provider not enabled");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
        //Request location permission
        locationPermissionRequest.launch(new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.POST_NOTIFICATIONS
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
        locationManager.removeUpdates(locationListener);
    }

    //press dialog
    private void addNewLocationDialogue(GeoPoint geoPoint) {
        EditText locationNameInput = new EditText(this);

        new AlertDialog.Builder(this)
                .setTitle("Add New Location")
                .setView(locationNameInput)
                .setPositiveButton("OK", (dialog, which) -> {
                    String locationName = locationNameInput.getText().toString();
                    if (!locationName.isEmpty()) {
                        StoredLocation newLocation = new StoredLocation(locationName, geoPoint.getLatitude(), geoPoint.getLongitude());
                        storedLocations.put(locationName, newLocation);
                        DocumentReference documentReference = collection.document(locationName);
                        documentReference.set(newLocation).addOnSuccessListener(aVoid -> {
                            Log.d("MyLocation", "Location added to Firestore");
                        }).addOnFailureListener(e -> {
                            Log.d("MyLocation", "Error adding location to Firestore: " + e.getMessage());
                        });
                    } else {
                        Toast.makeText(this, "Please enter a location name", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", (dialog, which) -> {
                    dialog.cancel();
                })
                .create()
                .show();
    }

    //snapshot listener


    //Create a notification channel



}

