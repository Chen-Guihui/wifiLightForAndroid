package com.example.wifi_light;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;


/**
 * Created by chenguihui on 2017/11/7.
 * Email: 494723324@qq.com
 */

public class DynamicPermissions {
    private Activity activity;
    private Dialog dialog;
    DynamicPermissions(Activity activity){
        this.activity = activity;
    }
    public void showDialogTipUserGoToAppSettting() {

        dialog = new AlertDialog.Builder(activity)
                .setTitle("权限不可用")
                .setMessage("请在-应用设置-权限-中，开启权限")
                .setPositiveButton("立即开启", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 跳转到应用设置界面
                        goToAppSetting();
                    }
                })
                .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        activity.finish();
                    }
                }).setCancelable(false).show();
    }

    // 跳转到当前应用的设置界面
    private void goToAppSetting() {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", activity.getPackageName(), null);
        intent.setData(uri);
        activity.startActivityForResult(intent, 123);
    }
}
