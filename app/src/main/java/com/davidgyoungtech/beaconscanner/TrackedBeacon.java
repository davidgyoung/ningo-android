package com.davidgyoungtech.beaconscanner;

import org.altbeacon.beacon.Beacon;

import java.util.Date;


/**
 * Created by dyoung on 11/17/17.
 */

public class TrackedBeacon {

    private Beacon mBeacon;
    private double mPacketsPerSec;
    private int mTotalPacketsDetected;
    private int mTotalRangeSamples;
    private int mLastMeasurementCount = -1;
    private boolean mMeasurementsStabilized;
    private Date mLastSeenTime;

    public TrackedBeacon(Beacon beacon) {
        mBeacon = beacon;
    }
    public Date getLastSeenTime() {
        return mLastSeenTime;
    }

    public void setLastSeenTime(Date lastSeenTime) {
        mLastSeenTime = lastSeenTime;
    }

    public Beacon getBeacon() {
        return mBeacon;
    }

    public void setBeacon(Beacon beacon) {
        mBeacon = beacon;
    }

    public double getPacketsPerSec() {
        return mPacketsPerSec;
    }

    public void setPacketsPerSec(double packetsPerSec) {
        mPacketsPerSec = packetsPerSec;
    }

    public int getTotalPacketsDetected() {
        return mTotalPacketsDetected;
    }

    public void setTotalPacketsDetected(int totalPacketsDetected) {
        mTotalPacketsDetected = totalPacketsDetected;
    }

    public int getTotalRangeSamples() {
        return mTotalRangeSamples;
    }

    public void setTotalRangeSamples(int totalRangeSamples) {
        mTotalRangeSamples = totalRangeSamples;
    }

    public int getLastMeasurementCount() {
        return mLastMeasurementCount;
    }

    public void setLastMeasurementCount(int lastMeasurementCount) {
        mLastMeasurementCount = lastMeasurementCount;
    }

    public boolean isMeasurementsStabilized() {
        return mMeasurementsStabilized;
    }

    public void setMeasurementsStabilized(boolean measurementsStabilized) {
        mMeasurementsStabilized = measurementsStabilized;
    }

    public String toString() {
        return mBeacon.toString();
    }

    /**
     * Calculate a hashCode for this beacon
     * @return
     */
    @Override
    public int hashCode() {
        return getBeacon().hashCode();
    }

    /**
     * Two detected beacons are considered equal if they share the same three identifiers, regardless of their mDistance or RSSI.
     */
    @Override
    public boolean equals(Object that) {
        if (!(that instanceof TrackedBeacon)) {
            return false;
        }
        return this.getBeacon().equals(((TrackedBeacon) that).getBeacon());
    }

}


