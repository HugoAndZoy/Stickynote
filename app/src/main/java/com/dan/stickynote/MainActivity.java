package com.dan.stickynote;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputFilter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    //PowerManager.WakeLock mWakeLock;
    private List<Fruit> fruitList = new ArrayList<>();

    private FruitAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        load();

        adapter = new FruitAdapter(MainActivity.this, R.layout.fruit_item, fruitList);
        final ListView listView = (ListView) findViewById(R.id.list);
        listView.setAdapter(adapter);

        //添加长按响应
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                showNormalDialog(position);
                return false;
            }
        });


        startService(new Intent(this, MyService.class));

        Button button_add = findViewById(R.id.button_add);
        button_add.setOnClickListener(this);

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
        if (v.getId() == R.id.button_add)
            showInputDialog();


    }


    public class Fruit {

        private String name;
        private int imageId;

        public Fruit(String name, int imageId) {
            this.name = name;
            this.imageId = imageId;
        }

        public String getName() {
            return name;

        }

        public int getImageId() {
            return imageId;
        }
    }

    public class FruitAdapter extends ArrayAdapter<Fruit> {
        private int resourceId;

        public FruitAdapter(Context context, int textViewResourcId, List<Fruit> objects) {
            super(context, textViewResourcId, objects);
            resourceId = textViewResourcId;
        }

        public View getView(int position, View convertView, ViewGroup parent) {

            Fruit fruit = getItem(position);
            View view = LayoutInflater.from(getContext()).inflate(resourceId, parent, false);
            TextView fruitName = view.findViewById(R.id.fruit_name);
            ImageView fruitImage = view.findViewById(R.id.fruit_image);
            fruitImage.setImageResource(fruit.getImageId());
            fruitName.setText(fruit.getName());
            return view;
        }

    }

    private void showInputDialog() {
        /*@setView 装入一个EditView
         */
        final EditText addText = new EditText(MainActivity.this);

        //设置最大输入字符数
        addText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(8)});
        AlertDialog.Builder inputDialog = new AlertDialog.Builder(MainActivity.this);
        inputDialog.setTitle("Add new ").setView(addText);
        inputDialog.setPositiveButton("ok", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                        if (addText.getText().length() > 0) {
                            save(addText.getText().toString());
                            load();
                            refresh();
                        }
                    }
                }
        ).show();
//        inputDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
//            @Override
//            public void onDismiss(DialogInterface dialog) {
//                onCreate(null);
//            }
//        });
    }


    public void save(String data) {
        SharedPreferences.Editor editor = getSharedPreferences("StickyNoteData", MODE_PRIVATE).edit();
        int count = fruitList.size();        //获取当前的任务数量
        editor.putString(count + "", data);        //将新任务添加到列表里
        editor.putInt("ArraySize", count + 1);    //用ArraySize来记录最新任务的数量
        editor.apply();
    }

    public void load() {
        SharedPreferences pref = getSharedPreferences("StickyNoteData", MODE_PRIVATE);
        int count = pref.getInt("ArraySize", 0);       //获取数组长度

        //移除现有数组
//        for(int i=0; i<fruitList.size();i++)
//            fruitList.remove(i);
        fruitList = new ArrayList<>();

        //如果长度大于0，则加载数组
        if (count > 0) {
            for (int i = 0; i < count; i++) {
                String item = pref.getString(i + "", null);
                fruitList.add(i, new Fruit(item, R.drawable.point));
            }

        }
    }

    //删除数据
    private void delete(String s) {
        //先删除
        SharedPreferences.Editor editor = getSharedPreferences("StickyNoteData", MODE_PRIVATE).edit();
        editor.remove(s);
        editor.apply();

        //在修改数量
        SharedPreferences pref = getSharedPreferences("StickyNoteData", MODE_PRIVATE);
        int count = pref.getInt("ArraySize", 0);       //获取数组长度
        count = count - 1;
        editor.putInt("ArraySize", count);                           //修改数组长度-1
        editor.apply();
    }


    //删除提示
    private void showNormalDialog(int i) {
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
                        //...To-do
                        delete(fruitList.get(it).getName());
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

    private void refresh() {
        Intent it = new Intent(MainActivity.this, MainActivity.class);
        startActivity(it);
        overridePendingTransition(0, 0);
        MainActivity.this.finish();
    }
}

