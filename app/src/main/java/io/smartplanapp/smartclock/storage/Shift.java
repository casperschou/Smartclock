package io.smartplanapp.smartclock.storage;

public class Shift {

    private String begin;
    private String end;
    private String location;

    public Shift(String begin, String end, String location) {
        this.begin = begin;
        this.end = end;
        this.location = location;
    }

    public String getBegin() {
        return begin;
    }

    public String getEnd() {
        return end;
    }

    public String getLocation() {
        return location;
    }

    public String toString() {
        return "Begin: " + begin + " End: " + end + " Location: " + location;
    }

}
