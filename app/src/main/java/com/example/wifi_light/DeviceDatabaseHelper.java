package com.example.wifi_light;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

import androidx.annotation.Nullable;

public class DeviceDatabaseHelper extends SQLiteOpenHelper {
    public static final String CREATE_DEVICE = "create table Device ("
            + "id integer primary key, "
            + "IP text, "
            + "status integer, "
            + "mode integer,"
            + "bright integer)";
    private Context context;
    public DeviceDatabaseHelper(@Nullable Context context, @Nullable String name, @Nullable SQLiteDatabase.CursorFactory factory, int version) {
        super( context, name, factory, version );
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL( CREATE_DEVICE );
        Toast.makeText( context, "Create succeeded", Toast.LENGTH_SHORT ).show();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
