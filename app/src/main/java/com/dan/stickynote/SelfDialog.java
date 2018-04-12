package com.dan.stickynote;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
/**
 * Created by Dan on 2018/4/5.
 * It's a dialog we create on our own in order to add the task(name/date/hour/min).
 */

public class SelfDialog extends Dialog {

    private Button yes;                              //yes button                    //确定按钮
    private TextView titleTv;                        //title                        //消息标题文本
    private TextView messageTv;                      //editText to get taskname     //输入任务文本
    public   TextView messagetime;                   //get hour the min             //输入hour
    public   TextView date;                          //get date                       //输入日期
    private String titleStr;                         //this string is to set the title                  //从外界设置的title文本

    private String messageStr;                       //                              //从外界设置的消息文本
    private String taskStr;                          //set taskname
    private  String dateStr;                         //set date                                //从外界设置的时间
    private String timeStr;                          //set time
    private String yesStr;                           //set yes


    private onYesOnclickListener yesOnclickListener;                                               //确定按钮被点击了的监听器
    private onYesOnclickListener DateOnclickListener;                                              //确定按钮被点击了的监听器
    private onYesOnclickListener HourOnclickListener;                                              //确定按钮被点击了的监听器

    /**
     * 设置确定按钮的显示内容和监听
     *
     * @param str
     * @param onYesOnclickListener
     */
    public void setYesOnclickListener(String str, onYesOnclickListener onYesOnclickListener) {
        if (str != null) {
            yesStr = str;
        }
        this.yesOnclickListener = onYesOnclickListener;
    }

    //Date 监听    listen to the Date button
    public void setDateOnclickListener(onYesOnclickListener onYesOnclickListener) {
        this.DateOnclickListener = onYesOnclickListener;
    }
    //Hour 监听    listen to the Hour button
    public void setHourOnclickListener(onYesOnclickListener onYesOnclickListener) {
        this.HourOnclickListener = onYesOnclickListener;
    }


    public SelfDialog(Context context) {
        super(context, R.style.MyDialog);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog);
        //按空白处不能取消动画
        setCanceledOnTouchOutside(true);

        //初始化界面控件
        initView();
        //初始化界面数据
        initData();
        //初始化界面控件的事件
        initEvent();

    }

    /**
     * 初始化界面的确定和取消监听器
     */
    private void initEvent() {
        //设置确定按钮被点击后，向外界提供监听
        yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (yesOnclickListener != null) {
                    yesOnclickListener.onYesClick();
                }
            }
        });

        //设置Date按钮被点击后，向外界提供监听
        date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (DateOnclickListener != null) {
                    DateOnclickListener.onYesClick();
                }
            }
        });

        //设置Hour按钮被点击后，向外界提供监听
        messagetime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (HourOnclickListener != null) {
                    HourOnclickListener.onYesClick();
                }
            }
        });
    }

    /**
     * 初始化界面控件的显示数据
     */
    private void initData() {
        //如果用户自定了title和message
        if (titleStr != null) {
            titleTv.setText(titleStr);
        }
        //如果设置按钮的文字
        if (yesStr != null) {
            yes.setText(yesStr);
        }
        //设置任务
        if (taskStr != null) {
            messageTv.setText(taskStr);
        }
        //设置日期
        if (dateStr != null) {
            date.setText(dateStr);
        }
        //设置小时分钟
        if (timeStr != null) {
            messagetime.setText(timeStr);
        }
    }

    /**
     * 初始化界面控件
     */
    private void initView() {
        yes = (Button) findViewById(R.id.yes);
        titleTv = (TextView) findViewById(R.id.get_task);
        messageTv = (TextView) findViewById(R.id.setTask);
        messagetime=(TextView)findViewById(R.id.hour);
        date=(TextView)findViewById(R.id.deadline);

    }

    /**
     * 从外界Activity为Dialog设置标题
     *
     * @param title
     */
    public void setTitle(String title) {
        titleStr = title;
    }

    /**
     * 从外界Activity为Dialog设置dialog的message
     *
     * @param task
     */
    public void setTask(String task) {
        taskStr = task;
    }
    public void setDate(String date) {  dateStr = date; }
    public void setTime(String time) {
        timeStr = time;
    }

    /**
     * 设置确定按钮
     */
    public interface onYesOnclickListener {
        public void onYesClick();
    }

    /**
     * 设置确定按钮
     */
    public interface onDateOnclickListener {
        public void onYesClick();
    }

    public interface onHourOnclickListener {
        public void onYesClick();
    }


    public String getTask()
    {
        return messageTv.getText().toString();
    }

    public String getDate()
    {
        return date.getText().toString();
    }

    public String getTime()
    {
        return messagetime.getText().toString();
    }
}
