package com.example.wifi_light;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.bluetooth.BluetoothClass;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.ColorLong;
import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Array;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.Buffer;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.EventListener;
import java.util.List;
import java.util.Objects;

import static android.R.attr.button;
import static android.R.attr.id;
import static android.R.attr.listChoiceIndicatorSingle;
import static android.R.attr.switchMinWidth;

public class MainActivity extends AppCompatActivity {
    private AlertDialog dialog;
    private List<Device> deviceList = new ArrayList<Device>();
    private Button routerBt;
    private EditText routerName;
    private EditText routerPassword;
    private EditText deviceAP_Name;
    private Button configBt;
    private Button connectBt;
    private Button scanBt;
    private Configuration config;
    private WifiAdmin wifiAdmin;
    final int MAX_DATA_PACKET_LENGTH = 128;
    private DatagramSocket udpSocket;
    private DatagramPacket dataPacket;
    private DeciveAdapter adapter;
    private DeviceDatabaseHelper dbHelper;
    private SQLiteDatabase device_db;
    private ListView listView;
    DeviceControl deviceControl = DeviceControl.getDeviceControl( new DeviceControl.ControlInterface() {
        @Override
        public void commandInterface() {

            byte[] buffer = new byte[MAX_DATA_PACKET_LENGTH];
            dataPacket = new DatagramPacket( buffer, MAX_DATA_PACKET_LENGTH );

            try {
                udpSocket = new DatagramSocket( 8228 );
            } catch (SocketException e) {
                e.printStackTrace();
            }

            dataPacket.setPort( 8228 );
            Looper.prepare();
            if (theardHandler == null) {
                theardHandler = new Handler( Objects.requireNonNull( Looper.myLooper() ) ) {
                    @Override
                    public void handleMessage(Message msg) {
                        Command command = (Command) msg.obj;
                        JSONObject obj = new JSONObject();
                        Log.i( "子线程收到一条消息", "正在处理" );
                        try {
                            switch (command.aciton) {

                                case Command.OPEN: {

                                    obj.put( "action", Command.OPEN );
                                    obj.put( "mode", command.mode );
                                }
                                break;

                                case Command.CLOSE: {
                                    obj.put( "action", Command.CLOSE );
                                    break;
                                }
                                case Command.BRIGHT: {
                                    obj.put( "action", Command.BRIGHT );
                                    obj.put( "bright", command.arg );
                                    break;
                                }
                                case Command.SCAN: {
                                    try {
                                        Log.i( "扫描设备", "正发送广播以扫描设备" );
                                        obj.put( "action", Command.SCAN );
                                        //obj.put("IP",IP_Utils.getIPAddress(MainActivity.this));
                                        WifiManager wifiManager = (WifiManager) MainActivity.this.getApplicationContext().getSystemService( Context.WIFI_SERVICE );
                                        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                                        obj.put( "IP", wifiInfo.getIpAddress() );
                                        Log.i( "扫描设备", "正发送广播以扫描设备 "  );
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }

                                    break;
                                }
                                default:
                                    throw new IllegalStateException( "Unexpected value: " + command.aciton );
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        String str = obj.toString();
                        InetAddress inetAddress = null;
                        try {
                            inetAddress = InetAddress.getByName( command.IP );
                        } catch (UnknownHostException e) {
                            e.printStackTrace();
                        }
                        dataPacket.setData( str.getBytes() );
                        dataPacket.setAddress( inetAddress );
                        try {
                            Log.i( "UDP", "正发送一个数据包 "  );
                            udpSocket.send( dataPacket );
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    };

                };
            }
            Looper.loop();
        }
        @Override
        public void responseInterface() {
            byte[] buffer = new byte[MAX_DATA_PACKET_LENGTH];
            DatagramPacket rceDataPacket = new DatagramPacket(buffer,MAX_DATA_PACKET_LENGTH);
            while (udpSocket == null);
            while (true)
            {
                try {

                    udpSocket.receive(rceDataPacket);
                    byte[] receiveBuf = new byte[rceDataPacket.getLength()];
                    for (int i = 0;i < receiveBuf.length;i++) {
                        receiveBuf[i] = 0x00;
                    }
                    System.arraycopy( rceDataPacket.getData(),0,receiveBuf,0,rceDataPacket.getLength() );
                    Log.i( "UDP接收线程:" + rceDataPacket.getLength(),new String( receiveBuf,"UTF-8") );
                    JSONObject jsonObject = new JSONObject( new String( receiveBuf ) );
                    if (jsonObject != null) {
                        int status = jsonObject.getInt( "status" );
                        Command command = new Command( );
                        switch (status) {
                            case 0:
                                Log.i( "设备回传信息","指令执行成功" );
                                int action = jsonObject.getInt( "action" );
                                int ID = jsonObject.getInt( "ID" );
                                int IP = jsonObject.getInt( "IP" );
                                command.ID = ID ;
                                switch (action){
                                    case Command.OPEN:

                                        break;
                                    case Command.CLOSE:

                                        break;
                                    case Command.BRIGHT:
                                        break;
                                    case Command.SCAN:


                                        Log.i("扫描设备成功:","ID:" + ID + "IP:" + IP );
                                        command.aciton = Command.SCAN;
                                        Log.i("扫描设备成功","设备列表长度: " + deviceList.size());
                                        break;
                                }
                                command.aciton = action;
                                command.IP = IP_Utils.intIP2StringIP( IP );
                                Device device = new Device( command.ID,command.IP );
                                device.setBright( jsonObject.getInt( "bright" ) );
                                device.setStatus( jsonObject.getInt( "deviceStatus" ) );
                                device.setMode( jsonObject.getInt( "mode" ) );
                                command.obj = device;
                                break;
                            case 1:
                                break;
                            case 3:
                                Log.i("设备回传信息","数据解析错误");
                                break;
                            default:
                                break;
                        }
                        Message msg = new Message();
                        msg.what = 0;
                        msg.obj = command;
                        mainHandler.sendMessage( msg );
                    }
                } catch (IOException | JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    } );
    private static MyHandler mainHandler;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        routerBt = (Button) findViewById(R.id.router);
        routerName = (EditText) findViewById(R.id.routerNameText);
        routerPassword = (EditText) findViewById(R.id.routerPasswordText);
        deviceAP_Name = (EditText)findViewById(R.id.deviceAP_Name);
        configBt = (Button) findViewById(R.id.configBt);
        connectBt = (Button) findViewById(R.id.connectBt);
        scanBt = (Button)findViewById(R.id.scanBt);
        config = new Configuration(MainActivity.this);
        wifiAdmin = new WifiAdmin(MainActivity.this);

        dbHelper = new DeviceDatabaseHelper( this,"DeviceStore.db",null,1 );
        device_db =  dbHelper.getWritableDatabase();

        listView = (ListView) findViewById(R.id.deviceListView);

        Cursor cursor  = device_db.query( "Device",null,null,null,null,null,null,null );
        if (cursor.moveToFirst()){
            do{
                int ID = cursor.getInt( cursor.getColumnIndex( "id" ) );
                String IP = cursor.getString( cursor.getColumnIndex( "IP" ) );
                Device device = new Device( ID,IP );
                if(device.deviceIsExist( deviceList)){
                    continue;
                }
                device.setMode( cursor.getInt( cursor.getColumnIndex( "mode" ) ) );
                device.setStatus( cursor.getInt( cursor.getColumnIndex( "status" ) ) );
                device.setBright( cursor.getInt( cursor.getColumnIndex( "bright" ) ) );
                deviceList.add( device );
            }while (cursor.moveToNext());
        }
       /* Device device = new Device("12345678","192.168.100.3");
        deviceList.add(device);*/
        listView.setOnItemClickListener( new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        } );
        routerBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //String wifiName = wifiAdmin.connectedWifiName();
                String wifiName = ((WifiManager)MainActivity.this.getApplicationContext().getSystemService(Context.WIFI_SERVICE)).getConnectionInfo().getSSID();
                routerName.setText(wifiName.substring(1, wifiName.length() - 1));
            }
        });
        connectBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setAction("android.net.wifi.PICK_WIFI_NETWORK");
                startActivityForResult(intent,101);
            }
        });

        configBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!EventBus.getDefault().isRegistered(MainActivity.this))
                    EventBus.getDefault().register(MainActivity.this);
                config.setRouterInfo(routerName.getText().toString(),routerPassword.getText().toString());
            }
        });

        scanBt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Command command = new Command(0,"255.255.255.255",Command.SCAN,0,0,null);
                Message msg = new Message();
                msg.what = 0;
                msg.obj = command;
                theardHandler.obtainMessage();
                theardHandler.sendMessage(msg);
            }
        });

    }
    @Override
    protected  void onResume() {
        //adapter.notifyDataSetChanged();
        Log.i( "onResume" ,"重新加载活动");
        super.onResume();
        adapter = new DeciveAdapter( MainActivity.this, R.layout.device_item, deviceList, new DeciveAdapter.Callback() {
            @Override
            public void onClick(View view) {
                View parentView = (View) view.getParent();
                TextView ID_text = (TextView) parentView.findViewById( R.id.ID_textView );
                TextView IP_text = (TextView) parentView.findViewById( R.id.IP_textView );
                int id = Integer.parseInt( ID_text.getText().toString() );
                int deviceStaus = 0;
                int devimode = 0;
                for (Device device: deviceList
                     ) {
                    if (device.getID() == id)
                    {
                        deviceStaus = device.getStatus();
                        devimode = device.getMode();
                        break;
                    }
                }
                switch (view.getId()){

                    case R.id.readBt: {
                        Log.i( "MainActivity", "阅读按键被按下" );
                        Button bt = (Button)view;
                        Command command = new Command(Integer.parseInt(ID_text.getText().toString()), IP_text.getText().toString(), (deviceStaus == Command.OPEN && devimode == Command.READ_MODE)?Command.CLOSE:Command.OPEN, Command.READ_MODE, 0 ,null);
                        Message msg = new Message();
                        msg.what = 0;
                        msg.obj = command;
                        theardHandler.obtainMessage();
                        theardHandler.sendMessage( msg );
                    }
                    break;
                    case R.id.writBt: {
                        Log.i( "MainActivity", "书写按键被按下" );
                        Command command = new Command( Integer.parseInt(ID_text.getText().toString()), IP_text.getText().toString(), (deviceStaus == Command.OPEN && devimode == Command.WRITE_MODE)?Command.CLOSE:Command.OPEN, Command.WRITE_MODE, 0 ,null);
                        Message msg = new Message();
                        msg.what = 0;
                        msg.obj = command;
                        theardHandler.obtainMessage();
                        theardHandler.sendMessage( msg );
                        break;
                    }
                    default:
                        break;
                }
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                View parentView = (View) seekBar.getParent();
                TextView brigthTv = parentView.findViewById( R.id.brightTv );
                TextView IP_text = (TextView) parentView.findViewById( R.id.IP_textView );
                TextView ID_text = (TextView) parentView.findViewById( R.id.ID_textView );
                Command command = new Command( Integer.parseInt(ID_text.getText().toString()), IP_text.getText().toString(), Command.BRIGHT, Command.WRITE_MODE, 0,null );
                command.arg =  seekBar.getProgress();
                Message msg = new Message();
                msg.what = 0;
                msg.obj = command;
                theardHandler.obtainMessage();
                theardHandler.sendMessage( msg );
                //brigthTv.setText( String.format( "%d%%",seekBar.getProgress() ) );
            }
        } );
        listView.setAdapter(adapter);
        mainHandler =new MyHandler( );
        mainHandler.setAdapter( adapter );
    }

    @Override
    protected void onPause() {
        super.onPause();
        adapter =null;
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mainHandler=null;

    //System.exit( 0 );
    }

    @Subscribe
    public void onEventMainThread(WiFiEvent wiFiEvent) {
        Log.i("MainActivity", "onEventMainThread: WiFi连接事件已发生 连接的wifi名称为：" + wifiAdmin.connectedWifiName());
       if (wifiAdmin.connectedWifiName().substring(1,wifiAdmin.connectedWifiName().length()-1).equals(deviceAP_Name.getText().toString()))
       {

           config.setRouterInfo(routerName.getText().toString(),routerPassword.getText().toString());
       }
       else {

           MainActivity.this.startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
       }

        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // 检查该权限是否已经获取
                int i = ContextCompat.checkSelfPermission(this, permissions[0]);
                // 权限是否已经 授权 GRANTED---授权  DINIED---拒绝
                if (i != PackageManager.PERMISSION_GRANTED) {
                    // 提示用户应该去应用设置界面手动开启权限
                    Toast.makeText(this, "权限获取失败", Toast.LENGTH_SHORT).show();
                    //new DynamicPermissions(MainActivity.this).showDialogTipUserGoToAppSettting();
                    ;
                } else {
                    if (dialog != null && dialog.isShowing()) {
                        dialog.dismiss();
                    }
                    Toast.makeText(this, "权限获取成功", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 101:
                String wifiName = wifiAdmin.connectedWifiName();
                deviceAP_Name.setText(wifiName.substring(1, wifiName.length() - 1));
                break;
            default:
                break;
        }
    }
    private static Handler theardHandler = null;
    class MyHandler extends Handler{
        private DeciveAdapter adapter;
        public void setAdapter(DeciveAdapter adapter){
            this.adapter = adapter;
        }
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage( msg );
            Log.i("主线程收到一条消息","正在处理deviceList: " + deviceList.size());
            if (msg.what == 0) {
                Command command = (Command)msg.obj;
                Device device = (Device)command.obj;
                switch (command.aciton){
                    case Command.SCAN:
                        if (!device.deviceIsExist( deviceList )) {
                            deviceList.add( (Device) command.obj );
                            ContentValues values = new ContentValues(  );
                            values.put("id",device.getID());
                            values.put("IP",device.getIP());
                            values.put("status",device.getStatus());
                            values.put("mode",device.getMode());
                            values.put( "bright",device.getBright() );
                            device_db.insert( "Device",null,values );
                        }
                        break;
                    case Command.OPEN:
                        device.deviceIsExist( deviceList );
                        break;
                    case Command.CLOSE:
                        device.deviceIsExist( deviceList );
                        break;
                        case Command.BRIGHT:
                            device.deviceIsExist( deviceList );
                        break;
                    default:
                        break;

                }
                adapter.notifyDataSetChanged();
            }
        }
    }




}
