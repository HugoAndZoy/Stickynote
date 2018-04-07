package com.dan.stickynote;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Build;
import android.os.PowerManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.text.InputFilter;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    //    PowerManager.WakeLock mWakeLock;
    private List<Task> taskList = new ArrayList<>( );
    // 数据库 dabatase
    private MyDatabaseHelper dbHelper;
    //自定义 define dialog
    private SelfDialog selfDialog;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //ActivityCollector.removeActivity(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // *********************************************************设置状态透明

        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        ViewGroup decorViewGroup = (ViewGroup) window.getDecorView();
        View statusBarView = new View(window.getContext());
        int statusBarHeight = getStatusBarHeight(window.getContext());
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, statusBarHeight);
        params.gravity = Gravity.TOP;
        statusBarView.setLayoutParams(params);
        statusBarView.setBackgroundColor(Color.rgb( 65,105,225));
        decorViewGroup.addView(statusBarView);
        //取消设置透明状态栏,使 ContentView 内容不再覆盖状态栏
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        //需要设置这个 flag 才能调用 setStatusBarColor 来设置状态栏颜色
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        //设置状态栏颜色
        window.setStatusBarColor(Color.rgb( 65,105,225));

    //*******************************************************************设置dialog


    //*************************************************加载数据库
        dbHelper = new MyDatabaseHelper(this, "TaskStore.db", null, 1);
        dbHelper.getWritableDatabase();

        //装载listView  Load the ListView
        load_task();

        //找到recycler_view    find
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);

        recyclerView.addOnItemTouchListener(new OnRecyclerItemClickListener(recyclerView) {
            @Override
            public void onItemClick(RecyclerView.ViewHolder viewHolder) {
                int position = viewHolder.getAdapterPosition();
                //Toast.makeText(MainActivity.this,"you clicked "+taskList.get(position).getName() ,Toast.LENGTH_LONG).show();
                edit_task(taskList.get(position).getName(), taskList.get(position).getDeadline());
            }
            //添加长按相应   Long Click
            @Override
            public void onItemLOngClick(RecyclerView.ViewHolder viewHolder) {
                int position = viewHolder.getAdapterPosition();
                showNormalDialog(position);
            }
        });


        TaskAdapter adapter2 = new TaskAdapter(taskList);
        recyclerView.setAdapter(adapter2);


        startService(new Intent(this, MyService.class));

        Button button_add=(Button)findViewById(R.id.button_add);
        button_add.setOnClickListener(this);

    }

   //获取通知栏高度
    private static int getStatusBarHeight(Context context) {
        int statusBarHeight = 0;
        Resources res = context.getResources();
        int resourceId = res.getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            statusBarHeight = res.getDimensionPixelSize(resourceId);
        }
        return statusBarHeight;
    }

    public void onClick(View v) {
        if(v.getId()==R.id.button_add)
        {
                    selfDialog = new SelfDialog(MainActivity.this);
//                    selfDialog.setTitle("Add task");
//                    selfDialog.setMessage("Input the task");

                    selfDialog.setDateOnclickListener(new SelfDialog.onYesOnclickListener(){
                        @Override
                        public void onYesClick() {
                            datePicker(selfDialog.date);
                        }
                    });

            selfDialog.setHourOnclickListener(new SelfDialog.onYesOnclickListener(){
                @Override
                public void onYesClick() {
                    hourPicker(selfDialog.messagetime);
                }
            });

                    selfDialog.setYesOnclickListener("OK", new SelfDialog.onYesOnclickListener() {
                        @Override
                        public void onYesClick() {
                            if(cheack_same(selfDialog.getTask())) {
                                Toast.makeText(MainActivity.this, "You must add a different task name", Toast.LENGTH_LONG).show();
                            }
                            else if(selfDialog.getTask().length()>0 &&
                                    !selfDialog.getDate().equals("set date") &&
                                    !selfDialog.getTime().equals("set hour")) {
                                add_task(selfDialog.getTask(), selfDialog.getDate(), selfDialog.getTime());
                                load_task();
                                refresh();
                                selfDialog.dismiss();
                            }
                            else
                                Toast.makeText(MainActivity.this,"You must set the task and time",Toast.LENGTH_LONG).show();
                        }


                    });
                    selfDialog.show();
                }
    }

    //点击删除任务按钮的对话框   click to remove the task
    private void showNormalDialog(int i){
        /* @setIcon 设置对话框图标
         * @setTitle 设置对话框标题
         * @setMessage 设置对话框消息提示
         * setXXX方法返回Dialog对象，因此可以链式设置属性
         */
        final int it = i;
        final AlertDialog.Builder normalDialog =
                new AlertDialog.Builder(MainActivity.this);
        normalDialog.setTitle("Delete？");
        //normalDialog.setMessage("你要点击哪一个按钮呢?");
        normalDialog.setPositiveButton("Yes",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                         delete_task(taskList.get(it).getName());
                        refresh();
                    }
                });
        normalDialog.setNegativeButton("No",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
        // 显示
        normalDialog.show();
    }

    //刷新界面    refresh the activity
    private void refresh(){
        Intent it = new Intent(MainActivity.this, MainActivity.class);
        startActivity(it);
        overridePendingTransition(0, 0);
        MainActivity.this.finish();
    }

    //向数据库添加任务记录  add the task record to the database
    public void add_task(String t, String date, String time){
        int year=0, month=0, day=0, hour=0, min=0;
            //解析出年月日时分
            String[] parts = date.split("/");
            String[] parts2 = time.split(":");
            try {
                year = Integer.parseInt(parts[0]);
                month = Integer.parseInt(parts[1]);
                day = Integer.parseInt(parts[2]);
                hour = Integer.parseInt(parts2[0]);
                min = Integer.parseInt(parts2[1]);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }

        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        //组装数据 setup the data
        values.put("task",t);
            values.put("year",year);
            values.put("month",month);
            values.put("day",day);
            values.put("hour",hour);
            values.put("min",min);
        db.insert("Task", null, values);

        values.clear();
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

    //打开选择日期的界面
    public void datePicker(TextView tv){

        final TextView text = tv;
        //获取年月日
        Calendar cal;
        int year,month,day,hour,minute;
        cal=Calendar.getInstance();
        year=cal.get(Calendar.YEAR);       //获取年月日时分秒
        month=cal.get(Calendar.MONTH);   //获取到的月份是从0开始计数
        day=cal.get(Calendar.DAY_OF_MONTH);


        DatePickerDialog.OnDateSetListener listener=new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker arg0, int year, int month, int day) {
                text.setText(year+"/"+(++month)+"/"+day);      //将选择的日期显示到TextView中,因为之前获取month直接使用，所以不需要+1，这个地方需要显示，所以+1
            }
        };
        DatePickerDialog dialog=new DatePickerDialog(MainActivity.this, 0,listener,year,month,day);//后边三个参数为显示dialog时默认的日期，月份从0开始，0-11对应1-12个月
        dialog.show();


    }
    public void hourPicker(TextView tv){
        final TextView text = tv;
        Calendar cal;
        int hour,minute;
        cal=Calendar.getInstance();
        hour = cal.get(Calendar.HOUR_OF_DAY);
        minute = cal.get(Calendar.MINUTE);

        new TimePickerDialog(MainActivity.this,new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                text.setText(hourOfDay+":"+minute);
            }
        }, hour, minute, true).show();
    }

    //重新编辑任务信息对话框    the dialog uesd to reedit the task information
    private void edit_task(String old, String time){
        final String origin = old;
        String date, hour;
        Toast.makeText(MainActivity.this,"you clicked "+ origin ,Toast.LENGTH_LONG).show();
        selfDialog = new SelfDialog(MainActivity.this);
        selfDialog.setTitle("Edit task");
        String parts[] = time.split(" ");
        date =  parts[0];
        hour = parts[1];
        selfDialog.setTask(old);
        selfDialog.setDate(date);
        selfDialog.setTime(hour);

        selfDialog.setDateOnclickListener(new SelfDialog.onYesOnclickListener(){
            @Override
            public void onYesClick() {
                datePicker(selfDialog.date);
            }
        });

        selfDialog.setHourOnclickListener(new SelfDialog.onYesOnclickListener(){
            @Override
            public void onYesClick() {hourPicker(selfDialog.messagetime);
            }
        });

        selfDialog.setYesOnclickListener("OK", new SelfDialog.onYesOnclickListener() {
            @Override
            public void onYesClick() {
                if(selfDialog.getTask().length()>0 &&
                        !selfDialog.getDate().equals("set date") &&
                        !selfDialog.getTime().equals("set hour")) {
                    reedit_task(origin, selfDialog.getTask(), selfDialog.getDate(), selfDialog.getTime());
                    load_task();
                    refresh();
                    selfDialog.dismiss();
                }
                else
                    Toast.makeText(MainActivity.this,"You must set the task and time",Toast.LENGTH_LONG).show();
            }


        });
        selfDialog.show();
    }

    private void reedit_task(String old, String t, String date, String time){

        int year=0, month=0, day=0, hour=0, min=0;
        //解析出年月日时分
        String[] parts = date.split("/");
        String[] parts2 = time.split(":");
        try {
            year = Integer.parseInt(parts[0]);
            month = Integer.parseInt(parts[1]);
            day = Integer.parseInt(parts[2]);
            hour = Integer.parseInt(parts2[0]);
            min = Integer.parseInt(parts2[1]);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }

        //链接数据库     connect
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        //组装数据 setup the data
        values.put("task",t);
        values.put("year",year);
        values.put("month",month);
        values.put("day",day);
        values.put("hour",hour);
        values.put("min",min);

        db.update("Task", values, "task = ?", new String[]{ old });
    }

    private boolean cheack_same(String name){
        load_task();
        if(taskList.size()>0){
            for(int i=0; i<taskList.size(); i++)
            {
                if( name.equals(taskList.get(i).getName()) )
                    return true;
            }
        }
        return false;
    }
}

