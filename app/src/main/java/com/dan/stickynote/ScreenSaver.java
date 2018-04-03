package com.dan.stickynote;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.PowerManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ScreenSaver extends AppCompatActivity implements View.OnClickListener{

    private HomeWatcherReceiver mHomeWatcherReceiver = null;
    private MyDatabaseHelper dbHelper;    //数据库
    PowerManager.WakeLock mWakeLock;
    private String[] tasks = new String[]{
            "HOMEWORK","WASH THE CAR","COOK","HAVE CLASS","THESIS","PAINT","REVIEW","RECITE WORDS","SWEEP"
    };
    private int task_number =0;
    //private ArrayList<String> arr_task = new ArrayList<String>();
    private List<Task> taskList=new ArrayList<>( );


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if(keyCode == KeyEvent.KEYCODE_HOME)
        {
            //ActivityCollector.finishAll();
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mHomeWatcherReceiver != null) {
            try {
                unregisterReceiver(mHomeWatcherReceiver);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.getWindow().setFlags(0x80000000,0x80000000);
        setContentView(R.layout.activity_screen_saver);
        //ActivityCollector.addActivity(this);

        //连接数据库
        dbHelper = new MyDatabaseHelper(this, "TaskStore.db", null, 1);
        dbHelper.getWritableDatabase();


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
        load_task();


        //给任务按钮添加事件相应
        Button task = (Button)findViewById(R.id.job);
        task.setOnClickListener(this);

        //随机一个任务显示
        if(taskList.size()>0) {
            Random right = new Random();
            task_number = Math.abs(right.nextInt()) % taskList.size();           //获取正确的那个
            task.setText(taskList.get(task_number).getName());
        }
        else
        {
            task.setText("No Tasks!");
        }

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
            if(taskList.size()>0) {
                delete_task(taskList.get(task_number).getName());     //删除数据库中的记录
                taskList.remove(task_number);                    //删除数组中的记录
            }
        Button task = (Button) findViewById(R.id.job);
        //如果还有任务要做
        if(taskList.size()>0) {
            Random right = new Random();
            int temp = Math.abs(right.nextInt()) % taskList.size();           //获取正确的那个
            if(temp==task_number) temp = Math.abs(right.nextInt()) % taskList.size();    //如果和当前重复，再随机一次
            else task_number=temp;
            task.setText(taskList.get(task_number).getName());
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
        if(taskList.size()>0) {
            Random right = new Random();
            int temp = Math.abs(right.nextInt()) % taskList.size();           //获取正确的那个
            if(temp==task_number) temp = Math.abs(right.nextInt()) % taskList.size();    //如果和当前重复，再随机一次
            else task_number=temp;
            task.setText(taskList.get(task_number).getName());
        }
        //如果没有任务要做
        else
        {
            task.setText("NO TASKS!");
        }
    }


    //删除数据库的记录  delete the record in the database
    public void delete_task(String t){
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete("Task", "task = ?", new String[]{ t });
    }

    //查询并载入列表 check and load the task Liat
    public void load_task(){
        //移除现有数组   remove the TaskList and recreate a new one
        taskList=new ArrayList<>( );
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        //查询所有数据
        Cursor cursor = db.query("Task", null, null, null, null, null, null);
        if(cursor.moveToFirst()){
            do{
                //遍历cursor
                String task = cursor.getString(cursor.getColumnIndex("task"));
                taskList.add(new Task(task, R.drawable.point));
            }while(cursor.moveToNext());
        }
        cursor.close();
    }

    public class HomeWatcherReceiver extends BroadcastReceiver {

        private static final String SYSTEM_DIALOG_REASON_KEY = "reason";
        private static final String SYSTEM_DIALOG_REASON_HOME_KEY = "homekey";

        @Override
        public void onReceive(Context context, Intent intent) {

            String intentAction = intent.getAction();
            if (TextUtils.equals(intentAction, Intent.ACTION_CLOSE_SYSTEM_DIALOGS)) {
                String reason = intent.getStringExtra(SYSTEM_DIALOG_REASON_KEY);
                if (TextUtils.equals(SYSTEM_DIALOG_REASON_HOME_KEY, reason)) {
                    ScreenSaver.this.finish();
                }
            }
        }
    }




}
