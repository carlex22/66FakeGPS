package com.carlex.drive.gnssData;

import com.carlex.drive.R;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
import android.os.Handler;
import android.widget.ScrollView;

public class Main extends AppCompatActivity {
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private FusedLocationProviderClient fusedLocationClient;
    private TextView textView;
    private Handler handler = new Handler();
    private Runnable runnable;
    private boolean isUpdating = true;

    private ScrollView scrollView;
 



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gnssmain);

	scrollView = findViewById(R.id.scrollView);


        textView = findViewById(R.id.textView);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
	    startUpdatingData();
        }
    }


private void startUpdatingData() {
        runnable = new Runnable() {
            @Override
            public void run() {
                if (isUpdating) {
		    getLastLocation();
                    handler.postDelayed(this, 1000); // Executa novamente após 1 segundo
                }
            }
        };
        handler.post(runnable);
    }


private void stopUpdatingData() {
        isUpdating = false;
        handler.removeCallbacks(runnable);
    }

    private void getLastLocation() {
        fusedLocationClient.getLastLocation()
            .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null) {
                        executeGnssLogic(location);
                    }
                }
            });
    }

    private void executeGnssLogic(Location location) {
        Position position = new Position(location.getLatitude(), location.getLongitude());
        double altitude = location.getAltitude();
        double speed = location.getSpeed() * 1.94384; // Convert m/s to knots
        double heading = location.getBearing();

        // Geração das mensagens NMEA
        GnssMessages gnssMessages = new GnssMessages(position, altitude, speed, heading);
        String[] nmeaSentences = gnssMessages.generateNmeaMessages();



        // Armazenando as mensagens NMEA na classe GnssDataStorage
        for (String sentence : nmeaSentences) {
            GnssDataStorage.addNmeaMessage(sentence);
        }

        // Processamento das mensagens NMEA para obter o status GNSS
        NmeaParser parser = new NmeaParser();
        for (String sentence : nmeaSentences) {
            parser.parseNmeaSentence(sentence);
        }

        // Armazenando os dados GNSS na classe GnssDataStorage
        GnssDataStorage.setLatitude(position.convertLatitudeToDegMin());
        GnssDataStorage.setLongitude(position.convertLongitudeToDegMin());
        GnssDataStorage.setAltitude(altitude);
        GnssDataStorage.setHdop(parser.getHdop());
        GnssDataStorage.setVdop(parser.getVdop() != null ? parser.getVdop() : "Unavailable");
        GnssDataStorage.setValidFix(parser.isValidFix());
        GnssDataStorage.setNumSatellites(parser.getNumSatellites());
        GnssDataStorage.setSpeed(parser.getSpeed());
        GnssDataStorage.setCourse(parser.getCourse());
        GnssDataStorage.getSatelliteDataList().addAll(parser.getSatelliteDataList());

        // Exibição dos dados armazenados no TextView
        displayGnssData();
    }

    private void displayGnssData() {


        StringBuilder gnssData = new StringBuilder();
        gnssData.append("NMEA Messages:\n");
        for (String message : GnssDataStorage.getNmeaMessages()) {
            gnssData.append(message).append("\n");
        }

        gnssData.append("\nParsed Data:\n")
                .append("Latitude: ").append(GnssDataStorage.getLatitude()).append("\n")
                .append("Longitude: ").append(GnssDataStorage.getLongitude()).append("\n")
                .append("Altitude: ").append(GnssDataStorage.getAltitude()).append("\n")
                .append("HDOP: ").append(GnssDataStorage.getHdop()).append("\n")
                .append("VDOP: ").append(GnssDataStorage.getVdop()).append("\n")
                .append("Valid Fix: ").append(GnssDataStorage.isValidFix()).append("\n")
                .append("Number of Satellites: ").append(GnssDataStorage.getNumSatellites()).append("\n")
                .append("Speed: ").append(GnssDataStorage.getSpeed()).append(" knots\n")
                .append("Course: ").append(GnssDataStorage.getCourse()).append(" degrees\n");

        gnssData.append("Satellite Data:\n");
        for (SatelliteData satellite : GnssDataStorage.getSatelliteDataList()) {
            gnssData.append("PRN: ").append(satellite.getPrn())
                    .append(", Elevation: ").append(satellite.getElevation())
                    .append(", Azimuth: ").append(satellite.getAzimuth())
                    .append(", SNR: ").append(satellite.getSnr()).append("\n");
        }

        textView.setText(gnssData.toString());

	scrollToBottom();
    }


private void scrollToBottom() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                scrollView.fullScroll(ScrollView.FOCUS_DOWN);
            }
        });
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLastLocation();
            }
        }
    }
}


/*public class Main {
    public static void main(String[] args) {
        Position position = new Position(-23.5000, -44.666);
        double altitude = 15.2;
        double speed = 33.0;
        double heading = 45.0;

        // Geração das mensagens NMEA
        GnssMessages gnssMessages = new GnssMessages(position, altitude, speed, heading);
        String[] nmeaSentences = gnssMessages.generateNmeaMessages();

        // Armazenando as mensagens NMEA na classe GnssDataStorage
        for (String sentence : nmeaSentences) {
            GnssDataStorage.addNmeaMessage(sentence);
        }

        // Processamento das mensagens NMEA para obter o status GNSS
        NmeaParser parser = new NmeaParser();
        for (String sentence : nmeaSentences) {
            parser.parseNmeaSentence(sentence);
        }

        // Armazenando os dados GNSS na classe GnssDataStorage
        GnssDataStorage.setLatitude(position.convertLatitudeToDegMin());
        GnssDataStorage.setLongitude(position.convertLongitudeToDegMin());
        GnssDataStorage.setAltitude(altitude);
        GnssDataStorage.setHdop(parser.getHdop());
        GnssDataStorage.setVdop(parser.getVdop() != null ? parser.getVdop() : "Unavailable");
        GnssDataStorage.setValidFix(parser.isValidFix());
        GnssDataStorage.setNumSatellites(parser.getNumSatellites());
        GnssDataStorage.setSpeed(parser.getSpeed());
        GnssDataStorage.setCourse(parser.getCourse());
        GnssDataStorage.getSatelliteDataList().addAll(parser.getSatelliteDataList());

        // Geração do status GNSS e exibição dos dados
        GnssStatus status = new GnssStatus(parser);
        System.out.println("------------------------------------------------------------");
        status.printStatus();
		System.out.println("\n");
		
        // Exibição dos dados armazenados
        GnssDataStorage.printGnssData();
		
		
    }
}
*/
