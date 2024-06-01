package com.juicy.cloudrunner;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;

class CurveGenerator {
    interface ValueProvider {
        double val(double x);
    }

    static class Static implements ValueProvider {
        double val;
        Static(double _val) {
            val = _val;
        }
        public double val(double x) {
            return val;
        }
    }

    static class Range implements ValueProvider {
        double min,max;
        Range(double _min,double _max) {
            min = _min; max = _max;
        }
        public double val(double x) {
            return rand(min,max);
        }
    }

    private static class Point {
        double x,y;
        Point(double _x,double _y) {
            x = _x; y = _y;
        }
    }

    private ValueProvider minProvider,maxProvider,cycleProvider,noiseProvider;
    private double last;
    private LinkedList<Point> list;
    private final int MAX_CYCLE = 10;
    private final int MAX_NOISE = 5;

    CurveGenerator(ValueProvider min,ValueProvider max,ValueProvider cycle,ValueProvider noise,double start) {
        minProvider = min; maxProvider = max; cycleProvider = cycle;noiseProvider = noise;
        last = start;
        list = new LinkedList<>();
        list.addLast(new Point(start, minProvider.val(start)));
    }

    synchronized double get() throws IllegalArgumentException {
        double x = System.nanoTime();
        if(last > x) {
            MainHook.log("IllegalArgumentException:last = " + last + ",x = " + x);
            throw new IllegalArgumentException();
        }
        last = x;
        while(true) {
            while (list.size() > 1 && list.get(1).x < x) {
                list.removeFirst();
            }
            if (list.size() == 1) {
                double p = list.getFirst().x;
                for (int i = 0; i < MAX_CYCLE; i++) {
                    ArrayList<Point> newWave = new ArrayList<>();
                    double c = cycleProvider.val(p);
                    newWave.add(new Point(p + rand(0.3, 0.7) * c, maxProvider.val(p)));
                    newWave.add(new Point(p + c, minProvider.val(p)));
                    int noiseCnt = (int)rand(0, MAX_NOISE + 1);
                    while((noiseCnt--) > 0) {
                        double pos = p + c * Math.random();
                        int idx;
                        for(idx = 0;idx < newWave.size() - 1;idx++) {
                            if(newWave.get(idx).x < pos && newWave.get(idx + 1).x > pos) {
                                break;
                            }
                        }
                        if(idx == newWave.size() - 1) {
                            continue; //lucky...
                        }
                        double xdelta = Math.min(pos - newWave.get(idx).x,newWave.get(idx + 1).x - pos) * rand(0.2, 0.5);
                        double v1 = newWave.get(idx).y,v2 = newWave.get(idx + 1).y;
                        double mid = v1 + (v2 - v1) * rand(0.2, 0.8);
                        if(v1 > v2) {
                            double ydelta = Math.min(Math.min(v1 - mid,mid - v2) * rand(0.2,0.5),noiseProvider.val(pos));
                            newWave.add(idx + 1,new Point(pos + xdelta, mid + ydelta));
                            newWave.add(idx + 1,new Point(pos - xdelta, mid - ydelta));
                        } else {
                            double ydelta = Math.min(Math.min(v2 - mid,mid - v1) * rand(0.2,0.5),noiseProvider.val(pos));
                            newWave.add(idx + 1,new Point(pos + xdelta, mid - ydelta));
                            newWave.add(idx + 1,new Point(pos - xdelta, mid + ydelta));
                        }
                    }
                    p += c;
                    list.addAll(newWave);
                }
            } else {
                break;
            }
        }

        Point p1 = list.getFirst(),p2 = list.get(1);
        return ((p1.y - p2.y) / 2 * Math.cos((x - p1.x) / (p2.x - p1.x) * Math.PI) + (p1.y + p2.y) / 2);
    }
/*
    void reset(double start) {
        last = start;
        list.clear();
        list.addLast(new Point(start, rand(mn)));
    }
*/
    private static double rand(double min,double max) {
        return new Random().nextDouble() * (max - min) + min;
    }
}
