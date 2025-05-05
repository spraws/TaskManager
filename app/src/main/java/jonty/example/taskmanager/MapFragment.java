package jonty.example.taskmanager;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.tilesource.OnlineTileSourceBase;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.util.MapTileIndex;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.MapEventsOverlay;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.events.MapEventsReceiver;

import java.util.HashMap;
import java.util.Map;

public class MapFragment extends Fragment {

    MapView mapView;
    ActivityResultLauncher<String[]> locationPermissionRequest;

    Map<String, StoredLocation> storedLocations = new HashMap<>();

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    ListenerRegistration listenerRegistration;
    CollectionReference collection = db.collection("locations");

    long minimumTimeBetweenUpdates = 50000;
    float minimumDistanceBetweenUpdates = 0.05f;
    LocationListener locationListener;
    LocationManager locationManager;
    NotificationManagerCompat notificationManager;
    boolean showNotification = true;


    double triggerDistance = 100;

    static final int NOTIFICATION_INTENT_CODE = 0;
    static final String NOTIFICATION_KEY = "MyLocation";

//
//    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
//    String uid = currentUser.getUid();
    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Configuration.getInstance().setUserAgentValue(requireContext().getPackageName());
        return inflater.inflate(R.layout.activity_omap, container, false);

    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        mapView = view.findViewById(R.id.map);

        mapView.setTileSource(new OnlineTileSourceBase(
                "Carto Dark", 1, 20, 256, ".png",
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

        if (currentUser != null) {
            String uid = currentUser.getUid();
            // Proceed with using the UID
        } else {
            // Handle the case where no user is signed in
            Log.e("Auth", "No current user found.");
        }

        MapEventsOverlay mapEventsOverlay = new MapEventsOverlay(new MapEventsReceiver() {
            @Override
            public boolean singleTapConfirmedHelper(GeoPoint p) {
                return false;
            }

            @Override
            public boolean longPressHelper(GeoPoint p) {
                addNewLocationDialogue(p);
                return true;
            }
        });

        mapView.getOverlays().add(mapEventsOverlay);
        mapView.setMultiTouchControls(true);

        locationPermissionRequest = registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(),
                result -> {
                    boolean fineLocationAllowed = Boolean.TRUE.equals(result.get(Manifest.permission.ACCESS_FINE_LOCATION));
                    if (fineLocationAllowed) {
                        updateLocation();
                    } else {
                        Log.d("MyLocation", "Permission denied");
                    }
                });

        IMapController mapController = mapView.getController();
        mapController.setZoom(6.0);
        GeoPoint startPoint = new GeoPoint(54.5, -3);
        mapController.setCenter(startPoint);

//        StoredLocation location1 = new StoredLocation("Home", 52.37496005586914, -2.30763083411824);
//        StoredLocation location2 = new StoredLocation("Work", 53.37496005586914, -2.30763083411824);
//
//        storedLocations.put(location1.locationName, location1);
//
//        WriteBatch batch = db.batch();
//        batch.set(collection.document(location1.locationName), location1);
//        batch.set(collection.document(location2.locationName), location2);
//        batch.commit().addOnSuccessListener(aVoid -> {
//            Log.d("MyLocation", "Locations added to Firestore");
//        }).addOnFailureListener(e -> {
//            Log.d("MyLocation", "Error: " + e.getMessage());
//        });

        notificationManager = NotificationManagerCompat.from(requireContext());
        createNotificationChannel();
    }

    private void createNotificationChannel() {
        String channelName = "MyLocationChannel";
        int importance = NotificationManager.IMPORTANCE_DEFAULT;
        NotificationChannel channel = new NotificationChannel(NOTIFICATION_KEY, channelName, importance);
        channel.setDescription("MyLocation updates");
        notificationManager.createNotificationChannel(channel);
    }

    private Notification createNotification(StoredLocation storedLocation, double distance) {
        Intent intent = requireActivity().getIntent();
        intent.putExtra(NOTIFICATION_KEY, storedLocation.locationName);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                requireContext(),
                NOTIFICATION_INTENT_CODE,
                intent,
                PendingIntent.FLAG_IMMUTABLE
        );

