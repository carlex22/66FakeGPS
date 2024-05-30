package com.carlex.drive;


import android.content.Context;
import android.location.Location;

public class FakeLocationBuilder {

    private FusedLocationsProvider fusedLocationsProvider;
    private xLocationManager locationManager;

    public FakeLocationBuilder() {
        // Construtor padrão
    }

    public FakeLocationBuilder(Context context) {
        fusedLocationsProvider = new FusedLocationsProvider(context);
        xLocationManager.initTestProvider(context);
        locationManager = xLocationManager.getInstance(context);
    }

    public Location buildFakeLocation(Location location) {
        // Build fake location for Fused provider
        //Location fusedLocation = fusedLocationsProvider.build(latitude, longitude, accuracy, bearing, speed, altitude);
        // Set fake locations for GPS and Network providers
        //locationManager.setGpsProvider(latitude, longitude, bearing, speed, accuracy, altitude);
        //locationManager.setNetworkProvider(latitude, longitude, accuracy, bearing, altitude);
	
	Location fusedLocation = 
		fusedLocationsProvider.build(location);   
	locationManager.setGpsProvider(location);
        locationManager.setNetworkProvider(location);

        return fusedLocation;
    }

    public void spoofLocation(Location location) {
        // Spoof the location for all providers
        fusedLocationsProvider.spoof(location);
        locationManager.setGpsProvider(location);
        locationManager.setNetworkProvider(location);
    }
}






/*
public class FakeLocationBuilder {

    private FusedLocationsProvider fusedLocationsProvider;
    private xLocationManager locationManager;

    public FakeLocationBuilder() {
        // Construtor padrão
    }

    public FakeLocationBuilder(Context context) {
        fusedLocationsProvider = new FusedLocationsProvider(context);
        xLocationManager.initTestProvider(context);
        locationManager = xLocationManager.getInstance(context);
    }

    public Location buildFakeLocation(double latitude, double longitude, float accuracy, float bearing, float speed, float altitude) {
        // Build fake location for Fused provider
        Location fusedLocation = fusedLocationsProvider.build(latitude, longitude, accuracy, bearing, speed, altitude);

        // Set fake locations for GPS and Network providers
        locationManager.setGpsProvider(latitude, longitude, bearing, speed, accuracy, altitude);
        locationManager.setNetworkProvider(latitude, longitude, accuracy, bearing, altitude);

        return fusedLocation;
    }

    public void spoofLocation(Location location) {
        // Spoof the location for all providers
        fusedLocationsProvider.spoof(location);
        locationManager.setGpsProvider(
            location.getLatitude(),
            location.getLongitude(),
            (float) location.getBearing(),
            (float) location.getSpeed(),
            (float) location.getAccuracy(),
            (float) location.getAltitude()
        );
        locationManager.setNetworkProvider(
            location.getLatitude(),
            location.getLongitude(),
            (float) location.getAccuracy(),
            (float) location.getBearing(),
            (float) location.getAltitude()
        );
    }
}




/*package com.carlex.drive;

import android.content.Context;
import android.location.Location;

public class FakeLocationBuilder {

    private FusedLocationsProvider fusedLocationsProvider;
    private MockLocProvider mockLocProvider;


public FakeLocationBuilder() {
        // Construtor padrão
    }


    public FakeLocationBuilder(Context context) {
        fusedLocationsProvider = new FusedLocationsProvider(context);
        MockLocProvider.initTestProvider(context);
        mockLocProvider = new MockLocProvider();
    }


    public Location buildFakeLocation(double latitude, double longitude, float accuracy, float bearing, float speed, float altitude) {
        // Build fake location for Fused provider
        Location fusedLocation = fusedLocationsProvider.build(latitude, longitude, accuracy, bearing, speed, altitude);

        // Set fake locations for GPS and Network providers
        mockLocProvider.setGpsProvider(latitude, longitude, bearing, speed, accuracy, altitude);
        mockLocProvider.setNetworkProvider(latitude, longitude, accuracy, bearing, altitude);

        return fusedLocation;
    }

    public void spoofLocation(Location location) {
        // Spoof the location for all providers
        fusedLocationsProvider.spoof(location);
        mockLocProvider.setGpsProvider(
            location.getLatitude(),
            location.getLongitude(),
            (float) location.getBearing(),
            (float) location.getSpeed(),
            (float) location.getAccuracy(),
            (float) location.getAltitude()
        );
        mockLocProvider.setNetworkProvider(
            location.getLatitude(),
            location.getLongitude(),
            (float) location.getAccuracy(),
            (float) location.getBearing(),
            (float) location.getAltitude()
        );
    }
}
*/
