package com.example.wifi_light;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.Nullable;

import java.util.List;
import java.util.concurrent.RecursiveTask;

public class DeciveAdapter extends ArrayAdapter<Device> {
    private int resourceId;
    private Callback callback;
    public interface Callback {
        void onClick(View view);
        void onStopTrackingTouch(SeekBar seekBar);
    }
    public DeciveAdapter(Context context, int textViewResourceId, List<Device> object,Callback callback) {
        super(context,textViewResourceId,object);
        this.callback = callback;
        resourceId = textViewResourceId;
    }



    @Override
    public View getView(int posting, View convertView, ViewGroup parent) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.device_item,null);
        Device device = getItem(posting);
        TextView idText = (TextView) view.findViewById(R.id.ID_textView);
        TextView IP_Text = (TextView) view.findViewById(R.id.IP_textView);
        Button readBt = (Button) view.findViewById(R.id.readBt);
        Button writeBt = (Button) view.findViewById(R.id.writBt);
        SeekBar brightSb = (SeekBar) view.findViewById((R.id.brightSb));
        TextView brigthTv = (TextView) view.findViewById( R.id.brightTv ) ;
        brightSb.setProgress( device.getBright() );
        idText.setText(String.format( "%d",device.getID() ));
        brigthTv.setText( String.format( "%d%%",brightSb.getProgress() ) );
        IP_Text.setText(device.getIP());
        Log.i("viewEnvent","ID:" + device.getID());
        Log.i("viewEnvent","IP:" + device.getIP());
        if (device.getStatus() == Command.OPEN) {
            if (device.getMode() == Command.READ_MODE) {
                readBt.setBackgroundColor( Color.BLUE );
                writeBt.setBackgroundColor( Color.CYAN );
            }
            else {
                readBt.setBackgroundColor( Color.CYAN );
                writeBt.setBackgroundColor( Color.BLUE );
            }
        }
        else
        {
            readBt.setBackgroundColor( Color.CYAN );
            writeBt.setBackgroundColor( Color.CYAN );
        }
        brightSb.setOnSeekBarChangeListener( new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                callback.onStopTrackingTouch( seekBar );
            }
        } );
        class OnClickListener implements View.OnClickListener{
            @Override
            public void onClick(View v) {
                callback.onClick( v );
            }
        }
        readBt.setOnClickListener(new OnClickListener());
        writeBt.setOnClickListener( new OnClickListener() );
        return view;

    }

    @Override
    public void add(@Nullable Device object) {
        Log.i( "新增" ,"设备" );
        for (int i = 0;i < this.getCount();i++)
        {
            if(object.getID() == ( this.getItem( i ).getID() ))
                return;
        }
        super.add( object );
    }

}
