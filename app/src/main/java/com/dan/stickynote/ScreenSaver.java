package com.dan.stickynote;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Build;
import android.os.PowerManager;
import android.os.Vibrator;
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

    private Vibrator vibrator;
    private HomeWatcherReceiver mHomeWatcherReceiver = null;
    private MyDatabaseHelper dbHelper;    //数据库
    PowerManager.WakeLock mWakeLock;
    private String[] tasks = new String[]{
            "HOMEWORK","WASH THE CAR","COOK","HAVE CLASS","THESIS","PAINT","REVIEW","RECITE WORDS","SWEEP"
    };
    private int task_number =0;
    //private ArrayList<String> arr_task = new ArrayList<String>();
    private List<Task> taskList=new ArrayList<>( );


    //HOME键监听相关设置   the  settings   about    HOME   button
    static public final String SYSTEM_DIALOG_REASON_KEY = "reason";
    static public final String SYSTEM_DIALOG_REASON_GLOBAL_ACTIONS = "globalactions";
    static public final String SYSTEM_DIALOG_REASON_RECENT_APPS = "recentapps";
    static public final String SYSTEM_DIALOG_REASON_HOME_KEY = "homekey";
    static public final String SYSTEM_DIALOG_REASON_ASSIST = "assist";

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK) {
            //Toast.makeText(ScreenSaver.this, "返回键无效", Toast.LENGTH_SHORT).show();
            //return true;//return true;拦截事件传递,从而屏蔽back键。
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
        this.getWindow().setFlags(0x80000000, 0x80000000);
        setContentView(R.layout.activity_screen_saver);
        //ActivityCollector.addActivity(this);


        //创建广播
        InnerRecevier innerReceiver = new InnerRecevier();
        //动态注册广播
        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
        //启动广播
        registerReceiver(innerReceiver, intentFilter);


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
            task_number = sort_time();           //获取正确的那个
            task.setText(taskList.get(sort_time()).getName());
            task.setTextColor(Color.RED);
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

    //点击按钮，删除已完成的任务     Your pressing the button means the task has been down.
    public void abandon(){
        //先删除当前任务       delete the current task
        Button task = (Button) findViewById(R.id.job);
        task.setTextColor(Color.BLACK);
        vibrate();
        new Animations().shake(this, task);

        if(taskList.size()>0) {
                delete_task(taskList.get(task_number).getName());     //删除数据库中的记录
                taskList.remove(task_number);                    //删除数组中的记录
            }

        //如果还有任务要做    if there is still task to do
        if(taskList.size()>0) {
            Random right = new Random();
            int temp = Math.abs(right.nextInt()) % taskList.size();           //获取正确的那个
            if(temp==task_number) temp = Math.abs(right.nextInt()) % taskList.size();    //如果和当前重复，再随机一次
            else task_number=temp;
            task.setText(taskList.get(task_number).getName());
        }
        //如果没有任务要做     no other tasks
        else
        {
            task.setText("NO TASKS!");
        }
    }

    //摇动手机不删除任务  when you shake your phone, tasks will not be delete
    public void shake(){
        Button task = (Button) findViewById(R.id.job);
        task.setTextColor(Color.BLACK);
        new Animations().shake(this, task);
        vibrate();
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

                //合并时间
                String year=String.valueOf(cursor.getInt(cursor.getColumnIndex("year")));
                String month=String.valueOf(cursor.getInt(cursor.getColumnIndex("month")));
                String day=String.valueOf(cursor.getInt(cursor.getColumnIndex("day")));
                String hour=String.valueOf(cursor.getInt(cursor.getColumnIndex("hour")));
                String min=String.valueOf(cursor.getInt(cursor.getColumnIndex("min")));
                String time = year+"/"+month+"/"+day+" "+hour+":"+min;

                taskList.add(new Task(task, R.drawable.point, time));
            }while(cursor.moveToNext());
        }
        cursor.close();
    }

    //监听物理按键   listen to the actions about the HOME button
    public class HomeWatcherReceiver extends BroadcastReceiver {

        private static final String SYSTEM_DIALOG_REASON_KEY = "reason";
        private static final String SYSTEM_DIALOG_REASON_HOME_KEY = "homekey";

        @Override
        public void onReceive(Context arg0, Intent arg1) {

            String action = arg1.getAction();
            //按下Home键会发送ACTION_CLOSE_SYSTEM_DIALOGS的广播
            if (action.equals(Intent.ACTION_CLOSE_SYSTEM_DIALOGS)) {

                String reason = arg1.getStringExtra(SYSTEM_DIALOG_REASON_KEY);
                if (reason != null) {
                    if (reason.equals(SYSTEM_DIALOG_REASON_HOME_KEY)) {
                        // 短按home键
                        Toast.makeText(arg0, "短按Home键", Toast.LENGTH_SHORT).show();
                    } else if (reason.equals(SYSTEM_DIALOG_REASON_RECENT_APPS)) {
                        // RECENT_APPS键
                        Toast.makeText(arg0, "RECENT_APPS", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }

    //选择最为紧急的事件  put the most urgent things into the first
    public int sort_time(){
        int count = 8888, current = 0;
        if(taskList.size()>0){
            count = 0;
            SQLiteDatabase db = dbHelper.getWritableDatabase();     //打开数据库   connect the database
            Cursor cursor = db.query("Task", null, null, null, null, null, null);   //查询所有数据  query the database

            if(cursor.moveToFirst()){
                //记录第一行数据    move to the first data
                int count_year = cursor.getInt(cursor.getColumnIndex("year"));
                int count_month = cursor.getInt(cursor.getColumnIndex("month"));
                int count_day = cursor.getInt(cursor.getColumnIndex("day"));
                int count_hour = cursor.getInt(cursor.getColumnIndex("hour"));
                int count_min = cursor.getInt(cursor.getColumnIndex("min"));
                if(cursor.moveToNext()) {
                    //遍历后头的数据    next data
                    do {
                        int year = cursor.getInt(cursor.getColumnIndex("year"));
                        int month = cursor.getInt(cursor.getColumnIndex("month"));
                        int day = cursor.getInt(cursor.getColumnIndex("day"));
                        int hour = cursor.getInt(cursor.getColumnIndex("hour"));
                        int min = cursor.getInt(cursor.getColumnIndex("min"));
                        current++;
                        if (year < count_year) {    count_year = year;  count = current;  }
                        else if(year==count_year && month<count_month )   {  count_month = month; count = current; }
                        else if(year==count_year && month==count_month && day<count_day )  {  count_day = day;  count = current;  }
                        else if(year==count_year && month==count_month && day==count_day && hour<count_hour )  {  count_hour = hour;  count =current;  }
                        else if(year==count_year && month==count_month && day==count_day && hour==count_hour && min<count_min )
                                {  count_min = min;  count =current;  }
                    } while (cursor.moveToNext());
                }
            }
        }
        return count;
    }


    //摇动   shanke
    private void vibrate() {
        // Get an instance of Vibrator from the current Context
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        //Check if the device has a vibrator
        if (vibrator.hasVibrator()) {
            Log.d(getPackageName(), "Device hasVibrator (): "+ vibrator.hasVibrator());
            //Check if the vibrator has amplitude control
            // Vibrate the device for 400 milliseconds
            vibrator.vibrate(400);
        }
    }


    //recevier 监听
    class InnerRecevier extends BroadcastReceiver {

        final String SYSTEM_DIALOG_REASON_KEY = "reason";

        final String SYSTEM_DIALOG_REASON_RECENT_APPS = "recentapps";

        final String SYSTEM_DIALOG_REASON_HOME_KEY = "homekey";

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (Intent.ACTION_CLOSE_SYSTEM_DIALOGS.equals(action)) {
                String reason = intent.getStringExtra(SYSTEM_DIALOG_REASON_KEY);
                if (reason != null) {
                    if (reason.equals(SYSTEM_DIALOG_REASON_HOME_KEY)) {
                        ScreenSaver.this.finish();
                        //Toast.makeText(ScreenSaver.this, "Home键被监听", Toast.LENGTH_SHORT).show();
                    } else if (reason.equals(SYSTEM_DIALOG_REASON_RECENT_APPS)) {
                        //Toast.makeText(ScreenSaver.this, "多任务键被监听", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }

}
