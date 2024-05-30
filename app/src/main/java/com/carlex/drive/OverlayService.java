package com.carlex.drive;

import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import android.view.Gravity;

public class OverlayService extends Service implements OnMapReadyCallback {

    private WindowManager windowManager;
    private View overlayView;
    private MapView mapView;
    private GoogleMap googleMap;
    private Marker currentLocationMarker;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private PolylineOptions polylineOptions;
    private WindowManager.LayoutParams params; 
    // Move a declaração para o escopo da classe

    @Override
    public void onCreate() {
        super.onCreate();
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        overlayView = LayoutInflater.from(this).inflate(R.layout.overlay_layout, null);

        mapView = overlayView.findViewById(R.id.mapView);
        mapView.onCreate(null);
        mapView.onResume();
        mapView.getMapAsync(this);

        params = new WindowManager.LayoutParams(
                (int) (windowManager.getDefaultDisplay().getWidth() * 0.3),
                (int) (windowManager.getDefaultDisplay().getHeight() * 0.3),
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT);
	params.gravity = Gravity.BOTTOM | Gravity.RIGHT;

        //params.gravity = 0; // No specific gravity, allows moving freely

        windowManager.addView(overlayView, params);

        setupLocationListener();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && intent.hasExtra("polyline")) {
            polylineOptions = intent.getParcelableExtra("polyline");
            if (googleMap != null && polylineOptions != null) {
                googleMap.addPolyline(polylineOptions);
            }
        }
        return START_STICKY;
    }


    /*
    private void setupLocationListener() {
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE); 
	locationListener = new LocationListener() {   
	    
	    @Override
	    public void onLocationChanged(Location location) {
		    if (currentLocationMarker != null) {      
			    currentLocationMarker.setPosition(new LatLng(location.getLatitude(), location.getLongitude()));
			    googleMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(location.getLatitude(), location.getLongitude())));
                }                                                               
	    }

            @Override                                                           
	    public void onStatusChanged(String provider, int status, Bundle
extras) {}
                                                                
	    @Override
            public void onProviderEnabled(String provider) {}
                                                                        
	    @Override                         
	    public void onProviderDisabled(String provider) {}

        };

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 100, 0, locationListener);
        }             
    }

*/


    // Dentro da classe OverlayService

private void setupLocationListener() {
    //locationManager = xLocationManager.getInstance(this);
    xLocationManager locationManager = xLocationManager.getInstance(this);

    locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            if (googleMap != null) {

		LatLng latLng = locationManager.getLatLngFromLocation();
                if (currentLocationMarker != null) {
                    currentLocationMarker.setPosition(latLng);
                } else {
                    currentLocationMarker = googleMap.addMarker(new MarkerOptions().position(latLng).title("Localização atual"));
                }
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15));
            }
        }

        //@Override
        //public void onStatusChanged(String provider, int status, Bundle extras) {}

        //@Override
        //public void onProviderEnabled(String provider) {}

        //@Override
        //public void onProviderDisabled(String provider) {}
    };


    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, locationListener);

}




    @Override
    public void onDestroy() {
        super.onDestroy();
        if (locationManager != null && locationListener != null) {
            locationManager.removeUpdates(locationListener);
        }
        if (overlayView != null && windowManager != null) {
            windowManager.removeView(overlayView);
        }
        if (mapView != null) {
            mapView.onDestroy();
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.googleMap = googleMap;
        googleMap.getUiSettings().setMapToolbarEnabled(false);

        // Mover o mapa para o canto inferior direito ao ser iniciado
        moveMapToBottomRight();

        // Permitir que o usuário mova o mapa para qualquer lugar na tela
        overlayView.setOnTouchListener(new View.OnTouchListener() {
            private int initialX;
            private int initialY;
            private float initialTouchX;
            private float initialTouchY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initialX = params.x;
                        initialY = params.y;
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        return true;
                    case MotionEvent.ACTION_MOVE:
                        params.x = initialX + (int) (event.getRawX() - initialTouchX);
                        params.y = initialY + (int) (event.getRawY() - initialTouchY);
                        windowManager.updateViewLayout(overlayView, params);
                        return true;
                }
                return false;
            }
        });
    }

    private void moveMapToBottomRight() {
        if (googleMap != null) {
            LatLng bottomRight = new LatLng(googleMap.getProjection().getVisibleRegion().latLngBounds.southwest.latitude,
                    googleMap.getProjection().getVisibleRegion().latLngBounds.northeast.longitude);
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(bottomRight));
        }
    }
}