        return new NotificationCompat.Builder(requireContext(), NOTIFICATION_KEY)
                .setSmallIcon(R.drawable.map_pin_white)
                .setContentTitle("MyLocation update: " + storedLocation.locationName)
                .setContentText("You are " + ((int) distance) + " metres from " + storedLocation.locationName)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true)
                .build();
    }

    private void showNotificationDialog(StoredLocation storedLocation) {
        if(showNotification == false) {
            new AlertDialog.Builder(requireContext())
                    .setTitle(storedLocation.locationName)
                    .setMessage("You are close to " + storedLocation.locationName +
                            ". Do you want to receive future notifications?")
                    .setPositiveButton("Yes", (dialog, i) -> {
                        storedLocation.notificationActive = false;
                        storedLocation.notificationsRequired = true;
                        collection.document(storedLocation.locationName).set(storedLocation);
                        showNotification = true;
                    })
                    .setNegativeButton("No", (dialog, i) -> {
                        storedLocation.notificationActive = false;
                        storedLocation.notificationsRequired = false;
                        collection.document(storedLocation.locationName).set(storedLocation);
                        showNotification = false;
                    })
                    .create().show();

        }

    }

    @SuppressLint("MissingPermission")
    public void updateLocation() {
        locationManager = (LocationManager) requireContext().getSystemService(Context.LOCATION_SERVICE);

        locationListener = location -> {
            GeoPoint currentLocation = new GeoPoint(location.getLatitude(), location.getLongitude());

            for (StoredLocation storedLocation : storedLocations.values()) {
                GeoPoint geoPoint = new GeoPoint(storedLocation.latitude, storedLocation.longitude);
                double distance = currentLocation.distanceToAsDouble(geoPoint);

                if (distance < triggerDistance && !storedLocation.notificationActive && storedLocation.notificationsRequired) {
                    int notificationID = storedLocation.locationName.hashCode();
                    Notification notification = createNotification(storedLocation, distance);
                    notificationManager.notify(notificationID, notification);
                    storedLocation.notificationActive = true;
                    Toast.makeText(requireContext(), "You are close to " + storedLocation.locationName, Toast.LENGTH_SHORT).show();

                } else {
                    storedLocation.notificationActive = false;
                }

            }

            Marker currentLocationMarker = new Marker(mapView);
            currentLocationMarker.setPosition(currentLocation);
            Drawable markerIcon = ResourcesCompat.getDrawable(getResources(), R.drawable.map_pin, null);
            if (markerIcon != null) {
                DrawableCompat.setTint(markerIcon, Color.RED);
                currentLocationMarker.setIcon(markerIcon);
            }
            currentLocationMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            mapView.getOverlays().add(currentLocationMarker);
            mapView.getController().setCenter(currentLocation);
            mapView.invalidate();
        };

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

    private void addNewLocationDialogue(GeoPoint geoPoint) {
        EditText locationNameInput = new EditText(requireContext());
        new AlertDialog.Builder(requireContext())
                .setTitle("Add New Location")
                .setView(locationNameInput)
                .setPositiveButton("OK", (dialog, which) -> {
                    String locationName = locationNameInput.getText().toString();
                    if (!locationName.isEmpty()) {
                        StoredLocation newLocation = new StoredLocation(locationName, geoPoint.getLatitude(), geoPoint.getLongitude());
                        storedLocations.put(locationName, newLocation);
                        db.collection("users")
                                .document(currentUser.getUid())
                                .collection("locations")
                                .document(locationName)
                                .set(newLocation);
                    } else {
                        Toast.makeText(requireContext(), "Enter a location name", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.cancel())
                .create()
                .show();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
        locationPermissionRequest.launch(new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.POST_NOTIFICATIONS
        });
        CollectionReference collectionReference = db.collection("users").document(currentUser.getUid()).collection("locations");
        listenerRegistration = collectionReference.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                Log.d("MyLocation", "changed");

                for(QueryDocumentSnapshot document : value) {
                    StoredLocation storedLocation = document.toObject(StoredLocation.class);
                    storedLocations.put(storedLocation.locationName, storedLocation);

                    if (storedLocation.notificationsRequired) {
                        showNotificationDialog(storedLocation);
                    }
                    for(StoredLocation location : storedLocations.values()) {
                        GeoPoint geoPoint = new GeoPoint(location.latitude, location.longitude);
                        Marker marker = new Marker(mapView);
                        marker.setPosition(geoPoint);
                        // Set drawable icon
                        Drawable markerIcon = ResourcesCompat.getDrawable(getResources(), R.drawable.map_pin, null);
                        if (markerIcon != null) {
                            DrawableCompat.setTint(markerIcon, Color.RED); // Optional: tint the drawable
                            marker.setIcon(markerIcon);
                        }

                        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                        mapView.getOverlays().add(marker);
                    }


                }

                if (error != null) {
                    Log.w("MyLocation", "Listen failed.", error);
                }
            }

        });
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
        if (locationManager != null && locationListener != null) {
            locationManager.removeUpdates(locationListener);
        }
        listenerRegistration.remove();
    }
}
