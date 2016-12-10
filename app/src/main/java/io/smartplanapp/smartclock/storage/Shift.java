package io.smartplanapp.smartclock.storage;

public class Shift {

    private String begin;
    private String end;

    public Shift() {
    }

    public Shift(String begin, String end) {
        this.begin = begin;
        this.end = end;
    }

    public String getBegin() {
        return begin;
    }

    public void setBegin(String begin) {
        this.begin = begin;
    }

    public String getEnd() {
        return end;
    }

    public void setEnd(String end) {
        this.end = end;
    }

    public String toString() {
        return "From " + begin + " to " + end;
    }

}
