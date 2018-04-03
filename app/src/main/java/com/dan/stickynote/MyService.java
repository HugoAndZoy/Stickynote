package com.dan.stickynote;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Created by 52641 on 2018/3/26.
 */

public class MyService extends Service {
    @Override
    public void onCreate() {
                BroadcastReceiver mMasterResetReciever= new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent){
                try{
                    Intent i = new Intent();
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);//解释
                    i.setClass(context, ScreenSaver.class);
                    context.startActivity(i);
                    //MainActivity.this.finish();
                }catch(Exception e){
                    Log.i("Output:", e.toString());
                }
            }

        };

        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        registerReceiver(mMasterResetReciever, filter);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


}
