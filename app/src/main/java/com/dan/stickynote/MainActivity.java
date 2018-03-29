package com.dan.stickynote;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.PowerManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

//    PowerManager.WakeLock mWakeLock;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startService(new Intent(this, MyService.class));

//        BroadcastReceiver mMasterResetReciever= new BroadcastReceiver() {
//            public void onReceive(Context context, Intent intent){
//                try{
//                    Intent i = new Intent();
//                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);//解释
//                    i.setClass(context, ScreenSaver.class);
//                    context.startActivity(i);
//                    MainActivity.this.finish();
//                }catch(Exception e){
//                    Log.i("Output:", e.toString());
//                }
//            }
//
//        };
//
//        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
//        registerReceiver(mMasterResetReciever, filter);


//        PowerManager pm = (PowerManager)getSystemService(POWER_SERVICE);
//         mWakeLock = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.SCREEN_DIM_WAKE_LOCK |
//                        PowerManager.ON_AFTER_RELEASE, "SimpleTimer");


        Button mybuttom=(Button)findViewById(R.id.button);
        mybuttom.setOnClickListener(this);

    }

//    @Override
//    protected void onPause() {
//        PowerManager.WakeLock mWakeLock;
//        super.onPause();
//    }
//
//    @Override
//    protected void onResume() {
//        mWakeLock.acquire();
//        super.onResume();
//    }

    @Override
    public void onClick(View v) {
        if(v.getId()==R.id.button)
            MainActivity.this.finish();


    }
}
