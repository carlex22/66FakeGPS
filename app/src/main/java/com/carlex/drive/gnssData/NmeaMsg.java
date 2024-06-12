package com.carlex.drive.gnssData;

import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

public class NmeaMsg {
    private LocalDateTime utcDateTime;
    private Position position;
    private double speed;
    private double heading;
    private double speedTargeted;
    private double headingTargeted;
    private GpgsvGroup gpgsvGroup;
    private Gpgsa gpgsa;
    private Gpgga gga;
    private Gpgll gpgll;
    private Gprmc gprmc;
    private Gphdt gphdt;
    private Gpvtg gpvtg;
    private Gpzda gpzda;
    private List<NmeaSentence> nmeaSentences;

    public NmeaMsg(Position position, double altitude, double speed, double heading) {
        this.utcDateTime = LocalDateTime.now();
        this.position = position;
        this.speed = speed;
        this.speedTargeted = speed;
        this.heading = heading;
        this.headingTargeted = heading;
        this.gpgsvGroup = new GpgsvGroup(position, altitude);
        this.gpgsa = new Gpgsa(gpgsvGroup);
        this.gga = new Gpgga(gpgsa.getSatsCount(), utcDateTime, position, altitude);
        this.gpgll = new Gpgll(utcDateTime, position);
        this.gprmc = new Gprmc(utcDateTime, position, speed, heading);
        this.gphdt = new Gphdt(heading);
        this.gpvtg = new Gpvtg(heading, speed);
        this.gpzda = new Gpzda(utcDateTime);
        this.nmeaSentences = new ArrayList<>();
        this.nmeaSentences.add(gga);
        this.nmeaSentences.add(gpgsa);
        this.nmeaSentences.addAll(gpgsvGroup.getGpgsvInstances());
        this.nmeaSentences.add(gpgll);
        this.nmeaSentences.add(gprmc);
        this.nmeaSentences.add(gphdt);
        this.nmeaSentences.add(gpvtg);
        this.nmeaSentences.add(gpzda);
    }

    public static String checkSum(String data) {
        int checkSum = 0;
        for (char c : data.toCharArray()) {
            checkSum ^= c;
        }
        String hexStr = Integer.toHexString(checkSum).toUpperCase();
        return (hexStr.length() == 2) ? hexStr : "0" + hexStr;
    }

    public List<NmeaSentence> getNmeaSentences() {
        return nmeaSentences;
    }

    @Override
    public String toString() {
        StringBuilder nmeaMsgsStr = new StringBuilder();
        for (NmeaSentence nmea : nmeaSentences) {
            nmeaMsgsStr.append(nmea.toString()).append("\n");
        }
        return nmeaMsgsStr.toString();
    }
}

