package com.example.wifi_light;

public class Command {
    public static final int CLOSE = 0;
    public static final int OPEN = 1;
    public static final int BRIGHT = 2;
    public static final int SCAN = 3;
    public static final int READ_MODE = 0;
    public static final int WRITE_MODE = 1;
    public int ID;
    public String IP;
    public int aciton;
    public int mode;
    public int arg;
    public Object obj;

    public Command(int ID, String IP, int aciton, int mode, int arg, Object obj) {
        this.ID = ID;
        this.IP = IP;
        this.aciton = aciton;
        this.mode = mode;
        this.arg = arg;
        this.obj = obj;
    }
    public Command(){

    }
}
