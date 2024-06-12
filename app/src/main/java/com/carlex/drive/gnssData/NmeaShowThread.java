package com.carlex.drive.gnssData;

public class NmeaShowThread extends Thread {
    private final NmeaMsg nmeaObject;

    public NmeaShowThread(NmeaMsg nmeaObject) {
        super("nmea_show_thread");
        this.nmeaObject = nmeaObject;
    }

    @Override
    public void run() {
		//while (true){
        	System.out.println(nmeaObject.toString());
		//}
    }
}

