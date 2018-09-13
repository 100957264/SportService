package com.yt.sportservice.event;

/**
 * Created by sean on 8/26/16.
 */
public class StatusEvent {
    public int status;

    public StatusEvent(int status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "StatusEvent{" +
                "status=" + status +
                '}';
    }
}
