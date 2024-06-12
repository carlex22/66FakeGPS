package com.carlex.drive.gnssData;

import java.util.ArrayList;
import java.util.List;


public class GnssMessages {
    private Position position;
    private double altitude;
    private double speed;
    private double heading;

    public GnssMessages(Position position, double altitude, double speed, double heading) {
        this.position = position;
        this.altitude = altitude;
        this.speed = speed;
        this.heading = heading;
    }

    public String[] generateNmeaMessages() {
        NmeaMsg nmeaMsg = new NmeaMsg(position, altitude, speed, heading);
        List<String> messages = new ArrayList<>();
        for (NmeaSentence sentence : nmeaMsg.getNmeaSentences()) {
            messages.add(sentence.toString());
        }
        return messages.toArray(new String[0]);
    }
}



