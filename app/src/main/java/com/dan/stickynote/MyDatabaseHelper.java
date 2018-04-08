package com.dan.stickynote;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

/**
 * Created by Dan on 2018/4/3.
 */

public class MyDatabaseHelper extends SQLiteOpenHelper {

    public static final String CREATE_TASK = "create table Task ("
            +"task varchar(20)," +
            "year integer," +
            "month integer," +
            "day integer," +
            "hour integer," +
            "min integer)";

    private Context mContext;

    public MyDatabaseHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version){
        super(context, name, factory, version);
        mContext = context;
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TASK);
        Toast.makeText(mContext, "Create succeed", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("drop table if exists Task");
        onCreate(db);
    }
}
