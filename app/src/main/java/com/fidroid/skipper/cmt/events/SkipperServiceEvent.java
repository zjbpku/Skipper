package com.fidroid.skipper.cmt.events;

public class SkipperServiceEvent {
    public boolean isRunning = false;

    public SkipperServiceEvent(boolean isRunning) {
        this.isRunning = isRunning;
    }
}
