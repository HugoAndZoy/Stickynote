package com.dan.stickynote;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.PowerManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputFilter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

//    PowerManager.WakeLock mWakeLock;
private List<Fruit> fruitList=new ArrayList<>( );
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
       FruitAdapter adapter=new FruitAdapter(MainActivity.this,R.layout.fruit_item,fruitList);
       ListView listView=(ListView)findViewById(R.id.list) ;
       listView.setAdapter(adapter);

        startService(new Intent(this, MyService.class));

        Button button_add=(Button)findViewById(R.id.button_add);
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
        if(v.getId()==R.id.button_add)
            showInputDialog();


    }


    public class Fruit{

        private  String name;
   private  int imageId;
        public Fruit(String name ,int imageId)
        {
            this.name=name;
            this.imageId=imageId;
        }
        public String getName(){
            return  name;

        }
     public int getImageId(){
            return  imageId;
        }
    }
    public class FruitAdapter extends  ArrayAdapter<Fruit>{
        private  int resourceId;
        public FruitAdapter(Context context, int textViewResourcId, List<Fruit>objects){
            super(context,textViewResourcId, objects);
            resourceId=textViewResourcId;
        }
        public View getView(int position, View convertView, ViewGroup parent){

            Fruit fruit=getItem(position);
            View view= LayoutInflater.from(getContext()).inflate(resourceId,parent,false);
            TextView fruitName=(TextView)view.findViewById(R.id.fruit_name);
            ImageView fruitImage=(ImageView)view.findViewById(R.id.fruit_image);
            fruitImage.setImageResource(fruit.getImageId());
            fruitName.setText(fruit.getName());
            return view;
        }

    }

    private void showInputDialog() {
    /*@setView 装入一个EditView
     */
        final EditText addText = new EditText(MainActivity.this);
        addText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(15)});
        AlertDialog.Builder inputDialog = new AlertDialog.Builder(MainActivity.this);
        inputDialog.setTitle("Add new ").setView(addText);
        inputDialog.setPositiveButton("ok", new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int which) {
                     Fruit tasks=new Fruit(addText.getText().toString(),R.drawable.bimp);
                        fruitList.add(tasks);

                    }
                }
        ).show();
    }
}
