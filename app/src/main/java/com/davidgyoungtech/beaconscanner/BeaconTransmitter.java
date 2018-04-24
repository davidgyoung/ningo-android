package com.davidgyoungtech.beaconscanner;

import android.content.Context;
import android.util.Log;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.List;
import java.util.ArrayList;
import java.util.UUID;

/**
 * Created by dyoung on 3/2/18.
 */

public class BeaconTransmitter implements Serializable {
    private static final long serialVersionUID = 5L;
    private String mName;
    private String mDescription;
    private String mFormat;
    private String mId1;
    private String mId2;
    private String mId3;
    private int mMeasuredPower = -59;
    private String mData1;
    private int mAdvertisingRate = 10;
    private String mTransmitterPower = "LOW";
    private boolean mEnabled = false;
    private boolean mTransmitting = false;
    private String mUuid = UUID.randomUUID().toString();
    private long mTransmitStartTime = 0l;


    private static final String TAG = BeaconTransmitter.class.getSimpleName();
    private static final String SERIALIZATION_FILENAME = "beacon_transmitters.ser";
    public static BeaconTransmitter createTransmitter() {
        BeaconTransmitter tx = new BeaconTransmitter();
        return tx;
    }
    private BeaconTransmitter() {

    }

    public static synchronized void saveAll(Context context, List<BeaconTransmitter> transmitters) {
        try {
            Log.d(TAG, "saving transmitters");
            for (BeaconTransmitter transmitter : transmitters) {
                Log.d(TAG, transmitter.getUuid()+" / "+transmitter.hashCode()+" enabled: "+transmitter.isEnabled()+" transmitting: "+transmitter.getTransmitting());
            }

            logCounts(transmitters);
            FileOutputStream fileOutputStream = context.openFileOutput(SERIALIZATION_FILENAME, Context.MODE_PRIVATE);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(fileOutputStream);
            objectOutputStream.writeObject(serialVersionUID);
            objectOutputStream.writeObject(transmitters);
            objectOutputStream.close();
            fileOutputStream.close();
            Log.d(TAG, "saved transmitters");
            logCounts(transmitters);
        } catch (IOException e) {
            Log.d(TAG, "can't save transmitters", e);
        }
    }
    public static synchronized  List<BeaconTransmitter> loadAll(Context context) {
        try {
            Log.d(TAG, "****** loading transmitters");
            FileInputStream fileInputStream = context.openFileInput(SERIALIZATION_FILENAME);
            ObjectInputStream objectInputStream = new ObjectInputStream(fileInputStream);
            Long savedSerialVersionUID  = (Long) objectInputStream.readObject();
            if (serialVersionUID != savedSerialVersionUID) {
                throw new IOException("Bad saved serialVersionUID: "+savedSerialVersionUID+" vs. expected "+serialVersionUID);
            }
            List<BeaconTransmitter> transmitters = (List<BeaconTransmitter>) objectInputStream.readObject();
            objectInputStream.close();
            fileInputStream.close();
            Log.d(TAG, "********  loaded transmitters");
            logCounts(transmitters);
            return transmitters;
        } catch (IOException e) {
            Log.d(TAG, "can't load transmitters", e);
        }
        catch (ClassNotFoundException e) {
            Log.d(TAG, "can't load transmitters", e);
        }
        catch (ClassCastException e) {
            Log.d(TAG, "Wrong object");
        }

        // If we get to here we need to set up default data
        ArrayList<BeaconTransmitter> transmitters = new ArrayList<BeaconTransmitter>();
        BeaconTransmitter transmitter1 = BeaconTransmitter.createTransmitter();
        transmitter1.setName("iBeacon sample");
        transmitter1.setId1("2F234454-CF6D-4A0F-ADF2-F4911BA9FFA6");
        transmitter1.setId2("1");
        transmitter1.setId3("1");
        transmitter1.setFormat("ibeacon");
        transmitter1.setMeasuredPower(-59);
        transmitter1.setTransmitterPower("HIGH");
        transmitter1.setAdvertisingRate(10);
        BeaconTransmitter transmitter2 = BeaconTransmitter.createTransmitter();
        transmitter2.setName("AltBeacon sample");
        transmitter2.setId1("2F234454-CF6D-4A0F-ADF2-F4911BA9FFA6");
        transmitter2.setId2("1");
        transmitter2.setId3("2");
        transmitter2.setData1("0");
        transmitter2.setFormat("altbeacon");
        transmitter2.setMeasuredPower(-59);
        transmitter2.setTransmitterPower("HIGH");
        transmitter2.setAdvertisingRate(10);
        BeaconTransmitter transmitter3 = BeaconTransmitter.createTransmitter();
        transmitter3.setName("Eddystone-UID sample");
        transmitter3.setId1("0102030405060708090a");
        transmitter3.setId2("000000000001");
        transmitter3.setFormat("eddystone-uid");
        transmitter3.setMeasuredPower(-59+41);
        transmitter3.setTransmitterPower("HIGH");
        transmitter3.setAdvertisingRate(10);
        transmitter3.setData1("0");
        BeaconTransmitter transmitter4 = BeaconTransmitter.createTransmitter();
        transmitter4.setName("Eddystone-EID (static value) sample");
        transmitter4.setId1("0102030405060708");
        transmitter4.setFormat("eddystone-eid");
        transmitter4.setMeasuredPower(-59+41);
        transmitter4.setTransmitterPower("HIGH");
        transmitter4.setAdvertisingRate(10);
        BeaconTransmitter transmitter5 = BeaconTransmitter.createTransmitter();
        transmitter5.setName("Eddystone-URL sample");
        transmitter5.setId1("http://davidgyoungtech.com");
        transmitter5.setFormat("eddystone-url");
        transmitter5.setMeasuredPower(-59+41);
        transmitter5.setTransmitterPower("HIGH");
        transmitter5.setAdvertisingRate(10);
        transmitters.add(transmitter1);
        transmitters.add(transmitter2);
        transmitters.add(transmitter3);
        transmitters.add(transmitter4);
        transmitters.add(transmitter5);
        Log.d(TAG, "created transmitters");
        logCounts(transmitters);
        return transmitters;
    }

