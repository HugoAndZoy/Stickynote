package com.dan.stickynote;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.PowerManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ScreenSaver extends AppCompatActivity implements View.OnClickListener{

    PowerManager.WakeLock mWakeLock;
    private String[] tasks = new String[]{
            "HOMEWORK","WASH THE CAR","COOK","HAVE CLASS","THESIS","PAINT","REVIEW","RECITE WORDS","SWEEP"
    };
    private int task_number =0;
    //private ArrayList<String> arr_task = new ArrayList<String>();
    private List<Fruit> fruitList=new ArrayList<>( );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_screen_saver);

        //↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓与打开屏保有关的设置↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓
        //关闭电源时，这时屏保关闭
        BroadcastReceiver mMasterResetReciever= new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent){
                try{
                    ScreenSaver.this.finish();
                }catch(Exception e){
                    Log.i("Output:", e.toString());
                }
            }

        };

        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
        registerReceiver(mMasterResetReciever, filter);



        //电源开启时
        PowerManager pm = (PowerManager)getSystemService(POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.SCREEN_DIM_WAKE_LOCK |
                PowerManager.ON_AFTER_RELEASE, "SimpleTimer");


        //↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑与打开屏保有关的设置↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑↑


        //↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓屏保内互动设置↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓↓
        //让arrayList获得数据
        load();


        //给任务按钮添加事件相应
        Button task = (Button)findViewById(R.id.job);
        task.setOnClickListener(this);

        //随机一个任务显示
        Random right = new Random();
        task_number = Math.abs(right.nextInt())%fruitList.size();           //获取正确的那个
        task.setText(fruitList.get(task_number).getName());

        //滑动监听
        GestureDetector.SimpleOnGestureListener myGestureListener = new GestureDetector.SimpleOnGestureListener(){
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {

                Log.e("<--滑动测试-->", "开始滑动");
                float x = e1.getX()-e2.getX();
                float x2 = e2.getX()-e1.getX();
                if(x>0&&Math.abs(velocityX)>0){
                    Toast.makeText(ScreenSaver.this, "向左手势", Toast.LENGTH_SHORT).show();

                }else if(x2>0&&Math.abs(velocityX)>0){
                    Toast.makeText(ScreenSaver.this, "向右手势", Toast.LENGTH_SHORT).show();
                }

                return false;
            };
        };
        //滑动
        GestureDetector mGestureDetector = new GestureDetector(this, myGestureListener);

        //晃动
        SensorManagerHelper sensorHelper = new SensorManagerHelper(this);
        sensorHelper.setOnShakeListener(new SensorManagerHelper.OnShakeListener() {
            @Override
            public void onShake() {
                // TODO Auto-generated method stub
                shake();
            }
        });
    }

    @Override
    protected void onResume() {
        mWakeLock.acquire();
        super.onResume();
    }

    @Override
    protected void onPause() {
        PowerManager.WakeLock mWakeLock;
        super.onPause();
    }

    @Override
    public void onClick(View v) {


        if(v.getId()==R.id.job) {
           abandon();
        }
    }

    //点击按钮，删除已完成的任务
    public void abandon(){
        //先删除当前任务
            if(fruitList.size()>0) {
                delete(fruitList.get(task_number).getName());     //删除数据库中的记录

                fruitList.remove(task_number);                    //删除数组中的记录
            }
        Button task = (Button) findViewById(R.id.job);
        //如果还有任务要做
        if(fruitList.size()>0) {
            Random right = new Random();
            int temp = Math.abs(right.nextInt()) % fruitList.size();           //获取正确的那个
            if(temp==task_number) temp = Math.abs(right.nextInt()) % fruitList.size();    //如果和当前重复，再随机一次
            else task_number=temp;
            task.setText(fruitList.get(task_number).getName());
        }
        //如果没有任务要做
        else
        {
            task.setText("NO TASKS!");
        }
    }

    //摇动手机不删除任务
    public void shake(){
        Button task = (Button) findViewById(R.id.job);
        //如果还有任务要做
        if(fruitList.size()>0) {
            Random right = new Random();
            int temp = Math.abs(right.nextInt()) % fruitList.size();           //获取正确的那个
            if(temp==task_number) temp = Math.abs(right.nextInt()) % fruitList.size();    //如果和当前重复，再随机一次
            else task_number=temp;
            task.setText(fruitList.get(task_number).getName());
        }
        //如果没有任务要做
        else
        {
            task.setText("NO TASKS!");
        }
    }

    //装载fruitlist
    public void load(){
        SharedPreferences pref = getSharedPreferences("StickyNoteData",MODE_PRIVATE);
        int count = pref.getInt("ArraySize",0);       //获取数组长度

        //移除现有数组
//        for(int i=0; i<fruitList.size();i++)
//            fruitList.remove(i);
        fruitList=new ArrayList<>( );

        //如果长度大于0，则加载数组
        if(count>0) {
            for (int i = 0; i < count; i++) {
                String item = pref.getString(i + "", null);
                fruitList.add(i, new Fruit(item, R.drawable.bimp));
            }

        }
    }


    //删除数据
    private void delete(String s){
        //先删除
        SharedPreferences.Editor editor = getSharedPreferences("StickyNoteData", MODE_PRIVATE).edit();
        editor.remove(s);
        editor.apply();

        //在修改数量
        SharedPreferences pref = getSharedPreferences("StickyNoteData",MODE_PRIVATE);
        int count = pref.getInt("ArraySize",0);       //获取数组长度
        count = count-1;
        editor.putInt("ArraySize", count);                           //修改数组长度-1
        editor.apply();
    }
}
