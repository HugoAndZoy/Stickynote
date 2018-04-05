package com.dan.stickynote;

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
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    //    PowerManager.WakeLock mWakeLock;
    private List<Task> taskList = new ArrayList<>( );
    // 数据库 dabatase
    private MyDatabaseHelper dbHelper;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //ActivityCollector.removeActivity(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //透明状态栏
       // getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        //透明导航栏
       // getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);


        // 1.设置状态透明

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




        //加载数据库
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

    private static int getStatusBarHeight(Context context) {
        int statusBarHeight = 0;
        Resources res = context.getResources();
        int resourceId = res.getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            statusBarHeight = res.getDimensionPixelSize(resourceId);
        }
        return statusBarHeight;
    }


    @Override
    public void onClick(View v) {
        if(v.getId()==R.id.button_add)
            showInputDialog();


    }

    //点击添加新任务的按钮  click to add the new task
    private void showInputDialog() {
    /*@setView 装入一个EditView
     */
        final EditText addText = new EditText(MainActivity.this);
        //设置最大输入字符数
        addText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(15)});
        AlertDialog.Builder inputDialog = new AlertDialog.Builder(MainActivity.this);
        inputDialog.setTitle("Add new ").setView(addText);
        inputDialog.setPositiveButton("ok", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        if(addText.getText().length()>0) {
                            add_task(addText.getText().toString());
                            load_task();
                            refresh();
                        }
                    }
                }
        ).show();
    }

    //点击删除任务按钮   click to remove the task
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

    //refresh the activity
    private void refresh(){
        Intent it = new Intent(MainActivity.this, MainActivity.class);
        startActivity(it);
        overridePendingTransition(0, 0);
        MainActivity.this.finish();
    }

    //向数据库添加任务记录  add the task record to the database
    public void add_task(String t){
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        //组装数据 setup the data
        values.put("task",t);
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
                taskList.add(new Task(task, R.drawable.point));
            }while(cursor.moveToNext());
        }
        cursor.close();
    }
}