    public String getDescription() {
        return mDescription;
    }

    public void setDescription(String description) {
        mDescription = description;
    }

    public String getFormat() {
        return mFormat;
    }

    public void setFormat(String format) {
        mFormat = format;
    }

    public String getId1() {
        return mId1;
    }

    public void setId1(String id1) {
        mId1 = id1;
    }

    public String getId2() {
        return mId2;
    }

    public void setId2(String id2) {
        mId2 = id2;
    }

    public String getId3() {
        return mId3;
    }

    public void setId3(String id3) {
        mId3 = id3;
    }

    public int getMeasuredPower() {
        return mMeasuredPower;
    }

    public void setMeasuredPower(int measuredPower) {
        mMeasuredPower = measuredPower;
    }

    public String getData1() {
        return mData1;
    }

    public void setData1(String data1) {
        this.mData1 = data1;
    }

    public String getName() {
        return mName;
    }

    public void setName(String name) {
        mName = name;
    }

    public int getAdvertisingRate() {
        return mAdvertisingRate;
    }

    public void setAdvertisingRate(int advertisingRate) {
        mAdvertisingRate = advertisingRate;
    }

    public String getTransmitterPower() {
        return mTransmitterPower;
    }

    public void setTransmitterPower(String transmitterPower) {
        mTransmitterPower = transmitterPower;
    }

    public boolean isEnabled() {
        return mEnabled;
    }

    public void setEnabled(boolean enabled) {
        mEnabled = enabled;
    }

    public long getLastTransmitStartTime() {
        return mTransmitStartTime;
    }

    public void setLastTransmitStartTime(long transmitStartTime) {
        mTransmitStartTime = transmitStartTime;
    }

    public boolean getTransmitting() {
        return mTransmitting;
    }

    public void setTransmitting(boolean transmitting) {
        mTransmitting = transmitting;
    }

    public String getUuid() {
        return mUuid;
    }

    private static void logCounts(List<BeaconTransmitter> transmitters) {
        int transmitting = 0;
        int enabled = 0;
        for (BeaconTransmitter transmitter : transmitters) {
            if (transmitter.mTransmitting) {
                transmitting++;
            }
            if (transmitter.isEnabled()) {
                enabled++;
            }
        }
        Log.d(TAG, "transmitters: "+transmitters.size()+" enabled: "+enabled+" transmitting: "+transmitting);
    }
}

