package com.example.wifi_light;

public class CommandThread extends Thread{
    private CommandThread(){

    }
    public  static CommandThread getInstance(){
        return CommandThread.CommandThreadHolder.mInstance;
    }
    public static class CommandThreadHolder{
        public static final CommandThread mInstance = new CommandThread();
    }
    @Override
    public void run(){
        super.run();
    }
}
