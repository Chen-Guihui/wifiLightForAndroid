package com.example.wifi_light;

public class DeviceControl {
    private static DeviceControl deviceControl = null;
    private static Thread commandThread = null;
    private static Thread responseThread = null;
    public interface ControlInterface{
        public void commandInterface();
        public void responseInterface();
    }
    private ControlInterface controlInterface = null;
    private DeviceControl(){

        /*commandThread =  new Thread( new Runnable() {
            @Override
            public void run() {
                controlInterface.commandInterface();
            }
        } );
        responseThread =  new Thread( new Runnable() {
            @Override
            public void run() {
                controlInterface.responseInterface();
            }
        } );*/

    }
    public static DeviceControl getDeviceControl(ControlInterface controlInterface) {
        if (deviceControl == null){
            deviceControl = new DeviceControl();
        }
        deviceControl.controlInterface = controlInterface;

        Thread commandThread = CommandThread.getInstance( controlInterface );
        Thread responseThread = ResponseThread.getInstance( controlInterface );
        if (!commandThread.isAlive())
            commandThread.start();
        if (!responseThread.isAlive())
            responseThread.start();
        return deviceControl;
    }
    private static class  CommandThread extends Thread{
        private static ControlInterface controlInterface = null;
        private CommandThread(){

        }
        public  static CommandThread getInstance(ControlInterface controlInterface){
            CommandThread.controlInterface = controlInterface;
            return CommandThreadHolder.mInstance;
        }
        public static class CommandThreadHolder{
            public static final CommandThread mInstance = new CommandThread();
        }
        @Override
        public void run(){
            super.run();
            CommandThread.controlInterface.commandInterface();
        }
    }
    private static class ResponseThread extends Thread{
        private static ControlInterface controlInterface = null;
        private ResponseThread(){

        }
        public static ResponseThread getInstance(ControlInterface controlInterface){
            ResponseThread.controlInterface = controlInterface;
            return ResponseThreadHolder.mInstance;
        }
        public static class ResponseThreadHolder {
            public static final ResponseThread mInstance = new ResponseThread();
        }
        @Override
        public void run(){
            ResponseThread.controlInterface.responseInterface();
            super.run();
        }
    }

}
