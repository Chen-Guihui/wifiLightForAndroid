package com.example.wifi_light;

import android.util.Log;

import java.util.List;

public class Device {
    private int ID;
    private String IP;
    private int mode;
    private int status;
    private int bright;
    public Device(int ID,String IP)
    {
        this.ID = ID;
        this.IP = IP;
    }
    public int getID()
    {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public String getIP()
    {
        return IP;
    }

    public void setIP(String IP) {
        this.IP = IP;
    }

    public int getBright() {
        return bright;
    }

    public void setBright(int bright) {
        this.bright = bright;
    }

    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
    public boolean deviceIsExist(List<Device> list)
    {
        for (Device device: list
             ) {
            if (this.ID == device.getID() ){
                device.setStatus( this.status );
                device.setBright( this.bright );
                device.setIP( this.IP );
                device.setMode( this.mode );
                Log.i("deviceIsExist devicelist:","" + device.getID());
                return true;
            }

        }
        return false;
    }
}
